package com.helios.feature.battery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.BatteryRing
import com.helios.core.designsystem.component.FreshnessStamp
import com.helios.core.designsystem.component.MetricSparkline
import com.helios.core.designsystem.haptics.HapticAction
import com.helios.core.designsystem.haptics.HeliosHaptics
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.format.HeliosFormat

/**
 * The hero: the state of charge as a physical state, not a number in a list.
 *
 * Variants and states: charging (ring glow, motion-language section 5), discharging (ring
 * opacity pulse), idle, low reserve at or below 20 percent, a pack that did not report,
 * and a reading that is stale, offline, partial or simulated. The ring animates with the
 * gentle spring and the token trim duration; under Reduce Motion it is the short fade the
 * component already implements.
 *
 * Threshold haptics fire on crossing only: 80 percent light, 50 percent medium, 20 percent
 * heavy. Opening the screen never buzzes, because the first reading only seeds the
 * previous value.
 */
@Composable
fun BatteryHeroCard(
    state: BatteryScreenState,
    modifier: Modifier = Modifier,
    onOpenConnection: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val reading = state.reading
    val socPct = state.socPct
    val powerW = reading?.batteryPowerW ?: Double.NaN

    ThresholdHaptics(socPct = socPct, enabled = !socPct.isNaN())

    BatteryCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BatteryRing(
                socPct = socPct,
                powerW = powerW,
                size = 216.dp,
                mode = state.ringMode,
                reducedMotion = state.reducedMotion,
                thresholdLabel = if (state.lowReserve) "Low reserve" else null
            )

            Spacer(Modifier.height(HeliosSpacing.space2))
            FreshnessStamp(
                text = state.freshnessText,
                kind = state.freshnessKind,
                onClick = onOpenConnection
            )

            Spacer(Modifier.height(HeliosSpacing.space3))
            EnergyPill(
                availableKwh = state.availableKwh,
                capacityKwh = reading?.batteryCapacityKwh ?: Double.NaN
            )

            if (state.lowReserve) {
                Spacer(Modifier.height(HeliosSpacing.space3))
                Text(
                    text = "Low reserve \u00B7 charge before the next outage window",
                    style = HeliosTypography.caption,
                    color = colors.alertStrong
                )
            }
        }

        BatteryHairline()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroReading(
                label = "Power",
                value = state.powerLabel,
                modifier = Modifier.weight(1f),
                sparkline = state.trailValues
            )
            VerticalHairline()
            HeroReading(
                label = "Temp",
                value = state.temperatureLabel,
                modifier = Modifier.weight(1f)
            )
            VerticalHairline()
            HeroReading(
                label = "Cycles",
                value = reading?.let { it.batteryCycles.toString() } ?: HeliosFormat.NO_DATA,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** SoC as energy in the pack, because "62 percent" and "8.4 kWh" answer different questions. */
@Composable
private fun EnergyPill(availableKwh: Double, capacityKwh: Double) {
    val colors = LocalHeliosSemanticColors.current
    val text = if (availableKwh.isNaN() || capacityKwh.isNaN()) {
        "${HeliosFormat.NO_DATA} \u00B7 pack energy"
    } else {
        "${HeliosFormat.kwh(availableKwh, decimals = 1)} of ${HeliosFormat.kwh(capacityKwh, decimals = 1)}"
    }
    Text(
        text = text,
        style = HeliosTypography.caption,
        color = colors.textSecondary,
        modifier = Modifier
            .clip(HeliosShape.full)
            .background(colors.backgroundSecondary)
            .padding(horizontal = HeliosSpacing.space4, vertical = HeliosSpacing.space2)
    )
}

/** One column of the strip under the ring: uppercase label, value, optional sparkline. */
@Composable
private fun HeroReading(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    sparkline: List<Double> = emptyList()
) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "$label, $value"
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            style = HeliosTypography.caption2,
            color = colors.textTertiary
        )
        Spacer(Modifier.height(HeliosSpacing.space1))
        Text(
            text = value,
            style = HeliosTypography.headline,
            fontWeight = FontWeight.Normal,
            color = colors.textPrimary,
            maxLines = 1
        )
        if (sparkline.size > 1) {
            Spacer(Modifier.height(HeliosSpacing.space1))
            MetricSparkline(values = sparkline, color = colors.chart.battery, height = 18.dp)
        }
    }
}

/** The divider between the columns of the strip. */
@Composable
private fun VerticalHairline() {
    val colors = LocalHeliosSemanticColors.current
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(HeliosSpacing.space6)
            .background(colors.separatorHairline)
    )
}

/**
 * The threshold haptics of motion-language section 5, on crossing only.
 *
 * The previous reading is remembered across polls. A missing reading (NaN) neither fires
 * nor resets the memory, so a dropout cannot make the ring buzz twice on recovery.
 */
@Composable
private fun ThresholdHaptics(socPct: Double, enabled: Boolean) {
    val view = LocalView.current
    var previous by remember { mutableStateOf<Double?>(null) }
    LaunchedEffect(socPct) {
        val last = previous
        if (last != null && enabled) {
            crossedThresholds(last, socPct).forEach { threshold ->
                when (threshold) {
                    BatteryThreshold.EIGHTY -> HeliosHaptics.perform(view, HapticAction.BATTERY_80)
                    BatteryThreshold.FIFTY -> HeliosHaptics.perform(view, HapticAction.BATTERY_50)
                    BatteryThreshold.TWENTY -> HeliosHaptics.perform(view, HapticAction.BATTERY_20)
                }
            }
        }
        if (!socPct.isNaN()) previous = socPct
    }
}
