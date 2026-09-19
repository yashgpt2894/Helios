package com.helios.feature.battery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.helios.core.designsystem.component.SectionHeader
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.format.HeliosFormat
import com.helios.feature.dashboard.MetricTile

/** Nameplate cycle life of the reference pack. A rating, not a measurement. */
const val RATED_CYCLES = 6_000

/**
 * The condition grid: health, cell temperature, cycles against their rating, and pack
 * capacity.
 *
 * Every tile shows a reading the inverter reports, or "No data" for a register that did
 * not answer (F7). Round-trip efficiency is deliberately absent: the PWA prints a
 * hard-coded 94.2 percent for it and `SolarTelemetry` has no such field, so this screen
 * shows capacity instead of a number the app cannot read.
 *
 * Tiles reuse `MetricTile` from the dashboard package (the design system has no metric
 * tile yet and this feature must not edit it); a later step should hoist that composable
 * into `core/designsystem/component`.
 */
@Composable
fun BatteryHealthGrid(
    state: BatteryScreenState,
    modifier: Modifier = Modifier
) {
    val reading = state.reading
    val health = reading?.batteryHealthPct ?: Double.NaN
    val tempC = reading?.batteryTempC ?: Double.NaN
    val capacityKwh = reading?.batteryCapacityKwh ?: Double.NaN

    Column(modifier = modifier) {
        SectionHeader(title = "Pack condition", eyebrow = "Health")
        Spacer(Modifier.height(HeliosSpacing.space3))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
        ) {
            BatteryMetricTile(
                label = "Health",
                value = if (health.isNaN()) HeliosFormat.NO_DATA else HeliosFormat.fixed(health, 1),
                unit = if (health.isNaN()) null else "%",
                sub = state.healthWord,
                modifier = Modifier.weight(1f)
            )
            BatteryMetricTile(
                label = "Cell temp",
                value = if (tempC.isNaN()) HeliosFormat.NO_DATA else HeliosFormat.fixed(tempC, 1),
                unit = if (tempC.isNaN()) null else "\u00B0C",
                sub = "Battery cabinet",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(HeliosSpacing.space2))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
        ) {
            BatteryMetricTile(
                label = "Cycles",
                value = reading?.let { it.batteryCycles.toString() } ?: HeliosFormat.NO_DATA,
                sub = "of ${HeliosFormat.fixed(RATED_CYCLES.toDouble(), 0)} rated",
                modifier = Modifier.weight(1f)
            )
            BatteryMetricTile(
                label = "Capacity",
                value = if (capacityKwh.isNaN()) HeliosFormat.NO_DATA else HeliosFormat.fixed(capacityKwh, 1),
                unit = if (capacityKwh.isNaN()) null else "kWh",
                sub = "Installed pack",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * One tile of the grid. It is a thin, named wrapper over the shared [MetricTile] so the
 * two columns of the grid cannot drift apart in padding or type.
 */
@Composable
private fun BatteryMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    sub: String = ""
) {
    MetricTile(
        label = label,
        value = value,
        unit = unit,
        sub = sub,
        maxLines = 2,
        modifier = modifier
    )
}
