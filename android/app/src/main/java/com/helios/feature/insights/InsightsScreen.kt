package com.helios.feature.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.data.repository.InsightsRepository
import com.helios.core.data.repository.TelemetryRepository
import com.helios.core.domain.model.Insight
import com.helios.feature.dashboard.InsightHighlight

/**
 * InsightsScreen: full insights feed (M4 stub).
 */
@Composable
fun InsightsScreen() {
    val telemetry = remember { TelemetryRepository.readTelemetry() }
    val insights = remember { InsightsRepository.generateInsights(telemetry) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Insights",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(insights) { insight ->
                InsightHighlight(insight = insight)
            }
        }
    }
}
