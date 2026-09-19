package com.helios.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.HeliosSeverityKind
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.ForecastDay
import com.helios.core.domain.model.Insight
import com.helios.core.format.HeliosFormat
import androidx.compose.foundation.border

/**
 * Advisory and forecast surfaces: the rows that carry meaning rather than measurements.
 */

/** Severity in words, because a colour is not a severity (DESIGN.md 9). */
fun HeliosSeverityKind.word(): String = when (this) {
    HeliosSeverityKind.POSITIVE -> "Positive"
    HeliosSeverityKind.NEUTRAL -> "Neutral"
    HeliosSeverityKind.ATTENTION -> "Attention"
    HeliosSeverityKind.CRITICAL -> "Critical"
}

/**
 * Advisory row.
 *
 * Variants: positive, neutral, attention, critical; with and without an action; with and
 * without the demo qualifier. The action is a real button that runs [onAction], never a
 * dead control.
 */
@Composable
fun InsightCard(
    insight: Insight,
    severity: HeliosSeverityKind,
    modifier: Modifier = Modifier,
    demoQualifier: Boolean = false,
    onAction: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val foreground = colors.insight.foreground(severity)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(colors.backgroundTertiary)
            .border(HeliosElevation.MaterialTokens.innerStrokeWidth, colors.separatorHairline, HeliosShape.md)
            .padding(HeliosSpacing.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .heightIn(min = 48.dp)
                .clip(HeliosShape.xs)
                .background(foreground)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = insight.title,
                    style = HeliosTypography.headline,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (insight.metric != null) {
                    Text(
                        text = insight.metric,
                        style = HeliosTypography.title3,
                        color = foreground
                    )
                }
            }
            Spacer(Modifier.height(HeliosSpacing.space1))
            Text(
                text = insight.body,
                style = HeliosTypography.callout,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(HeliosSpacing.space2))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
            ) {
                Text(
                    text = severity.word(),
                    style = HeliosTypography.caption2,
                    color = foreground
                )
                if (insight.delta != null) {
                    Text(
                        text = insight.delta,
                        style = HeliosTypography.caption2,
                        color = colors.textTertiary
                    )
                }
                if (demoQualifier) {
                    Text(
                        text = "Demo data",
                        style = HeliosTypography.caption2,
                        color = colors.textTertiary
                    )
                }
            }
            if (insight.actionLabel != null && onAction != null) {
                Spacer(Modifier.height(HeliosSpacing.space2))
                HeliosGhostButton(text = insight.actionLabel, onClick = onAction)
            }
        }
    }
}

/**
 * Seven-day forecast as rows.
 *
 * States: ready, loading (three skeleton rows in the row shape), empty, stale, error
 * with a retry inside the section (F8 never blanks the screen).
 */
@Composable
fun ForecastCard(
    days: List<ForecastDay>,
    modifier: Modifier = Modifier,
    state: SurfaceState = SurfaceState.READY,
    demoQualifier: Boolean = false,
    onRetry: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(colors.backgroundTertiary)
            .border(HeliosElevation.MaterialTokens.innerStrokeWidth, colors.separatorHairline, HeliosShape.md)
    ) {
        when (state) {
            SurfaceState.LOADING -> repeat(3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(HeliosSpacing.cardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
                ) {
                    SkeletonBlock(height = 24.dp, modifier = Modifier.width(24.dp), shape = HeliosShape.xs)
                    SkeletonBlock(height = 14.dp, modifier = Modifier.width(96.dp))
                    SkeletonBlock(height = 14.dp, modifier = Modifier.width(56.dp))
                }
            }
            SurfaceState.EMPTY -> EmptyState(
                title = "No forecast for this location",
                message = "Pick a place by name to get a forecast for it.",
                modifier = Modifier.padding(HeliosSpacing.space1),
                actionLabel = "Choose a place",
                onAction = onRetry
            )
            SurfaceState.ERROR -> HeliosErrorState(
                title = "Forecast unavailable",
                message = "The forecast service did not answer. The rest of the dashboard is unaffected.",
                modifier = Modifier.padding(HeliosSpacing.space1),
                actionLabel = "Retry",
                onAction = onRetry
            )
            else -> days.forEachIndexed { index, day ->
                ForecastRow(
                    day = day,
                    dimmed = state == SurfaceState.STALE,
                    demoQualifier = demoQualifier && index == 0
                )
            }
        }
    }
}

/**
 * Weekday for an ISO date. A malformed date shows the raw string rather than crashing a
 * forecast list, because the date comes from a network response.
 */
