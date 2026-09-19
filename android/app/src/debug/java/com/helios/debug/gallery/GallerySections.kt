package com.helios.debug.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.data.fixture.FixtureData
import com.helios.core.data.repository.SolarCurve
import com.helios.core.data.fixture.FixtureScenario
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.ServiceGraph
import com.helios.core.data.service.UiState
import com.helios.core.data.service.freshnessLabel
import com.helios.core.data.service.uiState
import com.helios.core.data.service.valueOrNull
import com.helios.core.designsystem.color.HeliosSeverityKind
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.BatteryRing
import com.helios.core.designsystem.component.BatteryRingMode
import com.helios.core.designsystem.component.BrandTextMark
import com.helios.core.designsystem.component.ConnectionBanner
import com.helios.core.designsystem.component.ConnectionBannerState
import com.helios.core.designsystem.component.DeniedPermissionState
import com.helios.core.designsystem.component.EmptyState
import com.helios.core.designsystem.component.ForecastCard
import com.helios.core.designsystem.component.FreshnessStamp
import com.helios.core.designsystem.component.HeliosChip
import com.helios.core.designsystem.component.HeliosDestructiveButton
import com.helios.core.designsystem.component.HeliosErrorState
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.HeliosMark
import com.helios.core.designsystem.component.HeliosPrimaryButton
import com.helios.core.designsystem.component.HeliosSecondaryButton
import com.helios.core.designsystem.component.InsightCard
import com.helios.core.designsystem.component.LiveTrail
import com.helios.core.designsystem.component.SectionHeader
import com.helios.core.designsystem.component.ShareSheetContent
import com.helios.core.designsystem.component.ShareSheetStatus
import com.helios.core.designsystem.component.SkeletonBlock
import com.helios.core.designsystem.component.SkeletonLines
import com.helios.core.designsystem.component.SnapshotSummaryRow
import com.helios.core.designsystem.component.StatusPill
import com.helios.core.designsystem.component.SurfaceState
import com.helios.core.designsystem.component.WeatherConditions
import com.helios.core.designsystem.component.WeatherIcon
import com.helios.core.designsystem.component.WeekBar
import com.helios.core.designsystem.component.WeekChart
import com.helios.core.designsystem.component.toSeverityKind
import com.helios.core.designsystem.component.toSurfaceState
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.WeatherCondition
import com.helios.core.format.HeliosFormat
import com.helios.feature.dashboard.BottomNav
import com.helios.feature.dashboard.EnergyFlow
import com.helios.feature.dashboard.ForecastStrip
import com.helios.feature.dashboard.HeliosDestinations
import com.helios.feature.dashboard.InsightHighlight
import com.helios.feature.dashboard.LiveNumber
import com.helios.feature.dashboard.LiveNumberState
import com.helios.feature.dashboard.HeliosMotionSettings
import com.helios.feature.dashboard.MetricGrid
import com.helios.feature.dashboard.MetricTileSpec
import com.helios.feature.dashboard.MetricTileState
import com.helios.feature.dashboard.ProductionChart
import com.helios.feature.dashboard.TopBar

/**
 * Every gallery section. Each one renders a component in every variant and state the
 * design lists, so a page of captures is a review rather than an example.
 */

@Composable
private fun GalleryLabel(text: String) {
    Text(
        text = text,
        style = HeliosTypography.caption2,
        color = LocalHeliosSemanticColors.current.textTertiary
    )
}

private fun weekBars(): List<WeekBar> =
    FixtureData.weekSeries().map { WeekBar(it.dayLabel, it.producedKwh, it.consumedKwh) }

// ------------------------------------------------------------------ foundations

@Composable
fun TokenStrip(context: GalleryContext) {
    val colors = LocalHeliosSemanticColors.current
    val roles = listOf(
        "background.primary" to colors.backgroundPrimary,
        "background.tertiary" to colors.backgroundTertiary,
        "text.primary" to colors.textPrimary,
        "text.secondary" to colors.textSecondary,
        "accent.primary" to colors.accentPrimary,
        "solar.primary" to colors.solarPrimary,
        "flow.primary" to colors.flowPrimary,
        "battery.primary" to colors.batteryPrimary,
        "grid.importPrimary" to colors.gridImportPrimary,
        "grid.exportPrimary" to colors.gridExportPrimary,
        "alert.primary" to colors.alertPrimary
    )
    Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)) {
        roles.forEach { (name, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 22.dp)
                        .clip(HeliosShape.xs)
                        .background(color)
                )
                Spacer(Modifier.width(HeliosSpacing.space3))
                Text(text = name, style = HeliosTypography.caption, color = colors.textSecondary)
            }
        }
        GalleryLabel("Semantic roles only. Hue ramps live in HeliosColor; this palette is design/tokens.json.")
    }
}

