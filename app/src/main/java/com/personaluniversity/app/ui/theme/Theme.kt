package com.personaluniversity.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ---------- Palette (mirrors the web frontend's tokens) ----------

val Ink = Color(0xFF12161B)          // background
val Surface = Color(0xFF1A2028)      // cards, rail
val SurfaceRaised = Color(0xFF212A34)
val Gold = Color(0xFFC9A050)         // accent
val GoldDim = Color(0xFF8A7038)
val Parchment = Color(0xFFE9E4D8)    // primary text
val TextMuted = Color(0xFF8993A1)
val RuleLine = Color(0xFF2C3542)
val SuccessGreen = Color(0xFF7A9E7E)
val ErrorRed = Color(0xFFC17767)
val UserBubble = Color(0xFF3A3220)

private val AppColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF14100A),
    secondary = GoldDim,
    background = Ink,
    onBackground = Parchment,
    surface = Surface,
    onSurface = Parchment,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = TextMuted,
    outline = RuleLine,
    error = ErrorRed
)

// Display type leans on the platform serif to echo the web app's Fraunces
// headlines without requiring a bundled font file. Swap FontFamily.Serif for
// a custom downloaded Fraunces font resource if you want an exact match.
val DisplayFontFamily = FontFamily.Serif
val BodyFontFamily = FontFamily.SansSerif
val MonoFontFamily = FontFamily.Monospace

object AppType {
    val displayTitle = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp,
        lineHeight = 32.sp
    )
    val displayCard = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp
    )
    val eyebrow = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.sp
    )
    val body = TextStyle(
        fontFamily = BodyFontFamily,
        fontSize = 14.sp,
        lineHeight = 21.sp
    )
    val lede = TextStyle(
        fontFamily = BodyFontFamily,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        color = TextMuted
    )
    val meta = TextStyle(
        fontFamily = MonoFontFamily,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp
    )
}

@Composable
fun PersonalUniversityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content
    )
}
