package com.helios.core.data.service

import android.content.Context
import com.helios.core.data.repository.BrandRepository
import com.helios.core.data.repository.ForecastRepository
import com.helios.core.data.repository.InsightsRepository
import com.helios.core.data.repository.LocationRepository
import com.helios.core.data.repository.ShareRepository
import com.helios.core.data.repository.TelemetryRepository
import com.helios.core.data.repository.ThemeRepository
import com.helios.core.domain.model.Brand
import com.helios.core.domain.model.ConnectionConfig
import com.helios.core.domain.model.ForecastDay
import com.helios.core.domain.model.HistoryPoint
import com.helios.core.domain.model.Insight
import com.helios.core.domain.model.Location
import com.helios.core.domain.model.ProductionForecast
import com.helios.core.domain.model.SolarTelemetry
import com.helios.core.domain.model.SnapshotPayload
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Device adapters: the same interfaces as the fixture family, backed by the repositories
 * that already exist in `core/data/repository`.
 *
 * These are the adapters the running app installs through [ServiceGraph]. They are thin
 * on purpose: each one wraps an existing repository, adds the state the UI needs, and
 * stops there. Two of them are interim and say so in their KDoc: the connection store
 * has no Room database yet (SVC-19), and the forecast still returns the stub day list
 * until the Open-Meteo call is wired (SVC-04).
 */

/** Telemetry polled from the inverter repository (SVC-01). */
class RepositoryTelemetryService(private val pollIntervalMs: Long = 2_000) : TelemetryService {

    private val demoMode = MutableStateFlow(true)

    override val telemetry: Flow<Loadable<SolarTelemetry>> = flow {
        while (true) {
            emit(read())
            delay(pollIntervalMs)
        }
    }

    override suspend fun read(): Loadable<SolarTelemetry> {
        val reading = TelemetryRepository.readTelemetry()
        return Loadable.Ready(
            reading,
            SourceMeta(
                freshness = Freshness.LIVE,
                fetchedAt = reading.timestamp,
                ageMs = (System.currentTimeMillis() - reading.timestamp).coerceAtLeast(0),
                simulated = demoMode.value,
                note = if (demoMode.value) "Demo system" else null
            )
        )
    }

    override suspend fun refresh(): Loadable<SolarTelemetry> = read()

    override suspend fun setDemoMode(enabled: Boolean) {
        demoMode.value = enabled
    }

    override val isDemo: Boolean
        get() = demoMode.value
}

/** Today, live and week series from the simulation maths (SVC-02, SVC-03). */
class RepositorySeriesService(private val pollIntervalMs: Long = 2_000) : SeriesService {

    private val live = MutableStateFlow<List<LivePoint>>(emptyList())

    override val todaySeries: Flow<Loadable<List<HistoryPoint>>> = flow {
        while (true) {
            val series = TelemetryRepository.buildTodaySeries()
            emit(
                if (series.isEmpty()) {
                    Loadable.Empty(metaNow(partial = false), "No production recorded yet today", "Check the connection")
                } else {
                    Loadable.Ready(series, metaNow(partial = false))
                }
            )
            delay(pollIntervalMs * 15)
        }
    }

    override val liveSeries: Flow<Loadable<List<LivePoint>>> = flow {
        while (true) {
            val reading = TelemetryRepository.readTelemetry()
            val next = live.value + LivePoint(
                t = reading.timestamp,
                acPowerW = reading.acPowerW,
                homeLoadW = reading.homeLoadW,
                batteryW = reading.batteryPowerW,
                gridW = reading.gridExportW - reading.gridImportW,
                socPct = reading.batterySoc
            )
            live.value = next.takeLast(30)
            emit(Loadable.Ready(live.value, metaNow(partial = false)))
            delay(pollIntervalMs)
        }
    }

    override val weekSeries: Flow<Loadable<List<WeekPoint>>> = flow {
        while (true) {
            val days = java.time.LocalDate.now()
            val week = TelemetryRepository.buildWeekSeries().mapIndexed { index, (label, produced, consumed) ->
                WeekPoint(
                    dayLabel = label,
                    dateIso = days.minusDays((6 - index).toLong()).toString(),
                    producedKwh = produced,
                    consumedKwh = consumed
                )
            }
            emit(Loadable.Ready(week, metaNow(partial = false, ageMs = 30_000)))
            delay(60_000)
        }
    }

    override suspend fun refresh() {
        TelemetryRepository.tick()
    }

    private fun metaNow(partial: Boolean, ageMs: Long = 0): SourceMeta {
        val now = System.currentTimeMillis()
        return SourceMeta(Freshness.forAge(ageMs), now - ageMs, ageMs, partial = partial)
    }
}

/** Forecast from the forecast repository (SVC-04). */
class RepositoryForecastService : ForecastService {

    override val forecast: Flow<Loadable<ProductionForecast>> =
        ForecastRepository.forecastFlow.map { value ->
            if (value == null) {
                Loadable.Empty(meta(null), "No forecast yet", "Retry")
            } else {
                Loadable.Ready(value, meta(value.fetchedAt))
            }
        }

