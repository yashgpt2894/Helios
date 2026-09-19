package com.helios.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.EmptyState
import com.helios.core.designsystem.component.HeliosChip
import com.helios.core.designsystem.component.HeliosErrorState
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.InsightCard
import com.helios.core.designsystem.component.SectionHeader
import com.helios.core.designsystem.component.SkeletonBlock
import com.helios.core.designsystem.component.SkeletonLines
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.Insight
import com.helios.core.domain.model.InsightCategory

/**
 * The advisory feed: the header with the count, the category filter, the rows, and the
 * three ways it can be empty.
 *
 * The feed is unbounded (24 advisories in the long-content fixture), so the screen renders
 * it in a `LazyColumn` and these composables are the items, not the list.
 *
 * States: loading (rows in the row shape), ready, empty because the system is clean
 * (STS-042), paused because the reading is stale or the link is down (STS-045), and failed
 * with a retry that leaves the forecast and the savings above it intact.
 */

/** Header and filter row for the feed. */
@Composable
fun InsightFeedHeader(
    activeCount: Int,
    categories: List<Pair<InsightCategory, Int>>,
    selected: InsightCategory?,
    onSelect: (InsightCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = "All insights",
            eyebrow = "Full feed",
            trailing = {
                Text(
                    text = if (activeCount == 1) "1 advisory" else "$activeCount advisories",
                    style = HeliosTypography.caption,
                    color = colors.textTertiary
                )
            }
        )
        if (categories.size > 1) {
            Spacer(Modifier.height(HeliosSpacing.space3))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
            ) {
                HeliosChip(
                    label = "All",
                    selected = selected == null,
                    onClick = { onSelect(null) }
                )
                categories.forEach { (category, count) ->
                    HeliosChip(
                        label = "${category.label()} \u00B7 $count",
                        selected = selected == category,
                        onClick = { onSelect(if (selected == category) null else category) }
                    )
                }
            }
        }
    }
}

/** One advisory row of the feed. The action is only rendered when it can be honoured. */
@Composable
fun InsightFeedRow(
    insight: Insight,
    demoQualifier: Boolean,
    modifier: Modifier = Modifier,
    onAction: (() -> Unit)? = null
) {
    InsightCard(
        insight = insight,
        severity = insight.severity.kind(),
        demoQualifier = demoQualifier,
        onAction = onAction,
        modifier = modifier
    )
}

/** Loading: three rows in the row shape, not generic rectangles. */
@Composable
fun InsightFeedSkeleton(modifier: Modifier = Modifier) {
    val colors = LocalHeliosSemanticColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(3) { index ->
            if (index > 0) Spacer(Modifier.height(HeliosSpacing.space3))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(HeliosShape.md)
                    .background(colors.backgroundTertiary)
                    .padding(HeliosSpacing.cardPadding),
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
            ) {
                SkeletonBlock(
                    height = 64.dp,
                    shape = HeliosShape.xs,
                    modifier = Modifier.size(width = 4.dp, height = 64.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    SkeletonBlock(height = 14.dp, modifier = Modifier.fillMaxWidth(0.66f))
                    Spacer(Modifier.height(HeliosSpacing.space2))
                    SkeletonLines(lines = 2, lineHeight = 10.dp)
                    Spacer(Modifier.height(HeliosSpacing.space2))
                    SkeletonBlock(height = 10.dp, modifier = Modifier.fillMaxWidth(0.3f))
                }
            }
        }
    }
}

/**
 * Nothing to advise.
 *
 * Two different silences get two different sentences: a clean system is not the same as a
 * paused engine, and only the second one offers a retry.
 */
@Composable
fun InsightFeedEmpty(
    paused: Boolean,
    message: String?,
    actionLabel: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        title = if (paused) "Waiting for the inverter" else "No advisories right now",
        message = message ?: if (paused) {
            "Advisories are paused while the reading is stale, because advice cannot rest on data " +
                "the app cannot stand behind. The forecast and the savings above are still usable."
        } else {
            "The system is running cleanly. Nothing needs your attention today."
        },
        actionLabel = if (paused && actionLabel != null) actionLabel else null,
        onAction = if (paused && actionLabel != null) onRetry else null,
        modifier = modifier
    )
}

/** The feed failed while the rest of the screen works (F8 shape, applied to advisories). */
@Composable
fun InsightFeedError(
    message: String,
    detail: String?,
    actionLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    HeliosErrorState(
        title = "Advisories unavailable",
        message = message,
        detail = detail,
        actionLabel = actionLabel,
        onAction = onRetry,
        modifier = modifier
    )
}

/** The "Show all advisories" action of ACT-048, with the count it reveals. */
@Composable
fun InsightFeedMore(
    hiddenCount: Int,
    onShowAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    HeliosGhostButton(
        text = if (hiddenCount == 1) "Show 1 more advisory" else "Show $hiddenCount more advisories",
        onClick = onShowAll,
        modifier = modifier
    )
}