@Composable
fun LiveDashboardTop(context: GalleryContext) {
    val now = System.currentTimeMillis()
    val telemetry = FixtureData.telemetry(context.scenario, now)
    val state = telemetry.uiState()
    val reading = telemetry.valueOrNull()
    val statusKind = statusKindFor(state, context)
    Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
        TopBar(
            brandName = "helios\u00B0",
            subTitle = "Dashboard",
            statusKind = statusKind,
            freshnessText = freshnessTextFor(context, telemetry),
            freshnessKind = statusKind,
            onShare = {},
            onStatusClick = {},
            onFreshnessClick = {}
        )
        if (state == UiState.OFFLINE) {
            ConnectionBanner(
                state = ConnectionBannerState.OFFLINE,
                message = "Not on the same network as the inverter",
                reason = "Last good reading 1 min ago. Automatic retries stop after five attempts.",
                onRetry = {},
                onOpenConnection = {}
            )
        }
        if (state == UiState.STALE) {
            ConnectionBanner(
                state = ConnectionBannerState.RECONNECTING,
                message = "Reconnecting",
                reason = "The last reading is 42 s old, so the value below is labelled last known.",
                onRetry = {}
            )
        }
        if (state == UiState.LOADING) {
            SkeletonBlock(height = 56.dp, shape = HeliosShape.sm)
        }
        LiveNumber(
            label = "Live output",
            value = (reading?.acPowerW ?: Double.NaN) / 1000.0,
            unit = "kW",
            state = liveNumberStateFor(state, context)
        )
        MetricGrid(
            tiles = listOf(
                MetricTileSpec(
                    label = "Live output",
                    value = HeliosFormat.fixed((reading?.acPowerW ?: Double.NaN) / 1000.0, 2),
                    unit = "kW",
                    delta = "+0.4 kW vs 30 min ago",
                    deltaIsGood = true
                ),
                MetricTileSpec(
                    label = "Irradiance",
                    value = HeliosFormat.fixed(reading?.irradianceWm2 ?: Double.NaN, 0),
                    unit = "W/m\u00B2"
                ),
                MetricTileSpec(
                    label = "Grid",
                    value = HeliosFormat.fixed(
                        ((reading?.gridExportW ?: Double.NaN) - (reading?.gridImportW ?: Double.NaN)) / 1000.0,
                        2
                    ),
                    unit = "kW"
                ),
                MetricTileSpec(
                    label = "Battery",
                    value = HeliosFormat.fixed(reading?.batterySoc ?: Double.NaN, 0),
                    unit = "%",
                    delta = "-2% vs 30 min ago"
                )
            )
        )
        GalleryLabel("Scenario ${context.scenario.label} renders as ${state.name}")
    }
}

@Composable
fun LiveDashboardEnergy(context: GalleryContext) {
    val now = System.currentTimeMillis()
    val telemetry = FixtureData.telemetry(context.scenario, now)
    val state = telemetry.uiState()
    val reading = telemetry.valueOrNull()
    Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
        EnergyFlow(
            hubValue = (reading?.acPowerW ?: Double.NaN) / 1000.0,
            hubUnit = "kW",
            hubLabel = "Live output",
            solarW = reading?.acPowerW ?: Double.NaN,
            batteryW = reading?.batteryPowerW ?: Double.NaN,
            gridW = reading?.let { it.gridExportW - it.gridImportW } ?: Double.NaN,
            homeW = reading?.homeLoadW ?: Double.NaN,
            state = FixtureData.seriesLoadable(context.scenario, now).toSurfaceState(),
            liveState = liveNumberStateFor(state, context),
            flowHeight = 280.dp
        )
        ProductionChart(
            series = FixtureData.seriesLoadable(context.scenario, now),
            nowHour = SolarCurve.nowAsHourFloat(now)
        )
    }
}

@Composable
fun LiveDashboardTail(context: GalleryContext) {
    val now = System.currentTimeMillis()
    val forecast = FixtureData.forecast(context.scenario, now)
    val insights = FixtureData.insightsLoadable(context.scenario, now)
    Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
        ForecastStrip(
            days = (forecast.valueOrNull()?.days ?: emptyList()).take(5),
            state = forecast.toSurfaceState()
        )
        when (insights) {
            is Loadable.Ready -> InsightHighlight(
                insight = insights.value.first(),
                severity = insights.value.first().severity.toSeverityKind(),
                demoQualifier = context.demoMode
            )
            is Loadable.Loading -> SkeletonLines(lines = 2)
            is Loadable.Empty -> EmptyState(
                title = "Advisories paused",
                message = insights.message,
                actionLabel = insights.actionLabel,
                onAction = {}
            )
            is Loadable.Failed -> HeliosErrorState(
                title = "Advisories unavailable",
                message = insights.failure.message,
                detail = insights.failure.detail,
                onAction = {}
            )
        }
    }
}

private fun statusKindFor(state: UiState, context: GalleryContext): HeliosStatusKind = when (state) {
    UiState.OFFLINE -> HeliosStatusKind.OFFLINE
    UiState.ERROR, UiState.DENIED -> HeliosStatusKind.FAULT
    UiState.STALE -> HeliosStatusKind.CURTAILED
    else -> if (context.demoMode) HeliosStatusKind.DEMO else HeliosStatusKind.PRODUCING
}

