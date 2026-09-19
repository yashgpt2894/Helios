package com.helios.core.designsystem.component

import com.helios.core.data.service.Loadable
import com.helios.core.data.service.UiState
import com.helios.core.data.service.uiState
import com.helios.core.designsystem.color.HeliosSeverityKind
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.domain.model.InsightSeverity
import com.helios.core.domain.model.InverterStatus

/**
 * The one place service state becomes component state.
 *
 * Components take small, renderable inputs ([SurfaceState], [HeliosStatusKind],
 * [HeliosSeverityKind]); services speak in [Loadable] and domain enums. Mapping lives here
 * so two screens cannot map the same state two different ways.
 */

/** Inverter status to the pill's vocabulary. FAULT keeps its word; a simulated source is DEMO. */
fun InverterStatus.toStatusKind(simulated: Boolean = false): HeliosStatusKind = when {
    simulated -> HeliosStatusKind.DEMO
    else -> when (this) {
        InverterStatus.PRODUCING -> HeliosStatusKind.PRODUCING
        InverterStatus.STANDBY -> HeliosStatusKind.STANDBY
        InverterStatus.CURTAILED -> HeliosStatusKind.CURTAILED
        InverterStatus.NIGHT -> HeliosStatusKind.NIGHT
        InverterStatus.FAULT -> HeliosStatusKind.FAULT
    }
}

/** Insight severity to the advisory vocabulary. */
fun InsightSeverity.toSeverityKind(): HeliosSeverityKind = when (this) {
    InsightSeverity.positive -> HeliosSeverityKind.POSITIVE
    InsightSeverity.neutral -> HeliosSeverityKind.NEUTRAL
    InsightSeverity.attention -> HeliosSeverityKind.ATTENTION
    InsightSeverity.critical -> HeliosSeverityKind.CRITICAL
}

/**
 * Data state to render state. A partial reading is rendered as ready data, because the
 * values that did arrive are real and usable; the missing ones are named by the caller.
 * Denied permission and a link failure both render as an error surface, and the caller
 * picks the right words from the failure kind.
 */
fun Loadable<*>.toSurfaceState(): SurfaceState = when (uiState()) {
    UiState.LOADING -> SurfaceState.LOADING
    UiState.EMPTY -> SurfaceState.EMPTY
    UiState.STALE, UiState.OFFLINE -> SurfaceState.STALE
    UiState.ERROR, UiState.DENIED -> SurfaceState.ERROR
    UiState.READY, UiState.PARTIAL -> SurfaceState.READY
}
