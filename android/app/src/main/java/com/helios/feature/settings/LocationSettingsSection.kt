package com.helios.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.helios.core.data.service.AppServices
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.valueOrNull
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.DeniedPermissionState
import com.helios.core.designsystem.component.HeliosErrorState
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.HeliosPrimaryButton
import com.helios.core.designsystem.component.HeliosSecondaryButton
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.Location
import kotlinx.coroutines.launch

/**
 * SCR-13, Settings — location.
 *
 * Three honest ways to set the forecast location: the device fix behind the "Use my
 * location" action (the permission is requested at that tap and nowhere else), typed
 * coordinates, and doing nothing, which keeps the default. A denied permission is a state
 * with words and a way to the Android settings page, not an error wall (F9).
 *
 * Every accepted change re-requests the forecast, so the effect of the change is reported on
 * screen instead of being assumed (ACT-069).
 */
@Composable
fun LocationSettingsSection(
    services: AppServices,
    onBack: () -> Unit,
    onSnack: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val stored by services.location.location.collectAsState(initial = Loadable.Loading)
    var outcome by remember { mutableStateOf<LocationOutcome?>(null) }
    var sheetOpen by remember { mutableStateOf(false) }

    val current = stored.valueOrNull()
    val shown: Location? = (outcome as? LocationOutcome.Resolved)?.location ?: current

    fun applyLocation(location: Location, sourceNote: String) {
        outcome = LocationOutcome.Resolved(location)
        scope.launch {
            val forecast = services.forecast.load(location)
            val ready = forecast.valueOrNull()
            if (ready != null) {
                onSnack(sourceNote + ". Forecast updated for " + ready.days.size + " days.")
            } else {
                onSnack(sourceNote + ". Forecast unavailable, it can be retried without changing the location.")
            }
        }
    }

    fun requestDeviceLocation() {
        outcome = LocationOutcome.Resolving
        scope.launch {
            val result = services.location.useMyLocation()
            val mapped = locationOutcome(result)
            outcome = mapped
            when (mapped) {
                is LocationOutcome.Resolved -> applyLocation(mapped.location, "Location set from this device")
                is LocationOutcome.Denied -> onSnack("Location permission denied. The default location is in use.")
                is LocationOutcome.Failed -> onSnack("The location lookup failed. The previous location is unchanged.")
                LocationOutcome.Resolving -> Unit
            }
        }
    }

    BackHandler(enabled = true) { onBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = HeliosSpacing.gutter)
            .padding(top = HeliosSpacing.space4, bottom = HeliosSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.sectionRhythm)
    ) {
        SettingsTopBar(title = "Location", eyebrow = "Settings", onBack = onBack)

        SettingsCard {
            Text(
                text = shown?.label ?: "No location yet",
                style = HeliosTypography.headline,
                color = colors.textPrimary
            )
            if (shown != null) {
                Text(
                    text = coordinateLabel(shown.lat, shown.lng) + " \u00B7 " + locationSourceLabel(shown.source),
                    style = HeliosTypography.caption,
                    color = colors.textTertiary
                )
            }
            Text(
                text = "The forecast is the only feature that uses this. Coordinates go to Open-Meteo; " +
                    "nothing else leaves the device.",
                style = HeliosTypography.caption,
                color = colors.textTertiary
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            HeliosPrimaryButton(
                text = "Use my location",
                onClick = { requestDeviceLocation() },
                loading = outcome is LocationOutcome.Resolving,
                modifier = Modifier.fillMaxWidth()
            )
            if (outcome is LocationOutcome.Resolving) {
                Text(
                    text = "Detecting the device location. Controls are disabled until the lookup returns or fails.",
                    style = HeliosTypography.caption,
                    color = colors.textSecondary
                )
            }
            HeliosSecondaryButton(
                text = "Enter coordinates",
                onClick = { sheetOpen = true },
                modifier = Modifier.fillMaxWidth()
            )
        }

        when (val state = outcome) {
            is LocationOutcome.Denied -> Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)) {
                DeniedPermissionState(
                    title = "Using the default location",
                    message = "Android denied the location permission, so the forecast keeps the default " +
                        "location. The rest of the app is unaffected.",
                    actionLabel = "Open Android settings",
                    onAction = { openAppSettings(context) }
                )
                HeliosGhostButton(text = "Enter coordinates instead", onClick = { sheetOpen = true })
            }
            is LocationOutcome.Failed -> HeliosErrorState(
                title = state.failure.classLabel(),
                message = state.failure.action,
                detail = state.failure.detail,
                actionLabel = "Retry lookup",
                onAction = { requestDeviceLocation() }
            )
            is LocationOutcome.Resolved -> Text(
                text = "Location updated. The forecast was requested for the new place.",
                style = HeliosTypography.callout,
                color = colors.flowStrong
            )
            else -> Unit
        }

        Text(
            text = "A shared snapshot keeps whatever location label was in it. Opening a shared link never " +
                "changes this setting.",
            style = HeliosTypography.caption,
            color = colors.textTertiary
        )
    }

    if (sheetOpen) {
        CoordinateEntrySheet(
            onDismiss = { sheetOpen = false },
            onApply = { lat, lng, name ->
                sheetOpen = false
                scope.launch {
                    val result = services.location.setManual(lat, lng, name)
                    val location = result.valueOrNull()
                    if (location != null) {
                        applyLocation(location, "Coordinates set by hand")
                    } else {
                        outcome = locationOutcome(result)
                        onSnack("Those coordinates could not be stored.")
                    }
                }
            }
        )
    }
}
