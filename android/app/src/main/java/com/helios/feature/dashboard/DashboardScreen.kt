package com.helios.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helios.core.data.repository.TelemetryRepository
import com.helios.core.domain.model.HistoryPoint
import com.helios.core.domain.model.SolarTelemetry

/**
 * DashboardScreen — the main dashboard composable.
 * Sections:
 *   1. TopBar (brand + status)
 *   2. LiveNumber (real-time kW)
 *   3. MetricsGrid (2×2 tile grid)
 *   4. EnergyFlow (animated Canvas)
 *   5. ProductionChart (Canvas line/area chart)
 *   6. ForecastStrip (horizontal scroll 7-day)
 *   7. InsightHighlight (severity card)
 */
@Composable
fun DashboardScreen() {
    val telemetry by remember { mutableStateOf(TelemetryRepository.readTelemetry()) }
    val series by remember { mutableStateOf(TelemetryRepository.buildTodaySeries()) }

    val liveKw = "%.2f".format(telemetry.acPowerW / 1000)
    val irradiance = "%.0f".format(telemetry.irradianceWm2)
    val gridFlow = if (telemetry.gridExportW > 0) {
        "+%.2f".format(telemetry.gridExportW / 1000)
    } else {
        "%.2f".format(telemetry.gridImportW / 1000)
    }
    val batterySoc = "%.0f%%".format(telemetry.batterySoc)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. TopBar
        TopBar(
            subTitle = "Dashboard",
            status = telemetry.status.name
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. LiveNumber
        LiveNumber(
            label = "Live Output",
            value = liveKw,
            unit = "kW",
            isLive = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. MetricsGrid
        MetricsGrid(
            liveKw = liveKw,
            irradiance = irradiance,
            gridFlow = gridFlow,
            batterySoc = batterySoc,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4. EnergyFlow
        SectionHeader("Energy Flow")
        EnergyFlow(
            solarW = telemetry.acPowerW,
            batteryW = telemetry.batteryPowerW,
            gridW = if (telemetry.gridExportW > 0) telemetry.gridExportW else -telemetry.gridImportW,
            homeW = telemetry.homeLoadW,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 5. ProductionChart
        SectionHeader("Today's Production")
        ProductionChart(
            series = series,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 6. ForecastStrip
        SectionHeader("7-Day Forecast")
        ForecastStrip(
            days = com.helios.core.data.repository.ForecastRepository.currentForecast?.days ?: emptyList(),
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 7. InsightHighlight
        val insights = com.helios.core.data.repository.InsightsRepository.generateInsights(telemetry)
        insights.take(2).forEach { insight ->
            InsightHighlight(
                insight = insight,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(80.dp)) // bottom nav space
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}
