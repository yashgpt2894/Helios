package com.helios.feature.production

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.data.repository.TelemetryRepository
import com.helios.core.domain.model.HistoryPoint
import com.helios.feature.dashboard.ProductionChart

/**
 * ProductionScreen: detailed production insights (M4 stub).
 * Shows daily chart + panel strings + inverter status.
 */
@Composable
fun ProductionScreen() {
    val telemetry = remember { TelemetryRepository.readTelemetry() }
    val series = remember { TelemetryRepository.buildTodaySeries() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Production",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Today: %.1f kWh".format(telemetry.energyTodayKwh),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Daily Profile", style = MaterialTheme.typography.titleSmall)
        ProductionChart(series = series, modifier = Modifier.padding(vertical = 8.dp))

        Spacer(modifier = Modifier.height(16.dp))
        Text("Panel Strings", style = MaterialTheme.typography.titleSmall)
        telemetry.panels.forEach { string ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(string.label, style = MaterialTheme.typography.bodyMedium)
                Text("%.0f W".format(string.powerW), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
