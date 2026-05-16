package com.helios.feature.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.data.repository.BrandRepository
import com.helios.core.data.repository.ShareRepository
import com.helios.core.designsystem.color.HeliosColor
import com.helios.core.domain.model.SnapshotPayload

/**
 * SharedScreen: renders a deep-linked snapshot payload for the shared view.
 * Layout mirrors PWA landing page with brand tier, live snapshot stats, and forecast row.
 */
@Composable
fun SharedScreen(encodedPayload: String, brandId: String? = null) {
    val payload = remember { ShareRepository.decodeSnapshot(encodedPayload) }
    val brand = remember(brandId) { BrandRepository.resolve(brandId) }

    if (payload == null) {
        ErrorState(message = "Unable to decode snapshot.")
        return
    }

    val isDark = true
    val accent = HeliosColor.Solar.ramp(500, isDark)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = brand.name,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Light,
            color = accent
        )
        if (brand.tagline != null) {
            Text(
                text = brand.tagline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        SnapshotRow(label = "Live Power", value = "%.2f kW".format(payload.ac))
        SnapshotRow(label = "Today", value = "%.1f kWh".format(payload.todayKwh))
        SnapshotRow(label = "Lifetime", value = "%.1f kWh".format(payload.lifeKwh))
        SnapshotRow(label = "Battery", value = "%.0f%%".format(payload.soc))
        SnapshotRow(label = "Self-Use", value = "%.0f%%".format(payload.selfUse))

        if (payload.fc != null && payload.fc.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Forecast",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                payload.fc.take(7).forEach { f ->
                    Text(
                        text = "%.1f".format(f),
                        style = MaterialTheme.typography.bodySmall,
                        color = accent
                    )
                }
            }
        }
    }
}

@Composable
fun SnapshotRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}
