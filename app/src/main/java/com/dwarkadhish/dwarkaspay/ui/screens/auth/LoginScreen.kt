package com.dwarkadhish.dwarkaspay.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwarkadhish.dwarkaspay.data.auth.AuthRepository
import com.dwarkadhish.dwarkaspay.data.auth.AuthValidation
import com.dwarkadhish.dwarkaspay.data.auth.GoogleSignInHelper
import com.dwarkadhish.dwarkaspay.ui.components.GoogleSignInButton
import com.dwarkadhish.dwarkaspay.ui.components.VioraAuthTextField
import com.dwarkadhish.dwarkaspay.ui.components.VioraGradientButton
import com.dwarkadhish.dwarkaspay.ui.theme.DeepElevatedSurface
import com.dwarkadhish.dwarkaspay.ui.theme.DividerColor
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.ErrorRed
import com.dwarkadhish.dwarkaspay.ui.theme.MidnightBlack
import com.dwarkadhish.dwarkaspay.ui.theme.SoftLavender
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary
import com.dwarkadhish.dwarkaspay.ui.theme.WarningAmber
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToVerifyOtp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var unverifiedEmailNotice by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    // ── Real Google Sign-In launcher ──────────────────────────────────────────
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
                        onSuccess = { onLoginSuccess() },
                        onFailure = {
                            if (!account.email.isNullOrEmpty()) {
                                authRepository.signInWithGoogleAccountDirect(
                                    uid = account.id ?: "google_${account.email}",
                                    email = account.email!!,
                                    displayName = account.displayName,
                                    avatarUrl = account.photoUrl?.toString()
                                )
                                onLoginSuccess()
                            } else {
                                generalError = "Google Sign-In failed."
                            }
                        }
                    )
                }
            } else if (account != null && !account.email.isNullOrEmpty()) {
                // No ID token but account data present — use direct fallback
                isGoogleLoading = true
                authRepository.signInWithGoogleAccountDirect(
                    uid = account.id ?: "google_${account.email}",
                    email = account.email!!,
                    displayName = account.displayName,
                    avatarUrl = account.photoUrl?.toString()
                )
                isGoogleLoading = false
                onLoginSuccess()
            } else {
                isGoogleLoading = false
            }
        } catch (e: ApiException) {
            isGoogleLoading = false
            when (e.statusCode) {
                12501 -> { /* User cancelled account picker — do nothing */ }
                else -> {
                    val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
                    if (lastAccount != null && !lastAccount.email.isNullOrEmpty()) {
                        isGoogleLoading = true
                        authRepository.signInWithGoogleAccountDirect(
                            uid = lastAccount.id ?: "google_${lastAccount.email}",
                            email = lastAccount.email!!,
                            displayName = lastAccount.displayName,
                            avatarUrl = lastAccount.photoUrl?.toString()
                        )
                        isGoogleLoading = false
                        onLoginSuccess()
                    } else {
                        generalError = when (e.statusCode) {
                            10 -> "Google Sign-In setup error. Please contact support."
                            else -> "Google Sign-In failed (code ${e.statusCode}). Try again."
                        }
                    }
                }
            }
        }
    }

    fun handleEmailLogin() {
        emailError = AuthValidation.emailError(email)
        passwordError = AuthValidation.loginPasswordError(password)
        generalError = null
        unverifiedEmailNotice = null

        if (emailError == null && passwordError == null) {
            isLoading = true
            coroutineScope.launch {
                val result = authRepository.signInWithEmail(email, password)
                isLoading = false
                result.fold(
                    onSuccess = { onLoginSuccess() },
                    onFailure = { error ->
                        val msg = error.message ?: "Authentication failed."
                        if (msg.contains("EMAIL_NOT_VERIFIED", ignoreCase = true)) {
                            unverifiedEmailNotice = email.trim()
                        } else {
                            generalError = msg
                        }
                    }
                )
            }
        }
    }

    fun handleGoogleLogin() {
        // Do NOT set isGoogleLoading=true here — if picker is dismissed (12501),
        // the result callback sets it false, but without this the button stays stuck.
        // Loading state is managed entirely inside the result callback.
        generalError = null
        // Sign out previous session so the account picker always shows fresh
        GoogleSignInHelper.getGoogleSignInClient(context).signOut().addOnCompleteListener {
            googleSignInLauncher.launch(GoogleSignInHelper.getSignInIntent(context))
        }
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
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Header Brand
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
                    text = "Welcome Back",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Sign in to feel the sound and sync your playlists",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = generalError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ErrorRed
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Unverified Email Notice
                AnimatedVisibility(visible = unverifiedEmailNotice != null) {
                    if (unverifiedEmailNotice != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(WarningAmber.copy(alpha = 0.12f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Email not verified",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = WarningAmber
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "A new OTP has been sent to ${unverifiedEmailNotice}. Please check your inbox.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Verify Email →",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        unverifiedEmailNotice?.let { onNavigateToVerifyOtp(it) }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Email Field with clean, user-friendly placeholder
                VioraAuthTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null; generalError = null },
                    label = "Email Address",
                    placeholder = "name@example.com",
                    leadingIcon = Icons.Rounded.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    errorMessage = emailError
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password Field with clean, user-friendly placeholder
                VioraAuthTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = null; generalError = null },
                    label = "Password",
                    placeholder = "Enter your password",
                    leadingIcon = Icons.Rounded.Lock,
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    errorMessage = passwordError
                )

                // Forgot Password Link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot password?",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onNavigateToForgotPassword)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Login Button
                VioraGradientButton(
                    text = "Sign In",
                    onClick = { handleEmailLogin() },
                    isLoading = isLoading
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Divider with 'or'
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

                Spacer(modifier = Modifier.height(20.dp))

                // Real Google Sign-In button
                GoogleSignInButton(
                    onClick = { handleGoogleLogin() },
                    isLoading = isGoogleLoading
                )
            }

            // Footer Link: Register
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onNavigateToRegister)
                )
            }
        }
    }
}
