package com.helios.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.HeliosPrimaryButton
import com.helios.core.designsystem.component.SectionHeader
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography

/**
 * Coordinate entry, used by SCR-13 and by the "enter coordinates" action of SCR-06.
 *
 * A place-name search is deliberately absent: no service in `core/data/service` exposes a
 * geocoder (SVC-05 has `label()`, not `search()`), and a search field that cannot answer is
 * a dead control. Typed coordinates are a real, offline path with no extra permission.
 *
 * The sheet handles the keyboard itself (`imePadding`), so the fields stay visible while
 * typing, and it is dismissible by swipe, by the platform back gesture and by Cancel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoordinateEntrySheet(
    onDismiss: () -> Unit,
    onApply: (lat: Double, lng: Double, label: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }

    val lat = latitude.trim().toDoubleOrNull()
    val lng = longitude.trim().toDoubleOrNull()
    val latError = when {
        latitude.isBlank() -> null
        lat == null -> "Enter a decimal number"
        lat !in -90.0..90.0 -> "Latitude runs from -90 to 90"
        else -> null
    }
    val lngError = when {
        longitude.isBlank() -> null
        lng == null -> "Enter a decimal number"
        lng !in -180.0..180.0 -> "Longitude runs from -180 to 180"
        else -> null
    }
    val canApply = lat != null && lng != null && latError == null && lngError == null

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = HeliosSpacing.gutter)
                .padding(bottom = HeliosSpacing.space6),
            verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
        ) {
            SectionHeader(
                title = "Enter coordinates",
                eyebrow = "Forecast location",
                trailing = null
            )
            Text(
                text = "The forecast uses these coordinates. Only the coordinates are sent to Open-Meteo, " +
                    "and only when a forecast is requested.",
                style = HeliosTypography.caption,
                color = colors.textTertiary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
                SetupField(
                    label = "Latitude",
                    value = latitude,
                    onValueChange = { latitude = it },
                    errorText = latError,
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f)
                )
                SetupField(
                    label = "Longitude",
                    value = longitude,
                    onValueChange = { longitude = it },
                    errorText = lngError,
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f)
                )
            }
            SetupField(
                label = "Place name (optional)",
                value = label,
                onValueChange = { label = it },
                supporting = "Shown next to the forecast. Coordinates are used either way.",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )
            Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
                HeliosPrimaryButton(
                    text = "Use these coordinates",
                    onClick = {
                        val resolvedLat = lat
                        val resolvedLng = lng
                        if (resolvedLat != null && resolvedLng != null) {
                            val name = label.trim().ifBlank { coordinateLabel(resolvedLat, resolvedLng) }
                            onApply(resolvedLat, resolvedLng, name)
                        }
                    },
                    enabled = canApply,
                    disabledReason = if (!canApply) "Enter a latitude and a longitude inside their ranges" else null
                )
                HeliosGhostButton(text = "Cancel", onClick = onDismiss)
            }
        }
    }
}
