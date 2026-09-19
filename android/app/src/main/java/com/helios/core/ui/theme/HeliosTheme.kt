package com.helios.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.helios.core.designsystem.color.CarbonColors
import com.helios.core.designsystem.color.HeliosSemanticColors
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.color.PaperColors
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography

/**
 * Theme entry point. Every colour comes from [HeliosSemanticColors], which is
 * `design/tokens.json` `color.semantic` resolved for Paper (light) and Carbon (dark).
 *
 * Material roles are kept for Material components only. App components read
 * [HeliosThemeTokens.colors] so a semantic role cannot drift between screens.
 */
object HeliosThemeTokens {

    /** The semantic palette in the current theme. */
    val colors: HeliosSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalHeliosSemanticColors.current
}

private fun heliosLightScheme(colors: HeliosSemanticColors) = lightColorScheme(
    primary = colors.accentPrimary,
    onPrimary = colors.onAccent,
    primaryContainer = colors.accentSubtle,
    onPrimaryContainer = colors.onSubtle,
    secondary = colors.flowPrimary,
    onSecondary = colors.textInverse,
    secondaryContainer = colors.flowSubtle,
    onSecondaryContainer = colors.flowStrong,
    tertiary = colors.gridExportPrimary,
    onTertiary = colors.textInverse,
    tertiaryContainer = colors.gridExportSubtle,
    onTertiaryContainer = colors.gridExportStrong,
    error = colors.alertPrimary,
    onError = colors.onAlert,
    errorContainer = colors.alertSubtle,
    onErrorContainer = colors.alertStrong,
    background = colors.backgroundPrimary,
    onBackground = colors.textPrimary,
    surface = colors.backgroundPrimary,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.backgroundTertiary,
    onSurfaceVariant = colors.textSecondary,
    outline = colors.separatorStrong,
    outlineVariant = colors.separatorHairline,
    scrim = colors.scrim,
    inverseSurface = colors.textPrimary,
    inverseOnSurface = colors.textInverse,
    inversePrimary = colors.accentStrong
)

private fun heliosDarkScheme(colors: HeliosSemanticColors) = darkColorScheme(
    primary = colors.accentPrimary,
    onPrimary = colors.onAccent,
    primaryContainer = colors.accentSubtle,
    onPrimaryContainer = colors.onSubtle,
    secondary = colors.flowPrimary,
    onSecondary = colors.textInverse,
    secondaryContainer = colors.flowSubtle,
    onSecondaryContainer = colors.flowStrong,
    tertiary = colors.gridExportPrimary,
    onTertiary = colors.textInverse,
    tertiaryContainer = colors.gridExportSubtle,
    onTertiaryContainer = colors.gridExportStrong,
    error = colors.alertPrimary,
    onError = colors.onAlert,
    errorContainer = colors.alertSubtle,
    onErrorContainer = colors.alertStrong,
    background = colors.backgroundPrimary,
    onBackground = colors.textPrimary,
    surface = colors.backgroundPrimary,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.backgroundTertiary,
    onSurfaceVariant = colors.textSecondary,
    outline = colors.separatorStrong,
    outlineVariant = colors.separatorHairline,
    scrim = colors.scrim,
    inverseSurface = colors.textPrimary,
    inverseOnSurface = colors.textInverse,
    inversePrimary = colors.accentStrong
)

@Composable
fun HeliosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    colors: HeliosSemanticColors = if (darkTheme) CarbonColors else PaperColors,
    content: @Composable () -> Unit
) {
    @Suppress("UNUSED_EXPRESSION")
    dynamicColor // Dynamic colour is off: the palette is authored, not sampled.

    val colorScheme = if (darkTheme) heliosDarkScheme(colors) else heliosLightScheme(colors)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalHeliosSemanticColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(
                displayLarge = HeliosTypography.hero,
                displayMedium = HeliosTypography.title1,
                displaySmall = HeliosTypography.title2,
                headlineLarge = HeliosTypography.title3,
                headlineMedium = HeliosTypography.headline,
                headlineSmall = HeliosTypography.callout,
                bodyLarge = HeliosTypography.body,
                bodyMedium = HeliosTypography.callout,
                bodySmall = HeliosTypography.subheadline,
                labelLarge = HeliosTypography.caption,
                labelMedium = HeliosTypography.caption2,
                labelSmall = HeliosTypography.caption2
            ),
            shapes = Shapes(
                extraSmall = HeliosShape.xs,
                small = HeliosShape.sm,
                medium = HeliosShape.md,
                large = HeliosShape.lg,
                extraLarge = HeliosShape.xl
            ),
            content = content
        )
    }
}

/** Convenience for previews and the gallery: an explicit palette without a host Activity. */
@Composable
fun HeliosPreviewSurface(colors: HeliosSemanticColors, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalHeliosSemanticColors provides colors) {
        MaterialTheme(
            colorScheme = if (colors.isDark) heliosDarkScheme(colors) else heliosLightScheme(colors),
            content = content
        )
    }
}

/** White-label helper: the palette with a brand accent, hue roles untouched. */
fun HeliosSemanticColors.branded(accentHex: String, accentLightHex: String): HeliosSemanticColors =
    withBrandAccent(Color(android.graphics.Color.parseColor(accentHex)), Color(android.graphics.Color.parseColor(accentLightHex)))
