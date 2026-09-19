package com.helios.core.designsystem.color

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Semantic colour tokens from `design/tokens.json` (`color.semantic`), resolved for the
 * two themes the app ships: Paper (light) and Carbon (dark).
 *
 * Rules this file keeps, and that the gallery checks:
 *  - every role is a named semantic token, never a raw hue step at the call site;
 *  - the hue ramps in [HeliosColor] are the only source of colour values;
 *  - a white-label accent override replaces the `accent*` roles and nothing else.
 *
 * Ratios in the KDoc come from `design/tokens.json` `verify.contrast` (WCAG 2.1).
 */
@Immutable
data class HeliosSemanticColors(
    val isDark: Boolean,

    // background
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val backgroundTertiary: Color,
    val backgroundElevated: Color,
    val scrim: Color,

    // text
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textQuaternary: Color,
    val textInverse: Color,

    // separator
    val separatorHairline: Color,
    val separatorStrong: Color,

    // accent (white-label overridable)
    val accentPrimary: Color,
    val accentStrong: Color,
    val accentSubtle: Color,
    val onAccent: Color,
    val onSubtle: Color,

    val solarPrimary: Color,
    val solarStrong: Color,
    val solarSubtle: Color,
    val solarDim: Color,

    val flowPrimary: Color,
    val flowStrong: Color,
    val flowSubtle: Color,

    val batteryPrimary: Color,
    val batteryStrong: Color,
    val batterySubtle: Color,
    val batteryLow: Color,

    val gridImportPrimary: Color,
    val gridImportStrong: Color,
    val gridImportSubtle: Color,
    val gridExportPrimary: Color,
    val gridExportStrong: Color,
    val gridExportSubtle: Color,

    val alertPrimary: Color,
    val alertStrong: Color,
    val alertSubtle: Color,
    val onAlert: Color,

    val status: StatusColors,
    val insight: InsightColors,
    val chart: ChartColors,
    val skeleton: SkeletonColors,
    val focusRing: Color
) {
    /**
     * Replace the accent roles for a white-label brand. Semantic hue roles (solar, flow,
     * battery, grid, alert, status, insight) are intentionally not touched, so a brand
     * change can never move the meaning of a reading.
     */
    fun withBrandAccent(accent: Color, accentLight: Color): HeliosSemanticColors {
        // Same ramp steps as design/tokens.json accent.strong (700), accent.subtle (100)
        // and accent.onSubtle (800 for light, 700 for dark).
        val strong = if (isDark) accentLight else darken(accent, 0.3111f)
        val subtle = if (isDark) darken(accent, 0.56f) else mix(backgroundPrimary, accentLight, 0.20f)
        val onSubtle = if (isDark) accentLight else darken(accent, 0.4667f)
        return copy(
            accentPrimary = accent,
            accentStrong = strong,
            accentSubtle = subtle,
            onAccent = if (isDark) Carbon950 else Paper950,
            onSubtle = onSubtle
        )
    }

    private fun mix(from: Color, to: Color, amount: Float): Color = lerp(from, to, amount)

    private fun darken(color: Color, amount: Float): Color = lerp(color, Color.Black, amount)

    companion object {
        private val Paper950 = HeliosColor.Neutral.Light950
        private val Carbon950 = HeliosColor.Neutral.Dark50
    }
}

/** Status pill roles: producing, standby, curtailed, night, fault, offline, demo. */
@Immutable
data class StatusColors(
    val producingFg: Color,
    val producingBg: Color,
    val standbyFg: Color,
    val standbyBg: Color,
    val curtailedFg: Color,
    val curtailedBg: Color,
    val nightFg: Color,
    val nightBg: Color,
    val faultFg: Color,
    val faultBg: Color,
    val offlineFg: Color,
    val offlineBg: Color,
    val demoFg: Color,
    val demoBg: Color
) {
    fun foreground(kind: HeliosStatusKind): Color = when (kind) {
        HeliosStatusKind.PRODUCING -> producingFg
        HeliosStatusKind.STANDBY -> standbyFg
        HeliosStatusKind.CURTAILED -> curtailedFg
        HeliosStatusKind.NIGHT -> nightFg
        HeliosStatusKind.FAULT -> faultFg
        HeliosStatusKind.OFFLINE -> offlineFg
        HeliosStatusKind.DEMO -> demoFg
    }

    fun background(kind: HeliosStatusKind): Color = when (kind) {
        HeliosStatusKind.PRODUCING -> producingBg
        HeliosStatusKind.STANDBY -> standbyBg
        HeliosStatusKind.CURTAILED -> curtailedBg
        HeliosStatusKind.NIGHT -> nightBg
        HeliosStatusKind.FAULT -> faultBg
        HeliosStatusKind.OFFLINE -> offlineBg
        HeliosStatusKind.DEMO -> demoBg
    }
}

