package com.helios.feature.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.helios.core.data.repository.SolarCurve
import com.helios.core.data.service.Freshness
import com.helios.core.data.service.Loadable
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.EmptyState
import com.helios.core.designsystem.component.HeliosErrorState
import com.helios.core.designsystem.component.SkeletonBlock
import com.helios.core.designsystem.component.SurfaceState
import com.helios.core.designsystem.haptics.HapticAction
import com.helios.core.designsystem.haptics.HeliosHaptics
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.motion.HeliosMotion
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.HistoryPoint
import com.helios.core.format.HeliosFormat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Today's production curve, with a time-of-day marker that can be scrubbed.
 *
 * The curve is the study surface of the app: the area is production, the thin line is the
 * house's consumption, the dashed line is now, and the solid marker is the point the user is
 * reading. Dragging anywhere on the plot moves the marker to the nearest half hour and the
 * readout above the plot follows it; the same movement is available to a screen reader as
 * two custom actions ("Earlier reading", "Later reading") because a drag is not a gesture
 * everyone can perform (DESIGN.md section 9).
 *
 * States: ready, ready and stale (dimmed, with the marker still scrubbable because the past
 * is still true), loading (a skeleton in the plot's shape), empty (before sunrise, in words
 * plus the action that helps), and error (the classified failure with one recovery action).
 *
 * Motion: the curve draws in over `motion.durationMs.draw` (600 ms, decelerate easing) once,
 * and the marker follows a scrub with the snappy token spring so a drag reads as a physical
 * pick. Reduce Motion removes the draw-in and snaps the marker.
 */
@Composable
fun ProductionChart(
    series: Loadable<List<HistoryPoint>>,
    modifier: Modifier = Modifier,
    height: Dp = HeliosSpacing.LayoutMetrics.chartHeight,
    nowHour: Double = SolarCurve.nowAsHourFloat(),
    selectedT: Double? = null,
    onSelect: ((Double?) -> Unit)? = null,
    scrubEnabled: Boolean = true,
    motion: HeliosMotionSettings = HeliosMotionSettings.Default,
    onRetry: (() -> Unit)? = null,
    title: String = "Today"
) {
    val colors = LocalHeliosSemanticColors.current
    val state = when (series) {
        is Loadable.Loading -> SurfaceState.LOADING
        is Loadable.Empty -> SurfaceState.EMPTY
        is Loadable.Failed -> SurfaceState.ERROR
        is Loadable.Ready -> if (series.meta.freshness == Freshness.STALE) SurfaceState.STALE else SurfaceState.READY
    }

    Column(modifier = modifier.fillMaxWidth()) {
        when (state) {
            SurfaceState.LOADING -> SkeletonBlock(height = height, shape = HeliosShape.md)

            SurfaceState.EMPTY -> EmptyState(
                title = "Nothing produced yet today",
                message = "The curve starts at the first reading after sunrise. " +
                    (series as? Loadable.Empty)?.message.orEmpty(),
                actionLabel = (series as? Loadable.Empty)?.actionLabel,
                onAction = onRetry
            )

            SurfaceState.ERROR -> HeliosErrorState(
                title = "Today's curve is unavailable",
                message = (series as? Loadable.Failed)?.failure?.message
                    ?: "The inverter did not return the day's history.",
                detail = (series as? Loadable.Failed)?.failure?.detail,
                actionLabel = (series as? Loadable.Failed)?.failure?.action ?: "Retry now",
                onAction = onRetry
            )

            else -> {
                val points = (series as Loadable.Ready).value
                if (points.size < 2) {
                    EmptyState(
                        title = "Not enough of the day yet",
                        message = "The curve needs at least two readings to draw.",
                        actionLabel = null,
                        onAction = null
                    )
                    return@Column
                }
                val dimmed = state == SurfaceState.STALE
                ChartReadout(
                    points = points,
                    selectedT = selectedT,
                    nowHour = nowHour,
                    dimmed = dimmed
                )
                Spacer(Modifier.height(HeliosSpacing.space2))
                ScrubablePlot(
                    points = points,
                    height = height,
                    nowHour = nowHour,
                    selectedT = selectedT,
                    onSelect = onSelect,
                    scrubEnabled = scrubEnabled && onSelect != null,
                    motion = motion,
                    dimmed = dimmed
                )
                Spacer(Modifier.height(HeliosSpacing.space2))
                HourAxis()
                if (dimmed) {
                    Spacer(Modifier.height(HeliosSpacing.space2))
                    Text(
                        text = "Last known curve \u00B7 the link is down",
                        style = HeliosTypography.caption2,
                        color = colors.textTertiary
                    )
                }
                Spacer(Modifier.height(HeliosSpacing.space2))
                Text(
                    text = chartSummary(points, selectedT),
                    style = HeliosTypography.caption,
                    color = colors.textQuaternary
                )
            }
        }
    }
}

