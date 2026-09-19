package com.helios.feature.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helios.core.data.repository.ForecastRepository
import com.helios.core.data.repository.InsightsRepository
import com.helios.core.data.repository.TelemetryRepository
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.designsystem.component.SectionHeader
import com.helios.core.designsystem.component.toSeverityKind
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.InverterStatus

/**
 * Dashboard, in the order the design fixes: identity and state, the live number, the four
 * glance metrics, the energy model, today's curve, the forecast, then advisories.
 *
 * The screen still reads the repositories directly. Wiring it to the service contracts
 * (`core/data/service`) and to the theme repository belongs to the next step, which owns
 * navigation and the application shell.
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
    val batterySoc = "%.0f".format(telemetry.batterySoc)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopBar(
            subTitle = "Dashboard",
            statusKind = if (telemetry.status == InverterStatus.FAULT) {
                HeliosStatusKind.FAULT
            } else {
                HeliosStatusKind.DEMO
            },
            statusLabel = if (telemetry.status == InverterStatus.FAULT) "Fault" else "Demo system",
            freshnessText = "Demo data",
            freshnessKind = HeliosStatusKind.DEMO,
            onShare = {}
        )

        Spacer(Modifier.height(HeliosSpacing.space2))

        LiveNumber(
            label = "Live output",
            value = liveKw,
            unit = "kW",
            isLive = true,
            state = LiveNumberState.DEMO,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HeliosSpacing.gutter)
        )

        Spacer(Modifier.height(HeliosSpacing.space3))

        MetricsGrid(
            liveKw = liveKw,
            irradiance = irradiance,
            gridFlow = gridFlow,
            batterySoc = batterySoc,
            irradianceValue = telemetry.irradianceWm2,
            gridValue = telemetry.gridExportW - telemetry.gridImportW,
            modifier = Modifier.padding(horizontal = HeliosSpacing.space3)
        )

        Spacer(Modifier.height(HeliosSpacing.space5))

        SectionHeader(
            title = "Energy flow",
            modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
        )
        EnergyFlow(
            solarW = telemetry.acPowerW,
            batteryW = telemetry.batteryPowerW,
            gridW = if (telemetry.gridExportW > 0) telemetry.gridExportW else -telemetry.gridImportW,
            homeW = telemetry.homeLoadW,
            modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
        )

        Spacer(Modifier.height(HeliosSpacing.space5))

        SectionHeader(
            title = "Today's production",
            modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
        )
        ProductionChart(
            series = series,
            modifier = Modifier.padding(horizontal = HeliosSpacing.space3)
        )

        Spacer(Modifier.height(HeliosSpacing.space5))

        SectionHeader(
            title = "Next five days",
            eyebrow = "Forecast",
            modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
        )
        Spacer(Modifier.height(HeliosSpacing.space3))
        ForecastStrip(
            days = ForecastRepository.currentForecast?.days ?: emptyList(),
            state = com.helios.core.designsystem.component.SurfaceState.EMPTY
        )

        Spacer(Modifier.height(HeliosSpacing.space5))

        val insights = remember(telemetry) { InsightsRepository.generateInsights(telemetry) }
        if (insights.isEmpty()) {
            Text(
                text = "No advisories for the current reading.",
                style = HeliosTypography.callout,
                modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
            )
        } else {
            SectionHeader(
                title = "Advisories",
                modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
            )
            Spacer(Modifier.height(HeliosSpacing.space3))
            insights.take(2).forEach { insight ->
                InsightHighlight(
                    insight = insight,
                    severity = insight.severity.toSeverityKind(),
                    demoQualifier = true,
                    modifier = Modifier.padding(
                        horizontal = HeliosSpacing.gutter,
                        vertical = HeliosSpacing.space1
                    )
                )
            }
        }

        Spacer(Modifier.height(HeliosSpacing.LayoutMetrics.bottomNavHeight))
    }
}
