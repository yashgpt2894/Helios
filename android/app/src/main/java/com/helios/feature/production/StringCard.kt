package com.helios.feature.production

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.helios.core.designsystem.color.HeliosSeverityKind
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.PanelString
import com.helios.core.format.HeliosFormat
import com.helios.feature.dashboard.DashboardMetrics

/**
 * How a string is doing, as a word rather than a colour.
 *
 *  PRODUCING  at or above 60 percent of the string's rating
 *  SHADED     below 60 percent while the array is producing: the reason to open this screen
 *  IDLE       the inverter is not producing at all (night, standby, curtailed)
 *  NO_DATA    the string did not report (F7), which is never rendered as zero
 *  MISSING    the string is absent from the reading altogether
 */
enum class StringState(val word: String) {
    PRODUCING("Producing"),
    SHADED("Below the array"),
    IDLE("Idle"),
    NO_DATA("No data"),
    MISSING("Not reported")
}

/**
 * One string: name, output, how much of its rating it is using, and its own electrical
 * values.
 *
 * The bar is the only graphic, and it is never the only signal: the state word and the
 * percentage both carry the same information as the fill. The whole card is one
 * accessibility node whose range is the utilisation fraction, so TalkBack can read the bar
 * as a percentage rather than as an unlabelled rectangle.
 */
@Composable
fun PerStringCard(
    string: PanelString,
    arrayProducing: Boolean,
    modifier: Modifier = Modifier,
    state: StringState = stringState(string, arrayProducing),
    missingName: String? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val utilisation = if (string.ratedW > 0) (string.powerW / string.ratedW).coerceIn(0.0, 1.0) else Double.NaN
    val tint = when (state) {
        StringState.PRODUCING -> colors.solarPrimary
        StringState.SHADED -> colors.insight.foreground(HeliosSeverityKind.ATTENTION)
        StringState.IDLE -> colors.textTertiary
        StringState.NO_DATA, StringState.MISSING -> colors.textQuaternary
    }
    val label = missingName ?: string.label
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
            .padding(HeliosSpacing.cardPadding)
            .semantics(mergeDescendants = true) {
                contentDescription = buildString {
                    append("$label, ${state.word}")
                    if (string.powerW.isNaN()) {
                        append(", output not reported")
                    } else {
                        append(", ${HeliosFormat.wattsToKilowatts(string.powerW)}")
                        append(", ${HeliosFormat.percent(utilisation * 100.0, 0)} of rated")
                    }
                }
                if (!utilisation.isNaN()) {
                    progressBarRangeInfo = ProgressBarRangeInfo(utilisation.toFloat(), 0f..1f)
                }
            },
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = HeliosTypography.headline,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (string.powerW.isNaN()) HeliosFormat.NO_DATA else HeliosFormat.watts(string.powerW),
                style = HeliosTypography.callout,
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium
            )
        }

        UtilisationBar(fraction = if (utilisation.isNaN()) 0.0 else utilisation, tint = tint)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StringValue(
                label = "Voltage",
                value = DashboardMetrics.number(string.voltageV, 0),
                unit = "V",
                missing = string.voltageV.isNaN()
            )
            StringValue(
                label = "Current",
                value = DashboardMetrics.number(string.currentA, 1),
                unit = "A",
                missing = string.currentA.isNaN()
            )
            StringValue(
                label = "Rated",
                value = DashboardMetrics.number(string.ratedW / 1000.0, 1),
                unit = "kW \u00B7 ${string.panels} panels",
                missing = string.ratedW.isNaN()
            )
            Text(
                text = state.word,
                style = HeliosTypography.caption2,
                color = tint,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** A label, a value and a unit as one small column. A missing value drops the unit. */
@Composable
private fun StringValue(label: String, value: String, unit: String, missing: Boolean) {
    val colors = LocalHeliosSemanticColors.current
    Column {
        Text(
            text = label.uppercase(),
            style = HeliosTypography.caption2,
            color = colors.textQuaternary
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = if (missing) HeliosFormat.NO_DATA else value,
                style = HeliosTypography.callout,
                color = colors.textSecondary
            )
            if (!missing) {
                Spacer(Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = HeliosTypography.caption2,
                    color = colors.textQuaternary
                )
            }
        }
    }
}

/** The utilisation bar: a hairline track with a filled fraction. */
@Composable
private fun UtilisationBar(fraction: Double, tint: Color) {
    val colors = LocalHeliosSemanticColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(HeliosShape.xs)
            .background(colors.backgroundPrimary),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.toFloat().coerceIn(0f, 1f))
                .height(8.dp)
                .clip(HeliosShape.xs)
                .background(tint)
        )
    }
}

/**
 * The string's state from its own numbers. The rule is stated rather than inferred at the
 * call site, so the Dashboard and the Production screen cannot disagree about a shaded
 * string.
 */
fun stringState(string: PanelString, arrayProducing: Boolean): StringState = when {
    string.powerW.isNaN() || string.ratedW <= 0.0 -> StringState.NO_DATA
    !arrayProducing -> StringState.IDLE
    string.powerW / string.ratedW < 0.6 -> StringState.SHADED
    else -> StringState.PRODUCING
}

/**
 * The placeholder for a string that is absent from the reading: it keeps the row height and
 * names what is missing instead of silently dropping a string from the list.
 */
@Composable
fun MissingStringCard(name: String, modifier: Modifier = Modifier, reason: String? = null) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(colors.backgroundSecondary)
            .border(
                HeliosElevation.MaterialTokens.innerStrokeWidth,
                colors.separatorHairline,
                HeliosShape.md
            )
            .padding(HeliosSpacing.cardPadding)
            .semantics(mergeDescendants = true) {
                contentDescription = "String $name did not report"
            },
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space1)
    ) {
        Text(
            text = "String $name",
            style = HeliosTypography.headline,
            color = colors.textSecondary
        )
        Text(
            text = reason ?: "This string did not report in the last reading. " +
                "Nothing is shown for it, because 0 W would be read as a working string with no sun.",
            style = HeliosTypography.callout,
            color = colors.textTertiary
        )
    }
}
