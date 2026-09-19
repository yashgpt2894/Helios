package com.helios.feature.production

import androidx.compose.runtime.Immutable
import com.helios.core.data.fixture.FixtureData
import com.helios.core.data.fixture.FixtureScenario
import com.helios.core.data.repository.SolarCurve
import com.helios.core.data.service.ConnectionSnapshot
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.WeekPoint
import com.helios.core.domain.model.Brand
import com.helios.core.domain.model.HistoryPoint
import com.helios.core.domain.model.Insight
import com.helios.core.domain.model.PanelString
import com.helios.core.domain.model.SolarTelemetry
import com.helios.feature.dashboard.DashboardTab
import com.helios.feature.dashboard.ReadingState

/**
 * Everything the Production screen draws, as one explicit value.
 *
 * The same contract as the Dashboard's state: the screen renders this and nothing else, so
 * the ready, loading, stale, offline, partial, empty, error and long-content shapes can all
 * be produced from the fixtures without hardware. The reading keeps its age through
 * [ReadingState], which is shared with the Dashboard so both screens describe a stale number
 * the same way.
 */
@Immutable
data class ProductionState(
    val brand: Brand,
    val reading: ReadingState,
    val todaySeries: Loadable<List<HistoryPoint>>,
    val weekSeries: Loadable<List<WeekPoint>>,
    val forecast: Loadable<com.helios.core.domain.model.ProductionForecast>,
    val insights: Loadable<List<Insight>>,
    val connection: Loadable<ConnectionSnapshot>,
    val scenario: FixtureScenario = FixtureScenario.LIVE,
    val nowHour: Double = SolarCurve.nowAsHourFloat(),
    /** The half hour the user is reading on the curve, or null for "now". */
    val selectedT: Double? = null,
    /** The day the user is reading in the week chart, or null for none. */
    val selectedWeekIndex: Int? = null,
    val selectedTab: Int = DashboardTab.SOLAR
) {

    val telemetryValue: SolarTelemetry? get() = reading.value
    val strings: List<PanelString> get() = telemetryValue?.panels.orEmpty()
    val simulated: Boolean get() = reading.simulated

    /** Strings that did not report at all, so the list can name them instead of hiding them. */
    val missingStrings: List<String>
        get() = reading.missingFields.filter { it.startsWith("panels.") }
            .map { it.removePrefix("panels.") }
}

/**
 * The actions a host wires up. A control is only drawn when its handler exists: the scrub
 * marker, the week selection, the retry and the banner all require one.
 */
@Immutable
data class ProductionActions(
    val onShare: (() -> Unit)? = null,
    val onOpenConnection: (() -> Unit)? = null,
    val onRetry: (() -> Unit)? = null,
    val onNavigate: ((Int) -> Unit)? = null,
    val onSelectHour: ((Double?) -> Unit)? = null,
    val onSelectWeekDay: ((Int) -> Unit)? = null,
    val onOpenInsights: (() -> Unit)? = null
)

/** Production state from the fixtures, per declared scenario. */
object ProductionFixtures {

    fun state(
        scenario: FixtureScenario = FixtureScenario.LIVE,
        nowMs: Long = System.currentTimeMillis(),
        selectedT: Double? = null,
        selectedWeekIndex: Int? = null,
        selectedTab: Int = DashboardTab.SOLAR,
        simulated: Boolean = true
    ): ProductionState = ProductionState(
        brand = FixtureData.heliosBrand,
        reading = ReadingState(telemetry = FixtureData.telemetry(scenario, nowMs), simulated = simulated),
        todaySeries = FixtureData.seriesLoadable(scenario, nowMs),
        weekSeries = FixtureData.weekLoadable(scenario, nowMs),
        forecast = FixtureData.forecast(scenario, nowMs),
        insights = FixtureData.insightsLoadable(scenario, nowMs),
        connection = FixtureData.connectionSnapshot(scenario, nowMs),
        scenario = scenario,
        nowHour = SolarCurve.nowAsHourFloat(nowMs),
        selectedT = selectedT,
        selectedWeekIndex = selectedWeekIndex,
        selectedTab = selectedTab
    )

    /** The night shape: no production, the strings idle, the week chart still real. */
    fun nightState(
        scenario: FixtureScenario = FixtureScenario.LIVE,
        nowMs: Long = System.currentTimeMillis(),
        simulated: Boolean = true
    ): ProductionState = state(scenario, nowMs, simulated = simulated).copy(
        reading = ReadingState(telemetry = FixtureData.nightTelemetry(scenario, nowMs), simulated = simulated),
        nowHour = 22.5
    )
}