/** Value above the plot: the selected point, or the current reading when nothing is picked. */
@Composable
private fun ChartReadout(
    points: List<HistoryPoint>,
    selectedT: Double?,
    nowHour: Double,
    dimmed: Boolean
) {
    val colors = LocalHeliosSemanticColors.current
    val alpha = if (dimmed) 0.6f else 1f
    val point = selectedT?.let { nearestPoint(points, it) } ?: nearestPoint(points, nowHour)
    val produced = point?.productionW ?: Double.NaN
    val used = point?.consumptionW ?: Double.NaN
    val battery = point?.batteryW ?: Double.NaN
    Column {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = HeliosFormat.wattsToKilowatts(produced),
                style = HeliosTypography.title1,
                color = colors.textPrimary.copy(alpha = alpha),
                fontWeight = FontWeight.Light
            )
            Spacer(Modifier.width(HeliosSpacing.space2))
            // The readout names the half hour it belongs to rather than saying "now": the
            // curve is sampled every half hour, and the live figure is the hero number above
            // it, which comes from a different register.
            Text(
                text = if (selectedT != null) {
                    hourLabel(point?.t ?: selectedT) + " selected"
                } else {
                    hourLabel(point?.t ?: nowHour) + " half hour"
                },
                style = HeliosTypography.caption2,
                color = colors.textTertiary.copy(alpha = alpha)
            )
        }
        Spacer(Modifier.height(HeliosSpacing.space1))
        Text(
            text = "home ${HeliosFormat.wattsToKilowatts(used)} \u00B7 battery ${batteryStateWord(battery).lowercase()} " +
                HeliosFormat.wattsToKilowatts(abs(battery)),
            style = HeliosTypography.callout,
            color = colors.textSecondary.copy(alpha = alpha)
        )
    }
}

