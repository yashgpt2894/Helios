package com.helios.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import com.helios.core.data.service.Freshness
import com.helios.core.data.service.Loadable
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.SectionHeader
import com.helios.core.designsystem.component.SkeletonLines
import com.helios.core.designsystem.component.toSeverityKind
import com.helios.core.designsystem.component.toSurfaceState
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography

/**
 * The Dashboard: helios in one instrument.
 *
 * Reading order is the selected direction (design direction D1, `design/DESIGN.md` section
 * 6): identity and state, the live output with the four-endpoint energy model and the
 * ledger, the rank-1 advisory, the five-day glance, today's curve, then the totals grid.
 * The screen is read-only apart from share, drill-down and connection recovery, which is why
 * there is no primary call to action.
 *
 * Everything it draws arrives as [DashboardState]; nothing here reads a service, starts a
 * coroutine or looks at the clock. That is what makes every declared state renderable — the
 * same call with an offline, stale, partial, empty, loading or long-content state from
 * `FixtureScenario` produces the screen for that state, and a host can therefore capture it.
 *
 * Motion durations come from [HeliosMotionSettings], which resolves the token durations
 * against Reduce Motion and the device animation scale.
 */
@Composable
fun DashboardScreen(
    state: DashboardState = DashboardFixtures.state(),
    actions: DashboardActions = DashboardActions(),
    motion: HeliosMotionSettings? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    val motionSettings = motion ?: rememberHeliosMotionSettings()
    val scroll = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.backgroundPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            TopBar(
                brandName = state.brand.name,
                subTitle = "Dashboard",
                statusKind = state.statusKind,
                statusLabel = state.statusLabel,
                freshnessText = state.freshnessText,
                freshnessKind = state.freshnessKind,
                usesRadialMark = state.brand.mark != "text",
                textMark = state.brand.textMark,
                onFreshnessClick = actions.onOpenConnection,
                onStatusClick = actions.onOpenConnection,
                onShare = actions.onShare
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scroll)
            ) {
                ReadingNotice(
                    reading = state.reading,
                    modifier = Modifier.padding(horizontal = HeliosSpacing.gutter),
                    onRetry = actions.onRetry,
                    onOpenConnection = actions.onOpenConnection
                )

                HeroBlock(
                    state = state,
                    motion = motionSettings,
                    actions = actions
                )

                Spacer(Modifier.height(HeliosSpacing.sectionRhythm))

                AdvisoryBlock(state = state, actions = actions)

                Spacer(Modifier.height(HeliosSpacing.sectionRhythm))

                ForecastBlock(state = state, actions = actions)

                Spacer(Modifier.height(HeliosSpacing.sectionRhythm))

                TodayCurveBlock(state = state, motion = motionSettings, actions = actions)

                Spacer(Modifier.height(HeliosSpacing.sectionRhythm))

                TotalsBlock(state = state, actions = actions)

                Spacer(Modifier.height(HeliosSpacing.space6))
                if (actions.onNavigate != null) {
                    Spacer(Modifier.height(HeliosSpacing.LayoutMetrics.bottomNavHeight))
                } else {
                    Spacer(Modifier.height(HeliosSpacing.space6).navigationBarsPadding())
                }
            }

            if (actions.onNavigate != null) {
                BottomNav(
                    tabs = HeliosDestinations.default(insightBadge = state.insightBadgeCount),
                    selectedIndex = state.selectedTab,
                    onTabSelected = actions.onNavigate,
                    motion = motionSettings
                )
            }
        }
    }
}

/**
 * The hero: the four-endpoint energy model with the live output in the middle, and the
 * three-cell ledger under a hairline.
 *
 * The block is one raised surface, so the instrument reads as a single object rather than as
 * a stack of cards.
 */