private fun liveNumberStateFor(state: UiState, context: GalleryContext): LiveNumberState = when {
    state == UiState.LOADING -> LiveNumberState.STATIC
    state == UiState.STALE || state == UiState.OFFLINE -> LiveNumberState.STALE
    context.demoMode -> LiveNumberState.DEMO
    else -> LiveNumberState.UPDATING
}

private fun freshnessTextFor(context: GalleryContext, telemetry: Loadable<*>): String =
    when (context.scenario) {
        FixtureScenario.LOADING -> "Loading"
        FixtureScenario.STALE -> "Stale 42 s"
        FixtureScenario.OFFLINE -> "Offline \u00B7 last known"
        else -> if (context.demoMode) "Demo data" else telemetry.freshnessLabel()
    }

// ------------------------------------------------------------------ energy flow

@Composable
fun EnergyFlowLiveNight(context: GalleryContext) {
    val day = FixtureData.daytimeTelemetry(System.currentTimeMillis())
    GalleryLabel("Live: dots run at one per kilowatt, in the direction the energy goes")
    EnergyFlow(
        hubValue = day.acPowerW / 1000.0,
        hubUnit = "kW",
        hubLabel = "Live output",
        solarW = day.acPowerW,
        batteryW = day.batteryPowerW,
        gridW = day.gridExportW,
        homeW = day.homeLoadW,
        liveState = LiveNumberState.UPDATING,
        flowHeight = 280.dp
    )
    GalleryLabel("Night: nothing on the solar spoke, the home runs from the battery")
    EnergyFlow(
        hubValue = 0.0,
        hubUnit = "kW",
        hubLabel = "Live output",
        solarW = 0.0,
        batteryW = -620.0,
        gridW = 0.0,
        homeW = 620.0,
        liveState = LiveNumberState.STATIC,
        flowHeight = 280.dp
    )
}

@Composable
fun EnergyFlowDegraded(context: GalleryContext) {
    val day = FixtureData.daytimeTelemetry(System.currentTimeMillis())
    GalleryLabel("Stale: dots stop, a static arrow keeps the last known direction")
    EnergyFlow(
        hubValue = day.acPowerW / 1000.0,
        hubUnit = "kW",
        hubLabel = "Live output",
        solarW = day.acPowerW,
        batteryW = day.batteryPowerW,
        gridW = day.gridExportW,
        homeW = day.homeLoadW,
        state = SurfaceState.STALE,
        liveState = LiveNumberState.STALE,
        flowHeight = 240.dp
    )
    GalleryLabel("Offline: the same picture, with the failure state")
    EnergyFlow(
        hubValue = day.acPowerW / 1000.0,
        hubUnit = "kW",
        hubLabel = "Live output",
        solarW = day.acPowerW,
        batteryW = day.batteryPowerW,
        gridW = day.gridExportW,
        homeW = day.homeLoadW,
        state = SurfaceState.ERROR,
        liveState = LiveNumberState.OFFLINE,
        flowHeight = 240.dp
    )
    GalleryLabel("Reduce Motion: no dots at all, static arrows only")
    EnergyFlow(
        hubValue = day.acPowerW / 1000.0,
        hubUnit = "kW",
        hubLabel = "Live output",
        solarW = day.acPowerW,
        batteryW = day.batteryPowerW,
        gridW = day.gridExportW,
        homeW = day.homeLoadW,
        motion = HeliosMotionSettings.ReduceMotion,
        flowHeight = 240.dp
    )
}

// ------------------------------------------------------------------ battery

@Composable
fun BatteryRingCharging(context: GalleryContext) {
    Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
        BatteryRing(
            socPct = 86.0,
            powerW = 1120.0,
            size = 150.dp,
            mode = BatteryRingMode.CHARGING,
            thresholdLabel = "Above 80%"
        )
        BatteryRing(socPct = 42.0, powerW = -620.0, size = 150.dp, mode = BatteryRingMode.DISCHARGING)
    }
    GalleryLabel("Charge glow above 30 W, discharge pulse below -30 W (motion-language section 5)")
}

@Composable
fun BatteryRingIdleAndMissing(context: GalleryContext) {
    Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
        BatteryRing(socPct = 62.0, powerW = 0.0, size = 150.dp, mode = BatteryRingMode.IDLE)
        BatteryRing(
            socPct = 18.0,
            powerW = -800.0,
            size = 150.dp,
            mode = BatteryRingMode.DISCHARGING,
            thresholdLabel = "Below 20%"
        )
    }
    GalleryLabel("A failed register read is not 0 percent")
    BatteryRing(socPct = Double.NaN, powerW = Double.NaN, size = 150.dp)
}

// ------------------------------------------------------------------ charts

@Composable
fun ProductionChartReadyEmpty(context: GalleryContext) {
    var selected by remember { mutableStateOf<Double?>(14.5) }
    val now = System.currentTimeMillis()
    GalleryLabel("Ready, scrubbing: tap or drag for any half hour")
    ProductionChart(
        series = FixtureData.seriesLoadable(FixtureScenario.LIVE, now),
        nowHour = SolarCurve.nowAsHourFloat(now),
        selectedT = selected,
        onSelect = { selected = it }
    )
    GalleryLabel("Before sunrise: empty, with the reason")
    ProductionChart(series = FixtureData.seriesLoadable(FixtureScenario.EMPTY, now))
}

