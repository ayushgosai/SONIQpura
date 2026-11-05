package com.dwarkadhish.dwarkaspay.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwarkadhish.dwarkaspay.data.auth.AuthRepository
import com.dwarkadhish.dwarkaspay.data.auth.AuthValidation
import com.dwarkadhish.dwarkaspay.ui.components.GoogleSignInButton
import com.dwarkadhish.dwarkaspay.ui.components.VioraAuthTextField
import com.dwarkadhish.dwarkaspay.ui.components.VioraGradientButton
import com.dwarkadhish.dwarkaspay.ui.theme.DividerColor
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.dwarkadhish.dwarkaspay.data.auth.GoogleSignInHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.ErrorRed
import com.dwarkadhish.dwarkaspay.ui.theme.MidnightBlack
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun RegisterScreen(
    authRepository: AuthRepository,
    // Called with (name, email, password) — navigate to ProfileSetup screen
    onNavigateToProfileSetup: (name: String, email: String, password: String) -> Unit,
    onGoogleSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    // Legacy callback kept for backward compat (now unused by RegisterScreen directly)
    onRegistrationInitiated: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                isGoogleLoading = true
                coroutineScope.launch {
                    val authResult = authRepository.signInWithGoogleIdToken(idToken)
                    isGoogleLoading = false
                    authResult.fold(
                        onSuccess = { onGoogleSuccess() },
                        onFailure = {
                            if (!account.email.isNullOrEmpty()) {
                                authRepository.signInWithGoogleAccountDirect(
                                    uid = account.id ?: "google_${account.email}",
                                    email = account.email!!,
                                    displayName = account.displayName,
                                    avatarUrl = account.photoUrl?.toString()
                                )
                                onGoogleSuccess()
                            } else {
                                generalError = "Google Sign-Up failed."
                            }
                        }
                    )
                }
            } else if (account != null && !account.email.isNullOrEmpty()) {
                authRepository.signInWithGoogleAccountDirect(
                    uid = account.id ?: "google_${account.email}",
                    email = account.email!!,
                    displayName = account.displayName,
                    avatarUrl = account.photoUrl?.toString()
                )
                onGoogleSuccess()
            } else {
                isGoogleLoading = false
            }
        } catch (e: ApiException) {
            isGoogleLoading = false
            val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
            if (lastAccount != null && !lastAccount.email.isNullOrEmpty()) {
                authRepository.signInWithGoogleAccountDirect(
                    uid = lastAccount.id ?: "google_${lastAccount.email}",
                    email = lastAccount.email!!,
                    displayName = lastAccount.displayName,
                    avatarUrl = lastAccount.photoUrl?.toString()
                )
                onGoogleSuccess()
            } else if (e.statusCode != 12501) {
                generalError = when (e.statusCode) {
                    10 -> "Google Sign-Up configuration error (Code 10: SHA-1 needed in Firebase)."
                    else -> "Google Sign-Up error (code ${e.statusCode}). Try again."
                }
            }
        }
    }

    // Ab directly profile setup screen pe navigate karo — signUpWithEmail wahan call hoga
    fun handleContinue() {
        nameError = AuthValidation.nameError(name)
        emailError = AuthValidation.emailError(email)
        passwordError = AuthValidation.registrationPasswordError(password)
        confirmError = AuthValidation.confirmPasswordError(password, confirmPassword)
        generalError = null

        if (nameError == null && emailError == null && passwordError == null && confirmError == null) {
            onNavigateToProfileSetup(name.trim(), email.trim().lowercase(), password)
        }
    }

    fun handleGoogleSignUp() {
        generalError = null
        isGoogleLoading = true
        googleSignInLauncher.launch(GoogleSignInHelper.getSignInIntent(context))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SONIQ MUSIC",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Join SONIQ to experience high fidelity music streaming",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Error Banner
                AnimatedVisibility(visible = generalError != null) {
                    if (generalError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ErrorRed.copy(alpha = 0.15f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = generalError ?: "",
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Name Input
                VioraAuthTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = "Full Name",
                    placeholder = "Enter your full name",
                    leadingIcon = Icons.Rounded.Person,
                    errorMessage = nameError
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Email Input
                VioraAuthTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null },
                    label = "Email Address",
                    placeholder = "name@example.com",
                    leadingIcon = Icons.Rounded.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    errorMessage = emailError
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password Input
                VioraAuthTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = null },
                    label = "Password (min 8 chars, letters & numbers)",
                    placeholder = "Create a password (min 8 chars)",
                    leadingIcon = Icons.Rounded.Lock,
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    errorMessage = passwordError
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Confirm Password Input
                VioraAuthTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; confirmError = null },
                    label = "Confirm Password",
                    placeholder = "Re-enter your password",
                    leadingIcon = Icons.Rounded.Lock,
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    errorMessage = confirmError
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Continue Button — now goes to Profile Setup first
                VioraGradientButton(
                    text = "Continue →",
                    onClick = { handleContinue() },
                    isLoading = false
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.weight(1f))
                    Text(
                        text = "or",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Google Direct Sign-Up
                GoogleSignInButton(
                    text = "Sign Up with Google",
                    onClick = { handleGoogleSignUp() },
                    isLoading = isGoogleLoading
                )
            }

            // Footer Link to Login
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onNavigateToLogin)
                )
            }
        }
    }
}
