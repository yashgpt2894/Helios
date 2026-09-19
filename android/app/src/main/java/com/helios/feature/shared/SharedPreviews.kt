package com.helios.feature.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.helios.core.data.fixture.FixtureData
import com.helios.core.data.fixture.FixtureServicesFamily
import com.helios.core.designsystem.color.CarbonColors
import com.helios.core.designsystem.color.PaperColors
import com.helios.core.ui.theme.HeliosPreviewSurface

/**
 * Isolated examples of the shared-snapshot viewer: a decoded v1 payload, a payload with no
 * forecast array, a white-label payload, an older reading, and a payload that cannot be
 * decoded at all.
 *
 * These previews are the review surface for SCR-17 without a device or a link.
 */

private const val HOUR_MS = 60L * 60L * 1000L

@Preview(name = "Shared snapshot: valid, dark", widthDp = 412, heightDp = 915)
@Composable
private fun SharedValid() {
    val encoded = FixtureServicesFamily.INSTANCE.share.encode(FixtureData.snapshotPayload())
    HeliosPreviewSurface(colors = CarbonColors) {
        SharedScreen(
            encodedPayload = encoded,
            services = FixtureServicesFamily.INSTANCE,
            onTryHelios = {}
        )
    }
}

@Preview(name = "Shared snapshot: minimal payload, light", widthDp = 412, heightDp = 915)
@Composable
private fun SharedMinimal() {
    val payload = FixtureData.snapshotPayload().copy(fc = null, br = null)
    val encoded = FixtureServicesFamily.INSTANCE.share.encode(payload)
    HeliosPreviewSurface(colors = PaperColors) {
        SharedScreen(encodedPayload = encoded, services = FixtureServicesFamily.INSTANCE)
    }
}

@Preview(name = "Shared snapshot: white label payload", widthDp = 412, heightDp = 915)
@Composable
private fun SharedWhiteLabel() {
    val payload = FixtureData.snapshotPayload().copy(br = "voltcraft")
    val encoded = FixtureServicesFamily.INSTANCE.share.encode(payload)
    HeliosPreviewSurface(colors = CarbonColors) {
        SharedScreen(encodedPayload = encoded, services = FixtureServicesFamily.INSTANCE)
    }
}

@Preview(name = "Shared snapshot: older reading", widthDp = 412, heightDp = 915)
@Composable
private fun SharedStale() {
    val payload = FixtureData.snapshotPayload(nowMs = System.currentTimeMillis() - 3 * HOUR_MS)
    val encoded = FixtureServicesFamily.INSTANCE.share.encode(payload)
    HeliosPreviewSurface(colors = CarbonColors) {
        SharedScreen(encodedPayload = encoded, services = FixtureServicesFamily.INSTANCE)
    }
}

@Preview(name = "Shared snapshot: invalid or truncated link", widthDp = 412, heightDp = 915)
@Composable
private fun SharedInvalid() {
    HeliosPreviewSurface(colors = CarbonColors) {
        SharedScreen(
            encodedPayload = "truncated-payload",
            services = FixtureServicesFamily.INSTANCE,
            onTryHelios = {}
        )
    }
}
