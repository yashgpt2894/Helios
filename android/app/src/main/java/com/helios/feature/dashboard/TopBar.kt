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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.BrandMark
import com.helios.core.designsystem.component.FreshnessStamp
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.StatusPill
import com.helios.core.designsystem.component.label
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography

/**
 * Top bar with three slots: the brand mark on the left, the freshness stamp and the
 * status pill on the right, plus the share action.
 *
 * States: connected (status word plus freshness), demo (the demo pill and "Demo system"
 * wording, never the word live), offline (the status pill goes to Offline and the
 * freshness stamp reads the last good time), and stale.
 *
 * Freshness and status form one polite live region: a screen reader announces a change of
 * state once, not on every poll.
 */
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    brandName: String = "helios\u00B0",
    subTitle: String = "",
    statusKind: HeliosStatusKind = HeliosStatusKind.PRODUCING,
    statusLabel: String = statusKind.label(),
    freshnessText: String? = null,
    freshnessKind: HeliosStatusKind = statusKind,
    usesRadialMark: Boolean = true,
    textMark: String? = null,
    onStatusClick: (() -> Unit)? = null,
    onFreshnessClick: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.backgroundPrimary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HeliosSpacing.gutter, vertical = HeliosSpacing.space3),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Row(
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
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
    }
}
