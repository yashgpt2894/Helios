package com.helios.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * MetricTile: 2×2 grid of key-value metrics (Live kW, irradiance, grid flow, etc.).
 */
@Composable
fun MetricTile(
    label: String,
    value: String,
    sub: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = 1
        )
        if (sub.isNotEmpty()) {
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                maxLines = 1
            )
        }
    }
}

@Composable
fun MetricsGrid(
    liveKw: String,
    irradiance: String,
    gridFlow: String,
    batterySoc: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricTile(label = "Live kW", value = liveKw, sub = "AC", modifier = Modifier.weight(1f))
            MetricTile(label = "Irradiance", value = irradiance, sub = "W/m²", modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricTile(label = "Grid Flow", value = gridFlow, sub = "Net", modifier = Modifier.weight(1f))
            MetricTile(label = "Battery", value = batterySoc, sub = "SoC", modifier = Modifier.weight(1f))
        }
    }
}