/** The seven words a status pill can show. */
enum class HeliosStatusKind {
    PRODUCING,
    STANDBY,
    CURTAILED,
    NIGHT,
    FAULT,
    OFFLINE,
    DEMO
}

/** Insight severity roles. Severity is always also written in words (DESIGN.md 9). */
@Immutable
data class InsightColors(
    val positiveFg: Color,
    val positiveBg: Color,
    val neutralFg: Color,
    val neutralBg: Color,
    val attentionFg: Color,
    val attentionBg: Color,
    val criticalFg: Color,
    val criticalBg: Color
) {
    fun foreground(severity: HeliosSeverityKind): Color = when (severity) {
        HeliosSeverityKind.POSITIVE -> positiveFg
        HeliosSeverityKind.NEUTRAL -> neutralFg
        HeliosSeverityKind.ATTENTION -> attentionFg
        HeliosSeverityKind.CRITICAL -> criticalFg
    }

    fun background(severity: HeliosSeverityKind): Color = when (severity) {
        HeliosSeverityKind.POSITIVE -> positiveBg
        HeliosSeverityKind.NEUTRAL -> neutralBg
        HeliosSeverityKind.ATTENTION -> attentionBg
        HeliosSeverityKind.CRITICAL -> criticalBg
    }
}

enum class HeliosSeverityKind { POSITIVE, NEUTRAL, ATTENTION, CRITICAL }

/** Chart series colours, one per measured quantity. */
@Immutable
data class ChartColors(
    val production: Color,
    val consumption: Color,
    val battery: Color,
    val gridImport: Color,
    val gridExport: Color,
    val axis: Color,
    val gridline: Color,
    val todayMarker: Color
)

@Immutable
data class SkeletonColors(val base: Color, val highlight: Color)