@Composable
private fun HeroBlock(
    state: DashboardState,
    motion: HeliosMotionSettings,
    actions: DashboardActions
) {
    val colors = LocalHeliosSemanticColors.current
    val telemetry = state.telemetryValue
    val series = (state.todaySeries as? Loadable.Ready)?.value.orEmpty()
    val producedKwh = telemetry?.energyTodayKwh ?: Double.NaN
    val selfUse = DashboardMetrics.selfUsePercent(series)
    val co2 = DashboardMetrics.co2Kg(producedKwh)
    val liveState = when {
        state.freshness == Freshness.DEMO -> LiveNumberState.DEMO
        state.freshness == Freshness.OFFLINE -> LiveNumberState.OFFLINE
        state.freshness == Freshness.STALE -> LiveNumberState.STALE
        state.freshness == Freshness.LIVE -> LiveNumberState.UPDATING
        else -> LiveNumberState.STATIC
    }
    val acPowerW = telemetry?.acPowerW ?: Double.NaN
    // The four paths are made to describe one system before they are drawn: the registers are
    // reported independently, so the battery takes what is left after the house and the grid
    // carries the residual. See DashboardMetrics.flowSample.
    val flow = DashboardMetrics.flowSample(
        solarW = acPowerW,
        homeW = telemetry?.homeLoadW ?: Double.NaN,
        batteryW = telemetry?.batteryPowerW ?: Double.NaN,
        gridW = when {
            telemetry == null -> Double.NaN
            telemetry.gridExportW > 0 -> telemetry.gridExportW
            else -> -telemetry.gridImportW
        }
    )

    Column(
        modifier = Modifier
            .padding(horizontal = HeliosSpacing.gutter)
            .fillMaxWidth()
            .clip(HeliosShape.lg)
            .background(colors.backgroundTertiary)
            .border(
                HeliosElevation.MaterialTokens.innerStrokeWidth,
                colors.separatorHairline,
                HeliosShape.lg
            )
            .padding(horizontal = HeliosSpacing.space3, vertical = HeliosSpacing.space2)
    ) {
        EnergyFlow(
            hubValue = if (acPowerW.isNaN()) Double.NaN else acPowerW / 1000.0,
            hubUnit = "kW",
            hubLabel = "Live output",
            solarW = flow.solarW,
            homeW = flow.homeW,
            batteryW = flow.batteryW,
            gridW = flow.gridW,
            state = state.surface,
            motion = motion,
            liveState = liveState,
            // Measured on the emulator: the instrument needs 240 dp for its two node rows and
            // the hub (at 220 dp the lower row is clipped by the panel), and with the TopBar and
            // the ledger the block then ends 406 dp below the top of the TopBar, inside the
            // 430 dp budget of design/DESIGN.md section 6. So the ledger is readable without a
            // scroll on a 412 x 915 dp viewport, and all four endpoints are visible.
            flowHeight = 240.dp,
            onNodeClick = { node ->
                when (node) {
                    EnergyNode.SOLAR -> actions.onOpenProduction?.invoke()
                    EnergyNode.BATTERY -> actions.onNavigate?.invoke(DashboardTab.BATTERY)
                    EnergyNode.GRID, EnergyNode.HOME -> actions.onNavigate?.invoke(DashboardTab.INSIGHTS)
                }
            }
        )
        Spacer(Modifier.height(HeliosSpacing.space2))
        EnergyLedger(
            cells = listOf(
                EnergyLedgerCell(label = "Today", value = DashboardMetrics.number(producedKwh, 1), qualifier = "kWh"),
                EnergyLedgerCell(label = "Self-use", value = DashboardMetrics.number(selfUse, 0), qualifier = "%"),
                EnergyLedgerCell(label = "CO2 avoided", value = DashboardMetrics.number(co2, 1), qualifier = "kg")
            ),
            dimmed = state.dimmed,
            place = state.forecastPlace
        )
    }
}

/**
 * The rank-1 advisory, or the placeholder when the data cannot support advice (design C3),
 * or the quiet empty line when there simply is nothing to report.
 */
@Composable
private fun AdvisoryBlock(state: DashboardState, actions: DashboardActions) {
    val colors = LocalHeliosSemanticColors.current
    val insights = (state.insights as? Loadable.Ready)?.value.orEmpty()
    val first = insights.firstOrNull()

    SectionHeader(
        title = "Advisory",
        modifier = Modifier.padding(horizontal = HeliosSpacing.gutter),
        trailing = if (insights.size > 1 && actions.onNavigate != null) {
            {
                HeliosGhostButton(
                    text = "All ${insights.size}",
                    onClick = { actions.onNavigate.invoke(DashboardTab.INSIGHTS) }
                )
            }
        } else {
            null
        }
    )
    Spacer(Modifier.height(HeliosSpacing.space3))

    val placeholder = state.insightPlaceholder
    when {
        placeholder != null -> InsightWaitingPlaceholder(
            reason = placeholder,
            modifier = Modifier.padding(horizontal = HeliosSpacing.gutter),
            actionLabel = if (actions.onRetry != null) "Retry now" else null,
            onAction = actions.onRetry
        )

        state.insights is Loadable.Loading -> SkeletonLines(
            lines = 3,
            modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
        )

        first != null -> InsightHighlight(
            insight = first,
            severity = first.severity.toSeverityKind(),
            demoQualifier = state.simulated,
            modifier = Modifier.padding(horizontal = HeliosSpacing.gutter),
            onAction = actions.onInsightAction?.let { handler -> { handler(first) } }
        )

        else -> Text(
            text = (state.insights as? Loadable.Failed)?.failure?.message
                ?: (state.insights as? Loadable.Empty)?.message
                ?: "No advisories for the current reading.",
            style = HeliosTypography.callout,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
        )
    }
}

