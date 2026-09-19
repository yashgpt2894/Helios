package com.helios.feature.insights

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helios.core.data.service.valueOrNull
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.ForecastCard
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.SectionHeader
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography

/**
 * The 7-day production outlook.
 *
 * States: ready (STS-039), loading with skeleton rows in the row shape (STS-040), failed
 * with an inline retry that leaves the advisories alone (STS-041, F8), empty when the
 * location has no forecast, and stale when the last fetch is old. The rows, the weather
 * words and the state treatment come from the design system's `ForecastCard`, so this
 * section only adds the header, the weekly total and where the forecast is for.
 */
@Composable
fun ForecastSection(
    state: InsightsScreenState,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onUseMyLocation: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = "7-day production forecast",
            eyebrow = "Weather \u00B7 AI forecast",
            trailing = state.forecastTotalLabel?.let { total ->
                {
                    Text(
                        text = total,
                        style = HeliosTypography.callout,
                        color = colors.textSecondary
                    )
                }
            }
        )
        Spacer(Modifier.height(HeliosSpacing.space3))
        ForecastCard(
            days = state.forecast.valueOrNull()?.days ?: emptyList(),
            state = state.forecastSurface,
            demoQualifier = state.showDemoQualifier,
            onRetry = onRetry
        )
        val comparand = state.forecastComparandLabel
        val place = state.forecastLocationLabel
        if (comparand != null || place != null || onUseMyLocation != null) {
            Spacer(Modifier.height(HeliosSpacing.space2))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HeliosSpacing.space1),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = listOfNotNull(place, comparand).joinToString(" \u00B7 "),
                    style = HeliosTypography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.weight(1f)
                )
                if (onUseMyLocation != null) {
                    HeliosGhostButton(text = "Use my location", onClick = onUseMyLocation)
                }
            }
        }
    }
}
