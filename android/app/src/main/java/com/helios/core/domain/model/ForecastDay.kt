package com.helios.core.domain.model

data class ForecastDay(
    val date: String,
    val weatherCode: Int,
    val condition: WeatherCondition,
    val conditionLabel: String,
    val tempHighC: Double,
    val tempLowC: Double,
    val precipitationMm: Double,
    val shortwaveRadiationMJ: Double,
    val cloudCoverPct: Double,
    val expectedKwh: Double,
    val expectedKwhVsTypical: Double
)
