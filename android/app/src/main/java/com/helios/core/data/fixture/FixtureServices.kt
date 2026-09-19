package com.helios.core.data.fixture

import com.helios.core.data.service.AppServices
import com.helios.core.data.service.BrandService
import com.helios.core.data.service.ConnectionService
import com.helios.core.data.service.FailureKind
import com.helios.core.data.service.Freshness
import com.helios.core.data.service.ForecastService
import com.helios.core.data.service.HeliosThemeMode
import com.helios.core.data.service.InsightsService
import com.helios.core.data.service.LivePoint
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.LocationService
import com.helios.core.data.service.ServiceFailure
import com.helios.core.data.service.ServiceGraph
import com.helios.core.data.service.ShareOutcome
import com.helios.core.data.service.ShareService
import com.helios.core.data.service.ShareTarget
import com.helios.core.data.service.SeriesService
import com.helios.core.data.service.SourceMeta
import com.helios.core.data.service.TelemetryService
import com.helios.core.data.service.ThemeService
import com.helios.core.data.service.WeekPoint
import com.helios.core.data.repository.ShareRepository
import com.helios.core.domain.model.Brand
import com.helios.core.domain.model.ConnectionConfig
import com.helios.core.domain.model.ForecastDay
import com.helios.core.domain.model.HistoryPoint
import com.helios.core.domain.model.Insight
import com.helios.core.domain.model.Location
import com.helios.core.domain.model.ProductionForecast
import com.helios.core.domain.model.SolarTelemetry
import com.helios.core.domain.model.SnapshotPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Fixture adapters: the same interfaces as the device adapters, backed by
 * [FixtureData] and one shared [FixtureScenario] switch.
 *
 * They exist so that every screen can be built, previewed, screenshot and reviewed with
 * no inverter, no permission dialog and no network. A screen that compiles against these
 * contracts is already correct for the device adapters, because the contract carries the
 * state, not just the value.
 *
 * Honest limits, stated in the code rather than the docs:
 *  - [refresh] re-reads the current scenario. It does not invent a recovered link.
 *  - the freshness timestamps move with the wall clock, so a running app ages the way
 *    the real one does.
 */
private inline fun <T> scenarioFlow(crossinline map: (FixtureScenario) -> Loadable<T>): Flow<Loadable<T>> =
    FixtureState.scenario.map { map(it) }

/** Telemetry, SVC-01. Fixtures are simulated by definition, so the demo flag starts on. */
class FixtureTelemetryService : TelemetryService {

    private val demoMode = MutableStateFlow(true)

    override val telemetry: Flow<Loadable<SolarTelemetry>> =
        combine(FixtureState.scenario, demoMode) { scenario, demo ->
            withDemo(FixtureData.telemetry(scenario), demo)
        }

    override suspend fun read(): Loadable<SolarTelemetry> =
        withDemo(FixtureData.telemetry(FixtureState.current()), demoMode.value)

    override suspend fun refresh(): Loadable<SolarTelemetry> =
        withDemo(FixtureData.telemetry(FixtureState.current()), demoMode.value)

    override suspend fun setDemoMode(enabled: Boolean) {
        demoMode.value = enabled
    }

    override val isDemo: Boolean
        get() = demoMode.value

    /** Status pill and freshness wording follow the demo flag, never a static string. */
    fun setDemo(enabled: Boolean) {
        demoMode.value = enabled
    }

    private fun withDemo(loadable: Loadable<SolarTelemetry>, demo: Boolean): Loadable<SolarTelemetry> {
        val meta = loadable.metaOrNullCopy() ?: return loadable
        val next = meta.copy(simulated = demo, note = meta.note ?: if (demo) "Demo system" else null)
        return when (loadable) {
            is Loadable.Ready -> Loadable.Ready(loadable.value, next)
            is Loadable.Empty -> Loadable.Empty(next, loadable.message, loadable.actionLabel)
            is Loadable.Failed -> Loadable.Failed(loadable.failure, next)
            Loadable.Loading -> Loadable.Loading
        }
    }

    private fun Loadable<*>.metaOrNullCopy(): SourceMeta? = when (this) {
        is Loadable.Ready -> meta
        is Loadable.Empty -> meta
        is Loadable.Failed -> meta
        Loadable.Loading -> null
    }
}

/** Today, live and week series, SVC-02 / SVC-03. */
class FixtureSeriesService : SeriesService {

    override val todaySeries: Flow<Loadable<List<HistoryPoint>>> =
        scenarioFlow { FixtureData.seriesLoadable(it) }

