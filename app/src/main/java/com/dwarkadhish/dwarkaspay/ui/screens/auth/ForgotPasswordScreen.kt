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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LockReset
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dwarkadhish.dwarkaspay.data.auth.AuthRepository
import com.dwarkadhish.dwarkaspay.data.auth.AuthValidation
import com.dwarkadhish.dwarkaspay.ui.components.VioraAuthTextField
import com.dwarkadhish.dwarkaspay.ui.components.VioraGradientButton
import com.dwarkadhish.dwarkaspay.ui.theme.DeepElevatedSurface
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.ErrorRed
import com.dwarkadhish.dwarkaspay.ui.theme.MidnightBlack
import com.dwarkadhish.dwarkaspay.ui.theme.SuccessGreen
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    authRepository: AuthRepository,
    onNavigateBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    fun handleSendResetLink() {
        emailError = AuthValidation.emailError(email)
        errorMessage = null

        if (emailError == null) {
            isLoading = true
            coroutineScope.launch {
                val result = authRepository.sendPasswordResetEmail(email)
                isLoading = false
                result.fold(
                    onSuccess = {
                        isSubmitted = true
                    },
                    onFailure = { error ->
                        errorMessage = error.message ?: "Failed to send reset link."
                    }
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Icon Header
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LockReset,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Forgot Password",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter your registered email address and we'll send you instructions to reset your password.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (isSubmitted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DeepElevatedSurface)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Reset Link Dispatched",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "If an account exists for $email, a password reset link has been dispatched to your inbox.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Error message
                    AnimatedVisibility(visible = errorMessage != null) {
                        if (errorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ErrorRed.copy(alpha = 0.15f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = ErrorRed,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Email Input
                    VioraAuthTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = "Email Address",
                        placeholder = "name@example.com",
                        leadingIcon = Icons.Rounded.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        errorMessage = emailError
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button
                    VioraGradientButton(
                        text = "Send Reset Link",
                        onClick = { handleSendResetLink() },
                        isLoading = isLoading
                    )
                }
            }

            // Footer: Back to Login
            Text(
                text = "← Back to Login",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = TextSecondary,
                modifier = Modifier.clickable(onClick = onNavigateBackToLogin)
            )
        }
    }
}
