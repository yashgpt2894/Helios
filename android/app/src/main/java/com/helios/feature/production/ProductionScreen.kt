package com.helios.feature.production

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.helios.core.data.service.Freshness
import com.helios.core.data.service.Loadable
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.SectionHeader
import com.helios.core.designsystem.component.WeekBar
import com.helios.core.designsystem.component.WeekChart
import com.helios.core.designsystem.component.toSurfaceState
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.format.HeliosFormat
import com.helios.feature.dashboard.DashboardMetrics
import com.helios.feature.dashboard.DashboardTab
import com.helios.feature.dashboard.HeliosDestinations
import com.helios.feature.dashboard.BottomNav
import com.helios.feature.dashboard.HeliosMotionSettings
import com.helios.feature.dashboard.MetricGrid
import com.helios.feature.dashboard.MetricTileSpec
import com.helios.feature.dashboard.MetricTileState
import com.helios.feature.dashboard.ProductionChart
import com.helios.feature.dashboard.ReadingNotice
import com.helios.feature.dashboard.TopBar
import com.helios.feature.dashboard.rememberHeliosMotionSettings
import com.helios.core.domain.model.InverterStatus

/**
 * Production: the study screen.
 *
 * Where the Dashboard answers "is my system working right now", this screen answers "why is
 * today shaped like this": the day's curve is the hero and it can be scrubbed to any half
 * hour, the per-string cards show which part of the array is underperforming, the week chart
 * puts today in context, and the telemetry grid carries the inverter's own registers for a
 * fault diagnosis.
 *
 * Everything it draws arrives as [ProductionState] and every interaction is a handler the host
 * supplied, so a state can be captured on its own and no control is a dead end.
 */
@Composable
fun ProductionScreen(
    state: ProductionState = ProductionFixtures.state(),
    actions: ProductionActions = ProductionActions(),
    motion: HeliosMotionSettings? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    val motionSettings = motion ?: rememberHeliosMotionSettings()
    val scroll = rememberScrollState()
    val telemetry = state.telemetryValue

    // The strings are only "producing" when the array is; a string at 12 percent of its
    // rating at midnight is idle, not shaded.
    val arrayProducing = telemetry != null &&
        telemetry.acPowerW > 1.0 &&
        telemetry.status == InverterStatus.PRODUCING

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
                subTitle = "Production",
                statusKind = state.reading.statusKind,
                statusLabel = state.reading.statusLabel,
                freshnessText = state.reading.freshnessText,
                freshnessKind = state.reading.freshnessKind,
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

                CurveBlock(state = state, actions = actions, motion = motionSettings)

                Spacer(Modifier.height(HeliosSpacing.sectionRhythm))

                TotalsBlock(state = state)

                Spacer(Modifier.height(HeliosSpacing.sectionRhythm))

                StringsBlock(state = state, arrayProducing = arrayProducing)

                Spacer(Modifier.height(HeliosSpacing.sectionRhythm))

                WeekBlock(state = state, actions = actions)

                Spacer(Modifier.height(HeliosSpacing.sectionRhythm))

                InverterBlock(state = state)

                Spacer(Modifier.height(HeliosSpacing.space6))
                if (actions.onNavigate != null) {
                    Spacer(Modifier.height(HeliosSpacing.LayoutMetrics.bottomNavHeight))
                } else {
                    Spacer(Modifier.height(HeliosSpacing.space6).navigationBarsPadding())
                }
            }

            if (actions.onNavigate != null) {
                BottomNav(
                    tabs = HeliosDestinations.default(),
                    selectedIndex = state.selectedTab,
                    onTabSelected = actions.onNavigate,
                    motion = motionSettings
                )
            }
        }
    }
}

