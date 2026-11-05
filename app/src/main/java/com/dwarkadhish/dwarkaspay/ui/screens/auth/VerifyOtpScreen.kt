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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwarkadhish.dwarkaspay.data.auth.AuthRepository
import com.dwarkadhish.dwarkaspay.ui.components.SixDigitOtpInput
import com.dwarkadhish.dwarkaspay.ui.components.VioraGradientButton
import com.dwarkadhish.dwarkaspay.ui.theme.DeepElevatedSurface
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.ErrorRed
import com.dwarkadhish.dwarkaspay.ui.theme.MidnightBlack
import com.dwarkadhish.dwarkaspay.ui.theme.SoftLavender
import com.dwarkadhish.dwarkaspay.ui.theme.SuccessGreen
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary
import com.dwarkadhish.dwarkaspay.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VerifyOtpScreen(
    email: String,
    authRepository: AuthRepository,
    onVerificationSuccess: () -> Unit,
    onNavigateBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var otpCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var resendCountdown by remember { mutableIntStateOf(45) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Mask email e.g. "alex@gmail.com" -> "a***@gmail.com"
    val maskedEmail = remember(email) {
        val parts = email.split("@")
        if (parts.size == 2 && parts[0].isNotEmpty()) {
            val name = parts[0]
            val maskedName = if (name.length <= 2) "${name.first()}*" else "${name.first()}${"*".repeat(name.length - 2)}${name.last()}"
            "$maskedName@${parts[1]}"
        } else {
            email
        }
    }

    // Latest demo/test OTP preview helper
    var liveOtpHint by remember { mutableStateOf<String?>(null) }

    fun refreshOtpHint() {
        liveOtpHint = authRepository.getLatestOtpForEmail(email)
    }

    // Resend countdown ticker
    LaunchedEffect(resendCountdown) {
        if (resendCountdown > 0) {
            delay(1000L)
            resendCountdown -= 1
        }
    }

    LaunchedEffect(Unit) {
        refreshOtpHint()
    }

    fun handleVerify() {
        if (otpCode.length < 6) {
            errorMessage = "Please enter all 6 digits of the verification code."
            return
        }

        errorMessage = null
        isLoading = true

        coroutineScope.launch {
            val result = authRepository.verifyOtp(email, otpCode)
            isLoading = false
            result.fold(
                onSuccess = {
                    showSuccessDialog = true
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Verification failed."
                }
            )
        }
    }

    fun handleResend() {
        if (resendCountdown > 0 || isResending) return
        isResending = true
        errorMessage = null

        coroutineScope.launch {
            val result = authRepository.resendOtp(email)
            isResending = false
            result.fold(
                onSuccess = {
                    resendCountdown = 45
                    refreshOtpHint()
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Failed to resend verification code."
                }
            )
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onVerificationSuccess()
            },
            containerColor = DeepElevatedSurface,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Email Verified!",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Your email has been verified! You're now signed in to Viora Music. Let's go!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onVerificationSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Let's Go!", color = MidnightBlack, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DeepElevatedSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MarkEmailRead,
                        contentDescription = null,
                        tint = ElectricViolet,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Verify Your Email",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "We sent a 6-digit verification code to\n$maskedEmail",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                // Demo Notice with live test OTP
                if (liveOtpHint != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricViolet.copy(alpha = 0.15f))
                            .clickable {
                                otpCode = liveOtpHint ?: ""
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Email Preview Code: $liveOtpHint (Tap to fill)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SoftLavender
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Error Banner
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
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // 6-digit OTP Box Input
                SixDigitOtpInput(
                    otpValue = otpCode,
                    onOtpChange = {
                        otpCode = it
                        errorMessage = null
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Verify Button
                VioraGradientButton(
                    text = "Verify Code",
                    onClick = { handleVerify() },
                    isLoading = isLoading,
                    enabled = otpCode.length == 6
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Resend Countdown Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Didn't receive code? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    if (resendCountdown > 0) {
                        Text(
                            text = "Resend in ${resendCountdown}s",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextTertiary
                        )
                    } else {
                        Text(
                            text = if (isResending) "Sending..." else "Resend Code",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricViolet,
                            modifier = Modifier.clickable(enabled = !isResending) {
                                handleResend()
                            }
                        )
                    }
                }
            }

            // Back to Login Footer
            Text(
                text = "← Back to Login",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = TextSecondary,
                modifier = Modifier.clickable(onClick = onNavigateBackToLogin)
            )
        }
    }
}
