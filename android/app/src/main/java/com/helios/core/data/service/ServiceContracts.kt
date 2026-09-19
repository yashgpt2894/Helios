package com.helios.core.data.service

import com.helios.core.data.fixture.FixtureServicesFamily

/**
 * The data contracts every screen renders against.
 *
 * A screen never talks to a transport. It reads one of the service interfaces in this
 * package and gets a [Loadable], which carries the value *and* the state the UI must
 * show: loading, ready, empty, partial, stale, offline, error or denied. Two families of
 * adapter implement these interfaces: `core/data/fixture` (deterministic, offline, used
 * by the component gallery and by any screen before hardware exists) and the device
 * adapters that wrap the repositories in `core/data/repository`.
 *
 * Nothing here touches the network, and nothing here is PWA-visible. Snapshot payload
 * production stays in [ShareService] and keeps the v1 shape byte-for-byte.
 */

/**
 * How old the displayed reading is. Thresholds are fixed by `design/ux-flows.md`
 * section 0: live at 5 s or less, aging to 15 s, stale past 15 s.
 */
enum class Freshness(val ageLimitMs: Long) {
    LIVE(5_000),
    AGING(15_000),
    STALE(Long.MAX_VALUE),
    OFFLINE(0),
    DEMO(0);

    companion object {
        fun forAge(ageMs: Long): Freshness = when {
            ageMs <= LIVE.ageLimitMs -> LIVE
            ageMs <= AGING.ageLimitMs -> AGING
            else -> STALE
        }
    }
}

/** Where a value came from and how much to trust it. */
data class SourceMeta(
    val freshness: Freshness,
    val fetchedAt: Long,
    val ageMs: Long,
    val partial: Boolean = false,
    /** Field names that did not report, for F7 "showing the last good reading". */
    val missing: List<String> = emptyList(),
    /** Short qualifier shown next to the value, for example "Demo system". */
    val note: String? = null,
    val simulated: Boolean = false
) {
    /** [ageMs] recomputed against a newer clock, for a running freshness stamp. */
    fun aged(nowMs: Long): SourceMeta = copy(ageMs = (nowMs - fetchedAt).coerceAtLeast(0))
}

/** The eight view states a screen can be in, derived from one [Loadable]. */
enum class UiState { LOADING, READY, EMPTY, PARTIAL, STALE, OFFLINE, ERROR, DENIED }

/**
 * The failure classes F1-F9 of `design/ux-flows.md` FLW-03. Each has its own words and
 * its own offered action, because the user cannot fix a wrong unit id the same way they
 * fix a disconnected Wi-Fi.
 */
enum class FailureKind(val code: String, val defaultMessage: String, val defaultAction: String) {
    NO_NETWORK("F1", "Not on the same network as the inverter", "Open Wi-Fi settings"),
    TIMEOUT("F2", "Inverter not responding", "Retry now"),
    REFUSED("F3", "Port refused the connection", "Edit port"),
    WRONG_UNIT_ID("F4", "Answered, but the unit id has no SunSpec model", "Edit unit id"),
    NOT_SUNSPEC("F5", "This device does not speak SunSpec Modbus", "Read the protocol note"),
    MULTIPLE_MASTERS("F6", "Another Modbus master is connected", "Retry now"),
    MALFORMED_RESPONSE("F7", "Incomplete data from the inverter", "Retry now"),
    FORECAST_UNAVAILABLE("F8", "Forecast unavailable", "Retry"),
    LOCATION_DENIED("F9", "Using the default location", "Choose a place by name"),
    UNKNOWN("F0", "Something went wrong", "Retry now");

    /** True when the user must grant something before a retry can help. */
    val needsPermission: Boolean
        get() = this == LOCATION_DENIED

    /** True when the link itself is down, so values must be labelled last known. */
    val linkDown: Boolean
        get() = this in setOf(NO_NETWORK, TIMEOUT, REFUSED, WRONG_UNIT_ID, NOT_SUNSPEC, MULTIPLE_MASTERS)
}