private fun weekdayLabel(dateIso: String): String = runCatching {
    HeliosFormat.weekday(
        java.time.LocalDate.parse(dateIso).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
}.getOrDefault(dateIso)

/** One forecast row: day, condition, expected energy and how it compares with typical. */
@Composable
fun ForecastRow(
    day: ForecastDay,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    demoQualifier: Boolean = false
) {
    val colors = LocalHeliosSemanticColors.current
    val alpha = if (dimmed) 0.6f else 1f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(HeliosSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
    ) {
        Text(
            text = weekdayLabel(day.date),
            style = HeliosTypography.caption2,
            color = colors.textTertiary.copy(alpha = alpha),
            modifier = Modifier.width(36.dp)
        )
        WeatherIcon(condition = day.condition, size = 22.dp, label = day.conditionLabel)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = day.conditionLabel,
                style = HeliosTypography.callout,
                color = colors.textPrimary.copy(alpha = alpha)
            )
            if (demoQualifier) {
                Text(
                    text = "Demo data",
                    style = HeliosTypography.caption2,
                    color = colors.textTertiary.copy(alpha = alpha)
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = HeliosFormat.kwh(day.expectedKwh, decimals = 1),
                style = HeliosTypography.headline,
                color = colors.textPrimary.copy(alpha = alpha)
            )
            Text(
                text = HeliosFormat.signed(day.expectedKwhVsTypical, "%", decimals = 0) + " vs typical",
                style = HeliosTypography.caption2,
                color = colors.textTertiary.copy(alpha = alpha)
            )
        }
    }
}

/** One metric line in the shared-snapshot viewer, including a missing optional value. */
@Composable
fun SnapshotSummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    missing: Boolean = false
) {
    val colors = LocalHeliosSemanticColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = HeliosSpacing.space2),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = HeliosTypography.callout,
            color = colors.textSecondary
        )
        Text(
            text = if (missing) "Not in this snapshot" else value,
            style = HeliosTypography.headline,
            color = if (missing) colors.textQuaternary else colors.textPrimary
        )
    }
}

/** What the share sheet is doing. */
enum class ShareSheetStatus { IDLE, PREPARING, COPIED, SHARED, FAILED }

/**
 * Share sheet content: the snapshot summary, the link, and one row per target.
 *
 * States: idle, preparing, copied (confirmation that names the clipboard), shared, and
 * failed with the reason. Copy is the fallback path, so it must work with no share
 * target installed at all.
 */
@Composable
fun ShareSheetContent(
    status: ShareSheetStatus,
    url: String,
    summary: List<Pair<String, String>>,
    targets: List<Pair<String, Boolean>>,
    modifier: Modifier = Modifier,
    failureReason: String? = null,
    onCopy: () -> Unit = {},
    onShare: (String) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(HeliosSpacing.space5),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space4)
    ) {
        SectionHeader(eyebrow = "Snapshot", title = "Share today's reading")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(HeliosShape.md)
                .background(colors.backgroundSecondary)
                .padding(horizontal = HeliosSpacing.cardPadding)
        ) {
            summary.forEach { (label, value) ->
                SnapshotSummaryRow(label = label, value = value)
            }
        }

        Text(
            text = url,
            style = HeliosTypography.caption,
            color = colors.textTertiary,
            maxLines = 2
        )

        when (status) {
            ShareSheetStatus.COPIED -> Text(
                text = "Link copied. The snapshot stays on this device.",
                style = HeliosTypography.callout,
                color = colors.flowStrong
            )
            ShareSheetStatus.SHARED -> Text(
                text = "Shared.",
                style = HeliosTypography.callout,
                color = colors.flowStrong
            )
            ShareSheetStatus.FAILED -> HeliosErrorState(
                title = "Could not build the snapshot",
                message = "The reading could not be encoded, so nothing was shared.",
                detail = failureReason,
                actionLabel = "Try again",
                onAction = onCopy
            )
            else -> Unit
        }

        Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            HeliosPrimaryButton(
                text = "Copy link",
                onClick = onCopy,
                loading = status == ShareSheetStatus.PREPARING
            )
            HeliosGhostButton(text = "Close", onClick = onDismiss)
        }

        targets.forEach { (label, available) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HeliosSpacing.minTouchTarget)
                    .clickable(enabled = available) { onShare(label) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = HeliosTypography.body,
                    color = if (available) colors.textPrimary else colors.textQuaternary
                )
                if (!available) {
                    Text(
                        text = "Not installed",
                        style = HeliosTypography.caption,
                        color = colors.textQuaternary
                    )
                }
            }
        }
    }
}