/** Paper: bone canvas, carbon text. Measured pairs are in design/tokens.json. */
val PaperColors = HeliosSemanticColors(
    isDark = false,
    backgroundPrimary = HeliosColor.Neutral.Light50,
    backgroundSecondary = HeliosColor.Neutral.Light100,
    backgroundTertiary = HeliosColor.Neutral.Light200,
    backgroundElevated = HeliosColor.Neutral.Light50,
    scrim = HeliosColor.Neutral.Light950.copy(alpha = 0.48f),
    textPrimary = HeliosColor.Neutral.Light950,
    textSecondary = HeliosColor.Neutral.Light800,
    textTertiary = HeliosColor.Neutral.Light800,
    textQuaternary = HeliosColor.Neutral.Light700,
    textInverse = HeliosColor.Neutral.Light50,
    separatorHairline = HeliosColor.Neutral.Light950.copy(alpha = 0.06f),
    separatorStrong = HeliosColor.Neutral.Light950.copy(alpha = 0.15f),
    // accent.* = the brand ramp for helios (design/tokens.json accent, light theme)
    accentPrimary = Color(0xFFB88A2E),
    accentStrong = Color(0xFF7F5F20),
    accentSubtle = Color(0xFFF6ECD7),
    onAccent = Color(0xFF0B0B0C),
    onSubtle = Color(0xFF624A19),
    solarPrimary = HeliosColor.Solar.Light500,
    solarStrong = HeliosColor.Solar.Light700,
    solarSubtle = HeliosColor.Solar.Light100,
    solarDim = HeliosColor.Solar.Light300,
    flowPrimary = HeliosColor.Flow.Light500,
    flowStrong = HeliosColor.Flow.Light700,
    flowSubtle = HeliosColor.Flow.Light100,
    batteryPrimary = HeliosColor.Battery.Light500,
    batteryStrong = HeliosColor.Battery.Light700,
    batterySubtle = HeliosColor.Battery.Light100,
    batteryLow = HeliosColor.Alert.Light500,
    gridImportPrimary = HeliosColor.GridImport.Light500,
    gridImportStrong = HeliosColor.GridImport.Light800,
    gridImportSubtle = HeliosColor.GridImport.Light100,
    gridExportPrimary = HeliosColor.GridExport.Light500,
    gridExportStrong = HeliosColor.GridExport.Light800,
    gridExportSubtle = HeliosColor.GridExport.Light100,
    alertPrimary = HeliosColor.Alert.Light500,
    alertStrong = HeliosColor.Alert.Light700,
    alertSubtle = HeliosColor.Alert.Light100,
    onAlert = HeliosColor.Neutral.Light50,
    status = StatusColors(
        producingFg = HeliosColor.Flow.Light700,
        producingBg = HeliosColor.Flow.Light100,
        standbyFg = HeliosColor.Neutral.Light800,
        standbyBg = HeliosColor.Neutral.Light200,
        curtailedFg = HeliosColor.Solar.Light700,
        curtailedBg = HeliosColor.Solar.Light100,
        nightFg = HeliosColor.GridExport.Light800,
        nightBg = HeliosColor.GridExport.Light100,
        faultFg = HeliosColor.Alert.Light700,
        faultBg = HeliosColor.Alert.Light100,
        offlineFg = HeliosColor.Neutral.Light800,
        offlineBg = HeliosColor.Neutral.Light300,
        demoFg = HeliosColor.Solar.Light700,
        demoBg = HeliosColor.Solar.Light100
    ),
    insight = InsightColors(
        positiveFg = HeliosColor.Flow.Light700,
        positiveBg = HeliosColor.Flow.Light100,
        neutralFg = HeliosColor.Neutral.Light800,
        neutralBg = HeliosColor.Neutral.Light200,
        attentionFg = HeliosColor.GridImport.Light800,
        attentionBg = HeliosColor.GridImport.Light100,
        criticalFg = HeliosColor.Alert.Light700,
        criticalBg = HeliosColor.Alert.Light100
    ),
    chart = ChartColors(
        production = HeliosColor.Solar.Light500,
        consumption = HeliosColor.Flow.Light500,
        battery = HeliosColor.Battery.Light500,
        gridImport = HeliosColor.GridImport.Light500,
        gridExport = HeliosColor.GridExport.Light500,
        axis = HeliosColor.Neutral.Light800,
        gridline = HeliosColor.Neutral.Light950.copy(alpha = 0.08f),
        todayMarker = HeliosColor.Solar.Light500
    ),
    skeleton = SkeletonColors(
        base = HeliosColor.Neutral.Light300,
        highlight = HeliosColor.Neutral.Light500.copy(alpha = 0.12f)
    ),
    focusRing = HeliosColor.Solar.Light500
)

