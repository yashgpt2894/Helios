package com.helios.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.helios.core.data.service.AppServices
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.BrandMark
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.HeliosSecondaryButton
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.Brand

/**
 * SCR-15, Settings — brand, the white-label chooser.
 *
 * The registry comes from the brand service, whose ids match `src/services/brand.ts`, so a
 * brand set here resolves the same brand in an opened snapshot link. Selecting a brand
 * changes the accent, the mark and the footer copy only: the semantic hue roles (solar,
 * flow, battery, grid, alert) keep their meaning, which is what keeps a white-label build
 * honest about the same physics.
 */
@Composable
fun BrandSettingsSection(
    services: AppServices,
    selected: Brand,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
    onSnack: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    var disclosureOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = true) { onBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = HeliosSpacing.gutter)
            .padding(top = HeliosSpacing.space4, bottom = HeliosSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.sectionRhythm)
    ) {
        SettingsTopBar(title = "Brand", eyebrow = "Settings", onBack = onBack)

        SettingsCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
            ) {
                BrandMark(
                    brandName = selected.name,
                    textMark = selected.textMark,
                    usesRadialMark = selected.mark == "helios"
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selected.name,
                        style = HeliosTypography.headline,
                        color = colors.textPrimary
                    )
                    Text(
                        text = selected.tagline ?: "White-label presentation of the same readings",
                        style = HeliosTypography.caption,
                        color = colors.textTertiary
                    )
                }
            }
        }

        SettingsCard {
            services.brand.registry.forEach { brand ->
                BrandOption(
                    brand = brand,
                    selected = brand.id == selected.id,
                    onSelect = { onSelect(brand.id) }
                )
            }
        }

        if (selected.id != services.brand.resolve("helios").id) {
            HeliosSecondaryButton(
                text = "Reset to helios",
                onClick = { onSelect("helios") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        SettingsDisclosure(
            label = "What the brand changes",
            expanded = disclosureOpen,
            onToggle = { disclosureOpen = !disclosureOpen }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space1)) {
                Text(
                    text = "Changes: the accent colour, the mark, the footer line, and the name shown on the"
                        + " dashboard header and the shared viewer.",
                    style = HeliosTypography.caption,
                    color = colors.textSecondary
                )
                Text(
                    text = "Does not change: solar, flow, battery and grid hues, alert colours, units, rounding,"
                        + " or any reading. Two brands show the same numbers for the same hardware.",
                    style = HeliosTypography.caption,
                    color = colors.textSecondary
                )
                Text(
                    text = "A shared snapshot can carry its own brand. That brand applies to the viewer only and"
                        + " never rewrites this setting.",
                    style = HeliosTypography.caption,
                    color = colors.textTertiary
                )
            }
        }

        HeliosGhostButton(
            text = "Share today's snapshot",
            onClick = { onSnack("Share a snapshot from the Settings list to build a link.") }
        )
    }
}

@Composable
private fun BrandOption(
    brand: Brand,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val colors = LocalHeliosSemanticColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .semantics {
                this.selected = selected
                contentDescription = brand.name + if (selected) ", selected" else ""
            }
            .padding(vertical = HeliosSpacing.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
    ) {
        RadioButton(selected = selected, onClick = null)
        AccentSwatch(color = brandAccentColor(brand.accent))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = brand.name,
                style = HeliosTypography.body,
                color = colors.textPrimary,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
            Text(
                text = brand.legalName ?: brand.id,
                style = HeliosTypography.caption,
                color = colors.textTertiary
            )
        }
    }
}

/** A brand accent hex that cannot be parsed falls back to the current accent, never to black. */
@Composable
fun brandAccentColor(hex: String): Color {
    val fallback = LocalHeliosSemanticColors.current.accentPrimary
    return runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(fallback)
}
