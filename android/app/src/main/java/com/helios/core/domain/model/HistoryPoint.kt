package com.helios.core.domain.model

/**
 * Half-hour history data point matched verbatim to PWA HistoryPoint.
 */
data class HistoryPoint(
    val t: Double,
    val productionW: Double,
    val consumptionW: Double,
    val batteryW: Double,
    val gridW: Double,
    val irradianceWm2: Double
)
