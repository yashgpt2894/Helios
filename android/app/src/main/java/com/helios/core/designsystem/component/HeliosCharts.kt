package com.helios.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.motion.HeliosMotion
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.format.HeliosFormat

/**
 * Canvas components with a data state and a text equivalent.
 *
 * A chart is never colour-only: [BatteryRing], [WeekChart] and [LiveTrail] all carry a
 * semantics description that reads the values, which is also how the per-point values of
 * a scrubbed chart reach a screen reader.
 */

/** Charge direction of the battery, which drives the ring treatment. */
enum class BatteryRingMode { CHARGING, DISCHARGING, IDLE }

/**
 * State of charge ring.
 *
 * Variants and states: charging (glow), discharging (pulse), idle, plus the threshold
 * crossings at 80, 50 and 20 percent that fire the haptics in `HeliosHaptics`. The trim
 * animates with the gentle spring; under Reduce Motion it is a short fade.
 */
@Composable
fun BatteryRing(
    socPct: Double,
    powerW: Double,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    mode: BatteryRingMode = when {
        powerW > 30 -> BatteryRingMode.CHARGING
        powerW < -30 -> BatteryRingMode.DISCHARGING
        else -> BatteryRingMode.IDLE
    },
    reducedMotion: Boolean = false,
    thresholdLabel: String? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val fraction = (socPct / 100.0).coerceIn(0.0, 1.0).toFloat()
    val trimSpec: androidx.compose.animation.core.FiniteAnimationSpec<Float> = if (reducedMotion) {
        tween<Float>(HeliosMotion.DurationMs.fast)
    } else {
        HeliosMotion.gentle
    }
    val animated by animateFloatAsState(
        targetValue = fraction,
        animationSpec = trimSpec,
        label = "socTrim"
    )
    val ringColor = when {
        socPct.isNaN() -> colors.textQuaternary
        socPct < 20 -> colors.batteryLow
        else -> colors.batteryPrimary
    }

    val pulseTransition = rememberInfiniteTransition(label = "ringPulse")
    val glow by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(HeliosMotion.DurationMs.chargeGlowPulse),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringGlow"
    )
    val treatmentAlpha = when {
        reducedMotion -> 1f
        mode == BatteryRingMode.CHARGING -> glow
        mode == BatteryRingMode.DISCHARGING -> 0.6f + glow * 0.5f
        else -> 1f
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = if (socPct.isNaN()) {
                        "Battery state of charge not reported"
                    } else {
                        "Battery ${socPct.toInt()} percent, ${mode.name.lowercase()}, " +
                            "${HeliosFormat.kilowatts(powerW / 1000.0)}"
                    }
                }
        ) {
            val stroke = this.size.minDimension * 0.09f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            drawArc(
                color = ringColor.copy(alpha = 0.15f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = ringColor.copy(alpha = treatmentAlpha.coerceIn(0.35f, 1f)),
                startAngle = 135f,
                sweepAngle = 270f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(HeliosSpacing.space4)
        ) {
            Text(
                text = if (socPct.isNaN()) HeliosFormat.NO_DATA else "${socPct.toInt()}%",
                style = HeliosTypography.title1,
                color = if (socPct.isNaN()) colors.textQuaternary else colors.textPrimary
            )
            Text(
                text = when (mode) {
                    BatteryRingMode.CHARGING -> "Charging"
                    BatteryRingMode.DISCHARGING -> "Discharging"
                    BatteryRingMode.IDLE -> "Idle"
                },
                style = HeliosTypography.caption,
                color = colors.textTertiary
            )
            if (thresholdLabel != null) {
                Spacer(Modifier.height(HeliosSpacing.space1))
                Text(
                    text = thresholdLabel,
                    style = HeliosTypography.caption2,
                    color = colors.batteryPrimary
                )
            }
        }
    }
}

/** One day in the week chart. */
data class WeekBar(
    val dayLabel: String,
    val producedKwh: Double,
    val consumedKwh: Double
)

/**
 * Produced versus consumed, seven days, tappable bars.
 *
 * Variants: ready, selected day, loading skeleton, empty, stale. Selecting a bar raises
 * the readout; the bar itself is a 48 dp-wide target and announces day plus both values.
 */