/** The hero: today's curve, scrubable to any half hour. */
@Composable
private fun CurveBlock(
    state: ProductionState,
    actions: ProductionActions,
    motion: HeliosMotionSettings
) {
    val colors = LocalHeliosSemanticColors.current
    val todayKwh = state.telemetryValue?.energyTodayKwh ?: Double.NaN
    val points = (state.todaySeries as? Loadable.Ready)?.value.orEmpty()
    SectionHeader(
        title = "Today's curve",
        eyebrow = "Production",
        modifier = Modifier.padding(horizontal = HeliosSpacing.gutter),
        trailing = if (actions.onNavigate != null) {
            {
                HeliosGhostButton(
                    text = "Insights",
                    onClick = { actions.onNavigate.invoke(DashboardTab.INSIGHTS) }
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
        height = HeliosSpacing.LayoutMetrics.heroChartHeight,
        nowHour = state.nowHour,
        selectedT = state.selectedT,
        onSelect = actions.onSelectHour,
        scrubEnabled = actions.onSelectHour != null,
        motion = motion,
        onRetry = actions.onRetry
    )
    // The caption describes the curve only when there is a curve: a loading skeleton, an empty
    // day and a failed series each say what they are above, and "No data across 0 half hours"
    // would be noise under them.
    if (state.todaySeries is Loadable.Ready) {
        Spacer(Modifier.height(HeliosSpacing.space2))
        Text(
            text = "Today's total ${HeliosFormat.kwh(todayKwh, decimals = 1)} across " +
                "${points.count { it.productionW > 1.0 }} half hours with output." +
                if (actions.onSelectHour != null) " Drag the plot to read any half hour." else "",
            style = HeliosTypography.caption2,
            color = colors.textQuaternary,
            modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
        )
    }
}

/** The energy totals: today, the month, the array's lifetime and how much was self-used. */
@Composable
private fun TotalsBlock(state: ProductionState) {
    val telemetry = state.telemetryValue
    val series = (state.todaySeries as? Loadable.Ready)?.value.orEmpty()
    val selfUse = DashboardMetrics.selfUsePercent(series)
    val tileState = if (state.reading.dimmed) MetricTileState.STALE else MetricTileState.DEFAULT
    SectionHeader(
        title = "Energy",
        eyebrow = "Totals",
        modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
    )
    Spacer(Modifier.height(HeliosSpacing.space3))
    MetricGrid(
        tiles = listOf(
            MetricTileSpec(
                label = "Today",
                value = DashboardMetrics.number(telemetry?.energyTodayKwh ?: Double.NaN, 1),
                unit = "kWh",
                sparkline = DashboardMetrics.sparkline(series),
                state = tileState
            ),
            MetricTileSpec(
                label = "This month",
                value = DashboardMetrics.number(telemetry?.energyMonthKwh ?: Double.NaN, 1),
                unit = "kWh",
                state = tileState
            ),
            MetricTileSpec(
                label = "Lifetime",
                value = DashboardMetrics.number((telemetry?.energyLifetimeKwh ?: Double.NaN) / 1000.0, 2),
                unit = "MWh",
                state = tileState
            ),
            MetricTileSpec(
                label = "Self-use",
                value = DashboardMetrics.number(selfUse, 0),
                unit = "%",
                delta = "of today's production",
                state = tileState
            )
        ),
        modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
    )
}

/**
 * The per-string cards.
 *
 * A string that reported is a card with its own output, utilisation and electrical values; a
 * string that did not report keeps its row and says so, instead of disappearing from the list
 * and leaving the owner to notice that three became two.
 */
@Composable
private fun StringsBlock(state: ProductionState, arrayProducing: Boolean) {
    SectionHeader(
        title = "Panel strings",
        eyebrow = "Per string",
        modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
    )
    Spacer(Modifier.height(HeliosSpacing.space3))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HeliosSpacing.gutter),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
    ) {
        if (state.strings.isEmpty() && state.missingStrings.isEmpty()) {
            Text(
                text = "The inverter reported no strings in this reading.",
                style = HeliosTypography.callout,
                color = LocalHeliosSemanticColors.current.textSecondary
            )
        }
        state.strings.forEach { string ->
            PerStringCard(string = string, arrayProducing = arrayProducing)
        }
        state.missingStrings.forEach { name ->
            MissingStringCard(name = name)
        }
    }
}

/** The week in context: produced against consumed, with a selectable day. */
@Composable
private fun WeekBlock(
    state: ProductionState,
    actions: ProductionActions
) {
    val colors = LocalHeliosSemanticColors.current
    val week = (state.weekSeries as? Loadable.Ready)?.value.orEmpty()
    val produced = week.sumOf { it.producedKwh }
    val consumed = week.sumOf { it.consumedKwh }
    SectionHeader(
        title = "This week",
        eyebrow = "Seven days",
        modifier = Modifier.padding(horizontal = HeliosSpacing.gutter),
        trailing = {
            Text(
                text = if (week.isEmpty()) "" else "${HeliosFormat.kwh(produced, decimals = 1)} produced",
                style = HeliosTypography.caption2,
                color = colors.textTertiary
            )
        }
    )
    Spacer(Modifier.height(HeliosSpacing.space3))
    WeekChart(
        bars = week.map { WeekBar(dayLabel = it.dayLabel, producedKwh = it.producedKwh, consumedKwh = it.consumedKwh) },
        modifier = Modifier.padding(horizontal = HeliosSpacing.gutter),
        selectedIndex = state.selectedWeekIndex,
        onSelect = actions.onSelectWeekDay,
        state = state.weekSeries.toSurfaceState(),
        height = HeliosSpacing.LayoutMetrics.chartHeight
    )
    if (week.isNotEmpty()) {
        Spacer(Modifier.height(HeliosSpacing.space3))
        Row(
            modifier = Modifier.padding(horizontal = HeliosSpacing.gutter),
            horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendSwatch(color = colors.chart.production, label = "Produced")
            LegendSwatch(color = colors.chart.consumption, label = "Consumed")
            Text(
                text = "Week total ${HeliosFormat.kwh(produced, 1)} against " +
                    "${HeliosFormat.kwh(consumed, 1)} used",
                style = HeliosTypography.caption2,
                color = colors.textQuaternary
            )
        }
    }
}

@Composable
private fun LegendSwatch(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(HeliosSpacing.space1))
        Text(
            text = label,
            style = HeliosTypography.caption2,
            color = LocalHeliosSemanticColors.current.textTertiary
        )
    }
}

/** The inverter's identity and its registers, including the ones that did not report. */
@Composable
private fun InverterBlock(state: ProductionState) {
    val colors = LocalHeliosSemanticColors.current
    val telemetry = state.telemetryValue
    SectionHeader(
        title = "Inverter",
        eyebrow = "Telemetry",
        modifier = Modifier.padding(horizontal = HeliosSpacing.gutter)
    )
    Spacer(Modifier.height(HeliosSpacing.space3))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HeliosSpacing.gutter),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
    ) {
        InverterIdentity(telemetry = telemetry)
        TelemetryGrid(rows = ProductionTelemetry.rows(telemetry))
        if (state.reading.missingFields.isNotEmpty()) {
            Text(
                text = "Not in this reading: ${state.reading.missingFields.joinToString(", ")}. " +
                    "A field that did not report is shown as no data, never as zero.",
                style = HeliosTypography.caption2,
                color = colors.textQuaternary
            )
        }
        if (state.reading.freshness == Freshness.DEMO) {
            Text(
                text = "Values come from the demo system, so they describe the reference array " +
                    "rather than this house.",
                style = HeliosTypography.caption2,
                color = colors.textQuaternary
            )
        }
    }
}