    override val liveSeries: Flow<Loadable<List<LivePoint>>> =
        scenarioFlow { FixtureData.liveLoadable(it) }

    override val weekSeries: Flow<Loadable<List<WeekPoint>>> =
        scenarioFlow { FixtureData.weekLoadable(it) }

    override suspend fun refresh() = Unit
}

/** Forecast, SVC-04. Failure class F8 is inline, so it never blanks a whole screen. */
class FixtureForecastService : ForecastService {

    override val forecast: Flow<Loadable<ProductionForecast>> = scenarioFlow { FixtureData.forecast(it) }

    override suspend fun load(location: Location): Loadable<ProductionForecast> =
        FixtureData.forecast(FixtureState.current())

    override suspend fun refresh(): Loadable<ProductionForecast> =
        FixtureData.forecast(FixtureState.current())

    override fun glanceDays(count: Int): Loadable<List<ForecastDay>> =
        when (val current = FixtureData.forecast(FixtureState.current())) {
            is Loadable.Ready -> Loadable.Ready(current.value.days.take(count), current.meta)
            is Loadable.Empty -> current
            is Loadable.Failed -> current
            Loadable.Loading -> Loadable.Loading
        }
}

/** Insights, SVC-07 / SVC-09, including the staleness suppression rule. */
class FixtureInsightsService : InsightsService {

    override val insights: Flow<Loadable<List<Insight>>> = scenarioFlow { FixtureData.insightsLoadable(it) }

    override suspend fun generate(telemetry: SolarTelemetry): Loadable<List<Insight>> =
        FixtureData.insightsLoadable(FixtureState.current())

    override suspend fun generateFor(forecast: ProductionForecast): Loadable<List<Insight>> =
        FixtureData.insightsLoadable(FixtureState.current())
}

/** Connection, SVC-19. Retry is bounded; a fixture retry cannot invent a live link. */
class FixtureConnectionService : ConnectionService {

    override val link: Flow<Loadable<com.helios.core.data.service.ConnectionSnapshot>> =
        scenarioFlow { FixtureData.connectionSnapshot(it) }

    override suspend fun test(candidate: ConnectionConfig): Loadable<com.helios.core.data.service.ConnectionSnapshot> =
        FixtureData.connectionSnapshot(FixtureState.current())

    override suspend fun connect(candidate: ConnectionConfig): Loadable<com.helios.core.data.service.ConnectionSnapshot> =
        FixtureData.connectionSnapshot(FixtureState.current())

    override suspend fun disconnect() = Unit

    override fun retrySchedule(): List<Long> = FixtureData.retryScheduleMs
}

/** Location, SVC-05. Denied is a normal, usable result (F9). */
class FixtureLocationService : LocationService {

    private val selected = MutableStateFlow<Location?>(null)

    /** Clear the transient fix, so one scenario cannot leak a selected location into the next. */
    fun reset() {
        selected.value = null
    }

    override val location: Flow<Loadable<Location>> = FixtureState.scenario.map { scenario ->
        selected.value?.let { Loadable.Ready(it, SourceMeta(Freshness.LIVE, System.currentTimeMillis(), 0)) }
            ?: FixtureData.location(scenario)
    }

    override suspend fun useMyLocation(): Loadable<Location> {
        val scenario = FixtureState.current()
        if (scenario == FixtureScenario.LOADING) {
            return Loadable.Loading
        }
        if (scenario == FixtureScenario.DENIED_PERMISSION) {
            return Loadable.Failed(
                ServiceFailure.of(FailureKind.LOCATION_DENIED, detail = "ACCESS_FINE_LOCATION denied"),
                SourceMeta(Freshness.LIVE, System.currentTimeMillis(), 0)
            )
        }
        selected.value = FixtureData.defaultLocation.copy(source = "browser")
        return Loadable.Ready(selected.value!!, SourceMeta(Freshness.LIVE, System.currentTimeMillis(), 0))
    }

    override suspend fun setManual(lat: Double, lng: Double, label: String): Loadable<Location> {
        selected.value = Location(lat = lat, lng = lng, label = label, source = "manual")
        return Loadable.Ready(selected.value!!, SourceMeta(Freshness.LIVE, System.currentTimeMillis(), 0))
    }

    override suspend fun label(lat: Double, lng: Double): String =
        selected.value?.label ?: "%.2f, %.2f".format(java.util.Locale.US, lat, lng)
}

