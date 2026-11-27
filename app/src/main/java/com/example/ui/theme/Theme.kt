package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LifeTrackDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldOnContainer,
    secondary = CyanSecondary,
    onSecondary = CyanOnSecondary,
    secondaryContainer = CyanContainer,
    onSecondaryContainer = CyanOnContainer,
    tertiary = VioletTertiary,
    onTertiary = VioletOnTertiary,
    tertiaryContainer = VioletContainer,
    onTertiaryContainer = VioletOnContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurfaceLow,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceContainer,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkSurfaceLowest,
    surfaceContainerLow = DarkSurfaceLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = DarkSurfaceHighest,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

private val LifeTrackLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceLow,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurfaceLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceHigh,
    surfaceContainerHighest = LightSurfaceHigh,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

val LifeTrackShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Immutable
data class LifeTrackExtraColors(
    val amber: Color = AmberAccent,
    val amberSoft: Color = AmberContainer,
    val emeraldGlow: Color = Color(0x2810B981),
    val cyanGlow: Color = Color(0x2806B6D4),
    val violetGlow: Color = Color(0x288B5CF6),
    val cardBorder: Color = DarkOutlineVariant,
    val isDark: Boolean = true
)

val LocalLifeTrackColors = staticCompositionLocalOf { LifeTrackExtraColors() }

@Composable
fun LifeTrackTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LifeTrackDarkColorScheme else LifeTrackLightColorScheme
    val extraColors = if (darkTheme) {
        LifeTrackExtraColors(
            amber = AmberAccent,
            amberSoft = Color(0x2AF59E0B),
            emeraldGlow = Color(0x3310B981),
            cyanGlow = Color(0x3306B6D4),
            violetGlow = Color(0x338B5CF6),
            cardBorder = Color(0xFF263044),
            isDark = true
        )
    } else {
        LifeTrackExtraColors(
            amber = Color(0xFFD97706),
            amberSoft = Color(0x22D97706),
            emeraldGlow = Color(0x18059669),
            cyanGlow = Color(0x180891B2),
            violetGlow = Color(0x187C3AED),
            cardBorder = Color(0xFFE2E8F0),
            isDark = false
        )
    }

    CompositionLocalProvider(LocalLifeTrackColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LifeTrackTypography,
            shapes = LifeTrackShapes,
            content = content
        )
    }
}
