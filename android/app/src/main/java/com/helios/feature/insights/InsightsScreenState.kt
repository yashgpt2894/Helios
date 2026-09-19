package com.helios.feature.insights

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.helios.core.data.fixture.FixtureData
import com.helios.core.data.fixture.FixtureScenario
import com.helios.core.data.fixture.FixtureState
import com.helios.core.data.service.Freshness
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.UiState
import com.helios.core.data.service.metaOrNull
import com.helios.core.data.service.uiState
import com.helios.core.data.service.valueOrNull
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.designsystem.component.SurfaceState
import com.helios.core.data.service.freshnessLabel
import com.helios.core.designsystem.component.toSurfaceState
import com.helios.core.designsystem.color.HeliosSeverityKind
import com.helios.core.domain.model.Insight
import com.helios.core.domain.model.InsightCategory
import com.helios.core.domain.model.InsightSeverity
import com.helios.core.domain.model.ProductionForecast
import com.helios.core.domain.model.SolarTelemetry
import com.helios.core.format.HeliosFormat

/**
 * SCR-09 Insights and savings, as an explicit, renderable state.
 *
 * The screen takes this object, so every state of `design/screen-inventory.md` SCR-09
 * (STS-039 to STS-045) can be built, captured and reviewed with no device and no network.
 * [fixture] fills it from `core/data/fixture`, which is the same data the fixture service
 * adapters return.
 *
 * Honest limits:
 *  - the savings tiles mirror `src/services/aiInsights.ts computeSavings`, including its
 *    month and lifetime self-consumption factors (0.85 and 0.8). They are the app's own
 *    arithmetic, stated in the disclosure, not a measured bill.
 *  - the feed is advisory. It is suppressed while the source is stale or offline
 *    (FLW-03), which shows as [Loadable.Empty] with a reason, never as a silent empty list.
 */

/** Savings, priced with the app's own rates. Any field is NaN when there is no reading. */
data class SavingsSummary(
    val today: Double = Double.NaN,
    val month: Double = Double.NaN,
    val lifetime: Double = Double.NaN,
    val co2AvoidedKg: Double = Double.NaN
) {
    companion object {
        /** Import rate and export rate live in [HeliosFormat] so there is one source. */
        val IMPORT_RATE: Double get() = HeliosFormat.IMPORT_RATE
        val EXPORT_RATE: Double get() = HeliosFormat.EXPORT_RATE

        /** Month-to-date and lifetime self-consumption factors, from the PWA savings maths. */
        const val MONTH_SELF_CONSUMPTION_FACTOR = 0.85
        const val LIFETIME_SELF_CONSUMPTION_FACTOR = 0.8

        /** Grid carbon intensity avoided per kWh, from the PWA (0.42 kg). */
        const val CO2_KG_PER_KWH = 0.42

        fun of(telemetry: SolarTelemetry?): SavingsSummary {
            if (telemetry == null) return SavingsSummary()
            return SavingsSummary(
                today = telemetry.energyTodayKwh * IMPORT_RATE,
                month = telemetry.energyMonthKwh * IMPORT_RATE * MONTH_SELF_CONSUMPTION_FACTOR,
                lifetime = telemetry.energyLifetimeKwh * IMPORT_RATE * LIFETIME_SELF_CONSUMPTION_FACTOR,
                co2AvoidedKg = telemetry.energyLifetimeKwh * CO2_KG_PER_KWH
            )
        }
    }
}

/** Words for the six advisory categories, used by the feed filter. */
fun InsightCategory.label(): String = when (this) {
    InsightCategory.production -> "Production"
    InsightCategory.consumption -> "Consumption"
    InsightCategory.battery -> "Battery"
    InsightCategory.savings -> "Savings"
    InsightCategory.maintenance -> "Maintenance"
    InsightCategory.forecast -> "Forecast"
}

/** Severity, as a word. A colour is not a severity (DESIGN.md 9). */
fun InsightSeverity.kind(): HeliosSeverityKind = when (this) {
    InsightSeverity.positive -> HeliosSeverityKind.POSITIVE
    InsightSeverity.neutral -> HeliosSeverityKind.NEUTRAL
    InsightSeverity.attention -> HeliosSeverityKind.ATTENTION
    InsightSeverity.critical -> HeliosSeverityKind.CRITICAL
}

/** Order in the feed: the critical advisory is never below a positive one. */
private fun InsightSeverity.rank(): Int = when (this) {
    InsightSeverity.critical -> 0
    InsightSeverity.attention -> 1
    InsightSeverity.positive -> 2
    InsightSeverity.neutral -> 3
}

/** How many advisories the feed shows before the "Show all" action (ACT-048). */
const val FEED_PAGE_SIZE = 5

