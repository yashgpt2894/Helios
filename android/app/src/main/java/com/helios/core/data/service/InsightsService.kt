package com.helios.core.data.service

import com.helios.core.domain.model.Insight
import com.helios.core.domain.model.ProductionForecast
import com.helios.core.domain.model.SolarTelemetry
import kotlinx.coroutines.flow.Flow

/**
 * Advisory insights (SVC-07, SVC-09). Hard rule from FLW-03: insights are suppressed
 * while the source is stale, offline or an unqualified demo, because the app must not
 * advise action on data it cannot support. The suppression is expressed as
 * [Loadable.Empty] with a reason, not as a silently empty list.
 */
interface InsightsService {

    val insights: Flow<Loadable<List<Insight>>>

    suspend fun generate(telemetry: SolarTelemetry): Loadable<List<Insight>>

    suspend fun generateFor(forecast: ProductionForecast): Loadable<List<Insight>>
}
