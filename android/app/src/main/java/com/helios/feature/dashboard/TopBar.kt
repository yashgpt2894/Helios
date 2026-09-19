package com.helios.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.BrandMark
import com.helios.core.designsystem.component.FreshnessStamp
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.label
import com.helios.core.designsystem.component.StatusPill
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography

/**
 * Screen header: brand on the left, the state of the reading on the right, and the share
 * action.
 *
 * Slots, in order: mark and screen name, the freshness stamp, the status pill, share.
 * [freshnessText] is never optional in a screen call: design C1 forbids a live number
 * without an age, so the Dashboard and the Production screen always pass one.
 *
 * States it renders: connected (status word plus age), demo (the demo pill and "Demo data",
 * never "Live"), aging (absolute clock time), stale, offline (the stamp goes to Offline and
 * the banner carries the reason), fault, night, standby, curtailed.
 *
 * Layout: one row when there is room, two rows once the user scales text past 130 percent,
 * which is the size at which the header stops fitting 412 dp (DESIGN.md section 10 risk 1).
 * Freshness and status form one polite live region, so a screen reader hears a change of
 * state once rather than on every poll.
 */
@Composable
fun TopBar(
    brandName: String,
    subTitle: String,
    statusKind: HeliosStatusKind,
    statusLabel: String = statusKind.label(),
    modifier: Modifier = Modifier,
    freshnessText: String? = null,
    freshnessKind: HeliosStatusKind = statusKind,
    usesRadialMark: Boolean = true,
    textMark: String? = null,
    onFreshnessClick: (() -> Unit)? = null,
    onStatusClick: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val fontScale = LocalDensity.current.fontScale
    val stacked = fontScale >= 1.3f

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.backgroundPrimary
    ) {
        if (stacked) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HeliosSpacing.gutter, vertical = HeliosSpacing.space3),
                verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
            ) {
                Identity(brandName = brandName, subTitle = subTitle, usesRadialMark = usesRadialMark, textMark = textMark)
                StateGroup(
                    freshnessText = freshnessText,
                    freshnessKind = freshnessKind,
                    statusKind = statusKind,
                    statusLabel = statusLabel,
                    onFreshnessClick = onFreshnessClick,
                    onStatusClick = onStatusClick,
                    onShare = onShare
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HeliosSpacing.gutter, vertical = HeliosSpacing.space3),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Identity(brandName = brandName, subTitle = subTitle, usesRadialMark = usesRadialMark, textMark = textMark)
                StateGroup(
                    freshnessText = freshnessText,
                    freshnessKind = freshnessKind,
                    statusKind = statusKind,
                    statusLabel = statusLabel,
                    onFreshnessClick = onFreshnessClick,
                    onStatusClick = onStatusClick,
                    onShare = onShare
                )
            }
        }
    }
}

@Composable
private fun Identity(
    brandName: String,
    subTitle: String,
    usesRadialMark: Boolean,
    textMark: String?
) {
    val colors = LocalHeliosSemanticColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        BrandMark(
            brandName = brandName,
            textMark = textMark,
            usesRadialMark = usesRadialMark
        )
        Spacer(Modifier.width(HeliosSpacing.space3))
        Column {
            Text(
                text = brandName,
                style = HeliosTypography.headline,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
            if (subTitle.isNotEmpty()) {
                Text(
                    text = subTitle,
                    style = HeliosTypography.caption,
                    color = colors.textTertiary
                )
            }
        }
    }
}

/** Age and inverter state: one polite live region, announced on change only. */
@Composable
private fun StateGroup(
    freshnessText: String?,
    freshnessKind: HeliosStatusKind,
    statusKind: HeliosStatusKind,
    statusLabel: String,
    onFreshnessClick: (() -> Unit)?,
    onStatusClick: (() -> Unit)?,
    onShare: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .semantics { liveRegion = LiveRegionMode.Polite }
            .height(HeliosSpacing.minTouchTarget),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
    ) {
        if (freshnessText != null) {
            FreshnessStamp(
                text = freshnessText,
                kind = freshnessKind,
                onClick = onFreshnessClick
            )
        }
        StatusPill(kind = statusKind, label = statusLabel, onClick = onStatusClick)
        if (onShare != null) {
            HeliosGhostButton(text = "Share", onClick = onShare)
        }
    }
}
