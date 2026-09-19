package com.helios.feature.insights

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.SectionHeader
import com.helios.core.designsystem.component.SkeletonBlock
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.motion.HeliosMotion
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.format.HeliosFormat
import com.helios.feature.dashboard.MetricTile

/**
 * Savings, with the basis stated.
 *
 * The four figures are the app's own arithmetic over readings the inverter reports, and
 * the rates it uses are printed in the disclosure rather than implied (SCR-09 note). When
 * there is no reading, the tiles say "No data" instead of a zero amount (STS-043).
 *
 * Tiles reuse `MetricTile` from the dashboard package; the design system has no metric tile
 * yet and this feature must not edit it.
 */
@Composable
fun SavingsSection(
    state: InsightsScreenState,
    modifier: Modifier = Modifier,
    expanded: Boolean = state.savingsExpanded,
    onExpandedChange: (Boolean) -> Unit = {},
    reducedMotion: Boolean = state.reducedMotion
) {
    val colors = LocalHeliosSemanticColors.current
    val savings = state.savings
    val sizeSpec = if (reducedMotion) {
        tween(HeliosMotion.DurationMs.reduceMotionCrossfade)
    } else {
        InsightsGentleSize
    }

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = "What the system saved", eyebrow = "Savings")
        Spacer(Modifier.height(HeliosSpacing.space3))

        if (state.savingsLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
            ) {
                SkeletonBlock(height = 96.dp, shape = HeliosShape.md, modifier = Modifier.weight(1f))
                SkeletonBlock(height = 96.dp, shape = HeliosShape.md, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(HeliosSpacing.space2))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
            ) {
                SkeletonBlock(height = 96.dp, shape = HeliosShape.md, modifier = Modifier.weight(1f))
                SkeletonBlock(height = 96.dp, shape = HeliosShape.md, modifier = Modifier.weight(1f))
            }
            return@Column
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
        ) {
            MetricTile(
                label = "Savings \u00B7 today",
                value = HeliosFormat.currency(savings.today),
                sub = "vs grid baseline",
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Savings \u00B7 month",
                value = HeliosFormat.currency(savings.month, decimals = 0),
                sub = "Month to date",
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(HeliosSpacing.space2))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
        ) {
            MetricTile(
                label = "Lifetime",
                value = HeliosFormat.currency(savings.lifetime, decimals = 0),
                sub = "Since install",
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "CO\u2082 avoided",
                value = if (savings.co2AvoidedKg.isNaN()) {
                    HeliosFormat.NO_DATA
                } else {
                    HeliosFormat.fixed(savings.co2AvoidedKg, 0)
                },
                unit = if (savings.co2AvoidedKg.isNaN()) null else "kg",
                sub = "Lifetime",
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(HeliosSpacing.space2))
        HeliosGhostButton(
            text = if (expanded) "Hide the calculation" else "How is this calculated?",
            onClick = { onExpandedChange(!expanded) }
        )

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = sizeSpec) +
                fadeIn(animationSpec = tween(HeliosMotion.DurationMs.fast)),
            exit = shrinkVertically(animationSpec = sizeSpec) +
                fadeOut(animationSpec = tween(HeliosMotion.DurationMs.fast))
        ) {
            Column(modifier = Modifier.padding(top = HeliosSpacing.space2)) {
                SavingsRule(
                    label = "Import rate avoided",
                    value = "${HeliosFormat.currency(SavingsSummary.IMPORT_RATE)} per kWh"
                )
                SavingsRule(
                    label = "Export rate credited",
                    value = "${HeliosFormat.currency(SavingsSummary.EXPORT_RATE)} per kWh"
                )
                SavingsRule(
                    label = "Month saving factor",
                    value = "${HeliosFormat.percent(SavingsSummary.MONTH_SELF_CONSUMPTION_FACTOR * 100, 0)} of month production"
                )
                SavingsRule(
                    label = "Lifetime saving factor",
                    value = "${HeliosFormat.percent(SavingsSummary.LIFETIME_SELF_CONSUMPTION_FACTOR * 100, 0)} of lifetime production"
                )
                SavingsRule(
                    label = "Grid carbon intensity avoided",
                    value = "${HeliosFormat.fixed(SavingsSummary.CO2_KG_PER_KWH, 2)} kg per kWh"
                )
                Text(
                    text = "These are the app's own rates, not a tariff you were billed. " +
                        "The export rate credits energy sent to the grid in the shared snapshot; " +
                        "the tiles above price production that stayed on site.",
                    style = HeliosTypography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(top = HeliosSpacing.space2)
                )
            }
        }
    }
}

@Composable
private fun SavingsRule(label: String, value: String) {
    val colors = LocalHeliosSemanticColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = HeliosSpacing.space1),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = HeliosTypography.callout, color = colors.textSecondary)
        Text(text = value, style = HeliosTypography.callout, color = colors.textPrimary)
    }
}
