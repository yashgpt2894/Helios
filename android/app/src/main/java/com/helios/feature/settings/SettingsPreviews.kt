package com.helios.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.helios.core.data.fixture.FixtureData
import com.helios.core.data.fixture.FixtureServicesFamily
import com.helios.core.designsystem.color.CarbonColors
import com.helios.core.designsystem.color.PaperColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.ui.theme.HeliosPreviewSurface

/**
 * Isolated examples of the inputs and rows Settings is built from, in the states they can be
 * in: valid, invalid with the error tied to the field, disabled-with-reason, selected,
 * unselected. They are the review surface for SCR-11 to SCR-15 without a device.
 *
 * These previews stand in for a gallery entry for this feature: the debug component gallery
 * lives in the debug source set, which this step does not own.
 */

@Preview(name = "SetupField: default, error, disabled", widthDp = 412, heightDp = 340)
@Composable
private fun SetupFieldStates() {
    HeliosPreviewSurface(colors = CarbonColors) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(HeliosSpacing.space4),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(HeliosSpacing.space3)
        ) {
            SetupField(label = "Inverter host or IP address", value = "192.168.1.42", onValueChange = {})
            SetupField(
                label = "Inverter host or IP address",
                value = "192.168.1.42:502",
                onValueChange = {},
                errorText = "Enter the host without a port; the port has its own field"
            )
            SetupField(
                label = "Modbus TCP port",
                value = "502",
                onValueChange = {},
                enabled = false,
                supporting = "Disabled while a probe runs",
                keyboardType = KeyboardType.Number
            )
        }
    }
}

@Preview(name = "SegmentedOptions: selected, unselected, disabled with reason", widthDp = 412, heightDp = 240)
@Composable
private fun SegmentedStates() {
    HeliosPreviewSurface(colors = PaperColors) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(HeliosSpacing.space4),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(HeliosSpacing.space3)
        ) {
            SegmentedOptions(
                options = listOf(
                    SegmentOption(id = "a", label = "Modbus TCP", caption = "LAN"),
                    SegmentOption(id = "b", label = "Modbus RTU", caption = "serial", disabledReason = "Not supported in this build")
                ),
                selectedId = "a",
                onSelect = {}
            )
            SegmentedOptions(
                options = listOf(
                    SegmentOption(id = "1", label = "1 s"),
                    SegmentOption(id = "2", label = "2 s", caption = "default"),
                    SegmentOption(id = "5", label = "5 s"),
                    SegmentOption(id = "10", label = "10 s")
                ),
                selectedId = "2",
                onSelect = {}
            )
        }
    }
}

@Preview(name = "Settings rows and disclosure", widthDp = 412, heightDp = 300)
@Composable
private fun SettingsRowsPreview() {
    HeliosPreviewSurface(colors = CarbonColors) {
        Column(modifier = Modifier.fillMaxWidth().padding(HeliosSpacing.space4)) {
            SettingsCard {
                SettingsRow(label = "Connection", value = "Demo system", supporting = "Simulated values", onClick = {})
                SettingsRow(label = "Appearance", value = "Carbon", supporting = "Fixed", onClick = {})
            }
            SettingsDisclosure(label = "About local-only data", expanded = true, onToggle = {}) {
                SettingsNote("Readings and settings stay on this device.")
            }
        }
    }
}

@Preview(name = "Connection form, valid", widthDp = 412, heightDp = 720)
@Composable
private fun ConnectionFieldsValid() {
    HeliosPreviewSurface(colors = CarbonColors) {
        Column(modifier = Modifier.fillMaxWidth().padding(HeliosSpacing.space4)) {
            ConnectionFields(
                draft = ConnectionDraft(),
                errors = validateConnectionDraft(ConnectionDraft()),
                onDraftChange = {},
                scheduleMs = FixtureData.retryScheduleMs
            )
        }
    }
}

@Preview(name = "Connection form, invalid", widthDp = 412, heightDp = 720)
@Composable
private fun ConnectionFieldsInvalid() {
    val draft = ConnectionDraft(host = "", port = "70000", unitId = "0")
    HeliosPreviewSurface(colors = PaperColors) {
        Column(modifier = Modifier.fillMaxWidth().padding(HeliosSpacing.space4)) {
            ConnectionFields(
                draft = draft,
                errors = validateConnectionDraft(draft),
                onDraftChange = {},
                scheduleMs = FixtureData.retryScheduleMs
            )
        }
    }
}

@Preview(name = "Settings section: connection, saved", widthDp = 412, heightDp = 915)
@Composable
private fun ConnectionSectionPreview() {
    HeliosPreviewSurface(colors = CarbonColors) {
        ConnectionSettingsSection(
            services = FixtureServicesFamily.INSTANCE,
            onBack = {},
            onSnack = {},
            onCopyToClipboard = {}
        )
    }
}

@Preview(name = "Settings section: location, denied", widthDp = 412, heightDp = 915)
@Composable
private fun LocationSectionPreview() {
    HeliosPreviewSurface(colors = PaperColors) {
        LocationSettingsSection(
            services = FixtureServicesFamily.INSTANCE,
            onBack = {},
            onSnack = {}
        )
    }
}

@Preview(name = "Settings section: appearance", widthDp = 412, heightDp = 915)
@Composable
private fun AppearanceSectionPreview() {
    HeliosPreviewSurface(colors = CarbonColors) {
        AppearanceSettingsSection(
            services = FixtureServicesFamily.INSTANCE,
            onBack = {},
            onSnack = {}
        )
    }
}

@Preview(name = "Settings section: brand chooser", widthDp = 412, heightDp = 915)
@Composable
private fun BrandSectionPreview() {
    HeliosPreviewSurface(colors = PaperColors) {
        BrandSettingsSection(
            services = FixtureServicesFamily.INSTANCE,
            selected = FixtureData.brands.first(),
            onSelect = {},
            onBack = {},
            onSnack = {}
        )
    }
}

@Preview(name = "Settings root", widthDp = 412, heightDp = 915)
@Composable
private fun SettingsRootPreview() {
    SettingsScreen(services = FixtureServicesFamily.INSTANCE)
}

@Preview(name = "Connection sheet", widthDp = 412, heightDp = 600)
@Composable
private fun CoordinateSheetPreview() {
    HeliosPreviewSurface(colors = CarbonColors) {
        Column(modifier = Modifier.fillMaxWidth().padding(HeliosSpacing.space4)) {
            com.helios.core.designsystem.component.SectionHeader(title = "Enter coordinates", eyebrow = "Forecast location")
            SetupField(label = "Latitude", value = "37.7749", onValueChange = {}, keyboardType = KeyboardType.Decimal)
            SetupField(label = "Longitude", value = "-122.4194", onValueChange = {}, keyboardType = KeyboardType.Decimal)
        }
    }
}
