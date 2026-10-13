package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Vibrant, Medical-Grade Human Tactile Theme Palette
val NavyBackground = Color(0xFF0A1118)
val NavySurface = Color(0xFF111E2E)
val NavyCard = Color(0xFF182A3E)
val NavyBorder = Color(0xFF243B53)
val NavyBorderGlow = Color(0xFF335375)

// Rich Vibrant Accents
val PrimaryTeal = Color(0xFF00E5B3)
val PrimaryTealLight = Color(0xFF70FAD4)
val PrimaryTealDark = Color(0xFF00A380)

val AccentAmber = Color(0xFFFFB300)
val AccentAmberLight = Color(0xFFFFD54F)

val AccentCoral = Color(0xFFFF4D4D)
val AccentCoralLight = Color(0xFFFF8585)

val ElectricBlue = Color(0xFF00B0FF)
val ElectricBlueLight = Color(0xFF80D8FF)

val SoftPurple = Color(0xFFB388FF)
val SoftPurpleDark = Color(0xFF7C4DFF)

val EmeraldGreen = Color(0xFF00E676)
val FreshnessLive = Color(0xFF00E676)
val FreshnessDelayed = Color(0xFFFFB300)
val FreshnessStale = Color(0xFF78909C)

val QualityValid = Color(0xFF00E676)
val QualitySuspect = Color(0xFFFF9100)
val QualityInvalid = Color(0xFFFF1744)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0C8DE)
val TextTertiary = Color(0xFF718B9E)

// Gradients
val VitalCardHeartGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF26121C), Color(0xFF14202E))
)

val VitalCardOxygenGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF0D253A), Color(0xFF122232))
)

val VitalCardTempGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF2B2212), Color(0xFF14212F))
)

val VitalCardBpGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF211633), Color(0xFF122030))
)

val VitalCardRespGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF0E2824), Color(0xFF142231))
)

val VoicePillGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00C9FF), Color(0xFF92FE9D))
)

val SosGlowGradient = Brush.radialGradient(
    colors = listOf(Color(0xFFFF5252), Color(0xFFB71C1C))
)