    /** The last requested location; a refresh without one cannot be answered honestly. */
    private var lastLocation: Location? = null

    override suspend fun load(location: Location): Loadable<ProductionForecast> {
        lastLocation = location
        return try {
            val forecast = ForecastRepository.loadForecast(location)
            Loadable.Ready(forecast, meta(forecast.fetchedAt))
        } catch (error: RuntimeException) {
            Loadable.Failed(ServiceFailure.of(FailureKind.FORECAST_UNAVAILABLE, detail = error.message))
        }
    }

    override suspend fun refresh(): Loadable<ProductionForecast> {
        val location = lastLocation
            ?: return Loadable.Empty(meta(null), "No location set", "Choose a place by name")
        return load(location)
    }

    override fun glanceDays(count: Int): Loadable<List<ForecastDay>> {
        val value = ForecastRepository.currentForecast
        return if (value == null) {
            Loadable.Empty(meta(null), "No forecast yet", "Retry")
        } else {
            Loadable.Ready(value.days.take(count), meta(value.fetchedAt))
        }
    }

    private fun meta(fetchedAt: Long?): SourceMeta {
        val now = System.currentTimeMillis()
        return if (fetchedAt == null) {
            SourceMeta(Freshness.LIVE, now, 0)
        } else {
            val age = (now - fetchedAt).coerceAtLeast(0)
            SourceMeta(Freshness.forAge(age), fetchedAt, age)
        }
    }
}

/** Insights from the insight templates (SVC-07, SVC-09), with the suppression rule. */
class RepositoryInsightsService : InsightsService {

    private val state = MutableStateFlow<Loadable<List<Insight>>>(
        Loadable.Empty(meta(), "No advisories for the current reading", null)
    )

    override val insights: Flow<Loadable<List<Insight>>> = state.asStateFlow()

    override suspend fun generate(telemetry: SolarTelemetry): Loadable<List<Insight>> {
        val age = (System.currentTimeMillis() - telemetry.timestamp).coerceAtLeast(0)
        if (Freshness.forAge(age) == Freshness.STALE) {
            return Loadable.Empty(
                meta(age, note = "Advisories paused"),
                "Advisories are paused while the reading is stale",
                "Retry now"
            )
        }
        val list = InsightsRepository.generateInsights(telemetry)
        val result: Loadable<List<Insight>> = if (list.isEmpty()) {
            Loadable.Empty(meta(age), "No advisories for the current reading", null)
        } else {
            Loadable.Ready(list, meta(age))
        }
        state.value = result
        return result
    }

    override suspend fun generateFor(forecast: ProductionForecast): Loadable<List<Insight>> {
        val list = InsightsRepository.generateForecastInsights(forecast)
        val result: Loadable<List<Insight>> = if (list.isEmpty()) {
            Loadable.Empty(meta(), "No forecast advisories", null)
        } else {
            Loadable.Ready(list, meta())
        }
        state.value = result
        return result
    }

    private fun meta(ageMs: Long = 0, note: String? = null): SourceMeta {
        val now = System.currentTimeMillis()
        return SourceMeta(Freshness.forAge(ageMs), now - ageMs, ageMs, note = note)
    }
}

/**
 * Connection store.
 *
 * Interim adapter: it holds the config in memory and does not persist it. The real
 * implementation is SVC-19 (Room-backed `ConnectionRepository`) and must replace this,
 * which is why the interface, not this class, is what screens depend on.
 */
class InMemoryConnectionService(
    initial: ConnectionConfig = ConnectionConfig(
        id = 1,
        protocol = "sunspec-modbus-tcp",
        host = "192.168.1.42",
        port = 502,
        unitId = 1,
        pollIntervalMs = 2_000,
        status = "disconnected"
    )
) : ConnectionService {

    private val config = MutableStateFlow(initial)
    private val snapshot = MutableStateFlow<Loadable<ConnectionSnapshot>>(
        Loadable.Ready(
            ConnectionSnapshot(initial, LinkState.DISCONNECTED, System.currentTimeMillis(), null, 0),
            SourceMeta(Freshness.OFFLINE, System.currentTimeMillis(), 0)
        )
    )

    override val link: Flow<Loadable<ConnectionSnapshot>> = snapshot.asStateFlow()

    override suspend fun test(candidate: ConnectionConfig): Loadable<ConnectionSnapshot> = attempt(candidate, persist = false)

    override suspend fun connect(candidate: ConnectionConfig): Loadable<ConnectionSnapshot> = attempt(candidate, persist = true)

    override suspend fun disconnect() {
        val now = System.currentTimeMillis()
        snapshot.value = Loadable.Ready(
            ConnectionSnapshot(config.value.copy(status = "disconnected"), LinkState.DISCONNECTED, now, null, 0),
            SourceMeta(Freshness.OFFLINE, now, 0)
        )
    }

    override fun retrySchedule(): List<Long> = listOf(1_000L, 2_000L, 4_000L, 30_000L, 60_000L)

    private fun attempt(candidate: ConnectionConfig, persist: Boolean): Loadable<ConnectionSnapshot> {
        val now = System.currentTimeMillis()
        val reading = TelemetryRepository.readTelemetry()
        if (persist) config.value = candidate.copy(status = "simulated")
        val connected = ConnectionSnapshot(
            config = config.value,
            state = LinkState.SIMULATED,
            lastCheckedAt = now,
            lastGoodAt = reading.timestamp,
            attempt = 0,
            identifiedAs = reading.model,
            firmware = reading.firmware
        )
        val meta = SourceMeta(Freshness.LIVE, now, 0, simulated = true, note = "Demo system")
        return Loadable.Ready(connected, meta).also { snapshot.value = it }
    }
}