@Composable
fun ProductionChartLoadingStale(context: GalleryContext) {
    val now = System.currentTimeMillis()
    GalleryLabel("Loading: a skeleton in the chart's own 160 dp box")
    ProductionChart(series = FixtureData.seriesLoadable(FixtureScenario.LOADING, now))
    GalleryLabel("Stale: dimmed, the marker still reads the past")
    ProductionChart(
        series = FixtureData.seriesLoadable(FixtureScenario.STALE, now),
        nowHour = SolarCurve.nowAsHourFloat(now)
    )
}

@Composable
fun WeekChartReady(context: GalleryContext) {
    var selected by remember { mutableStateOf<Int?>(2) }
    WeekChart(
        bars = weekBars(),
        selectedIndex = selected,
        onSelect = { selected = it }
    )
    val colors = LocalHeliosSemanticColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(colors.chart.production))
        Text("Produced", style = HeliosTypography.caption2, color = colors.textTertiary)
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(colors.chart.consumption))
        Text("Consumed", style = HeliosTypography.caption2, color = colors.textTertiary)
    }
}

@Composable
fun WeekChartLoadingEmpty(context: GalleryContext) {
    GalleryLabel("Loading: skeleton bars")
    WeekChart(bars = emptyList(), state = SurfaceState.LOADING, height = 120.dp)
    GalleryLabel("Empty: the first full day has not finished")
    WeekChart(bars = emptyList(), state = SurfaceState.EMPTY)
}

@Composable
fun WeekChartError(context: GalleryContext) {
    GalleryLabel("Error: the week block fails on its own, the today curve is unaffected")
    WeekChart(bars = emptyList(), state = SurfaceState.ERROR)
}

// ------------------------------------------------------------------ metric tiles

@Composable
fun MetricTilesBasics(context: GalleryContext) {
    MetricGrid(
        tiles = listOf(
            MetricTileSpec(label = "Live output", value = "4.23", unit = "kW"),
            MetricTileSpec(
                label = "Irradiance",
                value = "782",
                unit = "W/m\u00B2",
                sparkline = FixtureData.liveSeries().map { it.acPowerW }
            ),
            MetricTileSpec(
                label = "Today",
                value = "18.4",
                unit = "kWh",
                delta = "+2.1 vs yesterday",
                deltaIsGood = true
            ),
            MetricTileSpec(
                label = "Self-consumption",
                value = "74",
                unit = "%",
                delta = "-6% vs last week",
                deltaIsGood = false
            )
        )
    )
}

@Composable
fun MetricTilesEdge(context: GalleryContext) {
    MetricGrid(
        tiles = listOf(
            MetricTileSpec(label = "Subtle variant", value = "312", unit = "cycles", state = MetricTileState.SUBTLE),
            MetricTileSpec(
                label = "Disabled until a link exists",
                value = HeliosFormat.NO_DATA,
                state = MetricTileState.DISABLED
            ),
            MetricTileSpec(label = "Last known", value = "4.23", unit = "kW", state = MetricTileState.STALE)
        )
    )
    GalleryLabel("Missing register: no data, never zero")
    MetricGrid(
        tiles = listOf(
            MetricTileSpec(label = "DC voltage", value = HeliosFormat.NO_DATA, delta = "string C did not report")
        )
    )
    GalleryLabel("Long label, long unit, largest text scale")
    MetricGrid(
        tiles = listOf(
            MetricTileSpec(
                label = "Energy produced since the array was commissioned",
                value = "18 420.5",
                unit = "kilowatt hours",
                delta = "+31.6 today",
                deltaIsGood = true
            )
        )
    )
}

@Composable
fun LiveNumberStates(context: GalleryContext) {
    Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
        Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space5)) {
            LiveNumber(label = "Updating", value = 4.23, unit = "kW", state = LiveNumberState.UPDATING)
            LiveNumber(label = "Static", value = 18.4, unit = "kWh", state = LiveNumberState.STATIC)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space5)) {
            LiveNumber(label = "Stale", value = 4.23, unit = "kW", state = LiveNumberState.STALE)
            LiveNumber(label = "Demo", value = 4.23, unit = "kW", state = LiveNumberState.DEMO)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space5)) {
            LiveNumber(label = "Offline", value = 4.23, unit = "kW", state = LiveNumberState.OFFLINE)
            LiveNumber(label = "Did not report", value = Double.NaN, unit = "kW", state = LiveNumberState.STATIC)
        }
    }
}

// ------------------------------------------------------------------ status

@Composable
fun StatusPillStates(context: GalleryContext) {
    val colors = LocalHeliosSemanticColors.current
    Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)) {
        HeliosStatusKind.entries.forEach { kind ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(kind = kind, onClick = {})
                Spacer(Modifier.width(HeliosSpacing.space3))
                Text(
                    text = kind.name.lowercase(),
                    style = HeliosTypography.caption2,
                    color = colors.textTertiary
                )
            }
        }
        GalleryLabel("Every pill also carries its word, so status is never colour only.")
    }
}

