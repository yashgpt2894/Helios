package com.helios.feature.dashboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.SkeletonBlock
import com.helios.core.designsystem.component.SurfaceState
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.ForecastDay
import com.helios.core.format.HeliosFormat

/**
 * Five-day glance strip.
 *
 * States: ready, loading (five skeleton cells in the cell's exact shape), empty (the
 * forecast has nothing to show), stale (dimmed with the age stated by the caller), error.
 * Five days is the legibility ceiling at caption-2 on a 412 dp screen (DESIGN.md 6).
 */
@Composable
fun ForecastStrip(
    days: List<ForecastDay>,
    modifier: Modifier = Modifier,
    state: SurfaceState = SurfaceState.READY
) {
    val colors = LocalHeliosSemanticColors.current
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = HeliosSpacing.gutter),
            horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
        ) {
            when (state) {
                SurfaceState.LOADING -> repeat(5) {
                    SkeletonBlock(
                        height = 96.dp,
                        shape = HeliosShape.sm,
                        modifier = Modifier.width(HeliosSpacing.LayoutMetrics.forecastStripItemWidth)
                    )
                }
                SurfaceState.EMPTY -> Text(
                    text = "No forecast for this location. Pick a place to get one.",
                    style = HeliosTypography.callout,
                    color = colors.textSecondary
                )
                SurfaceState.ERROR -> Text(
                    text = "Forecast unavailable. The reading above is unaffected.",
                    style = HeliosTypography.callout,
                    color = colors.alertStrong
                )
                else -> days.forEach { day ->
                    ForecastStripItem(
                        label = HeliosFormat.weekday(
                            java.time.LocalDate.parse(day.date)
                                .atStartOfDay(java.time.ZoneId.systemDefault())
                                .toInstant().toEpochMilli()
                        ),
                        condition = day.conditionLabel,
                        kwh = day.expectedKwh,
                        accent = if (state == SurfaceState.STALE) colors.textTertiary else colors.solarPrimary,
                        dimmed = state == SurfaceState.STALE
                    )
                }
            }
        }
    }
}

@Composable
fun ForecastStripItem(
    label: String,
    condition: String,
    kwh: Double,
    accent: Color,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false
) {
    val colors = LocalHeliosSemanticColors.current
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "$label, $condition, expected ${HeliosFormat.kwh(kwh)}"
        },
        shape = HeliosShape.sm,
        color = colors.backgroundTertiary
    ) {
        Column(
            modifier = Modifier
                .width(HeliosSpacing.LayoutMetrics.forecastStripItemWidth)
                .padding(HeliosSpacing.space3),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = HeliosTypography.caption2,
                fontWeight = FontWeight.Medium,
                color = accent
            )
            Text(
                text = condition,
                style = HeliosTypography.caption2,
                color = colors.textTertiary,
                maxLines = 1
            )
            Spacer(Modifier.height(HeliosSpacing.space1))
            Text(
                text = HeliosFormat.fixed(kwh, 1),
                style = HeliosTypography.headline,
                color = if (dimmed) colors.textPrimary.copy(alpha = 0.6f) else colors.textPrimary
            )
            Text(
                text = "kWh",
                style = HeliosTypography.caption2,
                color = colors.textTertiary
            )
        }
    }
}