/** The five-day glance, with the place the forecast belongs to. */
@Composable
private fun ForecastBlock(state: DashboardState, actions: DashboardActions) {
    val colors = LocalHeliosSemanticColors.current
    val forecast = (state.forecast as? Loadable.Ready)?.value
    SectionHeader(
        title = "Next five days",
        eyebrow = "Forecast",
        modifier = Modifier.padding(horizontal = HeliosSpacing.gutter),
        trailing = {
            Text(
                text = state.forecastPlace,
                style = HeliosTypography.caption2,
                color = colors.textTertiary
            )
        }
    )
    Spacer(Modifier.height(HeliosSpacing.space3))
    ForecastStrip(
        days = forecast?.days?.take(5).orEmpty(),
        state = state.forecast.toSurfaceState(),
        demoQualifier = state.simulated,
        modifier = Modifier.padding(horizontal = HeliosSpacing.gutter),
        onRetry = actions.onRetry
    )
}

/** The glance chart. Scrubbing belongs to the Production screen, where the curve is the hero. */
@Composable
private fun TodayCurveBlock(
    state: DashboardState,
    motion: HeliosMotionSettings,
    actions: DashboardActions
) {
    val colors = LocalHeliosSemanticColors.current
    SectionHeader(
        title = "Today's production",
        modifier = Modifier.padding(horizontal = HeliosSpacing.gutter),
        trailing = if (actions.onOpenProduction != null) {
            {
                Text(
                    text = "Per-string detail",
                    style = HeliosTypography.caption2,
                    color = colors.textTertiary
                )
            }
        } else {
            null
        }
    )
    Spacer(Modifier.height(HeliosSpacing.space3))
    ProductionChart(
        series = state.todaySeries,
        modifier = Modifier.padding(horizontal = HeliosSpacing.gutter),
        height = HeliosSpacing.LayoutMetrics.chartHeight,
        nowHour = state.nowHour,
        scrubEnabled = false,
        selectedT = null,
        onSelect = null,
        motion = motion,
        onRetry = actions.onRetry
    )
}

/** Every number the dashboard does not already carry in the hero: the totals grid. */
@Composable
private fun TotalsBlock(state: DashboardState, actions: DashboardActions) {
    val telemetry = state.telemetryValue
    val series = (state.todaySeries as? Loadable.Ready)?.value.orEmpty()
    val producedKwh = telemetry?.energyTodayKwh ?: Double.NaN
    val homeKwh = DashboardMetrics.kwhFromSeries(series) { it.consumptionW }
    val tileState = if (state.dimmed) MetricTileState.STALE else MetricTileState.DEFAULT
    val spark = remember(series) { DashboardMetrics.sparkline(series) }

    SectionHeader(
        title = "System totals",
        modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
    )
    Spacer(Modifier.height(HeliosSpacing.space3))
    MetricGrid(
        tiles = listOf(
            MetricTileSpec(
                label = "Produced today",
                value = DashboardMetrics.number(producedKwh, 1),
                unit = "kWh",
                sparkline = spark,
                state = tileState
            ),
            MetricTileSpec(
                label = "Home usage",
                value = DashboardMetrics.number(homeKwh, 1),
                unit = "kWh",
                delta = surplusLabel(producedKwh, homeKwh),
                state = tileState
            ),
            MetricTileSpec(
                label = "Lifetime",
                value = DashboardMetrics.number((telemetry?.energyLifetimeKwh ?: Double.NaN) / 1000.0, 2),
                unit = "MWh",
                state = tileState
            ),
            MetricTileSpec(
                label = "Trees equivalent",
                value = DashboardMetrics.number(DashboardMetrics.treesEquivalent(producedKwh), 2),
                unit = "trees",
                delta = "CO2 avoided today",
                state = tileState
            )
        ),
        modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
    )
}

private fun surplusLabel(producedKwh: Double, homeKwh: Double): String = when {
    producedKwh.isNaN() || homeKwh.isNaN() -> "One value did not report"
    producedKwh >= homeKwh -> "${DashboardMetrics.number(producedKwh - homeKwh, 1)} kWh surplus"
    else -> "${DashboardMetrics.number(homeKwh - producedKwh, 1)} kWh from the grid and battery"
}
