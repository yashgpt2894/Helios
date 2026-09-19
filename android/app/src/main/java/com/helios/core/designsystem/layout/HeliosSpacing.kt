package com.helios.core.designsystem.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing and layout tokens from `design/tokens.json` (`spacing`, `layout`).
 *
 * Spacing is a 4 dp grid. Screens must use these values rather than literal dp, so the
 * rhythm stays measurable: gutter 20, section rhythm 20, card padding 16.
 */
object HeliosSpacing {

    val space0: Dp = 0.dp
    val space1: Dp = 4.dp
    val space2: Dp = 8.dp
    val space3: Dp = 12.dp
    val space4: Dp = 16.dp
    val space5: Dp = 20.dp
    val space6: Dp = 24.dp
    val space7: Dp = 28.dp
    val space8: Dp = 32.dp
    val space9: Dp = 36.dp
    val space10: Dp = 40.dp
    val space11: Dp = 44.dp
    val space12: Dp = 48.dp
    val space14: Dp = 56.dp
    val space16: Dp = 64.dp
    val space20: Dp = 80.dp
    val space24: Dp = 96.dp

    /** Screen gutter (`spacing.safeArea.horizontal`). */
    val gutter: Dp = 20.dp

    /** Vertical rhythm between sections. */
    val sectionRhythm: Dp = 20.dp

    /** Padding inside a card or tile. */
    val cardPadding: Dp = 16.dp

    /** Minimum touch target for every interactive element. */
    val minTouchTarget: Dp = 48.dp

    /** The 4 dp grid by step, for generated or computed layouts. */
    fun scale(step: Int): Dp = (step * 4).dp

    object LayoutMetrics {
        val referenceViewportWidth: Dp = 412.dp
        val referenceViewportHeight: Dp = 915.dp

        /** TopBar through the first section must end inside this height (DESIGN.md 6). */
        val heroBlockMax: Dp = 430.dp
        val topBarHeight: Dp = 64.dp
        val bottomNavHeight: Dp = 80.dp
        val chartHeight: Dp = 160.dp
        val heroChartHeight: Dp = 220.dp
        val forecastStripItemWidth: Dp = 72.dp

        /** Above this text scale the metric grid collapses to one column. */
        const val metricGridCollapseFontScale = 1.6f

        /** The hero number caps here at 200 percent text scaling (DESIGN.md 9). */
        const val heroCapSp = 44
    }
}
