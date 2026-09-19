package com.helios.feature.shared

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.data.service.AppServices
import com.helios.core.data.service.HeliosThemeMode
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.ServiceGraph
import com.helios.core.data.service.valueOrNull
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.BrandMark
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.HeliosMark
import com.helios.core.designsystem.component.HeliosPrimaryButton
import com.helios.core.designsystem.component.HeliosSecondaryButton
import com.helios.core.designsystem.component.SnapshotSummaryRow
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.Brand
import com.helios.core.domain.model.SnapshotPayload
import com.helios.core.format.HeliosFormat
import com.helios.core.ui.theme.HeliosTheme
import com.helios.core.ui.theme.branded
import kotlinx.coroutines.launch
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * SCR-17, the shared-snapshot viewer: read-only, offline, and honest about its own age.
 *
 * A snapshot is a moment, not a live link, so the screen says when it was taken and, past an
 * hour, says plainly that the reading may have moved on. Decoding is local - the payload is a
 * self-contained value - so the viewer works with no network at all; only the two optional
 * actions degrade, and one of them is hidden when Android has no browser to hand.
 *
 * The payload is v1 and stays byte-compatible with the PWA codec in `ShareService`. An
 * invalid or truncated payload is a state with words, not an empty screen or a stack trace.
 *
 * It is read-only by design: nothing here writes a preference, a configuration, or a
 * reading, so opening somebody else's link cannot change this device.
 */

/** A snapshot older than this is labelled as an older reading rather than a current one. */
const val SNAPSHOT_STALE_AFTER_MS = 60L * 60L * 1000L

/** Grams of CO2 avoided per kWh of production, matching the PWA copy. */
const val CO2_KG_PER_KWH = 0.42

/** True when the snapshot is old enough that its numbers may have moved on. */
fun isStaleSnapshot(ts: Long, nowMs: Long = System.currentTimeMillis()): Boolean =
    nowMs - ts > SNAPSHOT_STALE_AFTER_MS

/** "Thursday, 19 September", in the device locale. */
fun snapshotDateLabel(ts: Long, locale: Locale = Locale.getDefault()): String = runCatching {
    DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(ts))
}.getOrDefault(HeliosFormat.clockTime(ts))

/** "14:32", in the device locale. */
fun snapshotTimeLabel(ts: Long, locale: Locale = Locale.getDefault()): String = runCatching {
    DateTimeFormatter.ofPattern("HH:mm", locale)
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(ts))
}.getOrDefault(HeliosFormat.clockTime(ts))

/** The lifetime figure in MWh, which is how the viewer presents a 18 420 kWh system. */
fun lifetimeMegawattHours(lifeKwh: Int): String = HeliosFormat.fixed(lifeKwh / 1000.0, 2)

/** CO2 avoided today, one decimal, from the same 0.42 factor the PWA uses. */
fun co2AvoidedKg(todayKwh: Double): String = HeliosFormat.fixed(todayKwh * CO2_KG_PER_KWH, 1)

@Composable
fun SharedScreen(
    encodedPayload: String,
    brandId: String? = null,
    modifier: Modifier = Modifier,
    onTryHelios: (() -> Unit)? = null,
    services: AppServices = ServiceGraph.current
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    // Decoding is local and can fail on a truncated link; a failure is a value, not a crash.
    val payload = remember(encodedPayload) {
        runCatching { services.share.decode(encodedPayload) }.getOrNull()
    }
    val brand = remember(payload?.br, brandId) { services.brand.resolve(payload?.br ?: brandId) }

    // The host owns light and dark; this surface keeps the palette it was given and applies
    // only the payload's brand accent (STS-082), which is what makes a white-label link look
    // like that brand without changing this device's theme.
    val ambient = LocalHeliosSemanticColors.current
    val palette = ambient.branded(brand.accent, brand.accentLight)

    fun notify(message: String) {
        scope.launch { snackbarHost.showSnackbar(message) }
    }

    HeliosTheme(darkTheme = ambient.isDark, colors = palette) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = LocalHeliosSemanticColors.current.backgroundPrimary
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (payload == null) {
                    InvalidPayloadState(brand = brand, onTryHelios = onTryHelios)
                } else {
                    SnapshotContent(
                        payload = payload,
                        brand = brand,
                        url = remember(payload) { services.share.buildUrl(payload) },
                        browserAvailable = remember(payload) {
                            runCatching {
                                context.packageManager.resolveActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(services.share.buildUrl(payload))),
                                    android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
                                ) != null
                            }.getOrDefault(false)
                        },
                        onCopyLink = { link ->
                            clipboard.setText(AnnotatedString(link))
                            notify("Link copied")
                        },
                        onOpenInBrowser = { link ->
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        },
                        onTryHelios = onTryHelios
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
}

