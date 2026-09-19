package com.helios.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.MetricSparkline
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography

/**
 * How a tile is drawn.
 *
 *  DEFAULT  a measured value with an optional delta and sparkline
 *  SUBTLE   the same tile on the recessed surface, for a secondary row
 *  DISABLED a value that cannot be shown yet, dimmed and non-interactive with the reason
 *  STALE    a last-known value at 60 percent, which always travels with the banner
 */
enum class MetricTileState { DEFAULT, SUBTLE, DISABLED, STALE }

/**
 * One tile's content, as data, so a screen can build its grid from a list rather than from
 * five variations of the same composable call.
 */
data class MetricTileSpec(
    val label: String,
    val value: String,
    val unit: String = "",
    val delta: String? = null,
    val deltaIsGood: Boolean? = null,
    val sparkline: List<Double> = emptyList(),
    val tint: Color? = null,
    val state: MetricTileState = MetricTileState.DEFAULT,
    val onClick: (() -> Unit)? = null
)

/**
 * A labelled metric: label, value, unit, optional delta and optional sparkline.
 *
 * Variants and states: default, subtle, disabled, stale, with a positive or a negative
 * delta, with and without a sparkline, and a value that did not report ("No data" rather
 * than 0). The delta carries a sign in words, so a direction never depends on colour.
 *
 * Accessibility: label, value, unit, delta and state read as one node. A tile is only
 * clickable when the caller has somewhere to send the tap.
 */
@Composable
fun MetricTile(
    spec: MetricTileSpec,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    val disabled = spec.state == MetricTileState.DISABLED
    val alpha = when (spec.state) {
        MetricTileState.DISABLED -> 0.45f
        MetricTileState.STALE -> 0.6f
        else -> 1f
    }
    val valueColor = spec.tint ?: colors.textPrimary
    val surface = when (spec.state) {
        MetricTileState.SUBTLE -> colors.backgroundSecondary
        else -> colors.backgroundTertiary
    }
    val interactive = if (spec.onClick != null && !disabled) {
        modifier
            .defaultMinSize(minHeight = HeliosSpacing.minTouchTarget)
            .clip(HeliosShape.md)
            .clickable(role = Role.Button, onClick = spec.onClick)
    } else {
        modifier
    }
    Column(
        modifier = interactive
            .clip(HeliosShape.md)
            .background(surface)
            .border(
                HeliosElevation.MaterialTokens.innerStrokeWidth,
                colors.separatorHairline,
                HeliosShape.md
            )
            .padding(HeliosSpacing.cardPadding)
            .semantics(mergeDescendants = true) {
                contentDescription = buildString {
                    append("${spec.label} ${spec.value}")
                    if (spec.unit.isNotEmpty()) append(" ${spec.unit}")
                    if (spec.delta != null) append(", ${spec.delta}")
                    if (spec.state == MetricTileState.STALE) append(", last known")
                    if (spec.state == MetricTileState.DISABLED) append(", not available yet")
                }
                if (disabled) disabled()
            },
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space1)
    ) {
        Text(
            text = spec.label.uppercase(),
            style = HeliosTypography.caption2,
            color = colors.textTertiary.copy(alpha = alpha)
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = spec.value,
                style = HeliosTypography.title3,
                color = valueColor.copy(alpha = alpha),
                fontWeight = FontWeight.Normal
            )
            if (spec.unit.isNotEmpty()) {
                Spacer(Modifier.width(HeliosSpacing.space1))
                Text(
                    text = spec.unit,
                    style = HeliosTypography.caption,
                    color = colors.textTertiary.copy(alpha = alpha)
                )
            }
        }
        if (spec.delta != null) {
            Text(
                text = spec.delta,
                style = HeliosTypography.caption2,
                color = when (spec.deltaIsGood) {
                    true -> colors.flowStrong.copy(alpha = alpha)
                    false -> colors.textTertiary.copy(alpha = alpha)
                    null -> colors.textTertiary.copy(alpha = alpha)
                }
            )
        }
        if (spec.sparkline.size > 1) {
            Spacer(Modifier.height(HeliosSpacing.space1))
            MetricSparkline(
                values = spec.sparkline,
                color = spec.tint ?: colors.chart.production,
                height = 20.dp
            )
        }
    }
}

/**
 * The metric grid.
 *
 * Two columns on the reference viewport, one column once the user scales text past 160
 * percent (`layout.metricGridCollapseFontScale`), because at that size a two-column tile
 * cannot hold a label and a value without clipping.
 */
@Composable
fun MetricGrid(
    tiles: List<MetricTileSpec>,
    modifier: Modifier = Modifier,
    columns: Int? = null
) {
    val resolvedColumns = columns ?: if (
        LocalDensity.current.fontScale >= HeliosSpacing.LayoutMetrics.metricGridCollapseFontScale
    ) 1 else 2
    val rows = tiles.chunked(resolvedColumns.coerceAtLeast(1))
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
            ) {
                row.forEach { tile ->
                    MetricTile(spec = tile, modifier = Modifier.weight(1f))
                }
                repeat(resolvedColumns - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
