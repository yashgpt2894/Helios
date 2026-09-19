package com.helios.core.data.service

import com.helios.core.domain.model.Location
import com.helios.core.domain.model.ProductionForecast
import kotlinx.coroutines.flow.Flow

/**
 * Seven-day production forecast (SVC-04, SVC-08). The real implementation calls
 * Open-Meteo; failure is F8 and is inline in the forecast section, never a full-screen
 * error.
 */
interface ForecastService {

    val forecast: Flow<Loadable<ProductionForecast>>

    suspend fun load(location: Location): Loadable<ProductionForecast>

    suspend fun refresh(): Loadable<ProductionForecast>

    /** The five days the glance strip shows, or fewer when the response was partial. */
    fun glanceDays(count: Int = 5): Loadable<List<com.helios.core.domain.model.ForecastDay>>
}
