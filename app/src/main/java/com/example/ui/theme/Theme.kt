package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class GlassPalette(
    val canvasBackground: Color = MidnightCanvas,
    val deepLayer: Color = DeepSpaceNavy,
    val surfaceHigh: Color = GlassSurfaceHigh,
    val surfaceMedium: Color = GlassSurfaceMedium,
    val surfaceLow: Color = GlassSurfaceLow,
    val highlightBorder: Color = GlassHighlightBorder,
    val subtleBorder: Color = GlassSubtleBorder,
    val fieldContainer: Color = GlassFieldContainer,
    val cyanGlow: Color = CyanAurora,
    val violetGlow: Color = VioletNebula,
    val verifiedMint: Color = MintVerified,
    val warningAmber: Color = AmberWarm,
    val errorCoral: Color = CoralAlert,
    val textSecondary: Color = TextSecondary,
    val textMuted: Color = TextMuted,
    val onAccentDark: Color = OnAccentDark
)

val LocalGlassPalette = staticCompositionLocalOf { GlassPalette() }

private val LuminaDarkColorScheme = darkColorScheme(
    primary = CyanAurora,
    onPrimary = OnAccentDark,
    primaryContainer = CosmicIndigo,
    onPrimaryContainer = TextPure,
    secondary = VioletNebula,
    onSecondary = TextPure,
    secondaryContainer = DeepSpaceNavy,
    onSecondaryContainer = SoftLavender,
    tertiary = MintVerified,
    onTertiary = OnAccentDark,
    background = MidnightCanvas,
    onBackground = TextPure,
    surface = DeepSpaceNavy,
    onSurface = TextPure,
    surfaceVariant = CosmicIndigo,
    onSurfaceVariant = TextSecondary,
    outline = GlassSubtleBorder,
    outlineVariant = GlassHighlightBorder,
    error = CoralAlert,
    onError = OnAccentDark
)

val LuminaShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun LuminaTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalGlassPalette provides GlassPalette()) {
        MaterialTheme(
            colorScheme = LuminaDarkColorScheme,
            typography = Typography,
            shapes = LuminaShapes,
            content = content
        )
    }
}
