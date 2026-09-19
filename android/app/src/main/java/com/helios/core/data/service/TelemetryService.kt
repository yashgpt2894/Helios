package com.helios.core.data.service

import com.helios.core.domain.model.HistoryPoint
import com.helios.core.domain.model.SolarTelemetry
import kotlinx.coroutines.flow.Flow

/**
 * Live inverter telemetry (SVC-01). One reading per poll; the flow re-emits on every
 * poll and carries the freshness with the value.
 */
interface TelemetryService {

    val telemetry: Flow<Loadable<SolarTelemetry>>

    /** Read once, without waiting for the flow. */
    suspend fun read(): Loadable<SolarTelemetry>

    /** Ask for a fresh reading now, for a user-initiated retry. */
    suspend fun refresh(): Loadable<SolarTelemetry>

    /** Switch the source between the simulated system and real hardware (SVC-01). */
    suspend fun setDemoMode(enabled: Boolean)

    val isDemo: Boolean
}

/** One second-resolution sample for the live ticker and its short trail. */
data class LivePoint(
    val t: Long,
    val acPowerW: Double,
    val homeLoadW: Double,
    val batteryW: Double,
    val gridW: Double,
    val socPct: Double
)

/** One day of produced versus consumed energy for the week chart. */
data class WeekPoint(
    val dayLabel: String,
    val dateIso: String,
    val producedKwh: Double,
    val consumedKwh: Double
)

/**
 * The three series the charts draw: today at half-hour resolution (SVC-02), the live
 * trail (SVC-01 tail), and the week (SVC-03). They are separate flows because they fail
 * separately: a missing week reading must not blank the today curve.
 */
interface SeriesService {

    val todaySeries: Flow<Loadable<List<HistoryPoint>>>

    val liveSeries: Flow<Loadable<List<LivePoint>>>

    val weekSeries: Flow<Loadable<List<WeekPoint>>>

    suspend fun refresh()
}