@Composable
fun WeekChart(
    bars: List<WeekBar>,
    modifier: Modifier = Modifier,
    selectedIndex: Int? = null,
    onSelect: ((Int) -> Unit)? = null,
    state: SurfaceState = SurfaceState.READY,
    height: Dp = HeliosSpacing.LayoutMetrics.chartHeight
) {
    val colors = LocalHeliosSemanticColors.current
    when (state) {
        SurfaceState.LOADING -> SkeletonBlock(height = height, shape = HeliosShape.md, modifier = modifier)
        SurfaceState.EMPTY -> EmptyState(
            title = "No completed days yet",
            message = "The week chart fills in after the first full day of production.",
            modifier = modifier
        )
        SurfaceState.ERROR -> HeliosErrorState(
            title = "Week history unavailable",
            message = "The inverter did not return a full week of history.",
            modifier = modifier
        )
        else -> Column(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height),
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2),
                verticalAlignment = Alignment.Bottom
            ) {
                val maxKwh = maxOf(
                    bars.maxOfOrNull { maxOf(it.producedKwh, it.consumedKwh) } ?: 1.0,
                    1.0
                )
                bars.forEachIndexed { index, bar ->
                    val selected = index == selectedIndex
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = onSelect != null) { onSelect?.invoke(index) }
                            .semantics {
                                contentDescription = "${bar.dayLabel}: produced " +
                                    "${HeliosFormat.kwh(bar.producedKwh)}, consumed " +
                                    "${HeliosFormat.kwh(bar.consumedKwh)}"
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Spacer(Modifier.weight(1f))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(((bar.producedKwh / maxKwh) * (height.value - 28f)).dp.coerceAtLeast(4.dp))
                                    .clip(HeliosShape.xs)
                                    .background(
                                        if (selected) colors.chart.production
                                        else colors.chart.production.copy(alpha = if (state == SurfaceState.STALE) 0.4f else 0.85f)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(((bar.consumedKwh / maxKwh) * (height.value - 28f)).dp.coerceAtLeast(4.dp))
                                    .clip(HeliosShape.xs)
                                    .background(colors.chart.consumption.copy(alpha = 0.7f))
                            )
                        }
                        Spacer(Modifier.height(HeliosSpacing.space1))
                        Text(
                            text = bar.dayLabel.take(3),
                            style = HeliosTypography.caption2,
                            color = if (selected) colors.textPrimary else colors.textTertiary
                        )
                    }
                }
            }
            if (selectedIndex != null && bars.getOrNull(selectedIndex) != null) {
                val bar = bars[selectedIndex]
                Spacer(Modifier.height(HeliosSpacing.space3))
                Text(
                    text = "${bar.dayLabel}: ${HeliosFormat.kwh(bar.producedKwh)} produced, " +
                        "${HeliosFormat.kwh(bar.consumedKwh)} consumed",
                    style = HeliosTypography.callout,
                    color = colors.textSecondary
                )
            }
        }
    }
}

/** The four states a data surface can be in, as an input to a component. */
enum class SurfaceState { READY, LOADING, EMPTY, STALE, ERROR }

/**
 * The live trail: the last 30 samples of the reading.
 *
 * Variants: running (ticker on), stale (line dimmed, no marker), empty. The summary line
 * is the accessible equivalent of the shape.
 */
@Composable
fun LiveTrail(
    values: List<Double>,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    state: SurfaceState = SurfaceState.READY,
    summary: String? = null
) {
    val colors = LocalHeliosSemanticColors.current
    if (values.size < 2) {
        SkeletonBlock(height = height, shape = HeliosShape.sm, shimmer = state == SurfaceState.LOADING, modifier = modifier)
        return
    }
    val dimmed = state == SurfaceState.STALE || state == SurfaceState.ERROR
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics { contentDescription = summary ?: "Live reading trail" }
    ) {
        val minValue = values.min()
        val maxValue = values.max()
        val span = (maxValue - minValue).takeIf { it > 0.0 } ?: 1.0
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index.toFloat() / (values.size - 1) * size.width
            val y = size.height - ((value - minValue) / span).toFloat() * size.height * 0.8f - size.height * 0.1f
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = colors.chart.production.copy(alpha = if (dimmed) 0.35f else 1f),
            style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        if (!dimmed) {
            drawCircle(
                color = colors.chart.todayMarker,
                radius = 3.5f,
                center = Offset(size.width, size.height - ((values.last() - minValue) / span).toFloat() * size.height * 0.8f - size.height * 0.1f)
            )
        }
    }
}

/** A tiny inline sparkline for a metric tile, with no axes. */
@Composable
fun MetricSparkline(
    values: List<Double>,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 24.dp
) {
    if (values.size < 2) return
    Canvas(
        modifier = modifier
            .width(64.dp)
            .height(height)
    ) {
        val minValue = values.min()
        val maxValue = values.max()
        val span = (maxValue - minValue).takeIf { it > 0.0 } ?: 1.0
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index.toFloat() / (values.size - 1) * size.width
            val y = size.height - ((value - minValue) / span).toFloat() * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(width = 1.5f, cap = StrokeCap.Round))
    }
}
