package com.helios.feature.dashboard

import com.helios.core.data.fixture.FixtureData
import com.helios.core.data.fixture.FixtureScenario
import com.helios.core.data.service.Freshness
import com.helios.core.data.service.Loadable
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.designsystem.component.SurfaceState
import com.helios.core.domain.model.HistoryPoint
import com.helios.core.format.HeliosFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The render contract of the Dashboard, asserted as data rather than as pixels.
 *
 * A screenshot proves what one state looked like; these assertions prove that every declared
 * scenario still maps to the state it promises, that a simulated reading never claims to be
 * live, that a register which did not report is "No data" rather than 0, and that the motion
 * tokens survive the Reduce Motion and animation-scale paths.
 */
class DashboardStateTest {

    private val now = 1_760_000_000_000L

    /** One row per declared scenario: the state a capture of that scenario must show. */
    private data class Expected(
        val status: HeliosStatusKind,
        val freshness: Freshness,
        val surface: SurfaceState,
        val freshnessWord: String
    )

    private val connected = mapOf(
        FixtureScenario.LOADING to Expected(
            HeliosStatusKind.STANDBY, Freshness.LIVE, SurfaceState.LOADING, "Waiting for the inverter"
        ),
        FixtureScenario.LIVE to Expected(
            HeliosStatusKind.PRODUCING, Freshness.LIVE, SurfaceState.READY, "Live"
        ),
        FixtureScenario.EMPTY to Expected(
            HeliosStatusKind.PRODUCING, Freshness.LIVE, SurfaceState.READY, "Live"
        ),
        FixtureScenario.PARTIAL to Expected(
            HeliosStatusKind.PRODUCING, Freshness.AGING, SurfaceState.READY, "Updated"
        ),
        FixtureScenario.STALE to Expected(
            HeliosStatusKind.PRODUCING, Freshness.STALE, SurfaceState.STALE, "Stale"
        ),
        FixtureScenario.OFFLINE to Expected(
            HeliosStatusKind.OFFLINE, Freshness.OFFLINE, SurfaceState.ERROR, "Offline"
        ),
        FixtureScenario.ERROR to Expected(
            HeliosStatusKind.PRODUCING, Freshness.LIVE, SurfaceState.READY, "Live"
        ),
        FixtureScenario.DENIED_PERMISSION to Expected(
            HeliosStatusKind.PRODUCING, Freshness.LIVE, SurfaceState.READY, "Live"
        ),
        FixtureScenario.LONG_CONTENT to Expected(
            HeliosStatusKind.PRODUCING, Freshness.LIVE, SurfaceState.READY, "Live"
        )
    )

    @Test
    fun everyDeclaredScenarioMapsToItsDeclaredState() {
        connected.forEach { (scenario, expected) ->
            val state = DashboardFixtures.state(scenario, now, simulated = false)
            assertEquals("status for $scenario", expected.status, state.statusKind)
            assertEquals("freshness for $scenario", expected.freshness, state.freshness)
            assertEquals("surface for $scenario", expected.surface, state.surface)
            assertTrue(
                "stamp word for $scenario was ${state.freshnessText}",
                state.freshnessText.startsWith(expected.freshnessWord)
            )
        }
    }

    @Test
    fun aSimulatedReadingNeverReadsLive() {
        val state = DashboardFixtures.state(FixtureScenario.LIVE, now)
        assertEquals(HeliosStatusKind.DEMO, state.statusKind)
        assertEquals("Demo data", state.freshnessText)
        assertEquals(Freshness.DEMO, state.freshness)
        assertFalse(state.dimmed)
    }

    @Test
    fun adviceIsSuppressedWhenTheDataCannotSupportIt() {
        assertNull(DashboardFixtures.state(FixtureScenario.LIVE, now, simulated = false).insightPlaceholder)
        assertEquals(
            "Waiting for the inverter",
            DashboardFixtures.state(FixtureScenario.STALE, now, simulated = false).insightPlaceholder
        )
        assertEquals(
            "Waiting for the inverter",
            DashboardFixtures.state(FixtureScenario.OFFLINE, now, simulated = false).insightPlaceholder
        )
        // The simulated system is allowed to show advisories, but every one carries the
        // "Demo data" qualifier, which the screen takes from the same flag.
        val demo = DashboardFixtures.state(FixtureScenario.LIVE, now)
        assertNull(demo.insightPlaceholder)
        assertTrue(demo.simulated)
    }