/** Theme, SVC-18. */
class FixtureThemeService : ThemeService {

    private val mode = MutableStateFlow(HeliosThemeMode.DARK)
    val modeState: StateFlow<HeliosThemeMode> = mode.asStateFlow()

    override val themeMode: Flow<Loadable<HeliosThemeMode>> = FixtureState.scenario.map { scenario ->
        if (scenario == FixtureScenario.LOADING) Loadable.Loading
        else Loadable.Ready(mode.value, SourceMeta(Freshness.LIVE, System.currentTimeMillis(), 0))
    }

    override suspend fun setTheme(mode: HeliosThemeMode) {
        this.mode.value = mode
    }
}

/** Brand, SVC-10 / SVC-11. Registry ids match the PWA so a shared link resolves correctly. */
class FixtureBrandService : BrandService {

    private val current = MutableStateFlow(FixtureData.heliosBrand)

    override val brand: Flow<Loadable<Brand>> = FixtureState.scenario.map { scenario ->
        if (scenario == FixtureScenario.LOADING) Loadable.Loading
        else Loadable.Ready(current.value, SourceMeta(Freshness.LIVE, System.currentTimeMillis(), 0))
    }

    override val registry: List<Brand> = FixtureData.brands

    override fun resolve(id: String?): Brand =
        FixtureData.brands.firstOrNull { it.id == id?.lowercase() } ?: FixtureData.heliosBrand

    override suspend fun select(id: String): Loadable<Brand> {
        current.value = resolve(id)
        return Loadable.Ready(current.value, SourceMeta(Freshness.LIVE, System.currentTimeMillis(), 0))
    }

    override fun accentOverride(): Pair<String, String> = current.value.accent to current.value.accentLight
}

/**
 * Share, SVC-12 to SVC-15.
 *
 * The codec is not re-implemented here: the fixture adapter delegates to
 * `ShareRepository`, so a fixture link and a device link are produced by one
 * implementation. What the fixture adds is the deterministic outcome the gallery needs.
 */
class FixtureShareService : ShareService {

    override fun encode(payload: SnapshotPayload): String = ShareRepository.encodeSnapshot(payload)

    override fun decode(encoded: String): SnapshotPayload? = ShareRepository.decodeSnapshot(encoded)

    override fun buildSnapshot(
        telemetry: SolarTelemetry,
        locationLabel: String,
        forecastDays: List<Double>?,
        brandId: String?
    ): SnapshotPayload = ShareRepository.buildSnapshot(telemetry, locationLabel, forecastDays, brandId)

    override fun buildUrl(payload: SnapshotPayload): String = ShareRepository.buildShareUrl(payload)

    override fun prepare(
        telemetry: SolarTelemetry,
        locationLabel: String,
        forecastDays: List<Double>?,
        brandId: String?
    ): ShareOutcome = try {
        val payload = buildSnapshot(telemetry, locationLabel, forecastDays, brandId)
        ShareOutcome.Ready(encode(payload), buildUrl(payload), payload)
    } catch (error: RuntimeException) {
        ShareOutcome.Failed(ServiceFailure.of(FailureKind.UNKNOWN, detail = error.message))
    }

    override fun targets(): List<ShareTarget> = FixtureData.shareTargets()
}

/** The fixture family, in the order the gallery lists it. */
class FixtureServicesFamily private constructor() : AppServices {

    override val telemetry: TelemetryService = FixtureTelemetryService()
    override val series: SeriesService = FixtureSeriesService()
    override val forecast: ForecastService = FixtureForecastService()
    override val insights: InsightsService = FixtureInsightsService()
    override val connection: ConnectionService = FixtureConnectionService()
    override val location: LocationService = FixtureLocationService()
    override val theme: ThemeService = FixtureThemeService()
    override val brand: BrandService = FixtureBrandService()
    override val share: ShareService = FixtureShareService()

    /** Switch every adapter at once, so a screen cannot be half live and half offline. */
    fun select(scenario: FixtureScenario) {
        FixtureState.select(scenario)
        (location as? FixtureLocationService)?.reset()
    }

    fun current(): FixtureScenario = FixtureState.current()

    fun setDemoMode(enabled: Boolean) {
        (telemetry as? FixtureTelemetryService)?.setDemo(enabled)
    }

    companion object {
        /** The process-wide fixture family. [ServiceGraph] returns it until the app installs device adapters. */
        val INSTANCE: FixtureServicesFamily by lazy { FixtureServicesFamily() }
    }
}
