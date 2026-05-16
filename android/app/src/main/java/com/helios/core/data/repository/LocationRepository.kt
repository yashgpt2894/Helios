package com.helios.core.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.helios.core.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Location repository using Fused Location Provider.
 */
class LocationRepository(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _location = MutableStateFlow(
        Location(lat = 37.7749, lng = -122.4194, label = "San Francisco", source = "default")
    )
    val locationFlow: Flow<Location> = _location.asStateFlow()

    @SuppressLint("MissingPermission")
    suspend fun useMyLocation(): Location? {
        return try {
            val loc = suspendCoroutine { cont ->
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY, null
                ).addOnSuccessListener { result ->
                    cont.resume(result)
                }.addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
            }
            if (loc != null) {
                val newLoc = Location(
                    lat = loc.latitude,
                    lng = loc.longitude,
                    label = "%.2f, %.2f".format(loc.latitude, loc.longitude),
                    source = "browser"
                )
                _location.value = newLoc
                newLoc
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