/** STS-080: an invalid or truncated link, with one action and no blank screen. */
@Composable
private fun InvalidPayloadState(
    brand: Brand,
    onTryHelios: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(HeliosSpacing.gutter),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeliosMark(size = 36.dp, color = colors.textQuaternary)
        Spacer(Modifier.height(HeliosSpacing.space4))
        Text(
            text = "Link expired or invalid",
            style = HeliosTypography.title3,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(HeliosSpacing.space2))
        Text(
            text = "This shared snapshot could not be decoded. The link may have been truncated by the " +
                "messaging app that delivered it, or it may come from a newer format than this build reads.",
            style = HeliosTypography.callout,
            color = colors.textSecondary
        )
        Spacer(Modifier.height(HeliosSpacing.space2))
        Text(
            text = "The viewer reads snapshot version 1 only, and decoding happens on this device with no network.",
            style = HeliosTypography.caption,
            color = colors.textTertiary
        )
        if (onTryHelios != null) {
            Spacer(Modifier.height(HeliosSpacing.space6))
            HeliosPrimaryButton(text = "Go to " + brand.name, onClick = onTryHelios)
        }
    }
}

/** STS-079, STS-081, STS-082, STS-083: the decoded payload, and what it does not contain. */
@Composable
private fun SnapshotContent(
    payload: SnapshotPayload,
    brand: Brand,
    url: String,
    browserAvailable: Boolean,
    onCopyLink: (String) -> Unit,
    onOpenInBrowser: (String) -> Unit,
    onTryHelios: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    val stale = isStaleSnapshot(payload.ts)
    val age = (System.currentTimeMillis() - payload.ts).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = HeliosSpacing.gutter)
            .padding(top = HeliosSpacing.space4, bottom = HeliosSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.sectionRhythm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
            ) {
                BrandMark(
                    brandName = brand.name,
                    textMark = brand.textMark,
                    usesRadialMark = brand.mark == "helios",
                    size = 26.dp
                )
                Text(
                    text = brand.name,
                    style = HeliosTypography.headline,
                    color = colors.textPrimary
                )
            }
            Text(
                text = "Shared snapshot",
                style = HeliosTypography.caption2,
                color = colors.textTertiary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(HeliosShape.md)
                .background(colors.backgroundSecondary)
                .border(HeliosElevation.MaterialTokens.innerStrokeWidth, colors.separatorHairline, HeliosShape.md)
                .padding(HeliosSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
        ) {
            Text(
                text = payload.loc,
                style = HeliosTypography.callout,
                color = colors.textSecondary
            )
            Text(
                text = snapshotDateLabel(payload.ts) + " \u00B7 " + snapshotTimeLabel(payload.ts),
                style = HeliosTypography.caption2,
                color = colors.textTertiary
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = HeliosFormat.fixed(payload.ac, 2),
                    style = HeliosTypography.hero,
                    color = colors.textPrimary
                )
                Text(
                    text = "kW",
                    style = HeliosTypography.callout,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = HeliosSpacing.space2, bottom = HeliosSpacing.space3)
                )
            }
            Text(
                text = "Solar production at the moment this link was shared.",
                style = HeliosTypography.callout,
                color = colors.textSecondary
            )
        }

        if (stale) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(HeliosShape.md)
                    .background(colors.accentSubtle)
                    .padding(HeliosSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space1)
            ) {
                Text(
                    text = "Older reading",
                    style = HeliosTypography.headline,
                    color = colors.onSubtle
                )
                Text(
                    text = "Taken " + HeliosFormat.age(age) + " ago. A snapshot is a moment: the system has " +
                        "produced and consumed since, so treat these numbers as a record, not as live state.",
                    style = HeliosTypography.callout,
                    color = colors.onSubtle
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(HeliosShape.md)
                .background(colors.backgroundSecondary)
                .padding(HeliosSpacing.cardPadding)
        ) {
            SnapshotSummaryRow(label = "Today", value = HeliosFormat.kwh(payload.todayKwh))
            SnapshotSummaryRow(label = "Battery", value = HeliosFormat.percent(payload.soc.toDouble()))
            SnapshotSummaryRow(label = "Self-use", value = HeliosFormat.percent(payload.selfUse.toDouble()))
            SnapshotSummaryRow(label = "Lifetime", value = lifetimeMegawattHours(payload.lifeKwh) + " MWh")
            SnapshotSummaryRow(
                label = "CO2 avoided today",
                value = co2AvoidedKg(payload.todayKwh) + " kg"
            )
        }

        val forecast = payload.fc
        if (forecast != null && forecast.isNotEmpty()) {
            ForecastStrip(days = forecast)
        }

        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
                HeliosPrimaryButton(text = "Copy link", onClick = { onCopyLink(url) })
                if (browserAvailable) {
                    HeliosSecondaryButton(
                        text = "Open in browser",
                        onClick = { onOpenInBrowser(url) }
                    )
                }
            }
            if (!browserAvailable) {
                Text(
                    text = "No browser is installed on this device, so the link can only be copied here.",
                    style = HeliosTypography.caption,
                    color = colors.textTertiary
                )
            }
        }

        if (onTryHelios != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(HeliosShape.md)
                    .background(colors.backgroundTertiary)
                    .border(HeliosElevation.MaterialTokens.innerStrokeWidth, colors.separatorHairline, HeliosShape.md)
                    .padding(HeliosSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
            ) {
                Text(
                    text = "Want one for your roof?",
                    style = HeliosTypography.headline,
                    color = colors.textPrimary
                )
                Text(
                    text = brand.name + " works with any SunSpec-compatible inverter. The demo system shows " +
                        "what a monitored system looks like with no hardware at all.",
                    style = HeliosTypography.callout,
                    color = colors.textSecondary
                )
                HeliosGhostButton(text = "Open " + brand.name, onClick = onTryHelios)
            }
        }

        Text(
            text = if (brand.id == "helios") {
                "helios \u00B7 precision energy"
            } else {
                "Powered by helios \u00B7 " + brand.name
            },
            style = HeliosTypography.caption2,
            color = colors.textTertiary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** STS-079 with a forecast array: five days as values and bars, no invented weather icons. */
@Composable
private fun ForecastStrip(days: List<Int>, modifier: Modifier = Modifier) {
    val colors = LocalHeliosSemanticColors.current
    val max = (days.maxOrNull() ?: 1).coerceAtLeast(1)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(colors.backgroundSecondary)
            .padding(HeliosSpacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
    ) {
        Text(
            text = "Next " + days.size + " days \u00B7 forecast",
            style = HeliosTypography.caption2,
            color = colors.textTertiary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
        ) {
            days.forEachIndexed { index, kwh ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space1)
                ) {
                    Text(
                        text = forecastDayLabel(index),
                        style = HeliosTypography.caption2,
                        color = colors.textTertiary
                    )
                    Text(
                        text = kwh.toString(),
                        style = HeliosTypography.callout,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "kWh",
                        style = HeliosTypography.caption2,
                        color = colors.textTertiary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(HeliosShape.full)
                            .background(colors.backgroundTertiary)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((kwh.toFloat() / max).coerceIn(0.08f, 1f))
                                .height(2.dp)
                                .clip(HeliosShape.full)
                                .background(colors.solarPrimary)
                        )
                    }
                }
            }
        }
    }
}

/** "Tod", "Tom", then the day offset. The PWA uses the same three labels. */
private fun forecastDayLabel(index: Int): String = when (index) {
    0 -> "Tod"
    1 -> "Tom"
    else -> "+" + index
}