    @Test
    fun theOfflineScenarioCarriesTheClassifiedReason() {
        val state = DashboardFixtures.state(FixtureScenario.OFFLINE, now, simulated = false)
        val failure = (state.telemetry as? Loadable.Failed)?.failure
        assertNotNull(failure)
        assertEquals("Not on the same network as the inverter", failure?.message)
        assertTrue(failure?.detail?.startsWith("No route") == true)
    }

    @Test
    fun aPartialReadingNamesItsMissingFields() {
        val state = DashboardFixtures.state(FixtureScenario.PARTIAL, now, simulated = false)
        assertEquals(FixtureData.partialMissingFields, state.reading.missingFields)
        assertEquals(HeliosFormat.NO_DATA, DashboardMetrics.number(Double.NaN, 1))
        // A reported field is still a number, so the rule is not applied blindly.
        assertEquals("18.4", DashboardMetrics.number(18.4, 1))
    }

    @Test
    fun theNightStateKeepsADifferentSetOfValues() {
        val night = DashboardFixtures.nightState(FixtureScenario.LIVE, now)
        assertEquals(0.0, night.telemetryValue?.acPowerW ?: -1.0, 0.001)
        assertEquals(41.0, night.telemetryValue?.batterySoc ?: -1.0, 0.001)
        assertTrue(night.telemetryValue?.batteryPowerW ?: 0.0 < 0.0)
    }

    @Test
    fun everyStringTheDashboardShowsIsPlainText() {
        // No emoji, no symbol font, only the units and separators the design uses.
        FixtureScenario.entries.forEach { scenario ->
            val state = DashboardFixtures.state(scenario, now)
            val strings = listOf(
                state.freshnessText,
                state.statusLabel,
                state.forecastPlace,
                energyFlowSummary(
                    state.telemetryValue?.acPowerW ?: Double.NaN,
                    state.telemetryValue?.homeLoadW ?: Double.NaN,
                    state.telemetryValue?.batteryPowerW ?: Double.NaN,
                    state.telemetryValue?.gridExportW ?: Double.NaN
                ),
                chartSummary((state.todaySeries as? Loadable.Ready)?.value.orEmpty(), 14.5)
            )
            strings.forEach { text -> assertPlainText(text, scenario) }
        }
    }

    @Test
    fun motionSettingsApplyTheTokensAndTheDeviceScale() {
        val full = HeliosMotionSettings.Default
        assertEquals(250, full.durationMs(250))
        assertEquals(600, full.durationMs(600))
        assertTrue(full.animates())

        val scaled = HeliosMotionSettings(animationScale = 2f)
        assertEquals(500, scaled.durationMs(250))

        val reduced = HeliosMotionSettings.ReduceMotion
        assertFalse(reduced.animates())
        assertEquals(200, reduced.durationMs(600))
    }

    @Test
    fun selfUseIsProducedMinusExported() {
        val half = listOf(
            HistoryPoint(12.0, productionW = 4000.0, consumptionW = 3000.0, batteryW = 900.0, gridW = 100.0, irradianceWm2 = 800.0),
            HistoryPoint(12.5, productionW = 4000.0, consumptionW = 1000.0, batteryW = 2000.0, gridW = 1000.0, irradianceWm2 = 800.0)
        )
        // (8000 - 1100) / 8000 = 86.25 percent self-used.
        assertEquals(86.25, DashboardMetrics.selfUsePercent(half), 0.01)
        assertEquals(0.55, DashboardMetrics.exportedKwh(half), 0.001)
        assertTrue(DashboardMetrics.selfUsePercent(emptyList()).isNaN())
        // 8000 W for half an hour is 4.0 kWh.
        assertEquals(4.0, DashboardMetrics.kwhFromSeries(half) { it.productionW }, 0.001)
    }