@Composable
fun FreshnessStates(context: GalleryContext) {
    FreshnessStamp(text = "Live", kind = HeliosStatusKind.PRODUCING, onClick = {})
    FreshnessStamp(text = "Updated 12 s ago", kind = HeliosStatusKind.PRODUCING)
    FreshnessStamp(text = "Stale 42 s", kind = HeliosStatusKind.CURTAILED)
    FreshnessStamp(text = "Offline \u00B7 last known 1 min ago", kind = HeliosStatusKind.OFFLINE, onClick = {})
    FreshnessStamp(text = "Demo data", kind = HeliosStatusKind.DEMO)
}

// ------------------------------------------------------------------ insights

@Composable
fun InsightHighlightStates(context: GalleryContext) {
    val insights = FixtureData.insights()
    HeliosSeverityKind.entries.forEachIndexed { index, severity ->
        InsightHighlight(
            insight = insights[index],
            severity = severity,
            demoQualifier = index == 2
        )
    }
}

@Composable
fun InsightCardPositiveNeutral(context: GalleryContext) {
    FixtureData.insights().take(2).forEachIndexed { index, insight ->
        InsightCard(
            insight = insight,
            severity = insight.severity.toSeverityKind(),
            demoQualifier = context.demoMode && index == 0,
            onAction = if (insight.actionLabel != null) ({}) else null
        )
    }
    GalleryLabel("Severity is written as a word; an action without a handler stays hidden.")
}

@Composable
fun InsightCardAttentionCritical(context: GalleryContext) {
    FixtureData.insights().drop(2).take(2).forEach { insight ->
        InsightCard(
            insight = insight,
            severity = insight.severity.toSeverityKind(),
            onAction = if (insight.actionLabel != null) ({}) else null
        )
    }
    GalleryLabel("Attention and critical rows carry a real action button.")
}

@Composable
fun InsightCardLongBody(context: GalleryContext) {
    InsightCard(
        insight = FixtureData.longInsights().first(),
        severity = HeliosSeverityKind.ATTENTION,
        onAction = {}
    )
    GalleryLabel("Long body and long title in the card's own two-line rhythm")
}

// ------------------------------------------------------------------ forecast

@Composable
fun ForecastStripStatesTop(context: GalleryContext) {
    val days = FixtureData.forecastDays(7).take(5)
    GalleryLabel("Ready: five days at caption-2")
    ForecastStrip(days = days, state = SurfaceState.READY)
    GalleryLabel("Loading: five skeleton cells in the cell's own shape")
    ForecastStrip(days = emptyList(), state = SurfaceState.LOADING)
}

@Composable
fun ForecastStripStatesBottom(context: GalleryContext) {
    GalleryLabel("Empty: no forecast for the location")
    ForecastStrip(days = emptyList(), state = SurfaceState.EMPTY)
    GalleryLabel("Stale: dimmed, the age is stated by the section header")
    ForecastStrip(days = FixtureData.forecastDays(7).take(5), state = SurfaceState.STALE)
    GalleryLabel("Error: inline in the section, never a whole-screen failure")
    ForecastStrip(days = emptyList(), state = SurfaceState.ERROR)
}

@Composable
fun ForecastCardReady(context: GalleryContext) {
    ForecastCard(
        days = FixtureData.forecastDays(7),
        demoQualifier = context.demoMode,
        onRetry = {}
    )
}

@Composable
fun ForecastCardStatesMid(context: GalleryContext) {
    GalleryLabel("Loading: three skeleton rows")
    ForecastCard(days = emptyList(), state = SurfaceState.LOADING)
    GalleryLabel("Empty, with the action that fixes it")
    ForecastCard(days = emptyList(), state = SurfaceState.EMPTY, onRetry = {})
}

@Composable
fun ForecastCardError(context: GalleryContext) {
    GalleryLabel("Error class F8: the section fails on its own, with a retry inside it")
    ForecastCard(days = emptyList(), state = SurfaceState.ERROR, onRetry = {})
}

@Composable
fun ForecastCardStale(context: GalleryContext) {
    GalleryLabel("Stale: every row dimmed, the age is stated on the section header")
    ForecastCard(days = FixtureData.forecastDays(7), state = SurfaceState.STALE, onRetry = {})
}

// ------------------------------------------------------------------ structure

@Composable
fun SectionHeaderStates(context: GalleryContext) {
    SectionHeader(title = "Energy flow")
    SectionHeader(title = "Energy flow", eyebrow = "Live")
    SectionHeader(
        title = "Next five days",
        eyebrow = "Forecast",
        trailing = { HeliosGhostButton(text = "All days", onClick = {}) }
    )
}

@Composable
fun WeatherIconStates(context: GalleryContext) {
    val colors = LocalHeliosSemanticColors.current
    Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
        WeatherCondition.entries.chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space4)) {
                row.forEach { condition ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        WeatherIcon(condition = condition, size = 28.dp)
                        Text(
                            text = WeatherConditions.label(condition),
                            style = HeliosTypography.caption2,
                            color = colors.textTertiary
                        )
                    }
                }
            }
        }
        GalleryLabel(
            "WMO codes map through WeatherConditions.conditionFor: " +
                listOf(0, 2, 3, 45, 51, 61, 65, 71, 80, 95).joinToString(", ")
        )
    }
}