/**
 * Everything the Insights screen renders.
 *
 * @param telemetry SVC-01, priced into [SavingsSummary] and named in the subtitle.
 * @param forecast SVC-04 with its state, so a failed forecast is inline and never blanks
 *   the advisories (F8, STS-041).
 * @param feed SVC-07, the advisory list with its state: loading, ready, partial, empty
 *   (nothing to say, or paused because the reading is stale), or failed.
 * @param selectedCategory the feed filter, null for all categories.
 * @param showAll false until the reader asks for the whole list.
 * @param savingsExpanded ACT-047 disclosure state, session only.
 */
data class InsightsScreenState(
    val scenario: FixtureScenario = FixtureScenario.LIVE,
    val telemetry: Loadable<SolarTelemetry> = Loadable.Loading,
    val forecast: Loadable<ProductionForecast> = Loadable.Loading,
    val feed: Loadable<List<Insight>> = Loadable.Loading,
    val selectedCategory: InsightCategory? = null,
    val showAll: Boolean = false,
    val savingsExpanded: Boolean = false,
    val demoQualifier: Boolean = true,
    val reducedMotion: Boolean = false
) {
    companion object {

        /** The one fixture entry point. */
        fun fixture(
            scenario: FixtureScenario = FixtureState.current(),
            nowMs: Long = System.currentTimeMillis(),
            selectedCategory: InsightCategory? = null,
            showAll: Boolean = false,
            savingsExpanded: Boolean = false,
            reducedMotion: Boolean = false
        ): InsightsScreenState = InsightsScreenState(
            scenario = scenario,
            telemetry = FixtureData.telemetry(scenario, nowMs),
            forecast = FixtureData.forecast(scenario, nowMs),
            feed = FixtureData.insightsLoadable(scenario, nowMs),
            selectedCategory = selectedCategory,
            showAll = showAll,
            savingsExpanded = savingsExpanded,
            demoQualifier = true,
            reducedMotion = reducedMotion
        )

        /** Offline with a forecast that is still usable: the mixed state of STS-045. */
        fun offlineFixture(
            nowMs: Long = System.currentTimeMillis(),
            reducedMotion: Boolean = false
        ): InsightsScreenState = fixture(
            scenario = FixtureScenario.OFFLINE,
            nowMs = nowMs,
            reducedMotion = reducedMotion
        )
    }
}

// ------------------------------------------------------------------ derivations

val InsightsScreenState.reading: SolarTelemetry? get() = telemetry.valueOrNull()

/** Savings priced from the reading, or NaN fields when there is no reading (STS-043). */
val InsightsScreenState.savings: SavingsSummary get() = SavingsSummary.of(reading)

/** True while there is no reading yet, so the savings tiles show their own skeleton. */
val InsightsScreenState.savingsLoading: Boolean
    get() = telemetry.uiState() == UiState.LOADING

/** True when there is no reading to price, so the tiles say "no data" (STS-043). */
val InsightsScreenState.savingsUnavailable: Boolean
    get() = telemetry.valueOrNull() == null && telemetry.uiState() != UiState.LOADING

/** The value surface of the forecast section, mapped in one shared place. */
val InsightsScreenState.forecastSurface: SurfaceState get() = forecast.toSurfaceState()

/** The value surface of the advisory feed. */
val InsightsScreenState.feedSurface: SurfaceState get() = feed.toSurfaceState()

/** True while the feed is loading, so the screen can show rows in the card's shape. */
val InsightsScreenState.feedLoading: Boolean get() = feedSurface == SurfaceState.LOADING

/** True when the reading is too old, or the link is down, so advisories are paused. */
val InsightsScreenState.feedPaused: Boolean
    get() = feed is Loadable.Empty &&
        (feed.metaOrNull()?.freshness == Freshness.STALE || feed.metaOrNull()?.freshness == Freshness.OFFLINE)

/** Every advisory the source returned, ordered by severity then by the source order. */
val InsightsScreenState.orderedFeed: List<Insight>
    get() = (feed.valueOrNull() ?: emptyList())
        .withIndex()
        .sortedWith(compareBy({ it.value.severity.rank() }, { it.index }))
        .map { it.value }

/** The one ranked advisory the screen leads with: the most severe, or the newest. */
val InsightsScreenState.highlight: Insight?
    get() = orderedFeed.firstOrNull()

/**
 * The feed after the highlight, the category filter and the page size.
 *
 * The filter and the page size are arguments as well as state, because the screen keeps
 * them locally while the user is on it; passing them keeps one implementation of the
 * ordering rule.
 */
