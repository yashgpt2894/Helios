package com.helios.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.helios.core.designsystem.color.HeliosColor
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography

private val LightColorScheme = lightColorScheme(
    primary = HeliosColor.Solar.Light500,
    onPrimary = Color.White,
    primaryContainer = HeliosColor.Solar.Light100,
    onPrimaryContainer = HeliosColor.Solar.Light900,
    secondary = HeliosColor.Flow.Light500,
    onSecondary = Color.White,
    secondaryContainer = HeliosColor.Flow.Light100,
    onSecondaryContainer = HeliosColor.Flow.Light900,
    tertiary = HeliosColor.GridExport.Light500,
    onTertiary = Color.White,
    tertiaryContainer = HeliosColor.GridExport.Light100,
    onTertiaryContainer = HeliosColor.GridExport.Light900,
    error = HeliosColor.Alert.Light500,
    onError = Color.White,
    errorContainer = HeliosColor.Alert.Light100,
    onErrorContainer = HeliosColor.Alert.Light900,
    background = HeliosColor.Neutral.Light50,
    onBackground = HeliosColor.Neutral.Light950,
    surface = HeliosColor.Neutral.Light50,
    onSurface = HeliosColor.Neutral.Light950,
    surfaceVariant = HeliosColor.Neutral.Light200,
    onSurfaceVariant = HeliosColor.Neutral.Light700,
    outline = HeliosColor.Neutral.Light400,
    outlineVariant = HeliosColor.Neutral.Light200,
    inverseSurface = HeliosColor.Neutral.Light900,
    inverseOnSurface = HeliosColor.Neutral.Light50,
    inversePrimary = HeliosColor.Solar.Light300
)

private val DarkColorScheme = darkColorScheme(
    primary = HeliosColor.Solar.Dark500,
    onPrimary = HeliosColor.Solar.Dark900,
    primaryContainer = HeliosColor.Solar.Dark200,
    onPrimaryContainer = HeliosColor.Solar.Dark800,
    secondary = HeliosColor.Flow.Dark500,
    onSecondary = HeliosColor.Flow.Dark900,
    secondaryContainer = HeliosColor.Flow.Dark200,
    onSecondaryContainer = HeliosColor.Flow.Dark800,
    tertiary = HeliosColor.GridExport.Dark500,
    onTertiary = HeliosColor.GridExport.Dark900,
    tertiaryContainer = HeliosColor.GridExport.Dark200,
    onTertiaryContainer = HeliosColor.GridExport.Dark800,
    error = HeliosColor.Alert.Dark500,
    onError = HeliosColor.Alert.Dark900,
    errorContainer = HeliosColor.Alert.Dark200,
    onErrorContainer = HeliosColor.Alert.Dark800,
    background = HeliosColor.Neutral.Dark50,
    onBackground = HeliosColor.Neutral.Dark950,
    surface = HeliosColor.Neutral.Dark50,
    onSurface = HeliosColor.Neutral.Dark950,
    surfaceVariant = HeliosColor.Neutral.Dark200,
    onSurfaceVariant = HeliosColor.Neutral.Dark700,
    outline = HeliosColor.Neutral.Dark400,
    outlineVariant = HeliosColor.Neutral.Dark200,
    inverseSurface = HeliosColor.Neutral.Dark900,
    inverseOnSurface = HeliosColor.Neutral.Dark50,
    inversePrimary = HeliosColor.Solar.Dark300
)

@Composable
fun HeliosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

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
