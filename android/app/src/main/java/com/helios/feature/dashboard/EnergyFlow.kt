package com.helios.feature.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.format.HeliosFormat
import kotlin.math.abs

/**
 * The four-node live energy model: Solar above, Home in the centre, Battery and Grid
 * below.
 *
 * States: live, night (no solar dots), stale (trails stop, nodes dimmed), reduced motion
 * (static arrows instead of moving dots), offline (no trails at all, last known values
 * labelled by the caller).
 *
 * Dot density and dot speed both follow the wattage on that path, and battery charging
 * and grid export reverse the direction (motion-language section 4.2). The canvas carries
 * one text equivalent that summarises all four paths, because a picture of energy is not
 * readable by a screen reader.
 */
@Composable
fun EnergyFlow(
    solarW: Double,
    batteryW: Double,
    gridW: Double,
    homeW: Double,
    modifier: Modifier = Modifier,
    stale: Boolean = false,
    offline: Boolean = false,
    reducedMotion: Boolean = false,
    height: androidx.compose.ui.unit.Dp = 240.dp
) {
    val colors = LocalHeliosSemanticColors.current
    val running = !stale && !offline && !reducedMotion
    val dimFactor = if (stale || offline) 0.6f else 1f

    val transition = rememberInfiniteTransition(label = "energyFlowAnim")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = flowDurationMs(solarW, batteryW, gridW),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "flowProgress"
    )

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = colors.textSecondary)
    val description = buildString {
        append("Energy flow. Solar ")
        append(HeliosFormat.kilowatts(solarW / 1000.0))
        append(" to home. Home load ")
        append(HeliosFormat.kilowatts(homeW / 1000.0))
        append(". Battery ")
        append(if (batteryW >= 0) "charging " else "discharging ")
        append(HeliosFormat.kilowatts(abs(batteryW) / 1000.0))
        append(". Grid ")
        append(
            when {
                gridW > 0 -> "exporting ${HeliosFormat.kilowatts(gridW / 1000.0)}"
                gridW < 0 -> "importing ${HeliosFormat.kilowatts(abs(gridW) / 1000.0)}"
                else -> "0 W"
            }
        )
        if (offline) append(". Last known values, the link is down")
        if (stale) append(". Values are stale")
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics(mergeDescendants = true) { contentDescription = description }
    ) {
        val cx = size.width / 2f
        val solarPos = Offset(cx, 40f)
        val homePos = Offset(cx, size.height / 2f)
        val batteryPos = Offset(cx - 80f, size.height - 40f)
        val gridPos = Offset(cx + 80f, size.height - 40f)

        data class BezierPath(val p0: Offset, val cp: Offset, val p2: Offset, val path: Path)

        fun path(from: Offset, control: Offset, to: Offset) = BezierPath(
            p0 = from,
            cp = control,
            p2 = to,
            path = Path().apply {
                moveTo(from.x, from.y)
                quadraticBezierTo(control.x, control.y, to.x, to.y)
            }
        )

        val solarPath = path(
            solarPos,
            Offset(solarPos.x, solarPos.y + (homePos.y - solarPos.y) * 0.4f + 20f),
            homePos
        )
        val batteryPath = path(
            batteryPos,
            Offset((batteryPos.x + homePos.x) / 2f + 20f, (batteryPos.y + homePos.y) / 2f),
            homePos
        )
        val gridPath = path(
            gridPos,
            Offset((gridPos.x + homePos.x) / 2f - 20f, (gridPos.y + homePos.y) / 2f),
            homePos
        )

        val solarColor = colors.solarPrimary
        val batteryColor = colors.batteryPrimary
        val gridColor = if (gridW > 0) colors.gridExportPrimary else colors.gridImportPrimary
        val homeColor = colors.flowPrimary

        val stroke = Stroke(width = 1.5f, cap = StrokeCap.Round)
        drawPath(solarPath.path, solarColor.copy(alpha = 0.25f * dimFactor), style = stroke)
        drawPath(batteryPath.path, batteryColor.copy(alpha = 0.25f * dimFactor), style = stroke)
        drawPath(gridPath.path, gridColor.copy(alpha = 0.25f * dimFactor), style = stroke)

        fun sample(p: BezierPath, t: Float): Offset {
            val mt = 1f - t
            return Offset(
                x = mt * mt * p.p0.x + 2f * mt * t * p.cp.x + t * t * p.p2.x,
                y = mt * mt * p.p0.y + 2f * mt * t * p.cp.y + t * t * p.p2.y
            )
        }

        fun dots(p: BezierPath, color: Color, count: Int, reverse: Boolean) {
            if (count <= 0) return
            for (index in 0 until count) {
                var t = (progress + index.toFloat() / count) % 1f
                if (reverse) t = 1f - t
                val position = sample(p, t)
                drawCircle(color.copy(alpha = 0.15f * dimFactor), radius = 6f, center = position)
                drawCircle(color.copy(alpha = dimFactor), radius = 3f, center = position)
            }
        }

        fun arrows(p: BezierPath, color: Color, forward: Boolean) {
            listOf(0.3f, 0.5f, 0.7f).forEach { t ->
                val position = sample(p, t)
                val next = sample(p, if (forward) (t + 0.02f).coerceAtMost(1f) else (t - 0.02f).coerceAtLeast(0f))
                drawLine(
                    color = color.copy(alpha = 0.8f * dimFactor),
                    start = position,
                    end = next,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }

        val solarDots = densityFor(solarW)
        val batteryDots = densityFor(abs(batteryW))
        val gridDots = densityFor(abs(gridW))

        when {
            reducedMotion -> {
                arrows(solarPath, solarColor, forward = true)
                arrows(batteryPath, batteryColor, forward = batteryW < 0)
                arrows(gridPath, gridColor, forward = gridW > 0)
            }
            offline -> Unit
            else -> {
                dots(solarPath, solarColor, solarDots, reverse = false)
                dots(batteryPath, batteryColor, batteryDots, reverse = batteryW > 0)
                dots(gridPath, gridColor, gridDots, reverse = gridW > 0)
            }
        }

        fun label(text: String, position: Offset, color: Color) {
            val layout = textMeasurer.measure(text, labelStyle)
            drawText(
                layout,
                topLeft = Offset(position.x - layout.size.width / 2f, position.y - 22f)
            )
            drawCircle(color.copy(alpha = dimFactor), radius = 6f, center = position)
        }

        label("Solar", solarPos, solarColor)
        label("Home", homePos, homeColor)
        label("Battery", batteryPos, batteryColor)
        label("Grid", gridPos, gridColor)
    }
}

/** Dots per path, from the design's density bands. */
private fun densityFor(watts: Double): Int = when {
    watts <= 0 -> 0
    watts <= 500 -> 1
    watts <= 2_000 -> 2
    watts <= 5_000 -> 3
    else -> 4
}

/** Dot traversal time: 2.2 s at 1 kW down to 1.0 s at 10 kW. */
private fun flowDurationMs(solarW: Double, batteryW: Double, gridW: Double): Int {
    val peakKw = maxOf(solarW, abs(batteryW), abs(gridW)) / 1000.0
    val speed = (1 + (peakKw - 1).coerceAtLeast(0.0) * (1.2 / 9.0)).coerceIn(1.0, 2.2)
    return (2_200 / speed).toInt()
}
