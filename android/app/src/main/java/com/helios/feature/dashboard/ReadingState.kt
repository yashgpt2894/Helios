package com.helios.feature.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.helios.core.data.service.Freshness
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.UiState
import com.helios.core.data.service.metaOrNull
import com.helios.core.data.service.uiState
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.designsystem.component.ConnectionBanner
import com.helios.core.designsystem.component.ConnectionBannerState
import com.helios.core.designsystem.component.SurfaceState
import com.helios.core.designsystem.component.label
import com.helios.core.designsystem.component.toStatusKind
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.domain.model.SolarTelemetry
import com.helios.core.format.HeliosFormat

/**
 * One reading, with the state around it.
 *
 * Both screens read the same inverter, so both must describe its age the same way: the age
 * band, the status word, the stamp text and the dimming rule live here once, instead of in
 * each screen. A screen that renders a live number without this object cannot state how old
 * the number is, which is what design correction C1 forbids.
 */
@Immutable
data class ReadingState(
    val telemetry: Loadable<SolarTelemetry>,
    /** True when the source is the simulated system. It is never labelled live. */
    val simulated: Boolean = true
) {

    val value: SolarTelemetry? get() = (telemetry as? Loadable.Ready)?.value

    /** True while no reading has arrived yet: skeletons, no failure banner. */
    val loading: Boolean get() = telemetry is Loadable.Loading

    /**
     * The status the pill shows. A failed link wins over the last known inverter state, a
     * simulated source is always the demo system, and a first load with no reading yet can
     * only say that nothing is known about the inverter's state.
     */
    val statusKind: HeliosStatusKind
        get() = when {
            telemetry is Loadable.Failed -> HeliosStatusKind.OFFLINE
            simulated -> HeliosStatusKind.DEMO
            loading -> HeliosStatusKind.STANDBY
            else -> value?.status?.toStatusKind() ?: HeliosStatusKind.OFFLINE
        }

    val statusLabel: String get() = statusKind.label()

    val freshness: Freshness
        get() = when {
            telemetry is Loadable.Failed -> Freshness.OFFLINE
            simulated -> Freshness.DEMO
            else -> metaOrNull()?.freshness ?: Freshness.LIVE
        }

    private fun metaOrNull() = telemetry.metaOrNull()

    /**
     * The stamp text. It says "Live" only for a reading younger than five seconds, gives the
     * wall clock in the aging band, an age in the stale band, and never says live for the
     * demo system.
     */
    val freshnessText: String
        get() {
            val meta = telemetry.metaOrNull()
            return when {
                telemetry is Loadable.Failed -> "Offline"
                simulated -> "Demo data"
                telemetry is Loadable.Loading || meta == null -> "Waiting for the inverter"
                else -> when (meta.freshness) {
                    Freshness.LIVE -> "Live"
                    Freshness.AGING -> "Updated ${HeliosFormat.clockTime(meta.fetchedAt)}"
                    Freshness.STALE -> "Stale ${HeliosFormat.age(meta.ageMs)}"
                    Freshness.OFFLINE -> "Offline"
                    Freshness.DEMO -> "Demo data"
                }
            }
        }

    /** The stamp borrows the status vocabulary: a live reading reads as producing. */
    val freshnessKind: HeliosStatusKind
        get() = if (loading) HeliosStatusKind.STANDBY else when (freshness) {
            Freshness.LIVE -> HeliosStatusKind.PRODUCING
            Freshness.AGING -> HeliosStatusKind.STANDBY
            Freshness.STALE -> HeliosStatusKind.CURTAILED
            Freshness.OFFLINE -> HeliosStatusKind.OFFLINE
            Freshness.DEMO -> HeliosStatusKind.DEMO
        }

    /** True while values dim to 60 percent and no animation may claim to be live. */
    val dimmed: Boolean
        get() = freshness == Freshness.STALE || freshness == Freshness.OFFLINE

    /** What a data surface renders: loading, error, stale or ready. */
    val surface: SurfaceState
        get() = when {
            telemetry is Loadable.Loading -> SurfaceState.LOADING
            telemetry is Loadable.Failed -> SurfaceState.ERROR
            dimmed -> SurfaceState.STALE
            else -> SurfaceState.READY
        }

    /** Fields that did not report (F7), so a surface can name them instead of showing 0. */
    val missingFields: List<String> get() = telemetry.metaOrNull()?.missing.orEmpty()

    val isPartial: Boolean get() = telemetry.uiState() == UiState.PARTIAL
}

/**
 * The persistent failure notice: the classified reason, the age of the last good reading and
 * the two recovery actions (design correction C2 keeps connection one tap from the hero).
 *
 * It renders nothing when there is nothing wrong, so both screens can call it unconditionally.
 */
@Composable
fun ReadingNotice(
    reading: ReadingState,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onOpenConnection: (() -> Unit)? = null
) {
    val failure = (reading.telemetry as? Loadable.Failed)?.failure
    val stale = reading.freshness == Freshness.STALE
    // A first load is not a failure: the screen shows its skeletons instead of a banner.
    if (reading.loading) return
    if (failure == null && !stale) return
    val meta = reading.telemetry.metaOrNull()
    Column(modifier = modifier) {
        ConnectionBanner(
            state = if (failure != null) ConnectionBannerState.OFFLINE else ConnectionBannerState.RECONNECTING,
            message = failure?.message ?: "The inverter has not answered",
            reason = failure?.detail ?: (
                "Last good reading ${HeliosFormat.age(meta?.ageMs ?: 0L)} ago. " +
                    "Retrying on the bounded schedule."
                ),
            onRetry = onRetry,
            onOpenConnection = onOpenConnection
        )
        Spacer(Modifier.height(HeliosSpacing.space3))
    }
}
