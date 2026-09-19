package com.helios.feature.battery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.SectionHeader
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.motion.HeliosMotion
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.format.HeliosFormat

/**
 * Backup readiness: how long the pack would carry the essentials if the grid were lost.
 *
 * States: ready with the hours, and unreadable because the home load did not report
 * (STS-053). The second one says "no data" and keeps the energy figure that did arrive,
 * rather than dividing by a made-up load. The bar animates on the token duration; the
 * disclosure (ACT-053) expands in place and names the formula and its inputs.
 */
@Composable
fun BackupReadinessCard(
    state: BatteryScreenState,
    modifier: Modifier = Modifier,
    expanded: Boolean = state.readinessExpanded,
    onExpandedChange: (Boolean) -> Unit = {},
    reducedMotion: Boolean = state.reducedMotion
) {
    val colors = LocalHeliosSemanticColors.current
    val hours = state.hoursOfEssentials
    val availableKwh = state.availableKwh
    val loadKw = state.reading?.homeLoadW?.let { if (it.isNaN()) Double.NaN else it / 1000.0 } ?: Double.NaN
    val fraction by animateFloatAsState(
        targetValue = state.readinessFraction,
        animationSpec = if (reducedMotion) {
            tween(HeliosMotion.DurationMs.fast)
        } else {
            tween(HeliosMotion.DurationMs.ringTrim, easing = HeliosMotion.Curves.standard)
        },
        label = "readinessBar"
    )

    val sizeSpec = if (reducedMotion) {
        tween(HeliosMotion.DurationMs.reduceMotionCrossfade)
    } else {
        HeliosGentleSize
    }
    val hoursText = if (hours.isNaN()) HeliosFormat.NO_DATA else HeliosFormat.fixed(hours, 0)
    val loadText = if (loadKw.isNaN()) HeliosFormat.NO_DATA else HeliosFormat.kilowatts(loadKw)
    val availableText = if (availableKwh.isNaN()) {
        "${HeliosFormat.NO_DATA} available"
    } else {
        "${HeliosFormat.kwh(availableKwh, decimals = 1)} available"
    }

    Column(modifier = modifier) {
        SectionHeader(title = "Backup readiness", eyebrow = "Reserve")
        Spacer(Modifier.height(HeliosSpacing.space3))
        BatteryCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription = if (hours.isNaN()) {
                            "Backup readiness, no data. $availableText, home load not reporting"
                        } else {
                            "Backup readiness, $hoursText hours of essentials. " +
                                "$availableText at $loadText current draw"
                        }
                    },
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = hoursText,
                        style = HeliosTypography.title1,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "HOURS OF ESSENTIALS",
                        style = HeliosTypography.caption2,
                        color = colors.textTertiary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = availableText,
                        style = HeliosTypography.caption,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "at $loadText current draw",
                        style = HeliosTypography.caption2,
                        color = colors.textTertiary
                    )
                }
            }

            Spacer(Modifier.height(HeliosSpacing.space3))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(HeliosShape.full)
                    .background(colors.backgroundSecondary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .clip(HeliosShape.full)
                        .background(colors.batteryPrimary)
                )
            }

            if (hours.isNaN()) {
                Spacer(Modifier.height(HeliosSpacing.space3))
                Text(
                    text = "The home load is not reporting, so hours cannot be estimated. " +
                        "Showing the energy that is in the pack instead.",
                    style = HeliosTypography.callout,
                    color = colors.textSecondary
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
                Column(modifier = Modifier.padding(top = HeliosSpacing.space3)) {
                    ReadinessRule(
                        label = "Usable energy",
                        value = if (availableKwh.isNaN()) {
                            HeliosFormat.NO_DATA
                        } else {
                            HeliosFormat.kwh(availableKwh, decimals = 1)
                        }
                    )
                    ReadinessRule(
                        label = "Home load right now",
                        value = loadText
                    )
                    ReadinessRule(
                        label = "Load floor",
                        value = "0.40 kW"
                    )
                    Text(
                        text = "Hours = usable energy divided by the home load, rounded down, " +
                            "with loads under 0.40 kW treated as 0.40 kW so a quiet house " +
                            "cannot invent a double-digit runtime. Nothing here is estimated " +
                            "from history.",
                        style = HeliosTypography.caption,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(top = HeliosSpacing.space2)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadinessRule(label: String, value: String) {
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
