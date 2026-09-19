package com.helios.feature.production

import com.helios.core.data.fixture.FixtureData
import com.helios.core.data.fixture.FixtureScenario
import com.helios.core.data.service.Loadable
import com.helios.core.domain.model.PanelString
import com.helios.core.format.HeliosFormat
import com.helios.feature.dashboard.chartSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The render contract of the Production screen, as data.
 *
 * The screen's own extremes are the interesting ones: a string that did not report at all, a
 * string that is idle because the array is idle, a shaded string, and the scrub readout. Each
 * of them is a decision the screen must not make differently from the Dashboard.
 */
class ProductionStateTest {

    private val now = 1_760_000_000_000L

    @Test
    fun everyStringOfTheReadingBecomesOneCard() {
        val state = ProductionFixtures.state(FixtureScenario.LIVE, now, simulated = false)
        assertEquals(3, state.strings.size)
        assertTrue(state.missingStrings.isEmpty())
    }

    @Test
    fun aStringMissingFromTheReadingIsNamedRatherThanDropped() {
        val state = ProductionFixtures.state(FixtureScenario.PARTIAL, now, simulated = false)
        // The partial fixture drops the last string and names it in the reading's metadata.
        assertEquals(2, state.strings.size)
        assertEquals(listOf("C"), state.missingStrings)
        assertEquals(listOf("dcVoltageV", "dcCurrentA", "cabinetTempC", "batteryTempC", "panels.C"),
            state.reading.missingFields)
    }

    @Test
    fun aStringBelowItsRatingReadsAsBelowTheArray() {
        val string = panelString(powerW = 400.0, ratedW = 3200.0)
        assertEquals(StringState.SHADED, stringState(string, arrayProducing = true))
        assertEquals(StringState.PRODUCING, stringState(panelString(powerW = 2900.0, ratedW = 3200.0), true))
        assertEquals(StringState.IDLE, stringState(panelString(powerW = 0.0, ratedW = 3200.0), false))
        assertEquals(StringState.NO_DATA, stringState(panelString(powerW = Double.NaN, ratedW = 3200.0), true))
        assertEquals(StringState.NO_DATA, stringState(panelString(powerW = 400.0, ratedW = 0.0), true))
    }

    @Test
    fun aRegisterThatDidNotReportIsNoDataNeverZero() {
        val telemetry = FixtureData.partialTelemetry(now)
        val rows = ProductionTelemetry.rows(telemetry)
        val byLabel = rows.associateBy { it.label }
        listOf("DC voltage", "DC current", "Cabinet", "Battery temp").forEach { label ->
            val row = byLabel.getValue(label)
            assertTrue("$label should be flagged missing", row.missing)
            assertEquals(HeliosFormat.NO_DATA, row.value)
        }
        assertFalse(byLabel.getValue("AC voltage").missing)
        assertEquals("240.4", byLabel.getValue("AC voltage").value)
        assertEquals("4.23", byLabel.getValue("AC power").value)
    }

    @Test
    fun withNoReadingEveryRegisterIsMissing() {
        val rows = ProductionTelemetry.rows(null)
        assertTrue(rows.isNotEmpty())
        assertTrue(rows.all { it.missing })
        assertTrue(rows.all { it.value == HeliosFormat.NO_DATA })
    }

    @Test
    fun theNightStateIdlesTheStringsWithoutHidingThem() {
        val night = ProductionFixtures.nightState(FixtureScenario.LIVE, now)
        assertEquals(3, night.strings.size)
        night.strings.forEach { string ->
            assertEquals(StringState.IDLE, stringState(string, arrayProducing = false))
        }
    }

    @Test
    fun theScrubSelectionIsPartOfTheState() {
        val state = ProductionFixtures.state(FixtureScenario.LIVE, now, selectedT = 14.5, selectedWeekIndex = 2)
        assertEquals(14.5, state.selectedT)
        assertEquals(2, state.selectedWeekIndex)
        assertEquals(220, com.helios.core.designsystem.layout.HeliosSpacing.LayoutMetrics.heroChartHeight.value.toInt())
        val summary = chartSummary((state.todaySeries as? Loadable.Ready)?.value.orEmpty(), state.selectedT)
        assertTrue(summary, summary.contains("Selected 14:30"))
    }

    @Test
    fun everyStringTheProductionScreenShowsIsPlainText() {
        FixtureScenario.entries.forEach { scenario ->
            val state = ProductionFixtures.state(scenario, now)
            val texts = buildList {
                add(state.reading.freshnessText)
                add(state.reading.statusLabel)
                ProductionTelemetry.rows(state.telemetryValue).forEach { row ->
                    add(row.label)
                    add(row.value)
                    add(row.unit)
                }
                state.strings.forEach { add(it.label) }
                state.missingStrings.forEach { add("String $it") }
            }
            texts.forEach { text ->
                text.codePoints().forEach { codePoint ->
                    val plain = codePoint == 0x00B0 || codePoint == 0x00B7 || codePoint == 0x00B2 ||
                        codePoint == 0x00B3 || codePoint in 0x20..0x7E
                    assertTrue(
                        "$scenario carries a glyph that is not plain text: U+${codePoint.toString(16)} in \"$text\"",
                        plain
                    )
                }
            }
        }
    }

    private fun panelString(powerW: Double, ratedW: Double) = PanelString(
        id = "A",
        label = "String A",
        powerW = powerW,
        voltageV = 380.0,
        currentA = 1.1,
        ratedW = ratedW,
        panels = 8
    )
}
