package com.helios.core.designsystem

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import com.helios.core.designsystem.color.CarbonColors
import com.helios.core.designsystem.color.PaperColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.motion.HeliosMotion
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Guards the platform mapping: the Compose tokens must equal `design/tokens.json`.
 *
 * Without this, the token source and the code drift apart and nothing fails until a
 * colour is wrong on one platform. The test walks the JSON by path, so it also proves the
 * file keeps the shape the mapping table in design/DESIGN.md section 11.1 promises.
 */
class TokensMatchJsonTest {

    private val tokens: JsonElement = run {
        val file = generateSequence(File(".").absoluteFile) { it.parentFile }
            .map { File(it, "design/tokens.json") }
            .firstOrNull { it.exists() }
            ?: error("design/tokens.json not found above ${File(".").absolutePath}")
        Json.parseToJsonElement(file.readText())
    }

    private fun at(path: String): JsonElement {
        var node: JsonElement = tokens
        path.split(".").forEach { part ->
            node = when (node) {
                is JsonObject -> node.getValue(part)
                is JsonArray -> node[part.toInt()]
                else -> error("cannot descend into $part while reading $path")
            }
        }
        return node
    }

    private fun number(path: String): Double = at(path).jsonPrimitive.content.toDouble()

    /** The 8-digit ARGB literal the generator wrote for one role in one theme. */
    private fun argb(path: String, theme: String): Int =
        at("color.semantic.$path.$theme.argb").jsonPrimitive.content
            .removePrefix("0x").toLong(16).toInt()

    @Test
    fun `semantic colours match the token source in both themes`() {
        assertEquals(argb("text.primary", "light"), PaperColors.textPrimary.toArgb())
        assertEquals(argb("text.primary", "dark"), CarbonColors.textPrimary.toArgb())
        assertEquals(argb("text.secondary", "dark"), CarbonColors.textSecondary.toArgb())
        assertEquals(argb("text.quaternary", "light"), PaperColors.textQuaternary.toArgb())
        assertEquals(argb("background.primary", "light"), PaperColors.backgroundPrimary.toArgb())
        assertEquals(argb("background.primary", "dark"), CarbonColors.backgroundPrimary.toArgb())
        assertEquals(argb("background.tertiary", "light"), PaperColors.backgroundTertiary.toArgb())
        assertEquals(argb("accent.primary", "light"), PaperColors.accentPrimary.toArgb())
        assertEquals(argb("accent.primary", "dark"), CarbonColors.accentPrimary.toArgb())
        assertEquals(argb("accent.strong", "dark"), CarbonColors.accentStrong.toArgb())
        assertEquals(argb("accent.subtle", "light"), PaperColors.accentSubtle.toArgb())
        assertEquals(argb("accent.onAccent", "light"), PaperColors.onAccent.toArgb())
        assertEquals(argb("accent.onAccent", "dark"), CarbonColors.onAccent.toArgb())
        assertEquals(argb("accent.onSubtle", "light"), PaperColors.onSubtle.toArgb())
        assertEquals(argb("status.producing.fg", "light"), PaperColors.status.producingFg.toArgb())
        assertEquals(argb("status.producing.fg", "dark"), CarbonColors.status.producingFg.toArgb())
        assertEquals(argb("status.fault.bg", "light"), PaperColors.status.faultBg.toArgb())
        assertEquals(argb("status.night.fg", "dark"), CarbonColors.status.nightFg.toArgb())
        assertEquals(argb("insight.critical.fg", "dark"), CarbonColors.insight.criticalFg.toArgb())
        assertEquals(argb("insight.attention.bg", "light"), PaperColors.insight.attentionBg.toArgb())
        assertEquals(argb("chart.production", "light"), PaperColors.chart.production.toArgb())
        assertEquals(argb("chart.consumption", "dark"), CarbonColors.chart.consumption.toArgb())
        assertEquals(argb("chart.axis", "dark"), CarbonColors.chart.axis.toArgb())
        assertEquals(argb("solar.primary", "dark"), CarbonColors.solarPrimary.toArgb())
        assertEquals(argb("solar.dim", "light"), PaperColors.solarDim.toArgb())
        assertEquals(argb("flow.strong", "light"), PaperColors.flowStrong.toArgb())
        assertEquals(argb("battery.strong", "dark"), CarbonColors.batteryStrong.toArgb())
        assertEquals(argb("battery.low", "light"), PaperColors.batteryLow.toArgb())
        assertEquals(argb("alert.primary", "light"), PaperColors.alertPrimary.toArgb())
        assertEquals(argb("alert.onAlert", "dark"), CarbonColors.onAlert.toArgb())
        assertEquals(argb("grid.importStrong", "dark"), CarbonColors.gridImportStrong.toArgb())
        assertEquals(argb("grid.exportStrong", "light"), PaperColors.gridExportStrong.toArgb())
        assertEquals(argb("grid.exportSubtle", "light"), PaperColors.gridExportSubtle.toArgb())
        assertEquals(argb("skeleton.base", "dark"), CarbonColors.skeleton.base.toArgb())
        assertEquals(argb("skeleton.highlight", "dark"), CarbonColors.skeleton.highlight.toArgb())
        assertEquals(argb("focus.ring", "light"), PaperColors.focusRing.toArgb())
        assertEquals(argb("text.inverse", "dark"), CarbonColors.textInverse.toArgb())
    }

