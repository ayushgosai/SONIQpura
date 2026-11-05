package com.dwarkadhish.dwarkaspay.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Viora Music Premium Design Tokens
// Theme: Midnight Black + Electric Violet + Lavender

val MidnightBlack = Color(0xFF0B0B10)
val CharcoalSurface = Color(0xFF12121A)
val CardSurface = Color(0xFF181724)
val DeepElevatedSurface = Color(0xFF1B1B26)
val CardSurfaceVariant = Color(0xFF232232)

val ElectricViolet = Color(0xFF9B7BFF)
val ElectricVioletGlow = Color(0x339B7BFF)
val ElectricVioletLight = Color(0xFFB197FC)
val SoftLavender = Color(0xFFC4B5FD)
val DeepIndigo = Color(0xFF6366F1)

val TextPrimary = Color(0xFFF7F5FF)
val TextSecondary = Color(0xFFA7A4B5)
val TextTertiary = Color(0xFF6B687B)

val DividerColor = Color(0xFF302D3A)
val OutlineColor = Color(0xFF383545)

val SuccessGreen = Color(0xFF34D399)
val ErrorRed = Color(0xFFF87171)
val WarningAmber = Color(0xFFFBBF24)

// SONIQ Premium Light Mode Tokens (Warm Cream & Parchment Luxury from Reference Image)
val LightBackground = Color(0xFFF7F4EE)
val LightSurface = Color(0xFFFFFFFF)
val LightElevatedSurface = Color(0xFFF1ECE3)
val LightCardSurfaceVariant = Color(0xFFE8E2D7)

val LightTextPrimary = Color(0xFF1A1822)
val LightTextSecondary = Color(0xFF5E596D)
val LightTextTertiary = Color(0xFF918C9E)

val LightDivider = Color(0xFFE5DFD4)
val LightOutline = Color(0xFFD8D1C4)
val LightPrimaryViolet = Color(0xFF7C3AED)

// Gradient Brushes for Buttons, Badges, and Artwork Backdrops
val ElectricVioletGradient = Brush.horizontalGradient(
    colors = listOf(ElectricViolet, DeepIndigo)
)

val HeroCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0x809B7BFF),
        Color(0x201B1B26),
        MidnightBlack
    )
)

val PlayerBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF2A1B4E),
        Color(0xFF161226),
        MidnightBlack
    )
)
