package com.dwarkadhish.dwarkaspay.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// SONIQ Music Premium Dark Color Scheme (Midnight & Electric Violet)
private val DarkColorScheme = darkColorScheme(
    primary = ElectricViolet,
    onPrimary = MidnightBlack,
    primaryContainer = DeepElevatedSurface,
    onPrimaryContainer = SoftLavender,
    
    secondary = SoftLavender,
    onSecondary = MidnightBlack,
    secondaryContainer = CardSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    
    tertiary = DeepIndigo,
    onTertiary = TextPrimary,
    
    background = MidnightBlack,
    onBackground = TextPrimary,
    
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = DeepElevatedSurface,
    onSurfaceVariant = TextSecondary,
    
    outline = OutlineColor,
    outlineVariant = DividerColor,
    
    error = ErrorRed,
    onError = MidnightBlack
)

// SONIQ Music Premium Light Color Scheme (Warm Cream & Parchment Luxury)
private val LightColorScheme = lightColorScheme(
    primary = LightPrimaryViolet,
    onPrimary = Color.White,
    primaryContainer = LightElevatedSurface,
    onPrimaryContainer = LightTextPrimary,
    
    secondary = ElectricViolet,
    onSecondary = Color.White,
    secondaryContainer = LightCardSurfaceVariant,
    onSecondaryContainer = LightTextPrimary,
    
    tertiary = DeepIndigo,
    onTertiary = Color.White,
    
    background = LightBackground,
    onBackground = LightTextPrimary,
    
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightElevatedSurface,
    onSurfaceVariant = LightTextSecondary,
    
    outline = LightOutline,
    outlineVariant = LightDivider,
    
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun VioraMusicTheme(
    darkTheme: Boolean = false, // Light by default as requested by user
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                val bgArgb = if (darkTheme) MidnightBlack.toArgb() else LightBackground.toArgb()
                it.statusBarColor = bgArgb
                it.navigationBarColor = bgArgb
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// Alias for SONIQ Branding
@Composable
fun SoniqMusicTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) = VioraMusicTheme(darkTheme = darkTheme, content = content)
