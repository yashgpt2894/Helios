package com.helios.feature.dashboard

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helios.core.designsystem.color.HeliosColor
import kotlin.math.abs

/**
 * EnergyFlow: Compose Canvas with animated dot trails.
 * 4 nodes in diamond: Solar (top), Home (center), Battery (bottom-left), Grid (bottom-right).
 * Bezier paths with animated particles whose density modulates with wattage.
 */
@Composable
fun EnergyFlow(
    solarW: Double,
    batteryW: Double,
    gridW: Double,
    homeW: Double,
    modifier: Modifier = Modifier
) {
    val densityDotsSolar = when {
        solarW <= 0 -> 0; solarW <= 500 -> 1; solarW <= 2000 -> 2
        solarW <= 5000 -> 3; else -> 4
    }
    val densityDotsBattery = when {
        abs(batteryW) <= 0 -> 0; abs(batteryW) <= 500 -> 1
        abs(batteryW) <= 2000 -> 2; abs(batteryW) <= 5000 -> 3; else -> 4
    }
    val densityDotsGrid = when {
        abs(gridW) <= 0 -> 0; abs(gridW) <= 500 -> 1
        abs(gridW) <= 2000 -> 2; abs(gridW) <= 5000 -> 3; else -> 4
    }

    val infiniteTransition = rememberInfiniteTransition(label = "energyFlowAnim")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 2200)),
        label = "flowProgress"
    )

    val textMeasurer = rememberTextMeasurer()
    val solarColor = HeliosColor.Solar.ramp(500, true)
    val batteryColor = HeliosColor.Battery.ramp(500, true)
    val gridImportColor = HeliosColor.GridImport.ramp(500, true)
    val gridExportColor = HeliosColor.GridExport.ramp(500, true)
    val flowColor = HeliosColor.Flow.ramp(500, true)
    val neutralColor = HeliosColor.Neutral.ramp(600, true)
    val labelStyle = TextStyle(fontSize = 10.sp, color = Color.White)

    Canvas(modifier = modifier.fillMaxWidth().height(240.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2

        // Node positions (diamond layout)
        val solarPos = Offset(cx, 40f)
        val homePos = Offset(cx, h / 2)
        val batteryPos = Offset(cx - 80f, h - 40f)
        val gridPos = Offset(cx + 80f, h - 40f)

        val pathStroke = Stroke(width = 1.5f)

        // Define paths with control points for quadratic beziers
        data class BezierPath(val p0: Offset, val cp: Offset, val p2: Offset, val compPath: Path)

        val solarPath = BezierPath(
            p0 = solarPos,
            cp = Offset(solarPos.x, solarPos.y + (homePos.y - solarPos.y) * 0.4f + 20f),
            p2 = homePos,
            compPath = Path().apply {
                moveTo(solarPos.x, solarPos.y)
                quadraticBezierTo(
                    solarPos.x, solarPos.y + (homePos.y - solarPos.y) * 0.4f + 20f,
                    homePos.x, homePos.y
                )
            }
        )
        val batteryPath = BezierPath(
            p0 = batteryPos,
            cp = Offset((batteryPos.x + homePos.x) / 2 + 20f, (batteryPos.y + homePos.y) / 2),
            p2 = homePos,
            compPath = Path().apply {
                moveTo(batteryPos.x, batteryPos.y)
                quadraticBezierTo(
                    (batteryPos.x + homePos.x) / 2 + 20f,
                    (batteryPos.y + homePos.y) / 2,
                    homePos.x, homePos.y
                )
            }
        )
        val gridPath = BezierPath(
            p0 = gridPos,
            cp = Offset((gridPos.x + homePos.x) / 2 - 20f, (gridPos.y + homePos.y) / 2),
            p2 = homePos,
            compPath = Path().apply {
                moveTo(gridPos.x, gridPos.y)
                quadraticBezierTo(
                    (gridPos.x + homePos.x) / 2 - 20f,
                    (gridPos.y + homePos.y) / 2,
                    homePos.x, homePos.y
                )
            }
        )

        // Draw static paths
        drawPath(solarPath.compPath, solarColor.copy(alpha = 0.25f), style = pathStroke)
        drawPath(batteryPath.compPath, batteryColor.copy(alpha = 0.25f), style = pathStroke)
        drawPath(gridPath.compPath, neutralColor.copy(alpha = 0.25f), style = pathStroke)

        // Helper: sample point on quadratic bezier at t
        fun sample(p: BezierPath, t: Float): Offset {
            val mt = 1f - t
            return Offset(
                x = mt * mt * p.p0.x + 2f * mt * t * p.cp.x + t * t * p.p2.x,
                y = mt * mt * p.p0.y + 2f * mt * t * p.cp.y + t * t * p.p2.y
            )
        }

        // Draw animated dots
        fun drawDots(path: BezierPath, color: Color, count: Int, progress: Float, reverse: Boolean) {
            for (i in 0 until count) {
                var t = (progress + i.toFloat() / count) % 1f
                if (reverse) t = 1f - t
                val pos = sample(path, t)
                // Glow dot
                drawCircle(color.copy(alpha = 0.15f), radius = 6f, center = pos)
                // Core dot
                drawCircle(color, radius = 3f, center = pos)
            }
        }

        drawDots(solarPath, solarColor, densityDotsSolar, animationProgress, reverse = false)
        drawDots(batteryPath, batteryColor, densityDotsBattery, animationProgress, reverse = batteryW > 0)
        val gridColor = if (gridW > 0) gridExportColor else gridImportColor
        drawDots(gridPath, gridColor, densityDotsGrid, animationProgress, reverse = gridW > 0)

        // Node labels
        fun drawLabel(text: String, pos: Offset, color: Color) {
            val layout = textMeasurer.measure(text, labelStyle)
            drawText(layout, topLeft = Offset(pos.x - layout.size.width / 2, pos.y - 20f))
        }
        drawLabel("Solar", solarPos, solarColor)
        drawLabel("Home", homePos, flowColor)
        drawLabel("Battery", batteryPos, batteryColor)
        drawLabel("Grid", gridPos, neutralColor)
    }
}