/** The plot itself: area, consumption line, now marker, scrub marker and the gestures. */
@Composable
private fun ScrubablePlot(
    points: List<HistoryPoint>,
    height: Dp,
    nowHour: Double,
    selectedT: Double?,
    onSelect: ((Double?) -> Unit)?,
    scrubEnabled: Boolean,
    motion: HeliosMotionSettings,
    dimmed: Boolean
) {
    val colors = LocalHeliosSemanticColors.current
    val view = LocalView.current
    // The curve draws in once, over the chart-draw token, and never re-draws on a scrub.
    val reveal = remember { Animatable(if (motion.animates()) 0f else 1f) }
    LaunchedEffect(motion.animates()) {
        if (motion.animates()) {
            reveal.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = motion.durationMs(HeliosMotion.Entrance.chartDrawMs),
                    easing = HeliosMotion.Curves.decelerate
                )
            )
        }
    }
    val markerSpec = if (motion.animates()) HeliosMotion.snappy else motion.tweenMs(HeliosMotion.DurationMs.fast)
    val markerX by animateFloatAsState(
        targetValue = (selectedT ?: -1.0).toFloat(),
        animationSpec = markerSpec,
        label = "scrubMarker"
    )
    val alpha = if (dimmed) 0.55f else 1f
    val pick: (Float, Float) -> Unit = { x, width ->
        if (width > 0f) {
            val hour = (x / width * 24f).coerceIn(0f, 24f)
            val snapped = (hour * 2f).roundToInt() / 2.0
            HeliosHaptics.perform(view, HapticAction.PICKER_SCRUB)
            onSelect?.invoke(snapped)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(HeliosShape.md)
            .background(colors.backgroundSecondary)
            .semantics {
                contentDescription = chartSummary(points, selectedT)
                if (scrubEnabled) {
                    customActions = listOf(
                        CustomAccessibilityAction("Earlier reading") {
                            onSelect?.invoke(stepSelection(points, selectedT ?: nowHour, -0.5)); true
                        },
                        CustomAccessibilityAction("Later reading") {
                            onSelect?.invoke(stepSelection(points, selectedT ?: nowHour, 0.5)); true
                        }
                    )
                }
            }
            .pointerInput(scrubEnabled) {
                if (!scrubEnabled) return@pointerInput
                val width = size.width.toFloat()
                detectTapGestures { offset -> pick(offset.x, width) }
            }
            .pointerInput(scrubEnabled) {
                if (!scrubEnabled) return@pointerInput
                val width = size.width.toFloat()
                // Horizontal only: a vertical swipe over the hero chart must still scroll
                // the screen rather than being captured as a scrub.
                detectHorizontalDragGestures(
                    onDragStart = { offset -> pick(offset.x, width) },
                    onHorizontalDrag = { change, _ -> pick(change.position.x, width) }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val maxW = max(
                points.maxOfOrNull { it.productionW } ?: 0.0,
                points.maxOfOrNull { it.consumptionW } ?: 0.0
            ).coerceAtLeast(1000.0)
            fun yFor(watts: Double): Float =
                (size.height - (watts / maxW * size.height * 0.86) - size.height * 0.07).toFloat()

            fun xFor(hour: Double): Float = (hour / 24.0 * size.width).toFloat()

            // hour gridlines
            listOf(6.0, 12.0, 18.0).forEach { gridHour ->
                drawLine(
                    color = colors.chart.gridline,
                    start = Offset(xFor(gridHour), 0f),
                    end = Offset(xFor(gridHour), size.height),
                    strokeWidth = 1f
                )
            }

            val visible = if (motion.animates()) {
                (points.size * reveal.value).roundToInt().coerceAtLeast(2).coerceAtMost(points.size)
            } else {
                points.size
            }
            val slice = points.take(visible)

            val area = Path().apply {
                moveTo(xFor(slice.first().t), size.height)
                slice.forEach { p -> lineTo(xFor(p.t), yFor(p.productionW)) }
                lineTo(xFor(slice.last().t), size.height)
                close()
            }
            drawPath(
                path = area,
                brush = Brush.verticalGradient(
                    listOf(
                        colors.chart.production.copy(alpha = 0.32f * alpha),
                        colors.chart.production.copy(alpha = 0.02f)
                    )
                )
            )
            drawCurve(
                points = slice,
                xFor = { xFor(it.t) },
                yFor = { yFor(it.productionW) },
                color = colors.chart.production.copy(alpha = alpha),
                width = 3f
            )
            drawCurve(
                points = slice,
                xFor = { xFor(it.t) },
                yFor = { yFor(it.consumptionW) },
                color = colors.chart.consumption.copy(alpha = 0.85f * alpha),
                width = 2f
            )

            // now marker
            val nowX = xFor(nowHour)
            drawDashedLine(
                color = colors.chart.todayMarker.copy(alpha = 0.7f * alpha),
                x = nowX,
                height = size.height
            )

            // scrub marker
            if (markerX >= 0f) {
                val x = xFor(markerX.toDouble())
                val point = nearestPoint(points, markerX.toDouble())
                drawLine(
                    color = colors.textPrimary.copy(alpha = 0.7f * alpha),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2f
                )
                if (point != null) {
                    drawCircle(
                        color = colors.chart.todayMarker,
                        radius = 5f,
                        center = Offset(x, yFor(point.productionW))
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCurve(
    points: List<HistoryPoint>,
    xFor: (HistoryPoint) -> Float,
    yFor: (HistoryPoint) -> Float,
    color: Color,
    width: Float
) {
    if (points.size < 2) return
    val path = Path()
    points.forEachIndexed { index, p ->
        val x = xFor(p)
        val y = yFor(p)
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(path, color, style = Stroke(width = width, cap = StrokeCap.Round))
}

private fun DrawScope.drawDashedLine(color: Color, x: Float, height: Float) {
    val dash = 8f
    val gap = 6f
    var y = 0f
    while (y < height) {
        drawLine(
            color = color,
            start = Offset(x, y),
            end = Offset(x, (y + dash).coerceAtMost(height)),
            strokeWidth = 2f
        )
        y += dash + gap
    }
}

/** Hour ticks under the plot, at the same anchors the gridlines use. */
@Composable
private fun HourAxis() {
    val colors = LocalHeliosSemanticColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("00", "06", "12", "18", "24").forEach { label ->
            Text(
                text = label,
                style = HeliosTypography.caption2,
                color = colors.textQuaternary
            )
        }
    }
}

// ------------------------------------------------------------------ pure helpers

/** The series point nearest a given hour of the day. */
fun nearestPoint(points: List<HistoryPoint>, hour: Double): HistoryPoint? =
    points.minByOrNull { abs(it.t - hour) }

/** One half-hour step from a marker position, clamped to the day. */
fun stepSelection(points: List<HistoryPoint>, from: Double, step: Double): Double {
    val maxT = points.maxOfOrNull { it.t } ?: 24.0
    return (from + step).coerceIn(0.0, maxT)
}

/** "14:30" from an hour of the day. */
fun hourLabel(hour: Double): String {
    val total = (hour * 60).roundToInt().coerceIn(0, 24 * 60)
    val h = total / 60
    val m = total % 60
    return "%02d:%02d".format(java.util.Locale.US, h, m)
}

/**
 * The chart in words: the shape of the day and the point being read.
 *
 * It deliberately states no daily total. The inverter's own meter (`energyTodayKwh`) is the
 * one authority for that number, and a total summed from half-hour samples would state a
 * slightly different figure next to it on the same screen.
 */
fun chartSummary(points: List<HistoryPoint>, selectedT: Double?): String {
    if (points.isEmpty()) return "No production recorded yet today"
    val peak = points.maxByOrNull { it.productionW }
    val base = "Today's curve: peak " +
        "${HeliosFormat.wattsToKilowatts(peak?.productionW ?: Double.NaN)} at " +
        hourLabel(peak?.t ?: 0.0)
    val selected = selectedT?.let { nearestPoint(points, it) }
    return if (selected == null) {
        base
    } else {
        "$base. Selected ${hourLabel(selected.t)}: " +
            "${HeliosFormat.wattsToKilowatts(selected.productionW)} produced, " +
            "${HeliosFormat.wattsToKilowatts(selected.consumptionW)} used"
    }
}