    @Test
    fun theFourPathsOfTheHeroAlwaysConserveEnergy() {
        // A fixture reading whose registers do not add up: a 4.23 kW array cannot export
        // 2.92 kW while charging the battery at 4.04 kW.
        val sample = DashboardMetrics.flowSample(
            solarW = 4231.0,
            homeW = 1650.0,
            batteryW = 4041.0,
            gridW = 2921.0
        )
        assertTrue("sample should conserve: ${sample}", sample.conserves())
        assertEquals(4231.0, sample.solarW, 0.001)
        assertEquals(1650.0, sample.homeW, 0.001)
        assertEquals(2581.0, sample.batteryW, 0.001)
        assertEquals(0.0, sample.gridW, 0.001)

        // Every fixture scenario, and the night shape, keeps the rule.
        (FixtureScenario.entries + listOf(FixtureScenario.LIVE)).forEach { scenario ->
            val night = scenario == FixtureScenario.LIVE
            val reading = if (night) {
                FixtureData.nightTelemetry(scenario, now)
            } else {
                FixtureData.telemetry(scenario, now)
            }
            val value = (reading as? Loadable.Ready)?.value ?: return@forEach
            val flow = DashboardMetrics.flowSample(
                solarW = value.acPowerW,
                homeW = value.homeLoadW,
                batteryW = value.batteryPowerW,
                gridW = if (value.gridExportW > 0) value.gridExportW else -value.gridImportW
            )
            assertTrue("$scenario${if (night) " (night)" else ""} does not conserve: $flow", flow.conserves())
        }

        // A discharge and an import are kept, because they fit the reported output.
        val discharging = DashboardMetrics.flowSample(
            solarW = 0.0,
            homeW = 620.0,
            batteryW = -620.0,
            gridW = 0.0
        )
        assertEquals(-620.0, discharging.batteryW, 0.001)
        assertEquals(0.0, discharging.gridW, 0.001)

        val importing = DashboardMetrics.flowSample(
            solarW = 500.0,
            homeW = 2200.0,
            batteryW = 0.0,
            gridW = -1700.0
        )
        assertEquals(-1700.0, importing.gridW, 0.001)
        assertTrue(importing.conserves())

        // A register that did not report keeps the whole sample unread.
        val missing = DashboardMetrics.flowSample(Double.NaN, 1000.0, 0.0, 0.0)
        assertTrue(missing.solarW.isNaN())
        assertFalse(missing.conserves())
    }

    @Test
    fun theAvoidedCo2MatchesTheWebApp() {
        // src/pages/Dashboard.tsx: co2 = kWh * 0.42, trees = co2 / 21.
        assertEquals(7.728, DashboardMetrics.co2Kg(18.4), 0.0001)
        assertEquals(0.368, DashboardMetrics.treesEquivalent(18.4), 0.0001)
    }

    @Test
    fun theChartSummaryNamesThePeakAndTheSelectedHalfHour() {
        val points = FixtureData.todaySeries(now)
        val summary = chartSummary(points, 14.5)
        assertTrue(summary, summary.startsWith("Today's curve: peak"))
        assertTrue(summary, summary.contains("Selected 14:30"))
        // The daily total belongs to the inverter's own meter, not to the sampled curve.
        assertFalse(summary, summary.contains("kWh"))
        assertEquals("14:30", hourLabel(14.5))
        assertEquals("00:00", hourLabel(0.0))
        assertEquals("24:00", hourLabel(24.0))
        assertEquals(14.0, stepSelection(points, 14.5, -0.5), 0.001)
    }

    private fun assertPlainText(text: String, scenario: FixtureScenario) {
        text.codePoints().forEach { codePoint ->
            val plain = codePoint == 0x00B0 || codePoint == 0x00B7 || codePoint == 0x00B2 ||
                codePoint == 0x00B3 || codePoint in 0x20..0x7E || codePoint == 0x0A
            assertTrue(
                "$scenario carries a glyph that is not plain text: U+${codePoint.toString(16)} in \"$text\"",
                plain
            )
        }
    }
}
