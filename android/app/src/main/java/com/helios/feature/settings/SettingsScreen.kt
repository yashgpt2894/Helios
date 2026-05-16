package com.helios.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.data.repository.BrandRepository
import com.helios.core.data.repository.ShareRepository
import com.helios.core.data.repository.TelemetryRepository
import com.helios.core.domain.model.Brand
import com.helios.core.domain.model.Location

/**
 * SettingsScreen: brand selector, theme toggle, location, share snapshot (M4 stub).
 */
@Composable
fun SettingsScreen(
    onThemeToggle: () -> Unit = {},
    isDark: Boolean = true
) {
    val telemetry = remember { TelemetryRepository.readTelemetry() }
    val currentBrand = remember { BrandRepository.current() }
    var selectedBrand by remember { mutableStateOf(currentBrand.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Theme
        SettingsRow(
            label = "Appearance",
            value = if (isDark) "Dark" else "Light",
            onClick = onThemeToggle
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Brand
        Text(
            text = "Brand",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val brands = listOf("helios", "solaris", "volt", "aether")
            brands.forEach { id ->
                val brand = BrandRepository.resolve(id)
                Surface(
                    modifier = Modifier.clickable { selectedBrand = id; BrandRepository.resolve(id) },
                    color = if (selectedBrand == id) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = brand.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Share
        Button(
            onClick = {
                val snapshot = ShareRepository.buildSnapshot(telemetry, brandId = selectedBrand)
                val url = ShareRepository.buildShareUrl(snapshot)
                // In real app: copy to clipboard or share intent
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share Snapshot")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "System: ${telemetry.model}\nFirmware: ${telemetry.firmware}\nSerial: ${telemetry.serialNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun SettingsRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}