    @Test
    fun `alpha roles keep their alpha across the mapping`() {
        assertEquals(argb("separator.hairline", "light"), PaperColors.separatorHairline.toArgb())
        assertEquals(argb("separator.hairline", "dark"), CarbonColors.separatorHairline.toArgb())
        assertEquals(argb("separator.strong", "light"), PaperColors.separatorStrong.toArgb())
        assertEquals(argb("background.elevated", "dark"), CarbonColors.backgroundElevated.toArgb())
        assertEquals(argb("background.scrim", "light"), PaperColors.scrim.toArgb())
    }

    @Test
    fun `spacing, radius, elevation and layout tokens match`() {
        assertEquals(number("spacing.gutterDp").toFloat(), HeliosSpacing.gutter.value, 0.001f)
        assertEquals(number("spacing.cardPaddingDp").toFloat(), HeliosSpacing.cardPadding.value, 0.001f)
        assertEquals(number("spacing.sectionRhythmDp").toFloat(), HeliosSpacing.sectionRhythm.value, 0.001f)
        assertEquals(number("spacing.minTouchTargetDp").toFloat(), HeliosSpacing.minTouchTarget.value, 0.001f)
        assertEquals(number("spacing.scale.4").toFloat(), HeliosSpacing.space4.value, 0.001f)
        assertEquals(number("spacing.unitDp").toFloat(), HeliosSpacing.scale(1).value, 0.001f)

        val unitDensity = object : Density {
            override val density: Float = 1f
            override val fontScale: Float = 1f
        }
        val shapeSize = Size(100f, 100f)
        assertEquals(
            number("radius.md").toFloat(),
            HeliosShape.md.topStart.toPx(shapeSize, unitDensity),
            0.001f
        )
        assertEquals(
            number("radius.xl").toFloat(),
            HeliosShape.xl.topStart.toPx(shapeSize, unitDensity),
            0.001f
        )
        assertEquals(
            number("radius.sm").toFloat(),
            HeliosShape.sm.topStart.toPx(shapeSize, unitDensity),
            0.001f
        )

        assertEquals(number("elevation.levels.level-0").toFloat(), HeliosElevation.level0.value, 0.001f)
        assertEquals(number("elevation.levels.level-1").toFloat(), HeliosElevation.level1.value, 0.001f)
        assertEquals(number("elevation.levels.level-2").toFloat(), HeliosElevation.level2.value, 0.001f)
        assertEquals(number("elevation.levels.level-3").toFloat(), HeliosElevation.level3.value, 0.001f)
        assertEquals(number("elevation.levels.level-4").toFloat(), HeliosElevation.level4.value, 0.001f)
        assertEquals(number("elevation.levels.level-5").toFloat(), HeliosElevation.level5.value, 0.001f)
        assertEquals(
            number("elevation.material.innerStrokeDp").toFloat(),
            HeliosElevation.MaterialTokens.innerStrokeWidth.value,
            0.001f
        )
        assertEquals(
            number("elevation.material.regular.tonalElevationDp").toInt(),
            HeliosElevation.MaterialTokens.regularTonalElevation
        )

        assertEquals(number("layout.heroBlockMaxDp").toFloat(), HeliosSpacing.LayoutMetrics.heroBlockMax.value, 0.001f)
        assertEquals(number("layout.topBarHeightDp").toFloat(), HeliosSpacing.LayoutMetrics.topBarHeight.value, 0.001f)
        assertEquals(number("layout.bottomNavHeightDp").toFloat(), HeliosSpacing.LayoutMetrics.bottomNavHeight.value, 0.001f)
        assertEquals(number("layout.chartHeightDp").toFloat(), HeliosSpacing.LayoutMetrics.chartHeight.value, 0.001f)
        assertEquals(number("layout.heroChartHeightDp").toFloat(), HeliosSpacing.LayoutMetrics.heroChartHeight.value, 0.001f)
        assertEquals(
            number("layout.forecastStripItemWidthDp").toFloat(),
            HeliosSpacing.LayoutMetrics.forecastStripItemWidth.value,
            0.001f
        )
    }

