package com.helios.core.data

import com.helios.core.data.fixture.FixtureScenario
import com.helios.core.data.fixture.FixtureServicesFamily
import com.helios.core.data.service.UiState
import com.helios.core.data.service.uiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The nine demo scenarios, asserted through the service contracts.
 *
 * This is the test the gallery's state switch depends on: if a scenario stops producing
 * the state it promises, a screen review would be looking at the wrong thing and nobody
 * would notice from a screenshot.
 */
class FixtureStateCoverageTest {

    private val services = FixtureServicesFamily.INSTANCE

    /** telemetry, today series, forecast, insights, connection, location */
    private val expected: Map<FixtureScenario, List<UiState>> = mapOf(
        FixtureScenario.LOADING to listOf(
            UiState.LOADING, UiState.LOADING, UiState.LOADING,
            UiState.LOADING, UiState.LOADING, UiState.LOADING
        ),
        FixtureScenario.LIVE to listOf(
            UiState.READY, UiState.READY, UiState.READY,
            UiState.READY, UiState.READY, UiState.READY
        ),
        FixtureScenario.EMPTY to listOf(
            UiState.READY, UiState.EMPTY, UiState.EMPTY,
            UiState.EMPTY, UiState.READY, UiState.EMPTY
        ),
        FixtureScenario.PARTIAL to listOf(
            UiState.PARTIAL, UiState.PARTIAL, UiState.PARTIAL,
            UiState.PARTIAL, UiState.PARTIAL, UiState.READY
        ),
        FixtureScenario.STALE to listOf(
            UiState.STALE, UiState.STALE, UiState.STALE,
            UiState.EMPTY, UiState.STALE, UiState.READY
        ),
        FixtureScenario.OFFLINE to listOf(
            UiState.OFFLINE, UiState.OFFLINE, UiState.STALE,
            UiState.EMPTY, UiState.OFFLINE, UiState.STALE
        ),
        FixtureScenario.ERROR to listOf(
            UiState.READY, UiState.READY, UiState.ERROR,
            UiState.ERROR, UiState.OFFLINE, UiState.READY
        ),
        FixtureScenario.DENIED_PERMISSION to listOf(
            UiState.READY, UiState.READY, UiState.READY,
            UiState.READY, UiState.READY, UiState.DENIED
        ),
        FixtureScenario.LONG_CONTENT to listOf(
            UiState.READY, UiState.READY, UiState.READY,
            UiState.READY, UiState.READY, UiState.READY
        )
    )

    @Test
    fun `every scenario produces the state its name promises`() = runBlocking {
        FixtureScenario.entries.forEach { scenario ->
            services.select(scenario)
            val states = listOf(
                services.telemetry.read().uiState(),
                services.series.todaySeries.first().uiState(),
                services.forecast.forecast.first().uiState(),
                services.insights.insights.first().uiState(),
                services.connection.link.first().uiState(),
                services.location.location.first().uiState()
            )
            assertEquals("scenario $scenario", expected.getValue(scenario), states)
        }
        services.select(FixtureScenario.LIVE)
    }

    /** The week block fails on its own: under OFFLINE it reports F7, not a link failure. */
    private val expectedWeek: Map<FixtureScenario, UiState> = mapOf(
        FixtureScenario.LOADING to UiState.LOADING,
        FixtureScenario.LIVE to UiState.READY,
        FixtureScenario.EMPTY to UiState.EMPTY,
        FixtureScenario.PARTIAL to UiState.PARTIAL,
        FixtureScenario.STALE to UiState.STALE,
        FixtureScenario.OFFLINE to UiState.ERROR,
        FixtureScenario.ERROR to UiState.READY,
        FixtureScenario.DENIED_PERMISSION to UiState.READY,
        FixtureScenario.LONG_CONTENT to UiState.READY
    )

    @Test
    fun `the live trail follows the reading, the week block follows its own request`() = runBlocking {
        FixtureScenario.entries.forEach { scenario ->
            services.select(scenario)
            assertEquals(
                "live trail under $scenario",
                expected.getValue(scenario)[1],
                services.series.liveSeries.first().uiState()
            )
            assertEquals(
                "week block under $scenario",
                expectedWeek.getValue(scenario),
                services.series.weekSeries.first().uiState()
            )
        }
        services.select(FixtureScenario.LIVE)
    }

    @Test
    fun `insights are suppressed while the reading is stale or offline`() = runBlocking {
        listOf(FixtureScenario.STALE, FixtureScenario.OFFLINE).forEach { scenario ->
            services.select(scenario)
            assertEquals(
                "advisories under $scenario must not advise on data that cannot support it",
                UiState.EMPTY,
                services.insights.insights.first().uiState()
            )
        }
        services.select(FixtureScenario.LIVE)
    }

    @Test
    fun `requesting a fix succeeds except when the permission was denied`() = runBlocking {
        services.select(FixtureScenario.LIVE)
        assertEquals(UiState.READY, services.location.useMyLocation().uiState())

        services.select(FixtureScenario.DENIED_PERMISSION)
        assertEquals(UiState.DENIED, services.location.useMyLocation().uiState())

        // A denied permission leaves the default location in place, and it is still usable.
        assertEquals(UiState.DENIED, services.location.location.first().uiState())
        services.select(FixtureScenario.LIVE)
    }

    @Test
    fun `the retry schedule is bounded and matches the design`() {
        assertEquals(listOf(1_000L, 2_000L, 4_000L, 30_000L, 60_000L), services.connection.retrySchedule())
    }

    @Test
    fun `the brand registry matches the PWA ids`() {
        assertEquals(
            listOf("helios", "voltcraft", "sunworks", "meridian"),
            services.brand.registry.map { it.id }
        )
        assertEquals("helios", services.brand.resolve("HELIOS").id)
        assertEquals("SunWorks", services.brand.resolve("sunworks").name)
    }
}