/** Carbon: carbon canvas, bone text. */
val CarbonColors = HeliosSemanticColors(
    isDark = true,
    backgroundPrimary = HeliosColor.Neutral.Dark50,
    backgroundSecondary = HeliosColor.Neutral.Dark100,
    backgroundTertiary = HeliosColor.Neutral.Dark200,
    backgroundElevated = HeliosColor.Neutral.Dark400.copy(alpha = 0.90f),
    scrim = HeliosColor.Neutral.Dark50.copy(alpha = 0.56f),
    textPrimary = HeliosColor.Neutral.Dark950,
    textSecondary = HeliosColor.Neutral.Dark900,
    textTertiary = HeliosColor.Neutral.Dark900,
    textQuaternary = HeliosColor.Neutral.Dark800,
    textInverse = HeliosColor.Neutral.Dark50,
    separatorHairline = HeliosColor.Neutral.Dark950.copy(alpha = 0.06f),
    separatorStrong = HeliosColor.Neutral.Dark950.copy(alpha = 0.15f),
    // accent.* = the brand ramp for helios (design/tokens.json accent, dark theme)
    accentPrimary = Color(0xFFB88A2E),
    accentStrong = Color(0xFFF0C674),
    accentSubtle = Color(0xFF513D14),
    onAccent = Color(0xFF070708),
    onSubtle = Color(0xFFF0C674),
    solarPrimary = HeliosColor.Solar.Dark500,
    solarStrong = HeliosColor.Solar.Dark700,
    solarSubtle = HeliosColor.Solar.Dark100,
    solarDim = HeliosColor.Solar.Dark300,
    flowPrimary = HeliosColor.Flow.Dark500,
    flowStrong = HeliosColor.Flow.Dark700,
    flowSubtle = HeliosColor.Flow.Dark100,
    batteryPrimary = HeliosColor.Battery.Dark500,
    batteryStrong = HeliosColor.Battery.Dark700,
    batterySubtle = HeliosColor.Battery.Dark100,
    batteryLow = HeliosColor.Alert.Dark500,
    gridImportPrimary = HeliosColor.GridImport.Dark500,
    gridImportStrong = HeliosColor.GridImport.Dark800,
    gridImportSubtle = HeliosColor.GridImport.Dark100,
    gridExportPrimary = HeliosColor.GridExport.Dark500,
    gridExportStrong = HeliosColor.GridExport.Dark800,
    gridExportSubtle = HeliosColor.GridExport.Dark100,
    alertPrimary = HeliosColor.Alert.Dark500,
    alertStrong = HeliosColor.Alert.Dark700,
    alertSubtle = HeliosColor.Alert.Dark100,
    onAlert = HeliosColor.Neutral.Dark950,
    status = StatusColors(
        producingFg = HeliosColor.Flow.Dark700,
        producingBg = HeliosColor.Flow.Dark100,
        standbyFg = HeliosColor.Neutral.Dark900,
        standbyBg = HeliosColor.Neutral.Dark200,
        curtailedFg = HeliosColor.Solar.Dark700,
        curtailedBg = HeliosColor.Solar.Dark100,
        nightFg = HeliosColor.GridExport.Dark800,
        nightBg = HeliosColor.GridExport.Dark100,
        faultFg = HeliosColor.Alert.Dark700,
        faultBg = HeliosColor.Alert.Dark100,
        offlineFg = HeliosColor.Neutral.Dark900,
        offlineBg = HeliosColor.Neutral.Dark300,
        demoFg = HeliosColor.Solar.Dark700,
        demoBg = HeliosColor.Solar.Dark100
    ),
    insight = InsightColors(
        positiveFg = HeliosColor.Flow.Dark700,
        positiveBg = HeliosColor.Flow.Dark100,
        neutralFg = HeliosColor.Neutral.Dark900,
        neutralBg = HeliosColor.Neutral.Dark200,
        attentionFg = HeliosColor.GridImport.Dark800,
        attentionBg = HeliosColor.GridImport.Dark100,
        criticalFg = HeliosColor.Alert.Dark700,
        criticalBg = HeliosColor.Alert.Dark100
    ),
    chart = ChartColors(
        production = HeliosColor.Solar.Dark500,
        consumption = HeliosColor.Flow.Dark500,
        battery = HeliosColor.Battery.Dark500,
        gridImport = HeliosColor.GridImport.Dark500,
        gridExport = HeliosColor.GridExport.Dark500,
        axis = HeliosColor.Neutral.Dark900,
        gridline = HeliosColor.Neutral.Dark950.copy(alpha = 0.08f),
        todayMarker = HeliosColor.Solar.Dark700
    ),
    skeleton = SkeletonColors(
        base = HeliosColor.Neutral.Dark300,
        highlight = HeliosColor.Neutral.Dark500.copy(alpha = 0.12f)
    ),
    focusRing = HeliosColor.Solar.Dark700
)

/** Semantic colours for the current theme. Provided by `HeliosTheme`. */
val LocalHeliosSemanticColors = staticCompositionLocalOf { CarbonColors }
