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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.helios.core.data.service.AppServices
import com.helios.core.data.service.HeliosThemeMode
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.valueOrNull
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.helios.core.designsystem.color.CarbonColors
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.color.PaperColors
import com.helios.core.designsystem.component.SectionHeader
import com.helios.core.designsystem.component.StatusPill
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.ui.theme.HeliosPreviewSurface
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * SCR-14, Settings — appearance.
 *
 * Three choices: Carbon, Paper, and Auto, which follows the Android setting and reacts to a
 * system change without a restart. The choice applies immediately because the Settings
 * surface reads the same theme service and re-composes in the new palette; the host that
 * owns the app-level theme reads the same flow, so the two agree.
 *
 * Selected state is carried by the radio button and by words, never by colour alone
 * (DESIGN.md section 9). Android dynamic colour stays off, so the palette is never replaced
 * by wallpaper colours.
 */
@Composable
fun AppearanceSettingsSection(
    services: AppServices,
    onBack: () -> Unit,
    onSnack: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val themeMode by services.theme.themeMode.collectAsState(initial = Loadable.Loading)

    // A choice is shown the moment it is made and then confirmed by re-reading the service.
    // The fixture family drives its flows from one shared scenario switch, so it does not
    // re-emit on a preference write; re-reading is how the selection stays the service's
    // value rather than a local guess. The device adapter's DataStore flow emits as usual.
    var chosen by remember { mutableStateOf<HeliosThemeMode?>(null) }
    val selectedMode = chosen ?: themeMode.valueOrNull() ?: HeliosThemeMode.AUTO
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val previewDark = services.theme.resolveDark(selectedMode, systemDark)

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
        SettingsTopBar(title = "Appearance", eyebrow = "Settings", onBack = onBack)

        SettingsCard {
            themeOptions().forEach { option ->
                val selected = option.mode == selectedMode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = selected, role = Role.RadioButton) {
                            chosen = option.mode
                            scope.launch {
                                services.theme.setTheme(option.mode)
                                chosen = services.theme.themeMode.first().valueOrNull() ?: option.mode
                                onSnack("Theme set to " + option.label + ". The app follows this choice.")
                            }
                        }
                        .semantics {
                            this.selected = selected
                            contentDescription = option.label + ", " + option.description
                        }
                        .padding(vertical = HeliosSpacing.space1),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
                ) {
                    RadioButton(selected = selected, onClick = null)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = option.label,
                            style = HeliosTypography.body,
                            color = colors.textPrimary,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                        )
                        Text(
                            text = option.description,
                            style = HeliosTypography.caption,
                            color = colors.textTertiary
                        )
                    }
                    if (selected) {
                        Text(
                            text = "Selected",
                            style = HeliosTypography.caption2,
                            color = colors.accentStrong
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)) {
            SectionHeader(title = "Preview", eyebrow = if (previewDark) "Carbon" else "Paper")
            HeliosPreviewSurface(colors = if (previewDark) CarbonColors else PaperColors) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(HeliosSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
                ) {
                    Text(
                        text = "Live production",
                        style = HeliosTypography.caption2,
                        color = LocalHeliosSemanticColors.current.textTertiary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "4.23",
                            style = HeliosTypography.liveTicker,
                            color = LocalHeliosSemanticColors.current.textPrimary
                        )
                        Text(
                            text = " kW",
                            style = HeliosTypography.callout,
                            color = LocalHeliosSemanticColors.current.textTertiary,
                            modifier = Modifier.padding(start = HeliosSpacing.space1)
                        )
                    }
                    StatusPill(kind = HeliosStatusKind.PRODUCING)
                }
            }
            Text(
                text = "The preview uses the same tokens as every screen. Nothing here is a separate palette.",
                style = HeliosTypography.caption,
                color = colors.textTertiary
            )
        }

        SettingsCard {
            Text(
                text = "Text size",
                style = HeliosTypography.body,
                color = colors.textPrimary
            )
            Text(
                text = "Text size follows the Android setting. The app stores no separate size, so a system " +
                    "change applies everywhere without a restart.",
                style = HeliosTypography.caption,
                color = colors.textTertiary
            )
            Text(
                text = "System is currently " + (if (systemDark) "in dark mode" else "in light mode"),
                style = HeliosTypography.caption,
                color = colors.textSecondary
            )
        }
    }
}

private data class ThemeOption(
    val mode: HeliosThemeMode,
    val label: String,
    val description: String
)

private fun themeOptions(): List<ThemeOption> = listOf(
    ThemeOption(HeliosThemeMode.DARK, "Carbon", "The dark palette, always"),
    ThemeOption(HeliosThemeMode.LIGHT, "Paper", "The light palette, always"),
    ThemeOption(HeliosThemeMode.AUTO, "Auto", "Follows the Android setting and reacts to a change")
)

/** The theme in words, for the Settings row summary. */
fun themeModeLabel(mode: HeliosThemeMode): String = when (mode) {
    HeliosThemeMode.DARK -> "Carbon"
    HeliosThemeMode.LIGHT -> "Paper"
    HeliosThemeMode.AUTO -> "Auto"
}
