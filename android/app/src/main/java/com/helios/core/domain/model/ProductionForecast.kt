package com.helios.core.domain.model

data class ProductionForecast(
    val fetchedAt: Long,
    val location: Location,
    val days: List<ForecastDay>,
    val totalKwh: Double,
    val vsLastWeekPct: Double
)
