package com.helios.feature.landing

import com.helios.core.data.service.ConnectionSnapshot
import com.helios.core.data.service.Loadable
import com.helios.core.domain.model.ConnectionConfig
import com.helios.core.domain.model.Location

/**
 * The state of first run: which step is on screen, what the probe is doing, and what the flow
 * hands to the app when it finishes.
 *
 * The steps are the surfaces of FLW-01: four pager pages plus the connect result and the
 * completion summary. Keeping them as one enum, rather than as separate destinations, is what
 * lets back move one step without leaving the app until the first page.
 */
enum class OnboardingStep(val progressStep: Int) {
    /** SCR-02: one sentence, and the demo path in one tap. */
    WELCOME(1),

    /** SCR-03: what stays on the device. */
    LOCAL_FIRST(2),

    /** SCR-04: the minimum SunSpec parameters. */
    CONNECT(3),

    /** SCR-05: what the probe found, or why it did not. */
    RESULT(3),

    /** SCR-06: the forecast location, skippable. */
    LOCATION(4),

    /** The summary, once something is live. */
    COMPLETE(4)
}

/** Steps shown in the progress indicator (DESIGN.md section 8: four steps). */
const val ONBOARDING_PROGRESS_STEPS = 4

/** The source the user chose. A demo source is never reported as a live inverter. */
enum class OnboardingSource { DEMO_SYSTEM, INVERTER }

/** What first run produced. The shell persists it; this surface only reports it. */
data class OnboardingOutcome(
    val source: OnboardingSource,
    val config: ConnectionConfig?,
    val location: Location?,
    val pollIntervalMs: Int,
    val completedAt: Long
)

/** The probe, as the connect page and the result page need to show it. */
sealed interface ProbeState {

    data object Idle : ProbeState

    /** STS-007: the probe is running, and the screen states how long it has been running. */
    data class Testing(val startedAt: Long) : ProbeState

    data class Done(val result: Loadable<ConnectionSnapshot>) : ProbeState
}
