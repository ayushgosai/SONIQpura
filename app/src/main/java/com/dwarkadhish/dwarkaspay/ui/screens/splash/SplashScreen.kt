package com.dwarkadhish.dwarkaspay.ui.screens.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwarkadhish.dwarkaspay.data.auth.AuthRepository
import com.dwarkadhish.dwarkaspay.model.AuthState
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricVioletGlow
import com.dwarkadhish.dwarkaspay.ui.theme.MidnightBlack
import com.dwarkadhish.dwarkaspay.ui.theme.SoftLavender
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authRepository: AuthRepository,
    userPreferencesManager: com.dwarkadhish.dwarkaspay.data.preferences.UserPreferencesManager,
    onNavigateToWelcome: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToVerify: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        val minSplashDelay = 1200L
        val startTime = System.currentTimeMillis()

        val isOnboardingDone = userPreferencesManager.isOnboardingCompleted
        val authState = if (isOnboardingDone) authRepository.checkSession() else null

        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed < minSplashDelay) {
            delay(minSplashDelay - elapsed)
        }

        if (!isOnboardingDone) {
            onNavigateToWelcome()
            return@LaunchedEffect
        }

        when (authState) {
            is AuthState.Authenticated -> {
                if (authState.user.isEmailVerified) {
                    onNavigateToHome()
                } else {
                    onNavigateToVerify(authState.user.email)
                }
            }
            is AuthState.RegistrationPendingVerification -> {
                onNavigateToVerify(authState.email)
            }
            else -> {
                onNavigateToLogin()
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Glowing background halo
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Stylized Abstract Waveform Symbol (SONIQ Logo)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.height(72.dp)
            ) {
                val heights = listOf(28.dp, 52.dp, 72.dp, 44.dp, 64.dp, 36.dp, 20.dp)
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                heights.forEachIndexed { index, barHeight ->
                    Canvas(
                        modifier = Modifier
                            .width(7.dp)
                            .height(barHeight)
                    ) {
                        drawRoundRect(
                            color = if (index % 2 == 0) primaryColor else secondaryColor,
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }
                    if (index < heights.lastIndex) {
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "SONIQ MUSIC",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "FEEL THE SOUND • PREMIUM EXPERIENCE",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.5.dp
            )
        }
    }
}
