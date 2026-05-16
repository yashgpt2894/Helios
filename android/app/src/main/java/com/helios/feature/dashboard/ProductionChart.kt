package com.helios.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helios.core.designsystem.color.HeliosColor
import com.helios.core.domain.model.HistoryPoint
import java.util.Locale

/**
 * ProductionChart: stacked area chart for production/consumption over a day.
 * Canvas-based to avoid third-party chart libraries.
 */
@Composable
fun ProductionChart(
    series: List<HistoryPoint>,
    modifier: Modifier = Modifier
) {
    val isDark = true // requires theme context; simplified
    val productionColor = HeliosColor.Solar.ramp(500, isDark)
    val consumptionColor = HeliosColor.Flow.ramp(500, isDark)
    val backdropColor = HeliosColor.Neutral.ramp(700, isDark)
    val labelColor = HeliosColor.Neutral.ramp(600, isDark)
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxWidth().height(160.dp)) {
        if (series.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height
        val padLeft = 32f
        val padRight = 16f
        val padTop = 12f
        val padBot = 32f
        val chartW = w - padLeft - padRight
        val chartH = h - padTop - padBot

        val maxVal = maxOf(
            series.maxOf { it.productionW },
            series.maxOf { it.consumptionW }
        ).coerceAtLeast(100.0)

        fun Double.x(i: Int) = padLeft + (i.toFloat() / (series.size - 1).coerceAtLeast(1)) * chartW
        fun Double.y(v: Double) = padTop + chartH - (v / maxVal * chartH).toFloat()

        // Backdrop grid lines
        for (pct in listOf(0.25, 0.5, 0.75)) {
            val gy = padTop + chartH - (pct * chartH).toFloat()
            drawLine(backdropColor, Offset(padLeft, gy), Offset(w - padRight, gy), strokeWidth = 0.5f)
        }

        // Production area path
        val prodPath = Path().apply {
            var first = true
            series.forEachIndexed { i, pt ->
                val x = pt.t.x(i)
                val y = pt.productionW.y(pt.productionW)
                if (first) { moveTo(x, y); first = false }
                else lineTo(x, y)
            }
        }
        val prodFill = Path(prodPath).apply {
            val lastX = series.last().t.x(series.lastIndex)
            lineTo(lastX, padTop + chartH)
            lineTo(padLeft, padTop + chartH)
            close()
        }
        drawPath(prodFill, productionColor.copy(alpha = 0.12f))
        drawPath(prodPath, productionColor, style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Consumption line
        val consPath = Path().apply {
            var first = true
            series.forEachIndexed { i, pt ->
                val x = pt.t.x(i)
                val y = pt.consumptionW.y(pt.consumptionW)
                if (first) { moveTo(x, y); first = false }
                else lineTo(x, y)
            }
        }
        drawPath(consPath, consumptionColor, style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // X-axis labels
        val xLabelStyle = TextStyle(fontSize = 9.sp, color = labelColor)
        listOf(0f, 6f, 12f, 18f, 24f).forEach { hh ->
            val x = padLeft + (hh / 24f) * chartW
            val label = "%.0f".format(Locale.US, hh)
            val layout = textMeasurer.measure(label, xLabelStyle)
            drawText(layout, topLeft = Offset(x - layout.size.width / 2, h - padBot + 6f))
        }
    }
}
