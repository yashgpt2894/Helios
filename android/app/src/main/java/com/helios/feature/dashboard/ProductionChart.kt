package com.helios.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.SkeletonBlock
import com.helios.core.designsystem.component.SurfaceState
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.HistoryPoint
import com.helios.core.format.HeliosFormat
import java.util.Locale

/**
 * Today's production curve, with the consumption line over it.
 *
 * States: ready, empty (before sunrise there is genuinely nothing to draw), loading
 * (skeleton in the chart's own 160 dp box), stale (dimmed, no live marker), and scrubbing.
 * Scrubbing shows the value at the touched half hour, and the same sentence is the
 * chart's accessibility description, so a per-point value is reachable without sight.
 */
@Composable
fun ProductionChart(
    series: List<HistoryPoint>,
    modifier: Modifier = Modifier,
    state: SurfaceState = SurfaceState.READY,
    scrubEnabled: Boolean = true
) {
    val colors = LocalHeliosSemanticColors.current
    if (state == SurfaceState.LOADING) {
        SkeletonBlock(
            height = 160.dp,
            shape = HeliosShape.md,
            modifier = modifier
        )
        return
    }
    if (series.isEmpty() || state == SurfaceState.EMPTY) {
        Column(modifier = modifier.padding(vertical = 24.dp)) {
            androidx.compose.material3.Text(
                text = "No production yet today",
                style = HeliosTypography.callout,
                color = colors.textSecondary
            )
            androidx.compose.material3.Text(
                text = "The curve starts at sunrise.",
                style = HeliosTypography.caption,
                color = colors.textTertiary
            )
        }
        return
    }

    val dimmed = state == SurfaceState.STALE
    var scrubIndex by remember(series) { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()
    val summary = if (scrubIndex != null && series.getOrNull(scrubIndex!!) != null) {
        val point = series[scrubIndex!!]
        val hour = point.t.toInt()
        val minute = ((point.t - hour) * 60).toInt()
        "At %02d:%02d, production %s, consumption %s".format(
            Locale.US,
            hour,
            minute,
            HeliosFormat.kilowatts(point.productionW / 1000.0),
            HeliosFormat.kilowatts(point.consumptionW / 1000.0)
        )
    } else {
        val peak = series.maxByOrNull { it.productionW }
        val producedKwh = series.sumOf { it.productionW } * 0.5 / 1000.0
        "Today's production curve. Peak %s at %02d:00, about %s so far.".format(
            Locale.US,
            HeliosFormat.kilowatts((peak?.productionW ?: 0.0) / 1000.0),
            (peak?.t ?: 0.0).toInt(),
            HeliosFormat.kwh(producedKwh)
        )
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .semantics { contentDescription = summary }
                .then(
                    if (!scrubEnabled) Modifier else Modifier
                        .pointerInput(series) {
                            detectTapGestures { offset ->
                                scrubIndex = indexFor(offset.x, size.width, series.size)
                            }
                        }
                        .pointerInput(series) {
                            detectHorizontalDragGestures { change, _ ->
                                scrubIndex = indexFor(change.position.x, size.width, series.size)
                            }
                        }
                )
        ) {
            val padLeft = 34f
            val padRight = 16f
            val padTop = 12f
            val padBot = 28f
            val chartW = this.size.width - padLeft - padRight
            val chartH = this.size.height - padTop - padBot
            val maxValue = maxOf(
                series.maxOf { it.productionW },
                series.maxOf { it.consumptionW }
            ).coerceAtLeast(100.0)

            fun xFor(index: Int) = padLeft + index.toFloat() / (series.size - 1).coerceAtLeast(1) * chartW
            fun yFor(value: Double) = padTop + chartH - (value / maxValue * chartH).toFloat()

            for (percent in listOf(0.25, 0.5, 0.75)) {
                val y = padTop + chartH - (percent * chartH).toFloat()
                drawLine(
                    color = colors.chart.gridline,
                    start = Offset(padLeft, y),
                    end = Offset(this.size.width - padRight, y),
                    strokeWidth = 0.5f
                )
            }

            val productionPath = Path()
            val productionFill = Path()
            series.forEachIndexed { index, point ->
                val x = xFor(index)
                val y = yFor(point.productionW)
                if (index == 0) {
                    productionPath.moveTo(x, y)
                    productionFill.moveTo(x, padTop + chartH)
                    productionFill.lineTo(x, y)
                } else {
                    productionPath.lineTo(x, y)
                    productionFill.lineTo(x, y)
                }
            }
            productionFill.lineTo(xFor(series.lastIndex), padTop + chartH)
            productionFill.close()

            val productionAlpha = if (dimmed) 0.35f else 1f
            drawPath(productionFill, colors.chart.production.copy(alpha = 0.12f * productionAlpha))
            drawPath(
                productionPath,
                colors.chart.production.copy(alpha = productionAlpha),
                style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            val consumptionPath = Path()
            series.forEachIndexed { index, point ->
                val x = xFor(index)
                val y = yFor(point.consumptionW)
                if (index == 0) consumptionPath.moveTo(x, y) else consumptionPath.lineTo(x, y)
            }
            drawPath(
                consumptionPath,
                colors.chart.consumption.copy(alpha = if (dimmed) 0.5f else 0.9f),
                style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            val axisStyle = TextStyle(
                fontSize = 9.sp,
                color = colors.chart.axis.copy(alpha = if (dimmed) 0.6f else 1f)
            )
            listOf(0f, 6f, 12f, 18f, 24f).forEach { hour ->
                val x = padLeft + (hour / 24f) * chartW
                val layout = textMeasurer.measure("%02.0f".format(Locale.US, hour), axisStyle)
                drawText(
                    layout,
                    topLeft = Offset(x - layout.size.width / 2f, this.size.height - padBot + 6f)
                )
            }

            val index = scrubIndex
            if (index != null && series.getOrNull(index) != null) {
                val point = series[index]
                val x = xFor(index)
                drawLine(
                    color = colors.chart.axis.copy(alpha = 0.4f),
                    start = Offset(x, padTop),
                    end = Offset(x, padTop + chartH),
                    strokeWidth = 1f
                )
                drawCircle(colors.chart.production, radius = 4f, center = Offset(x, yFor(point.productionW)))
                drawCircle(colors.chart.consumption, radius = 3f, center = Offset(x, yFor(point.consumptionW)))
            }
        }
        if (scrubIndex != null) {
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.Text(
                text = summary,
                style = HeliosTypography.caption,
                color = colors.textSecondary
            )
        }
    }
}

/** Nearest series index for a horizontal touch position. */
private fun indexFor(x: Float, width: Int, count: Int): Int {
    val padLeft = 34f
    val padRight = 16f
    val chartW = (width - padLeft - padRight).coerceAtLeast(1f)
    val fraction = ((x - padLeft) / chartW).coerceIn(0f, 1f)
    return (fraction * (count - 1)).toInt().coerceIn(0, count - 1)
}
