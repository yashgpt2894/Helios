package com.helios.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.helios.core.data.service.AppServices
import com.helios.core.data.service.ConnectionSnapshot
import com.helios.core.data.service.HeliosThemeMode
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.Freshness
import com.helios.core.data.service.ServiceGraph
import com.helios.core.data.service.freshnessLabel
import com.helios.core.data.service.valueOrNull
import com.helios.core.domain.model.Brand
import com.helios.core.designsystem.color.CarbonColors
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.color.PaperColors
import com.helios.core.data.service.FailureKind
import com.helios.core.designsystem.component.FreshnessStamp
import com.helios.core.designsystem.component.HeliosMark
import com.helios.core.designsystem.component.HeliosPrimaryButton
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.format.HeliosFormat
import com.helios.core.ui.theme.HeliosTheme
import com.helios.core.ui.theme.branded
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * SCR-11, Settings, and the secondary surfaces behind it (SCR-12 to SCR-15).
 *
 * One entry point per concern, with the current value visible before entry, which is the
 * point of the screen: a user can see that the link is on the demo system, that the theme is
 * Carbon, and that the forecast uses a default location without opening anything.
 *
 * The secondary surfaces live in this same composable rather than in separate destinations,
 * because navigation is owned by the step that owns `core/nav`. Opening one is a real state
 * change, and back returns to the list: an in-app back control on every secondary surface and
 * the platform back gesture both work, including the leave guard on a dirty connection form.
 *
 * The surface applies the theme and the white-label accent it reads from the services. The
 * app shell applies the same values at the root, so the two agree; this surface can therefore
 * also be hosted alone - a preview, the gallery, or a deep link - and still render on the
 * theme and brand the user chose.
 */
enum class SettingsSection { ROOT, CONNECTION, LOCATION, APPEARANCE, BRAND }

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    services: AppServices = ServiceGraph.current,
    initialSection: SettingsSection = SettingsSection.ROOT
) {
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    // RTE-12 to RTE-16 are addressable secondary screens, so a host may open one directly.
    var section by rememberSaveable { mutableStateOf(initialSection) }
    var shareOpen by remember { mutableStateOf(false) }
    var residencyOpen by remember { mutableStateOf(false) }

    val themeMode by services.theme.themeMode.collectAsState(initial = Loadable.Loading)
    val brand by services.brand.brand.collectAsState(initial = Loadable.Loading)
    val link by services.connection.link.collectAsState(initial = Loadable.Loading)
    val location by services.location.location.collectAsState(initial = Loadable.Loading)
    val telemetry by services.telemetry.telemetry.collectAsState(initial = Loadable.Loading)

    val mode = themeMode.valueOrNull() ?: HeliosThemeMode.AUTO

    // The brand just chosen is shown at once and then confirmed by re-reading the service, for
    // the same reason as the theme choice: the fixture family re-emits per scenario, not per
    // preference write. With the device adapter the service flow and this value agree.
    var chosenBrand by remember { mutableStateOf<Brand?>(null) }
    val resolvedBrand = chosenBrand ?: brand.valueOrNull()

    // The host owns light and dark (`MainActivity` applies HeliosTheme from the stored theme),
    // so this surface keeps the palette it was given and re-applies only the white-label
    // accent. That is the one thing a brand change must show immediately, and re-applying the
    // same accent at the root later is idempotent.
    val ambient = LocalHeliosSemanticColors.current
    val palette = if (resolvedBrand == null) {
        ambient
    } else {
        ambient.branded(resolvedBrand.accent, resolvedBrand.accentLight)
    }

    fun notify(message: String) {
        scope.launch { snackbarHost.showSnackbar(message) }
    }

    fun selectBrand(id: String) {
        chosenBrand = services.brand.resolve(id)
        scope.launch {
            services.brand.select(id)
            chosenBrand = services.brand.brand.first().valueOrNull() ?: services.brand.resolve(id)
            notify("Brand set to " + (chosenBrand?.name ?: id))
        }
    }

    HeliosTheme(darkTheme = ambient.isDark, colors = palette) {
        Surface(color = LocalHeliosSemanticColors.current.backgroundPrimary) {
            Box(modifier = modifier.fillMaxSize()) {
                when (section) {
                    SettingsSection.ROOT -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .statusBarsPadding()
                            .padding(horizontal = HeliosSpacing.gutter)
                            .padding(top = HeliosSpacing.space4, bottom = HeliosSpacing.space8),
                        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.sectionRhythm)
                    ) {
                        SettingsRootHeader(
                            brandName = resolvedBrand?.name ?: "helios",
                            telemetryModel = telemetry.valueOrNull()?.model,
                            telemetrySerial = telemetry.valueOrNull()?.serialNumber,
                            firmware = telemetry.valueOrNull()?.firmware,
                            freshness = telemetry
                        )

                        SettingsCard {
                            SettingsRow(
                                label = "Connection",
                                value = linkWord(link, services.telemetry.isDemo),
                                supporting = linkSupporting(link, services.telemetry.isDemo),
                                onClick = { section = SettingsSection.CONNECTION }
                            )
                            SettingsRow(
                                label = "Location",
                                value = locationWord(location),
                                supporting = locationSupporting(location),
                                onClick = { section = SettingsSection.LOCATION }
                            )
                            SettingsRow(
                                label = "Appearance",
                                value = themeModeLabel(mode),
                                supporting = if (mode == HeliosThemeMode.AUTO) "Following the system setting" else "Fixed",
                                onClick = { section = SettingsSection.APPEARANCE }
                            )
                            SettingsRow(
                                label = "Brand",
                                value = resolvedBrand?.name ?: "helios",
                                supporting = "Accent and mark only",
                                onClick = { section = SettingsSection.BRAND }
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)) {
                            HeliosPrimaryButton(
                                text = "Share today's snapshot",
                                onClick = { shareOpen = true },
                                enabled = telemetry.valueOrNull() != null,
                                disabledReason = "Waiting for the first reading",
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Builds a link that opens the read-only viewer. The receiver needs no account, " +
                                    "and the viewer decodes offline.",
                                style = HeliosTypography.caption,
                                color = LocalHeliosSemanticColors.current.textTertiary
                            )
                        }

                        SettingsCard {
                            SettingsDisclosure(
                                label = "About local-only data",
                                expanded = residencyOpen,
                                onToggle = { residencyOpen = !residencyOpen }
                            ) {
                                Text(
                                    text = "Readings, the connection configuration, the theme and the brand stay on " +
                                        "this device. There is no account, no cloud sync and no analytics. The only " +
                                        "outbound request is the forecast, which sends coordinates to Open-Meteo.",
                                    style = HeliosTypography.caption,
                                    color = LocalHeliosSemanticColors.current.textSecondary
                                )
                            }
                        }

                        Text(
                            text = (resolvedBrand?.name ?: "helios") + " \u00B7 local-only \u00B7 no account",
                            style = HeliosTypography.caption2,
                            color = LocalHeliosSemanticColors.current.textTertiary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SettingsSection.CONNECTION -> ConnectionSettingsSection(
                        services = services,
                        onBack = { section = SettingsSection.ROOT },
                        onSnack = { notify(it) },
                        onCopyToClipboard = { clipboard.setText(AnnotatedString(it)) }
                    )

                    SettingsSection.LOCATION -> LocationSettingsSection(
                        services = services,
                        onBack = { section = SettingsSection.ROOT },
                        onSnack = { notify(it) }
                    )

                    SettingsSection.APPEARANCE -> AppearanceSettingsSection(
                        services = services,
                        onBack = { section = SettingsSection.ROOT },
                        onSnack = { notify(it) }
                    )

                    SettingsSection.BRAND -> BrandSettingsSection(
                        services = services,
                        selected = resolvedBrand ?: services.brand.resolve(null),
                        onSelect = { selectBrand(it) },
                        onBack = { section = SettingsSection.ROOT },
                        onSnack = { notify(it) }
                    )
                }

                SnackbarHost(
                    hostState = snackbarHost,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(HeliosSpacing.space4)
                )
            }
        }
    }

    if (shareOpen) {
        HeliosTheme(darkTheme = ambient.isDark, colors = palette) {
            ShareSnapshotSheet(
                services = services,
                locationLabel = location.valueOrNull()?.label ?: "Unknown location",
                brandId = resolvedBrand?.id,
                onDismiss = { shareOpen = false },
                onSnack = { notify(it) }
            )
        }
    }
}

