package com.helios.core.designsystem.shape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Radius tokens from `design/tokens.json` (`radius`).
 *
 * xs 4, sm 8, md 12, lg 16, xl 22, 2xl 28, full = pill. Elevation per level is in
 * [HeliosElevation]; a card is md radius at level-1.
 */
object HeliosShape {
    val none = RoundedCornerShape(0.dp)
    val xs = RoundedCornerShape(4.dp)
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(16.dp)
    val xl = RoundedCornerShape(22.dp)
    val xxl = RoundedCornerShape(28.dp)
    val full = RoundedCornerShape(percent = 50)
}