fun InsightsScreenState.feedItems(
    selectedCategory: InsightCategory? = this.selectedCategory,
    showAll: Boolean = this.showAll
): List<Insight> {
    val rest = orderedFeed.drop(1)
    val filtered = selectedCategory?.let { category -> rest.filter { it.category == category } } ?: rest
    return if (showAll) filtered else filtered.take(FEED_PAGE_SIZE)
}

/** How many advisories the current filter hides behind "Show all" (ACT-048). */
fun InsightsScreenState.hiddenCount(
    selectedCategory: InsightCategory? = this.selectedCategory,
    showAll: Boolean = this.showAll
): Int {
    if (showAll) return 0
    val rest = orderedFeed.drop(1)
    val filtered = selectedCategory?.let { category -> rest.filter { it.category == category } } ?: rest
    return (filtered.size - FEED_PAGE_SIZE).coerceAtLeast(0)
}

/** Categories present in the feed, with their counts, for the filter row. */
val InsightsScreenState.categoryCounts: List<Pair<InsightCategory, Int>>
    get() = InsightCategory.entries.mapNotNull { category ->
        val count = orderedFeed.count { it.category == category }
        if (count == 0) null else category to count
    }

/** True when the source is the simulated system, so every advisory carries the qualifier. */
val InsightsScreenState.showDemoQualifier: Boolean
    get() = demoQualifier && (telemetry.metaOrNull()?.simulated == true || feed.metaOrNull()?.simulated == true)

/** The reason the feed has nothing to show, in the words of the state. */
val InsightsScreenState.feedMessage: String?
    get() = (feed as? Loadable.Empty)?.message

/** The action the empty feed offers, when it offers one. */
val InsightsScreenState.feedActionLabel: String?
    get() = (feed as? Loadable.Empty)?.actionLabel

/** Age of the advisory list, in the words of the freshness contract. */
val InsightsScreenState.feedFreshnessText: String get() = feed.freshnessLabel()

/** Word-plus-dot role for the feed's freshness stamp. */
val InsightsScreenState.feedFreshnessKind: HeliosStatusKind
    get() = when {
        feed is Loadable.Failed -> HeliosStatusKind.OFFLINE
        feed.metaOrNull()?.simulated == true -> HeliosStatusKind.DEMO
        feedSurface == SurfaceState.STALE || feedPaused -> HeliosStatusKind.STANDBY
        else -> HeliosStatusKind.PRODUCING
    }

/** The failure message of the advisory feed, when it failed. */
val InsightsScreenState.feedFailureMessage: String
    get() = (feed as? Loadable.Failed)?.failure?.message ?: "The advisory engine did not answer."

/** Diagnostic detail for the failed feed, for example the exception class. */
val InsightsScreenState.feedFailureDetail: String?
    get() = (feed as? Loadable.Failed)?.failure?.detail

/** The recovery action of the failed feed. */
val InsightsScreenState.feedFailureAction: String
    get() = (feed as? Loadable.Failed)?.failure?.action ?: "Retry now"

/**
 * The sentence under the title: what the advisories are generated from, and nothing the
 * app cannot see. With no reading it says so instead of naming strings.
 */
val InsightsScreenState.subtitle: String
    get() {
        val strings = stringCount
        val source = if (strings > 0) {
            "Insights generated from $strings strings, your inverter, the battery cabinet and local weather."
        } else {
            "Insights are generated from your inverter, the battery cabinet and local weather."
        }
        return source
    }

/** How many strings the subtitle may name, taken from the reading rather than asserted. */
val InsightsScreenState.stringCount: Int get() = reading?.panels?.size ?: 0

/** The weekly total of the forecast, for the section header. */
val InsightsScreenState.forecastTotalLabel: String?
    get() = forecast.valueOrNull()?.let { HeliosFormat.kwh(it.totalKwh, decimals = 0) }

/** How the week compares with the last, in words with a sign. */
val InsightsScreenState.forecastComparandLabel: String?
    get() = forecast.valueOrNull()?.let { HeliosFormat.signed(it.vsLastWeekPct, "%", decimals = 1) + " vs last week" }

/** Where the forecast is for, so a wrong place is visible rather than silent. */
val InsightsScreenState.forecastLocationLabel: String?
    get() = forecast.valueOrNull()?.location?.label

/**
 * Remembered fixture state for a shell that has no state of its own.
 *
 * The default argument of [InsightsScreen], so `HeliosNavGraph` keeps calling
 * `InsightsScreen()` while a state-owning shell passes its own object.
 */
@Composable
fun rememberInsightsFixtureState(scenario: FixtureScenario = FixtureState.current()): InsightsScreenState =
    remember(scenario) { InsightsScreenState.fixture(scenario) }