@Composable
fun MarkStates(context: GalleryContext) {
    val colors = LocalHeliosSemanticColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space4)
    ) {
        HeliosMark(size = 16.dp)
        HeliosMark(size = 28.dp)
        HeliosMark(size = 48.dp)
        HeliosMark(size = 64.dp)
    }
    GalleryLabel("White-label text marks, one per registry entry")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space4)
    ) {
        FixtureData.brands.forEach { brand ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val accent = Color(android.graphics.Color.parseColor(brand.accent))
                if (brand.mark == "helios") {
                    HeliosMark(size = 28.dp, color = accent)
                } else {
                    BrandTextMark(text = brand.textMark ?: brand.name.take(1), size = 28.dp, color = accent)
                }
                Text(text = brand.name, style = HeliosTypography.caption2, color = colors.textTertiary)
            }
        }
    }
}

// ------------------------------------------------------------------ chrome

@Composable
fun BottomNavReady(context: GalleryContext) {
    var selected by remember { mutableStateOf(0) }
    BottomNav(
        tabs = HeliosDestinations.default(insightBadge = 2),
        selectedIndex = selected,
        onTabSelected = { selected = it }
    )
    GalleryLabel("Five destinations; the badge marks a destination that needs attention.")
}

@Composable
fun BottomNavSelected(context: GalleryContext) {
    HeliosDestinations.default().forEachIndexed { index, _ ->
        BottomNav(
            tabs = HeliosDestinations.default(insightBadge = if (index == 2) 3 else null),
            selectedIndex = index,
            onTabSelected = {}
        )
        Spacer(Modifier.height(HeliosSpacing.space1))
    }
}

@Composable
fun TopBarLive(context: GalleryContext) {
    GalleryLabel("Connected: mark, freshness, status")
    TopBar(
        brandName = "helios\u00B0",
        subTitle = "Dashboard",
        statusKind = HeliosStatusKind.PRODUCING,
        freshnessText = "Live",
        freshnessKind = HeliosStatusKind.PRODUCING,
        onShare = {},
        onFreshnessClick = {}
    )
    GalleryLabel("Demo: never the word live")
    TopBar(
        brandName = "helios\u00B0",
        subTitle = "Production",
        statusKind = HeliosStatusKind.DEMO,
        freshnessText = "Demo data",
        freshnessKind = HeliosStatusKind.DEMO,
        onShare = {}
    )
}

@Composable
fun TopBarDegraded(context: GalleryContext) {
    GalleryLabel("Stale")
    TopBar(
        brandName = "helios\u00B0",
        subTitle = "Dashboard",
        statusKind = HeliosStatusKind.CURTAILED,
        freshnessText = "Stale 42 s",
        freshnessKind = HeliosStatusKind.CURTAILED,
        onShare = {}
    )
    GalleryLabel("Offline: last known, with the age")
    TopBar(
        brandName = "helios\u00B0",
        subTitle = "Dashboard",
        statusKind = HeliosStatusKind.OFFLINE,
        freshnessText = "Offline \u00B7 last known 1 min ago",
        freshnessKind = HeliosStatusKind.OFFLINE,
        onShare = {}
    )
    GalleryLabel("Night, and a white-label text mark")
    TopBar(
        subTitle = "Dashboard",
        statusKind = HeliosStatusKind.NIGHT,
        freshnessText = "Live",
        freshnessKind = HeliosStatusKind.NIGHT,
        brandName = "Voltcraft",
        textMark = "V",
        usesRadialMark = false,
        onShare = {}
    )
}

// ------------------------------------------------------------------ actions

@Composable
fun ButtonStates(context: GalleryContext) {
    Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
        HeliosPrimaryButton(text = "Copy link", onClick = {})
        HeliosSecondaryButton(text = "Retry now", onClick = {})
    }
    Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
        HeliosPrimaryButton(text = "Applying", onClick = {}, loading = true)
        HeliosGhostButton(text = "Use demo system", onClick = {})
    }
    GalleryLabel("Disabled names its precondition")
    Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
        HeliosPrimaryButton(
            text = "Test connection",
            onClick = {},
            enabled = false,
            disabledReason = "Enter a host and a port first"
        )
        HeliosDestructiveButton(text = "Forget inverter", onClick = {})
    }
    GalleryLabel("Chips: selected and unselected")
    var selected by remember { mutableStateOf("Solar") }
    Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)) {
        listOf("Solar", "Battery", "Grid").forEach { label ->
            HeliosChip(label = label, selected = selected == label, onClick = { selected = label })
        }
    }
}

@Composable
fun ShareSheetIdle(context: GalleryContext) {
    GalleryLabel("Idle: the link is shown in full so it can be read out loud")
    ShareSheetContent(
        status = ShareSheetStatus.IDLE,
        url = "https://helios.app/share/v1-MTc2MjM0NTY3-fixture",
        summary = shareSummary(),
        targets = FixtureData.shareTargets().map { it.label to it.available }
    )
}

