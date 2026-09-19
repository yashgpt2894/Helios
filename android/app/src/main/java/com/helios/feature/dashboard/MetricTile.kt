package com.helios.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.MetricSparkline
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.format.HeliosFormat

/**
 * Labelled metric.
 *
 * Variants: default, with a positive delta, with a negative delta, subtle (no container),
 * disabled. States: value present, value missing (a failed register read, shown as
 * "No data" rather than zero), and a long label or unit, which must wrap instead of
 * clipping. Label, value and unit are one accessibility node.
 */
enum class MetricVariant { DEFAULT, SUBTLE, DISABLED }

@Composable
fun MetricTile(
    label: String,
    value: String,
    sub: String = "",
    modifier: Modifier = Modifier,
    unit: String? = null,
    delta: String? = null,
    deltaPositive: Boolean = true,
    variant: MetricVariant = MetricVariant.DEFAULT,
    sparkline: List<Double> = emptyList(),
    maxLines: Int = 1
) {
    val colors = LocalHeliosSemanticColors.current
    val container = when (variant) {
        MetricVariant.DEFAULT -> Modifier
            .clip(HeliosShape.md)
            .background(colors.backgroundTertiary)
        MetricVariant.SUBTLE -> Modifier
        MetricVariant.DISABLED -> Modifier
            .clip(HeliosShape.md)
            .background(colors.backgroundSecondary)
    }
    val valueColor = when (variant) {
        MetricVariant.DISABLED -> colors.textQuaternary
        else -> colors.textPrimary
    }
    val readable = buildString {
        append(label)
        append(", ")
        append(value)
        if (unit != null) append(" $unit")
        if (delta != null) append(", $delta")
    }
    Row(
        modifier = modifier
            .then(container)
            .padding(HeliosSpacing.space4)
            .semantics(mergeDescendants = true) { contentDescription = readable },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = HeliosTypography.caption,
                color = if (variant == MetricVariant.DISABLED) colors.textQuaternary else colors.textTertiary,
                maxLines = maxLines,
                softWrap = true
            )
            Spacer(Modifier.height(HeliosSpacing.space1))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = HeliosTypography.title3,
                    color = valueColor,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = maxLines
                )
                if (unit != null) {
                    Spacer(Modifier.width(HeliosSpacing.space1))
                    Text(
                        text = unit,
                        style = HeliosTypography.caption,
                        color = colors.textTertiary
                    )
                }
            }
            if (sub.isNotEmpty()) {
                Text(
                    text = sub,
                    style = HeliosTypography.caption2,
                    color = colors.textTertiary,
                    maxLines = maxLines
                )
            }
            if (delta != null) {
                Spacer(Modifier.height(HeliosSpacing.space1))
                Text(
                    text = delta,
                    style = HeliosTypography.caption2,
                    color = if (deltaPositive) colors.flowStrong else colors.alertStrong
                )
            }
        }
        if (sparkline.size > 1 && variant != MetricVariant.DISABLED) {
            MetricSparkline(
                values = sparkline,
                color = if (deltaPositive) colors.flowPrimary else colors.alertPrimary
            )
        }
    }
}

/** The four-glance grid. One column above the 1.6 text scale (DESIGN.md 9). */
@Composable
fun MetricsGrid(
    liveKw: String,
    irradiance: String,
    gridFlow: String,
    batterySoc: String,
    modifier: Modifier = Modifier,
    liveDelta: String? = null,
    batteryDelta: String? = null,
    irradianceValue: Double = Double.NaN,
    gridValue: Double = Double.NaN
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)) {
            MetricTile(
                label = "Live output",
                value = liveKw,
                unit = "kW",
                sub = "AC",
                delta = liveDelta,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Irradiance",
                value = if (irradianceValue.isNaN()) HeliosFormat.NO_DATA else irradiance,
                unit = "W/m\u00B2",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(HeliosSpacing.space2))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)) {
            MetricTile(
                label = "Grid flow",
                value = if (gridValue.isNaN()) HeliosFormat.NO_DATA else gridFlow,
                unit = "kW",
                sub = "Net",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Battery",
                value = batterySoc,
                unit = "%",
                sub = "SoC",
                delta = batteryDelta,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
