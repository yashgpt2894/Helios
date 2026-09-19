package com.helios.feature.production

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.SnapshotSummaryRow
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.SolarTelemetry
import com.helios.core.format.HeliosFormat

/** One line of the inverter telemetry grid. */
data class TelemetryRow(
    val label: String,
    val value: String,
    val unit: String = "",
    val missing: Boolean = false
)

/**
 * The inverter's own registers, as a grid of labelled values.
 *
 * State: every value is either the reading or [HeliosFormat.NO_DATA]; a register that did not
 * answer is never rendered as 0, because 0 V and "the read failed" send a fault diagnosis in
 * opposite directions (FLW-03 hard rules). Rows collapse to one column at the same text scale
 * at which the metric grid does, for the same reason.
 */
@Composable
fun TelemetryGrid(
    rows: List<TelemetryRow>,
    modifier: Modifier = Modifier,
    columns: Int? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val resolvedColumns = columns ?: if (
        LocalDensity.current.fontScale >= HeliosSpacing.LayoutMetrics.metricGridCollapseFontScale
    ) 1 else 2
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(colors.backgroundTertiary)
            .border(
                HeliosElevation.MaterialTokens.innerStrokeWidth,
                colors.separatorHairline,
                HeliosShape.md
            )
            .padding(HeliosSpacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
    ) {
        rows.chunked(resolvedColumns.coerceAtLeast(1)).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space4)
            ) {
                row.forEach { entry ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .semantics(mergeDescendants = true) {
                                contentDescription = if (entry.missing) {
                                    "${entry.label} not reported"
                                } else {
                                    "${entry.label} ${entry.value} ${entry.unit}".trim()
                                }
                            }
                    ) {
                        Text(
                            text = entry.label.uppercase(),
                            style = HeliosTypography.caption2,
                            color = colors.textQuaternary
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = entry.value,
                                style = HeliosTypography.callout,
                                color = if (entry.missing) colors.textQuaternary else colors.textPrimary,
                                fontWeight = if (entry.missing) FontWeight.Normal else FontWeight.Medium
                            )
                            if (entry.unit.isNotEmpty() && !entry.missing) {
                                Spacer(Modifier.width(HeliosSpacing.space1))
                                Text(
                                    text = entry.unit,
                                    style = HeliosTypography.caption2,
                                    color = colors.textTertiary
                                )
                            }
                        }
                    }
                }
                repeat(resolvedColumns - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Who the inverter says it is: manufacturer, model, firmware and serial.
 *
 * Long values wrap rather than clip, which is the content extreme the long-content fixture
 * exists for (a 47-character model name and a firmware string with a build hash).
 */
@Composable
fun InverterIdentity(
    telemetry: SolarTelemetry?,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(colors.backgroundSecondary)
            .padding(horizontal = HeliosSpacing.cardPadding)
    ) {
        SnapshotSummaryRow(
            label = "Manufacturer",
            value = telemetry?.manufacturer ?: HeliosFormat.NO_DATA,
            missing = telemetry == null
        )
        SnapshotSummaryRow(
            label = "Model",
            value = telemetry?.model ?: HeliosFormat.NO_DATA,
            missing = telemetry == null
        )
        SnapshotSummaryRow(
            label = "Firmware",
            value = telemetry?.firmware ?: HeliosFormat.NO_DATA,
            missing = telemetry == null
        )
        SnapshotSummaryRow(
            label = "Serial",
            value = telemetry?.serialNumber ?: HeliosFormat.NO_DATA,
            missing = telemetry == null
        )
    }
}

/** The telemetry rows for one reading. Pure, so a test can assert the missing-value rule. */
object ProductionTelemetry {

    fun rows(telemetry: SolarTelemetry?): List<TelemetryRow> = listOf(
        value("AC power", telemetry?.acPowerW, "kW", decimals = 2, scale = 0.001),
        value("AC voltage", telemetry?.acVoltageV, "V", decimals = 1),
        value("AC current", telemetry?.acCurrentA, "A", decimals = 1),
        value("Frequency", telemetry?.acFrequencyHz, "Hz", decimals = 2),
        value("DC power", telemetry?.dcPowerW, "kW", decimals = 2, scale = 0.001),
        value("DC voltage", telemetry?.dcVoltageV, "V", decimals = 0),
        value("DC current", telemetry?.dcCurrentA, "A", decimals = 1),
        value("Irradiance", telemetry?.irradianceWm2, "W/m\u00B2", decimals = 0),
        value("Heatsink", telemetry?.heatsinkTempC, "\u00B0C", decimals = 1),
        value("Cabinet", telemetry?.cabinetTempC, "\u00B0C", decimals = 1),
        value("Battery temp", telemetry?.batteryTempC, "\u00B0C", decimals = 1),
        value("Battery health", telemetry?.batteryHealthPct, "%", decimals = 1),
        TelemetryRow(
            label = "Battery cycles",
            value = telemetry?.batteryCycles?.toString() ?: HeliosFormat.NO_DATA,
            unit = "cycles",
            missing = telemetry == null
        ),
        value("Ambient", telemetry?.ambientTempC, "\u00B0C", decimals = 1)
    )

    private fun value(
        label: String,
        raw: Double?,
        unit: String,
        decimals: Int,
        scale: Double = 1.0
    ): TelemetryRow {
        val missing = raw == null || raw.isNaN()
        return TelemetryRow(
            label = label,
            value = if (missing) HeliosFormat.NO_DATA else HeliosFormat.fixed(raw * scale, decimals),
            unit = unit,
            missing = missing
        )
    }
}