@Composable
private fun SettingsRootHeader(
    brandName: String,
    telemetryModel: String?,
    telemetrySerial: String?,
    firmware: String?,
    freshness: Loadable<*>
) {
    val colors = LocalHeliosSemanticColors.current
    Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space4)) {
        Column {
            Text(
                text = "System",
                style = HeliosTypography.caption2,
                color = colors.textTertiary
            )
            Text(
                text = "Settings",
                style = HeliosTypography.title2,
                color = colors.textPrimary
            )
        }
        SettingsCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
            ) {
                HeliosMark(size = 40.dp, contentDescription = brandName + " mark")
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = telemetryModel ?: "No inverter identified yet",
                        style = HeliosTypography.headline,
                        color = colors.textPrimary
                    )
                    Text(
                        text = listOfNotNull(
                            telemetrySerial?.let { "S/N " + it },
                            firmware?.let { "fw " + it }
                        ).ifEmpty { listOf("Waiting for a reading") }.joinToString(" \u00B7 "),
                        style = HeliosTypography.caption,
                        color = colors.textTertiary
                    )
                }
            }
            FreshnessStamp(
                text = freshnessText(freshness),
                kind = freshnessKind(freshness),
                onClick = null
            )
        }
    }
}

/** The reading's age in words. A demo source is named as one, never as live. */
private fun freshnessText(freshness: Loadable<*>): String = freshness.freshnessLabel()

private fun freshnessKind(freshness: Loadable<*>): HeliosStatusKind = when (freshness) {
    is Loadable.Failed -> if (freshness.failure.kind.linkDown) HeliosStatusKind.OFFLINE else HeliosStatusKind.STANDBY
    is Loadable.Empty -> HeliosStatusKind.STANDBY
    is Loadable.Loading -> HeliosStatusKind.STANDBY
    is Loadable.Ready -> when {
        freshness.meta.simulated -> HeliosStatusKind.DEMO
        freshness.meta.freshness == Freshness.STALE -> HeliosStatusKind.OFFLINE
        else -> HeliosStatusKind.PRODUCING
    }
}

/** "San Francisco, CA" or the honest default. */
private fun locationWord(location: Loadable<com.helios.core.domain.model.Location>): String =
    location.valueOrNull()?.label ?: "Default location"

private fun locationSupporting(location: Loadable<com.helios.core.domain.model.Location>): String {
    val value = location.valueOrNull() ?: return "No location set; the forecast uses the default"
    return locationSourceLabel(value.source) + " \u00B7 " + coordinateLabel(value.lat, value.lng)
}

private fun linkSupporting(link: Loadable<ConnectionSnapshot>, demo: Boolean): String {
    if (demo) return "Simulated values, no inverter needed"
    val value = link.valueOrNull()
    val config = value?.config ?: return "Not configured yet"
    val lastGood = value.lastGoodAt?.let { "last good read " + HeliosFormat.clockTime(it) } ?: "no good read yet"
    return HeliosFormat.hostPort(config.host, config.port) + " \u00B7 " + lastGood
}