@Composable
fun ShareSheetCopied(context: GalleryContext) {
    GalleryLabel("Copied: the confirmation names the clipboard, the snapshot stays local")
    ShareSheetContent(
        status = ShareSheetStatus.COPIED,
        url = "https://helios.app/share/v1-MTc2MjM0NTY3-fixture",
        summary = shareSummary(),
        targets = FixtureData.shareTargets().map { it.label to it.available }
    )
}

@Composable
fun ShareSheetFailed(context: GalleryContext) {
    GalleryLabel("Failed: the reason is stated and the retry is offered")
    ShareSheetContent(
        status = ShareSheetStatus.FAILED,
        url = "https://helios.app/share/v1-MTc2MjM0NTY3-fixture",
        summary = shareSummary(),
        // The target rows are the same component as the idle page shows; leaving them out
        // here keeps the page inside one screen.
        targets = emptyList(),
        failureReason = "Snapshot payload v1 rejected the reading"
    )
}

@Composable
fun SnapshotSummaryRows(context: GalleryContext) {
    GalleryLabel("A missing optional value is stated, never left blank")
    Column {
        SnapshotSummaryRow(label = "Live power", value = "4.23 kW")
        SnapshotSummaryRow(label = "Today", value = "18.4 kWh")
        SnapshotSummaryRow(label = "Forecast days", value = "", missing = true)
    }
}

/**
 * Three rows, not five: the sheet is the tallest component in the gallery, and a section
 * has to fit one screen for the capture to be verifiable. The remaining rows are the same
 * component with the same contract.
 */
private fun shareSummary(): List<Pair<String, String>> = listOf(
    "Live power" to "4.23 kW",
    "Today" to "18.4 kWh",
    "Battery" to "62%"
)

// ------------------------------------------------------------------ states

@Composable
fun EmptyStates(context: GalleryContext) {
    GalleryLabel("Empty, with the action that fixes it")
    EmptyState(
        title = "No production yet today",
        message = "The curve starts at sunrise, around 06:12 today.",
        actionLabel = "Check the connection",
        onAction = {}
    )
    GalleryLabel("Empty with nothing to fix, so no action is shown")
    EmptyState(
        title = "No completed days yet",
        message = "The week chart fills in after the first full day of production."
    )
}

@Composable
fun ErrorStateOnly(context: GalleryContext) {
    GalleryLabel("Error with a diagnostic detail line and one recovery action")
    HeliosErrorState(
        title = "Inverter not responding",
        message = "192.168.1.42:502 did not answer within 2 s.",
        detail = "F2 timeout, attempt 3 of 5, next retry in 30 s",
        actionLabel = "Retry now",
        onAction = {}
    )
}

@Composable
fun DeniedAndSkeletonStates(context: GalleryContext) {
    GalleryLabel("Denied permission keeps a working path")
    DeniedPermissionState(
        title = "Using the default location",
        message = "Access to your location was denied, so the forecast uses San Francisco. " +
            "You can still choose a place by name.",
        actionLabel = "Open settings",
        onAction = {}
    )
    GalleryLabel("Loading: shapes that match the content they replace")
    SkeletonBlock(height = 56.dp, shape = HeliosShape.md)
    SkeletonLines(lines = 3)
}

@Composable
fun BannerStates(context: GalleryContext) {
    ConnectionBanner(
        state = ConnectionBannerState.OFFLINE,
        message = "Not on the same network as the inverter",
        reason = "No route to 192.168.1.42:502. The reading below is last known.",
        onRetry = {},
        onOpenConnection = {}
    )
    ConnectionBanner(
        state = ConnectionBannerState.RECONNECTING,
        message = "Reconnecting",
        reason = "Attempt 3 of 5, next retry in 30 s.",
        onRetry = {}
    )
    ConnectionBanner(
        state = ConnectionBannerState.RECOVERED,
        message = "Reconnected, updated now",
        reason = "This notice dismisses itself after 4 s."
    )
}

@Composable
fun TrailStates(context: GalleryContext) {
    GalleryLabel("Live trail: running")
    LiveTrail(values = FixtureData.liveSeries().map { it.acPowerW }, summary = "Live trail, steady near 4.23 kW")
    GalleryLabel("Live trail: stale, so the marker is gone")
    LiveTrail(
        values = FixtureData.liveSeries().map { it.acPowerW },
        state = SurfaceState.STALE,
        summary = "Live trail stopped 42 s ago"
    )
    GalleryLabel("Live trail: empty")
    LiveTrail(values = emptyList(), state = SurfaceState.EMPTY)
}

// ------------------------------------------------------------------ contracts

