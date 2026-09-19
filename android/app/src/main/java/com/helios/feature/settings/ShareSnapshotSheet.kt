package com.helios.feature.settings

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.helios.core.data.service.AppServices
import com.helios.core.data.service.ShareOutcome
import com.helios.core.data.service.metaOrNull
import com.helios.core.data.service.valueOrNull
import com.helios.core.designsystem.component.ShareSheetContent
import com.helios.core.designsystem.component.ShareSheetStatus
import com.helios.core.format.HeliosFormat
import kotlinx.coroutines.launch

/**
 * The share action (ACT-079, and the share flow of FLW-05).
 *
 * One snapshot is built from the current reading, the location label, the five forecast days
 * and the selected brand, encoded with the same v1 codec the PWA uses. The sheet then offers
 * the system share targets, with copy-to-clipboard as the path that always works - including
 * on a device with no share target installed, where the link stays selectable.
 *
 * A snapshot is a moment, so a stale or demo reading can be shared; the sheet says which one
 * it is before the link leaves the device.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSnapshotSheet(
    services: AppServices,
    locationLabel: String,
    brandId: String?,
    onDismiss: () -> Unit,
    onSnack: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var status by remember { mutableStateOf(ShareSheetStatus.PREPARING) }
    var url by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var failureReason by remember { mutableStateOf<String?>(null) }
    var qualifier by remember { mutableStateOf<String?>(null) }
    val targets = remember { services.share.targets().map { it.label to it.available } }

    LaunchedEffect(brandId, locationLabel) {
        status = ShareSheetStatus.PREPARING
        val reading = services.telemetry.read()
        val telemetry = reading.valueOrNull()
        if (telemetry == null) {
            status = ShareSheetStatus.FAILED
            failureReason = "There is no reading to share yet."
            return@LaunchedEffect
        }
        val days = services.forecast.glanceDays(5).valueOrNull()?.map { it.expectedKwh }
        when (val outcome = services.share.prepare(telemetry, locationLabel, days, brandId)) {
            is ShareOutcome.Ready -> {
                url = outcome.url
                val payload = outcome.payload
                summary = listOf(
                    "Location" to payload.loc,
                    "Live power" to HeliosFormat.kilowatts(payload.ac),
                    "Today" to HeliosFormat.kwh(payload.todayKwh),
                    "Lifetime" to HeliosFormat.kwh(payload.lifeKwh.toDouble(), decimals = 0),
                    "Battery" to HeliosFormat.percent(payload.soc.toDouble()),
                    "Self-use" to HeliosFormat.percent(payload.selfUse.toDouble())
                )
                qualifier = if (reading.metaOrNull()?.simulated == true) {
                    "Helios demo reading, shared as a snapshot"
                } else {
                    null
                }
                status = ShareSheetStatus.IDLE
            }
            is ShareOutcome.Failed -> {
                status = ShareSheetStatus.FAILED
                failureReason = outcome.failure.message
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, modifier = modifier) {
        ShareSheetContent(
            status = status,
            url = url,
            summary = summary,
            targets = targets,
            failureReason = failureReason,
            onCopy = {
                clipboard.setText(AnnotatedString(url))
                status = ShareSheetStatus.COPIED
                onSnack("Link copied")
            },
            onShare = { target ->
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, if (qualifier == null) url else qualifier + "\n" + url)
                }
                runCatching { context.startActivity(Intent.createChooser(send, target)) }
                status = ShareSheetStatus.SHARED
                onSnack("Shared")
            },
            onDismiss = onDismiss
        )
    }
}
