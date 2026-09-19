package com.helios.core.data.service

import com.helios.core.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Forecast location (SVC-05). The permission is requested from the "Use my location"
 * action only, never at launch (DESIGN.md 9), and a denied result is a usable state:
 * the default location stays and the note explains it (F9).
 */
interface LocationService {

    val location: Flow<Loadable<Location>>

    suspend fun useMyLocation(): Loadable<Location>

    suspend fun setManual(lat: Double, lng: Double, label: String): Loadable<Location>

    /** Reverse geocode for a coordinate pair; falls back to a coordinate label. */
    suspend fun label(lat: Double, lng: Double): String
}
