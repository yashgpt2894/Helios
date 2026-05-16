package com.helios.feature.dashboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.HeliosColor
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.domain.model.ForecastDay

/**
 * ForecastStrip: horizontally scrollable 7-day forecast row.
 */
@Composable
fun ForecastStrip(
    days: List<ForecastDay>,
    modifier: Modifier = Modifier
) {
    val isDark = true
    val solarColor = HeliosColor.Solar.ramp(500, isDark)
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            days.forEach { day ->
                ForecastStripItem(
                    label = day.date.takeLast(2),
                    condition = day.conditionLabel,
                    kwh = day.expectedKwh,
                    accent = solarColor
                )
            }
        }
    }
}

@Composable
fun ForecastStripItem(
    label: String,
    condition: String,
    kwh: Double,
    accent: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = HeliosShape.sm,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.width(72.dp).padding(8.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = accent
            )
            Text(
                text = condition.take(4),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "%.1f".format(kwh),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "kWh",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
