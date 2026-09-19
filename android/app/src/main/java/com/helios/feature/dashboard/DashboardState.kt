package com.helios.feature.dashboard

import androidx.compose.runtime.Immutable
import com.helios.core.data.fixture.FixtureData
import com.helios.core.data.fixture.FixtureScenario
import com.helios.core.data.repository.SolarCurve
import com.helios.core.data.service.ConnectionSnapshot
import com.helios.core.data.service.Freshness
import com.helios.core.data.service.LivePoint
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.WeekPoint
import com.helios.core.designsystem.component.SurfaceState
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.domain.model.Brand
import com.helios.core.domain.model.HistoryPoint
import com.helios.core.domain.model.Insight
import com.helios.core.domain.model.ProductionForecast
import com.helios.core.domain.model.SolarTelemetry

/**
 * Everything the Dashboard draws, as one explicit value.
 *
 * The screen has no service, no coroutine and no clock of its own: it renders
 * [DashboardState], so every declared state in `design/ux-flows.md` section 0 can be
 * captured, previewed or asserted without hardware, permission dialogs or a network. Values
 * keep their [Loadable] wrapper through [ReadingState], because the design's rule is that a
 * value may never be rendered without the state around it (C1: no live number without a
 * freshness statement).
 *
 * [DashboardFixtures] builds these values from `core/data/fixture`, which is the same
 * contract the device adapters implement.
 */
@Immutable
data class DashboardState(
    val brand: Brand,
    val reading: ReadingState,
    val todaySeries: Loadable<List<HistoryPoint>>,
    val liveTrail: Loadable<List<LivePoint>>,
    val weekSeries: Loadable<List<WeekPoint>>,
    val forecast: Loadable<ProductionForecast>,
    val insights: Loadable<List<Insight>>,
    val connection: Loadable<ConnectionSnapshot>,
    val scenario: FixtureScenario = FixtureScenario.LIVE,
    /** Current time of day in hours, which places the today-chart marker. */
    val nowHour: Double = SolarCurve.nowAsHourFloat(),
    /** Where the forecast came from: the chosen place, or the default. */
    val locationNote: String? = null,
    val selectedTab: Int = DashboardTab.HOME,
    val insightBadgeCount: Int? = null
) {

    val telemetry: Loadable<SolarTelemetry> get() = reading.telemetry
    val telemetryValue: SolarTelemetry? get() = reading.value
    val simulated: Boolean get() = reading.simulated
    val statusKind: HeliosStatusKind get() = reading.statusKind
    val statusLabel: String get() = reading.statusLabel
    val freshness: Freshness get() = reading.freshness
    val freshnessText: String get() = reading.freshnessText
    val freshnessKind: HeliosStatusKind get() = reading.freshnessKind
    val dimmed: Boolean get() = reading.dimmed
    val surface: SurfaceState get() = reading.surface

    /**
     * DESIGN.md C3: the advisory block is replaced by a placeholder when the data cannot
     * support advice. The reason is stated rather than the block quietly disappearing.
     */
    val insightPlaceholder: String?
        get() = if (!simulated && (freshness == Freshness.OFFLINE || freshness == Freshness.STALE)) {
            "Waiting for the inverter"
        } else {
            null
        }

    /** Where the forecast came from, in words, for the section header. */
    val forecastPlace: String get() = locationNote ?: "Default location"
}

/** The five destinations, matching the navigation bar order. */
object DashboardTab {
    const val HOME = 0
    const val SOLAR = 1
    const val INSIGHTS = 2
    const val BATTERY = 3
    const val SETTINGS = 4
}

/**
 * The actions a host wires up. Every one is nullable on purpose: a control is only drawn
 * when a host can actually do what the control claims, so no button on these screens is a
 * dead handler (skill section 4).
 */
@Immutable
data class DashboardActions(
    val onShare: (() -> Unit)? = null,
    val onOpenConnection: (() -> Unit)? = null,
    val onRetry: (() -> Unit)? = null,
    val onNavigate: ((Int) -> Unit)? = null,
    val onOpenProduction: (() -> Unit)? = null,
    val onInsightAction: ((Insight) -> Unit)? = null
)

/**
 * Dashboard state from the fixtures, one function per declared scenario.
 *
 * `nowMs` is a parameter so a capture, a preview or a test is reproducible: two runs at the
 * same timestamp produce the same curve, the same marker and the same freshness words.
 */
object DashboardFixtures {

    fun state(
        scenario: FixtureScenario = FixtureScenario.LIVE,
        nowMs: Long = System.currentTimeMillis(),
        selectedTab: Int = DashboardTab.HOME,
        simulated: Boolean = true
    ): DashboardState = DashboardState(
        brand = FixtureData.heliosBrand,
        reading = ReadingState(telemetry = FixtureData.telemetry(scenario, nowMs), simulated = simulated),
        todaySeries = FixtureData.seriesLoadable(scenario, nowMs),
        liveTrail = FixtureData.liveLoadable(scenario, nowMs),
        weekSeries = FixtureData.weekLoadable(scenario, nowMs),
        forecast = FixtureData.forecast(scenario, nowMs),
        insights = FixtureData.insightsLoadable(scenario, nowMs),
        connection = FixtureData.connectionSnapshot(scenario, nowMs),
        scenario = scenario,
        nowHour = SolarCurve.nowAsHourFloat(nowMs),
        locationNote = locationNote(scenario),
        selectedTab = selectedTab,
        insightBadgeCount = (FixtureData.insightsLoadable(scenario, nowMs) as? Loadable.Ready)?.value?.size
    )

    /** The night state, which is a different set of values rather than a different screen. */
    fun nightState(
        scenario: FixtureScenario = FixtureScenario.LIVE,
        nowMs: Long = System.currentTimeMillis(),
        selectedTab: Int = DashboardTab.HOME,
        simulated: Boolean = true
    ): DashboardState = state(scenario, nowMs, selectedTab, simulated).copy(
        reading = ReadingState(telemetry = FixtureData.nightTelemetry(scenario, nowMs), simulated = simulated),
        nowHour = 22.5
    )

    private fun locationNote(scenario: FixtureScenario): String = when (scenario) {
        FixtureScenario.DENIED_PERMISSION -> "Default location \u00B7 choose a place"
        FixtureScenario.LONG_CONTENT -> FixtureData.longLocation.label
        else -> FixtureData.defaultLocation.label
    }
}
