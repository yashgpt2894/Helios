package com.helios.core.designsystem.shape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Radius tokens from shared-spec/design-tokens.json.
 */
object HeliosShape {
    val none = RoundedCornerShape(0.dp)
    val xs = RoundedCornerShape(4.dp)
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(16.dp)
    val xl = RoundedCornerShape(22.dp)
    val xxl = RoundedCornerShape(28.dp)
    val full = RoundedCornerShape(50)
}
