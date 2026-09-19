package com.helios.debug.gallery

import androidx.compose.runtime.Composable

/**
 * The gallery is a list of small sections packed into pages that each fit one screen.
 *
 * Why pages and not one long scroll: a capture has to be verifiable. Every page ends with
 * [GalleryEndMarker], a solid magenta stripe, so a screenshot can be checked
 * programmatically for "the whole page is visible" instead of being trusted. A page that
 * overflows loses its marker, and the fix is a smaller page, not a blind retry.
 */
data class GallerySectionSpec(
    val title: String,
    /** Estimated height in dp, used only to pack pages; over-estimate rather than clip. */
    val estimateDp: Int,
    val content: @Composable (GalleryContext) -> Unit
)

/** Content budget per page: 800 dp screen minus the header, insets and the marker. */
const val GALLERY_PAGE_BUDGET_DP = 380

/** Every section, in review order. */
val gallerySections: List<GallerySectionSpec> = listOf(
    GallerySectionSpec("Tokens in use", 420) { TokenStrip(it) },
    GallerySectionSpec("Live dashboard: identity, state and metrics", 430) { LiveDashboardTop(it) },
    GallerySectionSpec("Live dashboard: energy flow and today's curve", 380) { LiveDashboardEnergy(it) },
    GallerySectionSpec("Live dashboard: forecast and advisories", 260) { LiveDashboardTail(it) },
    GallerySectionSpec("Energy flow: live and night", 330) { EnergyFlowLiveNight(it) },
    GallerySectionSpec("Energy flow: stale, offline, reduced motion", 470) { EnergyFlowDegraded(it) },
    GallerySectionSpec("Battery ring: charging and discharging", 330) { BatteryRingCharging(it) },
    GallerySectionSpec("Battery ring: idle, below 20 percent, no data", 460) { BatteryRingIdleAndMissing(it) },
    GallerySectionSpec("Production chart: ready and empty", 400) { ProductionChartReadyEmpty(it) },
    GallerySectionSpec("Production chart: loading and stale", 400) { ProductionChartLoadingStale(it) },
    GallerySectionSpec("Week chart: ready with a selected bar", 290) { WeekChartReady(it) },
    GallerySectionSpec("Week chart: loading and empty", 400) { WeekChartLoadingEmpty(it) },
    GallerySectionSpec("Week chart: error", 220) { WeekChartError(it) },
    GallerySectionSpec("Metric tile: default, sparkline, deltas", 330) { MetricTilesBasics(it) },
    GallerySectionSpec("Metric tile: subtle, disabled, missing, long", 420) { MetricTilesEdge(it) },
    GallerySectionSpec("Live number: updating, static, stale, demo", 190) { LiveNumberStates(it) },
    GallerySectionSpec("Status pill: every status word", 390) { StatusPillStates(it) },
    GallerySectionSpec("Freshness stamp: live to demo", 210) { FreshnessStates(it) },
    GallerySectionSpec("Insight highlight: four severities", 430) { InsightHighlightStates(it) },
    GallerySectionSpec("Insight card: positive and neutral", 340) { InsightCardPositiveNeutral(it) },
    GallerySectionSpec("Insight card: attention and critical with action", 360) { InsightCardAttentionCritical(it) },
    GallerySectionSpec("Insight card: long body", 240) { InsightCardLongBody(it) },
    GallerySectionSpec("Forecast strip: ready, loading, empty", 420) { ForecastStripStatesTop(it) },
    GallerySectionSpec("Forecast strip: stale and error", 300) { ForecastStripStatesBottom(it) },
    GallerySectionSpec("Forecast card: ready", 430) { ForecastCardReady(it) },
    GallerySectionSpec("Forecast card: loading and empty", 470) { ForecastCardStatesMid(it) },
    GallerySectionSpec("Forecast card: error", 240) { ForecastCardError(it) },
    GallerySectionSpec("Forecast card: stale", 460) { ForecastCardStale(it) },
    GallerySectionSpec("Section header variants", 220) { SectionHeaderStates(it) },
    GallerySectionSpec("Weather icon: all ten conditions", 240) { WeatherIconStates(it) },
    GallerySectionSpec("Brand marks: radial and text", 220) { MarkStates(it) },
    GallerySectionSpec("Bottom nav: five destinations", 190) { BottomNavReady(it) },
    GallerySectionSpec("Bottom nav: each destination selected", 470) { BottomNavSelected(it) },
    GallerySectionSpec("Top bar: connected and demo", 260) { TopBarLive(it) },
    GallerySectionSpec("Top bar: stale, offline, white label", 300) { TopBarDegraded(it) },
    GallerySectionSpec("Buttons and chips", 330) { ButtonStates(it) },
    GallerySectionSpec("Share sheet: idle", 620) { ShareSheetIdle(it) },
    GallerySectionSpec("Share sheet: copied", 620) { ShareSheetCopied(it) },
    GallerySectionSpec("Share sheet: failed", 620) { ShareSheetFailed(it) },
    GallerySectionSpec("Snapshot summary rows", 170) { SnapshotSummaryRows(it) },
    GallerySectionSpec("Empty states", 340) { EmptyStates(it) },
    GallerySectionSpec("Error state", 240) { ErrorStateOnly(it) },
    GallerySectionSpec("Denied permission and skeletons", 400) { DeniedAndSkeletonStates(it) },
    GallerySectionSpec("Connection banner: offline to recovered", 360) { BannerStates(it) },
    GallerySectionSpec("Live trail and sparkline", 300) { TrailStates(it) },
    GallerySectionSpec("Service contracts: all nine interfaces", 440) { ServiceContractStates(it) },
    GallerySectionSpec("Snapshot payload v1", 260) { SnapshotPayloadStates(it) }
)

/** Greedy pack: keep section order, never exceed the budget. */
fun packGalleryPages(
    sections: List<GallerySectionSpec> = gallerySections,
    budgetDp: Int = GALLERY_PAGE_BUDGET_DP
): List<List<GallerySectionSpec>> {
    val pages = mutableListOf<MutableList<GallerySectionSpec>>()
    var current = mutableListOf<GallerySectionSpec>()
    var used = 0
    sections.forEach { section ->
        if (current.isNotEmpty() && used + section.estimateDp > budgetDp) {
            pages.add(current)
            current = mutableListOf()
            used = 0
        }
        current.add(section)
        used += section.estimateDp
    }
    if (current.isNotEmpty()) pages.add(current)
    return pages
}

/** The packed pages, computed once. */
val galleryPages: List<List<GallerySectionSpec>> = packGalleryPages()

val GALLERY_PAGE_COUNT: Int = galleryPages.size

/** Page title: the sections it contains, in order. */
fun galleryPageTitle(page: Int): String =
    galleryPages.getOrNull(page)?.joinToString(" \u00B7 ") { it.title } ?: "Page ${page + 1}"
