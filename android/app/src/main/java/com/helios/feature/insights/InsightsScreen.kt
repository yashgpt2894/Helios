package com.helios.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.FreshnessStamp
import com.helios.core.designsystem.component.SurfaceState
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.Insight
import com.helios.core.domain.model.InsightCategory

/**
 * SCR-09 Insights and savings (tab destination 3 of 5), in the selected direction.
 *
 * Order: what we noticed (the one ranked advisory), the 7-day outlook, what the system
 * saved, then the whole advisory feed. The feed is unbounded, so the screen is one
 * [LazyColumn]: the highlight, the sections and the header are items, and the advisories
 * are the variable-length tail.
 *
 * The state argument carries every variant: ready, loading, empty feed, paused feed,
 * failed feed, failed forecast, unreadable savings, demo qualifier and long content.
 *
 * Actions: the category filter (a real filter over the feed), "Show more advisories"
 * (ACT-048), the savings disclosure (ACT-047), an advisory action (ACT-044) through
 * [onInsightAction], forecast retry (ACT-043) through [onRetry], and "Use my location"
 * (ACT-042) through [onUseMyLocation]. Each is only rendered when the caller can honour it,
 * so the screen never shows a control that does nothing.
 *
 * Not implemented in this step, and not faked: the advisory overflow menu and its copy
 * action (ACT-045, ACT-046). They need a scaffold, a snackbar and a dated dismissal
 * preference, which belong to the app shell rather than to this screen.
 */
@Composable
fun InsightsScreen(
    state: InsightsScreenState = rememberInsightsFixtureState(),
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onInsightAction: ((Insight) -> Unit)? = null,
    onUseMyLocation: (() -> Unit)? = null,
    onOpenConnection: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    var selectedCategory by remember { mutableStateOf(state.selectedCategory) }
    var showAll by remember { mutableStateOf(state.showAll) }
    var savingsExpanded by remember { mutableStateOf(state.savingsExpanded) }

    val feedItems = state.feedItems(selectedCategory = selectedCategory, showAll = showAll)
    val hidden = state.hiddenCount(selectedCategory = selectedCategory, showAll = showAll)
    val highlight = state.highlight

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary),
        contentPadding = PaddingValues(bottom = HeliosSpacing.LayoutMetrics.bottomNavHeight)
    ) {
        item(key = "header") {
            InsightsHeader(state = state, onOpenConnection = onOpenConnection)
        }

        if (highlight != null && state.feedSurface != SurfaceState.LOADING) {
            item(key = "highlight") {
                InsightsEntrance(step = 0, reducedMotion = state.reducedMotion) {
                    Gutter {
                        InsightHighlightCard(
                            insight = highlight,
                            demoQualifier = state.showDemoQualifier,
                            onAction = onInsightAction?.let { action -> { action(highlight) } }
                        )
                    }
                }
            }
        }

        item(key = "forecast") {
            InsightsEntrance(step = 1, reducedMotion = state.reducedMotion) {
                Gutter {
                    ForecastSection(
                        state = state,
                        onRetry = onRetry,
                        onUseMyLocation = onUseMyLocation
                    )
                }
            }
        }

        item(key = "savings") {
            InsightsEntrance(step = 1, reducedMotion = state.reducedMotion) {
                Gutter {
                    SavingsSection(
                        state = state,
                        expanded = savingsExpanded,
                        onExpandedChange = { savingsExpanded = it }
                    )
                }
            }
        }

        item(key = "feed-header") {
            InsightsEntrance(step = 2, reducedMotion = state.reducedMotion) {
                Gutter {
                    InsightFeedHeader(
                        activeCount = state.orderedFeed.size,
                        categories = state.categoryCounts,
                        selected = selectedCategory,
                        onSelect = { category ->
                            selectedCategory = category
                            showAll = false
                        }
                    )
                }
            }
        }

        when (state.feedSurface) {
            SurfaceState.LOADING -> items(count = 3, key = { index -> "feed-skeleton-$index" }) {
                Gutter { InsightFeedSkeleton() }
            }

            SurfaceState.EMPTY -> item(key = "feed-empty") {
                Gutter {
                    InsightFeedEmpty(
                        paused = state.feedPaused,
                        message = state.feedMessage,
                        actionLabel = state.feedActionLabel,
                        onRetry = onRetry
                    )
                }
            }

            SurfaceState.ERROR -> item(key = "feed-error") {
                Gutter {
                    InsightFeedError(
                        message = state.feedFailureMessage,
                        detail = state.feedFailureDetail,
                        actionLabel = state.feedFailureAction,
                        onRetry = onRetry
                    )
                }
            }

            else -> {
                items(items = feedItems, key = { insight -> insight.id }) { insight ->
                    Gutter {
                        InsightFeedRow(
                            insight = insight,
                            demoQualifier = state.showDemoQualifier,
                            onAction = onInsightAction?.let { action -> { action(insight) } }
                        )
                    }
                }
                if (hidden > 0) {
                    item(key = "feed-more") {
                        Gutter { InsightFeedMore(hiddenCount = hidden, onShowAll = { showAll = true }) }
                    }
                }
            }
        }
    }
}

/** Identity, what the advisory list is made of, and how old it is (C1). */
@Composable
private fun InsightsHeader(
    state: InsightsScreenState,
    onOpenConnection: (() -> Unit)?
) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = HeliosSpacing.gutter,
                end = HeliosSpacing.gutter,
                top = HeliosSpacing.space5,
                bottom = HeliosSpacing.space2
            )
    ) {
        Text(
            text = "HELIOS\u00B0 INTELLIGENCE",
            style = HeliosTypography.caption2,
            color = colors.textTertiary
        )
        Spacer(Modifier.height(HeliosSpacing.space2))
        Text(
            text = "What we noticed today",
            style = HeliosTypography.title2,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(HeliosSpacing.space1))
        Text(
            text = state.subtitle,
            style = HeliosTypography.callout,
            color = colors.textSecondary
        )
        Spacer(Modifier.height(HeliosSpacing.space2))
        Row(
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = "Advisory freshness, ${state.feedFreshnessText}"
            },
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
        ) {
            FreshnessStamp(
                text = state.feedFreshnessText,
                kind = state.feedFreshnessKind,
                onClick = onOpenConnection
            )
            if (state.showDemoQualifier) {
                Text(
                    text = "Demo data",
                    style = HeliosTypography.caption2,
                    color = colors.textTertiary
                )
            }
        }
    }
}

/** The screen gutter and rhythm for one member of the list. */
@Composable
private fun Gutter(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HeliosSpacing.gutter, vertical = HeliosSpacing.space3)
    ) {
        content()
    }
}
