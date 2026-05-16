package com.helios.core.data.repository

import com.helios.core.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Forecast repository stub. Open-Meteo fetch implementation is out of M4 scope.
 */
object ForecastRepository {

    enum class ForecastStatus { idle, loading, ready, error }

    private val _forecast = MutableStateFlow<ProductionForecast?>(null)
    val forecastFlow: Flow<ProductionForecast?> = _forecast.asStateFlow()

    private val _status = MutableStateFlow(ForecastStatus.idle)
    val statusFlow: Flow<ForecastStatus> = _status.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val errorFlow: Flow<String?> = _error.asStateFlow()

    suspend fun loadForecast(location: Location): ProductionForecast {
        _status.value = ForecastStatus.loading
        kotlinx.coroutines.delay(500)
        val days = (0..6).map { i ->
            ForecastDay(
                date = "2026-05-${16 + i}",
                weatherCode = 0,
                condition = WeatherCondition.clear,
                conditionLabel = "Clear",
                tempHighC = 28.0 + i,
                tempLowC = 18.0 + i,
                precipitationMm = 0.0,
                shortwaveRadiationMJ = 17.28,
                cloudCoverPct = 0.0,
                expectedKwh = 37.79,
                expectedKwhVsTypical = 5.0
            )
        }
        val forecast = ProductionForecast(
            fetchedAt = System.currentTimeMillis(),
            location = location,
            days = days,
            totalKwh = days.sumOf { it.expectedKwh },
            vsLastWeekPct = 13.44
        )
        _forecast.value = forecast
        _status.value = ForecastStatus.ready
        return forecast
    }
}