    @Test
    fun `motion durations, springs and the type scale match`() {
        assertEquals(number("motion.durationMs.instant").toInt(), HeliosMotion.DurationMs.instant)
        assertEquals(number("motion.durationMs.fast").toInt(), HeliosMotion.DurationMs.fast)
        assertEquals(number("motion.durationMs.normal").toInt(), HeliosMotion.DurationMs.normal)
        assertEquals(number("motion.durationMs.deliberate").toInt(), HeliosMotion.DurationMs.deliberate)
        assertEquals(number("motion.durationMs.shimmer").toInt(), HeliosMotion.DurationMs.shimmer)
        assertEquals(number("motion.durationMs.screenEnter").toInt(), HeliosMotion.DurationMs.screenEnter)
        assertEquals(number("motion.reduceMotion.crossfadeMs").toInt(), HeliosMotion.ReduceMotion.crossfadeMs)

        assertEquals(
            number("motion.spring.gentle.dampingRatio").toFloat(),
            HeliosMotion.gentle.dampingRatio,
            0.001f
        )
        assertEquals(number("motion.spring.gentle.stiffness").toFloat(), HeliosMotion.gentle.stiffness, 0.001f)
        assertEquals(
            number("motion.spring.default.dampingRatio").toFloat(),
            HeliosMotion.defaultSpring.dampingRatio,
            0.001f
        )
        assertEquals(
            number("motion.spring.default.stiffness").toFloat(),
            HeliosMotion.defaultSpring.stiffness,
            0.001f
        )
        assertEquals(number("motion.spring.snappy.stiffness").toFloat(), HeliosMotion.snappy.stiffness, 0.001f)
        assertEquals(number("motion.spring.bouncy.dampingRatio").toFloat(), HeliosMotion.bouncy.dampingRatio, 0.001f)

        assertEquals(number("motion.staggerMs.tight").toInt(), HeliosMotion.StaggerMs.tight)
        assertEquals(number("motion.staggerMs.normal").toInt(), HeliosMotion.StaggerMs.normal)
        assertEquals(number("motion.staggerMs.loose").toInt(), HeliosMotion.StaggerMs.loose)
        assertEquals(number("motion.entrance.statsStaggerMs").toInt(), HeliosMotion.Entrance.statsStaggerMs)
        assertEquals(number("motion.entrance.chartDrawMs").toInt(), HeliosMotion.Entrance.chartDrawMs)
        assertEquals(number("motion.entrance.chromeDelayMs").toInt(), HeliosMotion.Entrance.chromeDelayMs)

        assertEquals(number("typography.scale.0.sizeSp").toFloat(), HeliosTypography.hero.fontSize.value, 0.001f)
        assertEquals(number("typography.scale.0.lineHeightSp").toFloat(), HeliosTypography.hero.lineHeight.value, 0.001f)
        assertEquals(number("typography.scale.1.sizeSp").toFloat(), HeliosTypography.title1.fontSize.value, 0.001f)
        assertEquals(number("typography.scale.4.sizeSp").toFloat(), HeliosTypography.headline.fontSize.value, 0.001f)
        assertEquals(FontWeight.SemiBold, HeliosTypography.headline.fontWeight)
        assertEquals(number("typography.scale.5.sizeSp").toFloat(), HeliosTypography.body.fontSize.value, 0.001f)
        assertEquals(number("typography.scale.9.sizeSp").toFloat(), HeliosTypography.caption2.fontSize.value, 0.001f)
        assertEquals(FontWeight.Medium, HeliosTypography.caption2.fontWeight)
        assertEquals(
            number("typography.numeric.chartAxis.sizeSp").toFloat(),
            HeliosTypography.chartNumeric.fontSize.value,
            0.001f
        )
        assertEquals(
            number("typography.numeric.liveTicker.sizeSp").toFloat(),
            HeliosTypography.liveTicker.fontSize.value,
            0.001f
        )
    }

    @Test
    fun `the token source records no failing contrast pair and covers the UI`() {
        val contrast = at("verify.contrast").jsonObject
        val rows = contrast.getValue("rows").jsonArray
        val failures = contrast.getValue("failures").jsonArray

        assertEquals("every measured pair must pass its target", 0, failures.size)
        assertEquals("the audit must cover the UI pairs", 27, rows.size)
        rows.forEach { row ->
            val entry = row.jsonObject
            assertEquals(
                "light: ${entry.getValue("foreground").jsonPrimitive.content}",
                true,
                entry.getValue("lightPass").jsonPrimitive.content.toBoolean()
            )
            assertEquals(
                "dark: ${entry.getValue("foreground").jsonPrimitive.content}",
                true,
                entry.getValue("darkPass").jsonPrimitive.content.toBoolean()
            )
        }
    }

    @Test
    fun `the token source names the platform files the mapping table promises`() {
        val mapping = at("platformMapping.android").jsonObject
        listOf(
            "color", "typography", "spacing", "radius", "elevation", "motion", "haptics",
            "theme", "format", "components", "services", "fixtures", "gallery", "galleryPaging"
        ).forEach { group ->
            val path = mapping.getValue(group).jsonPrimitive.content
            val file = generateSequence(File(".").absoluteFile) { it.parentFile }
                .map { File(it, path) }
                .firstOrNull { it.exists() }
            assertEquals("$group maps to an existing path", true, file != null)
        }
        val registry = at("\$meta.brand.registry").jsonArray.map { it.jsonPrimitive.content }
        assertEquals(listOf("helios", "voltcraft", "sunworks", "meridian"), registry)
    }
}
