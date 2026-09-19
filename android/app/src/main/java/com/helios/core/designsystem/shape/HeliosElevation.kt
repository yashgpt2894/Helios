package com.helios.core.designsystem.shape

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Elevation tokens from `design/tokens.json` (`elevation`).
 *
 * Cards sit at level-1, sheets and the bottom nav at level-3, dialogs at level-5.
 * The material block is the tonal-elevation equivalent plus the hairline inner stroke
 * every surface carries.
 */
object HeliosElevation {
    val level0: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 3.dp
    val level3: Dp = 6.dp
    val level4: Dp = 8.dp
    val level5: Dp = 12.dp

    /** Material tonal elevation for the two glass levels, and the inner stroke width. */
    object MaterialTokens {
        const val regularTonalElevation = 3
        const val thinTonalElevation = 1
        val innerStrokeWidth: Dp = 0.5.dp

        /** Reduce Transparency: solid surface at the same elevation, stroke at 0.3 alpha. */
        const val reduceTransparencyStrokeAlpha = 0.3f
    }
}