/** One classified failure, ready to render. */
data class ServiceFailure(
    val kind: FailureKind,
    val message: String = kind.defaultMessage,
    val action: String = kind.defaultAction,
    /** Diagnostic detail for the Connection sheet: host, port, unit id, attempt count. */
    val detail: String? = null,
    val attempts: Int = 0,
    val lastGoodAt: Long? = null
) {
    companion object {
        fun of(kind: FailureKind, detail: String? = null, lastGoodAt: Long? = null, attempts: Int = 0) =
            ServiceFailure(kind = kind, detail = detail, lastGoodAt = lastGoodAt, attempts = attempts)
    }
}

/**
 * A value plus the state around it. [Loading] never replaces a shown value: a screen with
 * a value keeps it and dims it, so the ticker never resets to a spinner.
 */
sealed interface Loadable<out T> {

    data object Loading : Loadable<Nothing>

    data class Ready<T>(val value: T, val meta: SourceMeta) : Loadable<T>

    data class Empty(
        val meta: SourceMeta,
        val message: String,
        val actionLabel: String? = null
    ) : Loadable<Nothing>

    data class Failed(
        val failure: ServiceFailure,
        val meta: SourceMeta? = null
    ) : Loadable<Nothing>

    companion object {
        fun <T> ready(value: T, meta: SourceMeta): Loadable<T> = Ready(value, meta)
    }
}

/** The state a screen shows, derived from the data state. */
fun <T> Loadable<T>.uiState(): UiState = when (this) {
    is Loadable.Loading -> UiState.LOADING
    is Loadable.Ready -> when {
        meta.partial -> UiState.PARTIAL
        meta.freshness == Freshness.STALE -> UiState.STALE
        meta.freshness == Freshness.OFFLINE -> UiState.OFFLINE
        else -> UiState.READY
    }
    is Loadable.Empty -> UiState.EMPTY
    is Loadable.Failed -> when {
        failure.kind.needsPermission -> UiState.DENIED
        failure.kind.linkDown -> UiState.OFFLINE
        else -> UiState.ERROR
    }
}

fun <T> Loadable<T>.valueOrNull(): T? = (this as? Loadable.Ready)?.value

fun Loadable<*>.metaOrNull(): SourceMeta? = when (this) {
    is Loadable.Ready -> meta
    is Loadable.Empty -> meta
    is Loadable.Failed -> meta
    Loadable.Loading -> null
}

fun Loadable<*>.freshnessOrNull(): Freshness? = metaOrNull()?.freshness

/** True while the link is down or the value is too old to call live. */
fun Loadable<*>.isDimmed(): Boolean {
    val state = uiState()
    return state == UiState.STALE || state == UiState.OFFLINE || state == UiState.PARTIAL
}

/** Human age for the freshness stamp. Never says live when it is not. */
fun Loadable<*>.freshnessLabel(nowMs: Long = System.currentTimeMillis()): String {
    val meta = metaOrNull()
    if (this is Loadable.Failed) return failure.kind.defaultMessage
    if (meta == null) return "Loading"
    if (meta.simulated) return "Demo data"
    val age = (nowMs - meta.fetchedAt).coerceAtLeast(0)
    return when (Freshness.forAge(age)) {
        Freshness.LIVE -> "Live"
        Freshness.AGING -> "Updated ${age / 1000} s ago"
        else -> "Stale ${age / 1000} s"
    }
}

/**
 * Every service a screen may need. One container keeps the swap in one place: fixtures
 * for design and previews, device adapters in the running app.
 */
interface AppServices {
    val telemetry: TelemetryService
    val series: SeriesService
    val forecast: ForecastService
    val insights: InsightsService
    val connection: ConnectionService
    val location: LocationService
    val theme: ThemeService
    val brand: BrandService
    val share: ShareService
}

/**
 * Swappable service locator. The default is the fixture family, so any screen can be
 * rendered with no network and no hardware. The app replaces it once with the device
 * family; a later step moves this to Hilt without changing the interfaces.
 */
object ServiceGraph {

    @Volatile
    private var installed: AppServices? = null

    val current: AppServices
        get() = installed ?: FixtureServicesFamily.INSTANCE

    fun install(services: AppServices) {
        installed = services
    }

    /** Back to fixtures. Used by tests and by the gallery. */
    fun reset() {
        installed = null
    }
}