@Composable
fun ServiceContractStates(context: GalleryContext) {
    val services = ServiceGraph.current
    val colors = LocalHeliosSemanticColors.current
    val telemetry by services.telemetry.telemetry.collectAsState(initial = Loadable.Loading)
    val today by services.series.todaySeries.collectAsState(initial = Loadable.Loading)
    val live by services.series.liveSeries.collectAsState(initial = Loadable.Loading)
    val week by services.series.weekSeries.collectAsState(initial = Loadable.Loading)
    val forecast by services.forecast.forecast.collectAsState(initial = Loadable.Loading)
    val insights by services.insights.insights.collectAsState(initial = Loadable.Loading)
    val connection by services.connection.link.collectAsState(initial = Loadable.Loading)
    val location by services.location.location.collectAsState(initial = Loadable.Loading)
    val theme by services.theme.themeMode.collectAsState(initial = Loadable.Loading)
    val brand by services.brand.brand.collectAsState(initial = Loadable.Loading)

    val rows = listOf(
        Triple("TelemetryService (SVC-01)", telemetry, telemetry.valueOrNull()?.let { HeliosFormat.kilowatts(it.acPowerW / 1000.0) }),
        Triple("SeriesService.today (SVC-02)", today, today.valueOrNull()?.let { "${it.size} half-hour points" }),
        Triple("SeriesService.live", live, live.valueOrNull()?.let { "${it.size} samples" }),
        Triple("SeriesService.week (SVC-03)", week, week.valueOrNull()?.let { "${it.size} days" }),
        Triple("ForecastService (SVC-04)", forecast, forecast.valueOrNull()?.let { "${it.days.size} days" }),
        Triple("InsightsService (SVC-07)", insights, insights.valueOrNull()?.let { "${it.size} advisories" }),
        Triple("ConnectionService (SVC-19)", connection, connection.valueOrNull()?.let { "${it.state} ${it.config.host}:${it.config.port}" }),
        Triple("LocationService (SVC-05)", location, location.valueOrNull()?.label),
        Triple("ThemeService (SVC-18)", theme, theme.valueOrNull()?.name),
        Triple("BrandService (SVC-10)", brand, brand.valueOrNull()?.name)
    )

    Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)) {
        rows.forEach { (name, state, value) ->
            val ui = state.uiState()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(stateColor(ui))
                )
                Spacer(Modifier.width(HeliosSpacing.space3))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = name, style = HeliosTypography.caption, color = colors.textPrimary)
                    Text(
                        text = value ?: state.freshnessLabel(),
                        style = HeliosTypography.caption2,
                        color = colors.textTertiary
                    )
                }
                Text(
                    text = ui.name,
                    style = HeliosTypography.caption2,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        GalleryLabel("ShareService (SVC-12 to SVC-15) is exercised by the snapshot section below.")
    }
}

@Composable
private fun stateColor(state: UiState): Color {
    val colors = LocalHeliosSemanticColors.current
    return when (state) {
        UiState.READY -> colors.flowPrimary
        UiState.PARTIAL -> colors.solarPrimary
        UiState.STALE, UiState.OFFLINE -> colors.gridImportPrimary
        UiState.ERROR, UiState.DENIED -> colors.alertPrimary
        UiState.EMPTY -> colors.textQuaternary
        UiState.LOADING -> colors.accentPrimary
    }
}

@Composable
fun SnapshotPayloadStates(context: GalleryContext) {
    val colors = LocalHeliosSemanticColors.current
    val services = ServiceGraph.current
    val now = System.currentTimeMillis()
    val telemetry = FixtureData.telemetry(context.scenario, now).valueOrNull()
        ?: FixtureData.daytimeTelemetry(now)
    val payload = services.share.buildSnapshot(
        telemetry = telemetry,
        locationLabel = FixtureData.defaultLocation.label,
        forecastDays = FixtureData.forecastDays(7).map { it.expectedKwh },
        brandId = FixtureData.heliosBrand.id
    )
    val outcome = services.share.prepare(
        telemetry = telemetry,
        locationLabel = FixtureData.defaultLocation.label,
        forecastDays = FixtureData.forecastDays(7).map { it.expectedKwh },
        brandId = FixtureData.heliosBrand.id
    )
    val encoded = services.share.encode(payload)
    val decoded = services.share.decode(encoded)
    Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)) {
        GalleryLabel("v1 keys in PWA order: v, ts, loc, ac, todayKwh, lifeKwh, soc, selfUse, fc, br")
        Text(
            text = "ac=${payload.ac} todayKwh=${payload.todayKwh} lifeKwh=${payload.lifeKwh} " +
                "soc=${payload.soc} selfUse=${payload.selfUse} br=${payload.br}",
            style = HeliosTypography.caption,
            color = colors.textSecondary
        )
        Text(
            text = "base64url length ${encoded.length}; round trip decodes to v=${decoded?.v ?: -1}",
            style = HeliosTypography.caption,
            color = if (decoded != null) colors.flowStrong else colors.alertStrong
        )
        Text(
            text = when (outcome) {
                is com.helios.core.data.service.ShareOutcome.Ready -> "prepare() ready: ${outcome.url.take(44)}..."
                is com.helios.core.data.service.ShareOutcome.Failed -> "prepare() failed: ${outcome.failure.message}"
            },
            style = HeliosTypography.caption2,
            color = colors.textTertiary
        )
        val rejectsOtherVersions = services.share.decode("eyJ2IjoyfQ") == null
        Text(
            text = "decode() rejects any v other than 1: $rejectsOtherVersions",
            style = HeliosTypography.caption2,
            color = colors.textTertiary
        )
    }
}
