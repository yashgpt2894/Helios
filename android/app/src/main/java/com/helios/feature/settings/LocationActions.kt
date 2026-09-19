package com.helios.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.helios.core.data.service.FailureKind
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.ServiceFailure
import com.helios.core.domain.model.Location
import java.util.Locale

/**
 * The location actions first run (SCR-06) and Settings (SCR-13) both need.
 *
 * One place turns a [Loadable] into a location outcome, so the two screens cannot disagree
 * about what a denied permission is, and one place opens the Android app settings page that
 * the denied state offers (ACT-023, ACT-070). The permission itself is requested by the
 * caller at the moment of the action; nothing here prompts on its own.
 */

/** What the last "use my location" attempt produced. */
sealed interface LocationOutcome {

    data object Resolving : LocationOutcome

    data class Resolved(val location: Location) : LocationOutcome

    /** Permission denied or no fix returned: a usable state, never an error wall (F9). */
    data class Denied(val failure: ServiceFailure) : LocationOutcome

    /** A failure that a retry can plausibly fix: the geocoder or the transport. */
    data class Failed(val failure: ServiceFailure) : LocationOutcome
}

/** One mapping from service state to screen state, for both hosts. */
fun locationOutcome(loadable: Loadable<Location>): LocationOutcome = when (loadable) {
    is Loadable.Ready -> LocationOutcome.Resolved(loadable.value)
    is Loadable.Loading -> LocationOutcome.Resolving
    is Loadable.Failed -> if (loadable.failure.kind.needsPermission || loadable.failure.kind == FailureKind.LOCATION_DENIED) {
        LocationOutcome.Denied(loadable.failure)
    } else {
        LocationOutcome.Failed(loadable.failure)
    }
    is Loadable.Empty -> LocationOutcome.Failed(
        ServiceFailure.of(FailureKind.LOCATION_DENIED, detail = loadable.message)
    )
}

/** Coordinates as the app shows them: four decimals, Locale.US, so a capture reads the same. */
fun coordinateLabel(lat: Double, lng: Double): String =
    "%.4f, %.4f".format(Locale.US, lat, lng)

/** Where a location came from, in words. */
fun locationSourceLabel(source: String): String = when (source) {
    "default" -> "Default location"
    "browser", "device" -> "This device"
    "manual" -> "Entered by hand"
    else -> source
}

/** Opens this app's Android settings page, which is where a denied permission is reversed. */
fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