/** Location from the fused provider (SVC-05). Permission is requested by the caller, not here. */
class FusedLocationService(private val context: Context) : LocationService {

    private val repository = LocationRepository(context)

    override val location: Flow<Loadable<Location>> = repository.locationFlow.map { it ->
        val now = System.currentTimeMillis()
        Loadable.Ready(it, SourceMeta(Freshness.LIVE, now, 0))
    }

    override suspend fun useMyLocation(): Loadable<Location> = try {
        val value = repository.useMyLocation()
        if (value == null) {
            Loadable.Failed(ServiceFailure.of(FailureKind.LOCATION_DENIED, detail = "no fix returned"))
        } else {
            val now = System.currentTimeMillis()
            Loadable.Ready(value, SourceMeta(Freshness.LIVE, now, 0))
        }
    } catch (error: SecurityException) {
        Loadable.Failed(ServiceFailure.of(FailureKind.LOCATION_DENIED, detail = error.message))
    }

    override suspend fun setManual(lat: Double, lng: Double, label: String): Loadable<Location> {
        val now = System.currentTimeMillis()
        return Loadable.Ready(Location(1, lat, lng, label, "manual"), SourceMeta(Freshness.LIVE, now, 0))
    }

    override suspend fun label(lat: Double, lng: Double): String =
        "%.2f, %.2f".format(java.util.Locale.US, lat, lng)
}

/** Theme from DataStore (SVC-18). */
class DataStoreThemeService(private val context: Context) : ThemeService {

    private val repository = ThemeRepository(context)

    override val themeMode: Flow<Loadable<HeliosThemeMode>> = repository.themeFlow.map { mode ->
        Loadable.Ready(mode.toServiceMode(), SourceMeta(Freshness.LIVE, System.currentTimeMillis(), 0))
    }

    override suspend fun setTheme(mode: HeliosThemeMode) {
        repository.setTheme(mode.toRepositoryMode())
    }

    private fun ThemeRepository.ThemeMode.toServiceMode(): HeliosThemeMode = when (this) {
        ThemeRepository.ThemeMode.auto -> HeliosThemeMode.AUTO
        ThemeRepository.ThemeMode.light -> HeliosThemeMode.LIGHT
        ThemeRepository.ThemeMode.dark -> HeliosThemeMode.DARK
    }

    private fun HeliosThemeMode.toRepositoryMode(): ThemeRepository.ThemeMode = when (this) {
        HeliosThemeMode.AUTO -> ThemeRepository.ThemeMode.auto
        HeliosThemeMode.LIGHT -> ThemeRepository.ThemeMode.light
        HeliosThemeMode.DARK -> ThemeRepository.ThemeMode.dark
    }
}

/** Brand from the registry (SVC-10, SVC-11). */
class RegistryBrandService : BrandService {

    private val current = MutableStateFlow(BrandRepository.current())

    override val brand: Flow<Loadable<Brand>> = current.asStateFlow().map {
        Loadable.Ready(it, SourceMeta(Freshness.LIVE, System.currentTimeMillis(), 0))
    }

    override val registry: List<Brand> = BrandRepository.all()

    override fun resolve(id: String?): Brand = BrandRepository.resolve(id)

    override suspend fun select(id: String): Loadable<Brand> {
        val brand = BrandRepository.resolve(id)
        current.value = brand
        return Loadable.Ready(brand, SourceMeta(Freshness.LIVE, System.currentTimeMillis(), 0))
    }

    override fun accentOverride(): Pair<String, String> =
        current.value.accent to current.value.accentLight
}

/** Snapshot share from the repository implementation (SVC-12 to SVC-15). */
class RepositoryShareService : ShareService {

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

    override fun targets(): List<ShareTarget> = listOf(
        ShareTarget("clipboard", "Copy link", true),
        ShareTarget("messages", "Messages", true),
        ShareTarget("email", "Email", true)
    )
}

/** The device family the app installs. */
class HeliosDeviceServices(context: Context) : AppServices {
    override val telemetry: TelemetryService = RepositoryTelemetryService()
    override val series: SeriesService = RepositorySeriesService()
    override val forecast: ForecastService = RepositoryForecastService()
    override val insights: InsightsService = RepositoryInsightsService()
    override val connection: ConnectionService = InMemoryConnectionService()
    override val location: LocationService = FusedLocationService(context.applicationContext)
    override val theme: ThemeService = DataStoreThemeService(context.applicationContext)
    override val brand: BrandService = RegistryBrandService()
    override val share: ShareService = RepositoryShareService()
}
