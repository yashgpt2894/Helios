package com.helios.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.SkeletonBlock
import com.helios.core.designsystem.component.SurfaceState
import com.helios.core.designsystem.component.WeatherIcon
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.ForecastDay
import com.helios.core.format.HeliosFormat

/**
 * The forecast at the glance layer: five days, each as weekday, condition glyph and words,
 * expected energy, and a micro-bar that shows the shape of the week without an axis.
 *
 * States: ready, loading (skeleton columns in the strip's own shape), empty (an explanatory
 * line plus the action that fixes it), stale (dimmed, because a forecast read from a dead
 * link is still useful but must not look fresh), and error, which is inline: a failed
 * forecast is F8, so it never blanks the rest of the screen.
 *
 * Selecting a day raises a readout under the strip (weekday, condition, expected energy and
 * the comparison with typical) instead of navigating away, because the glance must stay a
 * glance.
 *
 * Width, measured on the 1080 x 2400 emulator (412 dp): five days at 64 dp with 12 dp between
 * them is 368 dp, which fits the 372 dp of content width inside the 20 dp gutter, so the
 * glance does not need a horizontal gesture. A longer list still scrolls rather than
 * truncating, which is what DESIGN.md section 10 asked to be measured rather than assumed.
 */
@Composable
fun ForecastStrip(
    days: List<ForecastDay>,
    modifier: Modifier = Modifier,
    state: SurfaceState = SurfaceState.READY,
    demoQualifier: Boolean = false,
    selectedIndex: Int? = null,
    onSelect: ((Int) -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    itemWidth: Dp = 64.dp
) {
    val colors = LocalHeliosSemanticColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        when (state) {
            SurfaceState.LOADING -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
            ) {
                repeat(5) {
                    SkeletonBlock(
                        height = 96.dp,
                        shape = HeliosShape.md,
                        modifier = Modifier.width(itemWidth)
                    )
                }
            }

            SurfaceState.EMPTY -> InlineNotice(
                text = "No forecast for this place yet. Pick a place to get one.",
                actionLabel = "Choose a place",
                onAction = onRetry
            )

            SurfaceState.ERROR -> InlineNotice(
                text = "Forecast unavailable. The rest of the dashboard is unaffected.",
                actionLabel = "Retry",
                onAction = onRetry,
                alert = true
            )

            else -> {
                val dimmed = state == SurfaceState.STALE
                val alpha = if (dimmed) 0.6f else 1f
                val maxKwh = days.maxOfOrNull { it.expectedKwh } ?: 1.0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
                ) {
                    days.forEachIndexed { index, day ->
                        ForecastDayItem(
                            day = day,
                            index = index,
                            maxKwh = maxKwh,
                            selected = index == selectedIndex,
                            dimmed = dimmed,
                            demoQualifier = demoQualifier && index == 0,
                            alpha = alpha,
                            width = itemWidth,
                            onSelect = onSelect
                        )
                    }
                }
                val selected = selectedIndex?.let { days.getOrNull(it) }
                if (selected != null) {
                    Spacer(Modifier.height(HeliosSpacing.space3))
                    Text(
                        text = forecastReadout(selected),
                        style = HeliosTypography.callout,
                        color = colors.textSecondary.copy(alpha = alpha)
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastDayItem(
    day: ForecastDay,
    index: Int,
    maxKwh: Double,
    selected: Boolean,
    dimmed: Boolean,
    demoQualifier: Boolean,
    alpha: Float,
    width: Dp,
    onSelect: ((Int) -> Unit)?
) {
    val colors = LocalHeliosSemanticColors.current
    val barFraction = (day.expectedKwh / maxKwh).coerceIn(0.05, 1.0).toFloat()
    val interactive = if (onSelect != null) {
        Modifier
            .clip(HeliosShape.md)
            .clickable(role = Role.Button) { onSelect(index) }
    } else {
        Modifier
    }
    Column(
        modifier = interactive
            .width(width)
            .clip(HeliosShape.md)
            .background(if (selected) colors.backgroundTertiary else colors.backgroundSecondary)
            .border(
                HeliosSpacing.space0,
                colors.separatorHairline,
                HeliosShape.md
            )
            .padding(HeliosSpacing.space2)
            .semantics(mergeDescendants = true) {
                contentDescription = "${dayShortLabel(day.date)}, ${day.conditionLabel}, " +
                    "${HeliosFormat.kwh(day.expectedKwh, decimals = 1)} expected"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space1)
    ) {
        Text(
            text = dayShortLabel(day.date),
            style = HeliosTypography.caption2,
            color = colors.textTertiary.copy(alpha = alpha),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        WeatherIcon(condition = day.condition, size = 20.dp, label = day.conditionLabel)
        Text(
            text = DashboardMetrics.number(day.expectedKwh, 1),
            style = HeliosTypography.callout,
            color = colors.textPrimary.copy(alpha = alpha),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "kWh",
            style = HeliosTypography.caption2,
            color = colors.textQuaternary.copy(alpha = alpha)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(HeliosShape.xs)
                .background(colors.backgroundPrimary),
            contentAlignment = Alignment.BottomStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(barFraction)
                    .height(28.dp)
                    .clip(HeliosShape.xs)
                    .background(
                        if (selected) colors.chart.production
                        else colors.chart.production.copy(alpha = 0.7f)
                    )
            )
        }
        if (demoQualifier) {
            Text(
                text = "Demo data",
                style = HeliosTypography.caption2,
                color = colors.textQuaternary.copy(alpha = alpha)
            )
        }
    }
}

@Composable
private fun InlineNotice(
    text: String,
    actionLabel: String,
    onAction: (() -> Unit)?,
    alert: Boolean = false
) {
    val colors = LocalHeliosSemanticColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(if (alert) colors.alertSubtle else colors.backgroundSecondary)
            .padding(HeliosSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = HeliosTypography.callout,
            color = if (alert) colors.alertStrong else colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        if (onAction != null) {
            HeliosGhostButton(text = actionLabel, onClick = onAction)
        }
    }
}

/** "Thu" from an ISO date, without throwing on a malformed one. */
fun dayShortLabel(dateIso: String): String = runCatching {
    HeliosFormat.weekday(
        java.time.LocalDate.parse(dateIso)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )
}.getOrDefault(dateIso)

/** The selected day in words, including how it compares with a typical day. */
fun forecastReadout(day: ForecastDay): String =
    "${dayShortLabel(day.date)} \u00B7 ${day.conditionLabel} \u00B7 " +
        "${HeliosFormat.kwh(day.expectedKwh, decimals = 1)} expected \u00B7 " +
        HeliosFormat.signed(day.expectedKwhVsTypical, "%", decimals = 0) + " vs typical"
