package com.helios.feature.battery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.helios.core.data.fixture.FixtureData
import com.helios.core.data.fixture.FixtureScenario
import com.helios.core.data.fixture.FixtureState
import com.helios.core.data.service.Freshness
import com.helios.core.data.service.LivePoint
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.ServiceFailure
import com.helios.core.data.service.UiState
import com.helios.core.data.service.freshnessLabel
import com.helios.core.data.service.metaOrNull
import com.helios.core.data.service.uiState
import com.helios.core.data.service.valueOrNull
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.designsystem.component.BatteryRingMode
import com.helios.core.designsystem.component.SurfaceState
import com.helios.core.designsystem.component.toSurfaceState
import com.helios.core.domain.model.SolarTelemetry
import com.helios.core.format.HeliosFormat
import kotlin.math.abs
import kotlin.math.floor

/**
 * SCR-10 Battery, as an explicit, renderable state.
 *
 * The screen takes this object rather than reaching for a repository, so every state in
 * `design/screen-inventory.md` SCR-10 (STS-046 to STS-053) can be rendered, captured and
 * reviewed without a device: construct it, pass it, look at it. [fixture] builds one from
 * the deterministic fixtures in `core/data/fixture`, and the values are the same ones the
 * fixture service adapters return, so a screen built against this state is already
 * correct for `TelemetryService`.
 *
 * Honest limits:
 *  - there is no `round-trip efficiency` reading in [SolarTelemetry]; the PWA hard-codes
 *    94.2 percent. This screen does not print a number the app cannot read, so the fourth
 *    health tile shows battery capacity, which the inverter does report.
 *  - "of 6,000 rated" is a nameplate figure for the reference battery, not a measurement.
 */

/** The three charge strategies of the inverter's configuration (SCR-10, ACT-050). */
enum class ChargeMode(
    val label: String,
    val description: String
) {
    SELF_CONSUMPTION(
        label = "Self-consumption",
        description = "Maximise using your own solar before drawing from the grid."
    ),
    TIME_OF_USE(
        label = "Time-of-use",
        description = "Charge from the grid off-peak, then discharge through the evening peak."
    ),
    BACKUP_ONLY(
        label = "Backup-only",
        description = "Hold at least 80 percent reserve for outage protection."
    )
}

/**
 * Where a mode change has got to (STS-051 mode-applying, STS-052 mode-failed). `RAW` means
 * "this is what the inverter reports", which is the resting state.
 */
enum class ChargeApplyStatus { REPORTED, APPLYING, CONFIRMED, FAILED }

/** SoC thresholds that fire a haptic on crossing only (motion language section 5). */
enum class BatteryThreshold(val socPct: Double, val word: String) {
    EIGHTY(80.0, "80 percent"),
    FIFTY(50.0, "50 percent"),
    TWENTY(20.0, "20 percent")
}

/**
 * Thresholds crossed between two readings, in the direction of travel.
 *
 * Crossing only: a reading that sits still, or that moves without passing a threshold,
 * returns an empty list, so the phone does not buzz on every poll inside a moving car.
 */
fun crossedThresholds(previousPct: Double?, nextPct: Double): List<BatteryThreshold> {
    if (previousPct == null || previousPct.isNaN() || nextPct.isNaN()) return emptyList()
    if (previousPct == nextPct) return emptyList()
    val low = minOf(previousPct, nextPct)
    val high = maxOf(previousPct, nextPct)
    return BatteryThreshold.entries.filter { it.socPct > low && it.socPct <= high }
}

/**
 * Everything the Battery screen renders.
 *
 * @param telemetry SVC-01, with its state: loading, ready, partial, stale, offline or
 *   failed. The screen never renders a value without the state that qualifies it.
 * @param liveTrail SVC-01 tail, drawn as the battery-power sparkline.
 * @param chargeMode what the inverter reports, and what the selector highlights.
 * @param chargeModeStatus a change in flight, a confirmed change, or a rejected one.
 * @param chargeModePending the mode a change is being applied to, or the one the inverter
 *   rejected, so the selector can mark the right row instead of guessing.
 * @param readinessExpanded ACT-053 disclosure state, session only.
 * @param demoQualifier true when the source is the simulated system (STS-044).
 */
data class BatteryScreenState(
    val scenario: FixtureScenario = FixtureScenario.LIVE,
    val telemetry: Loadable<SolarTelemetry> = Loadable.Loading,
    val liveTrail: Loadable<List<LivePoint>> = Loadable.Loading,
    val chargeMode: ChargeMode = ChargeMode.SELF_CONSUMPTION,
    val chargeModeStatus: ChargeApplyStatus = ChargeApplyStatus.REPORTED,
    val chargeModePending: ChargeMode? = null,
    val chargeModeFailure: String? = null,
    val readinessExpanded: Boolean = false,
    val demoQualifier: Boolean = true,
    val reducedMotion: Boolean = false
) {
    companion object {

        /**
         * The one fixture entry point. Every parameter is optional, so a capture or a
         * preview passes only what it is exercising.
         */
        fun fixture(
            scenario: FixtureScenario = FixtureState.current(),
            nowMs: Long = System.currentTimeMillis(),
            chargeMode: ChargeMode = ChargeMode.SELF_CONSUMPTION,
            chargeModeStatus: ChargeApplyStatus = ChargeApplyStatus.REPORTED,
            chargeModePending: ChargeMode? = null,
            chargeModeFailure: String? = null,
            readinessExpanded: Boolean = false,
            homeLoadReported: Boolean = true,
            reducedMotion: Boolean = false
        ): BatteryScreenState {
            val telemetry = FixtureData.telemetry(scenario, nowMs)
            val reported = when (telemetry) {
                is Loadable.Ready ->
                    if (homeLoadReported) telemetry
                    // A register that did not answer is NaN, never 0 W: 0 W and "no
                    // reading" mean different things to someone diagnosing a fault.
                    else Loadable.Ready(telemetry.value.copy(homeLoadW = Double.NaN), telemetry.meta)
                else -> telemetry
            }
            return BatteryScreenState(
                scenario = scenario,
                telemetry = reported,
                liveTrail = FixtureData.liveLoadable(scenario, nowMs),
                chargeMode = chargeMode,
                chargeModeStatus = chargeModeStatus,
                chargeModePending = chargeModePending,
                chargeModeFailure = chargeModeFailure,
                readinessExpanded = readinessExpanded,
                demoQualifier = true,
                reducedMotion = reducedMotion
            )
        }

        /** Night: no production, the home draws from the battery (STS-047 discharging). */
        fun nightFixture(
            scenario: FixtureScenario = FixtureState.current(),
            nowMs: Long = System.currentTimeMillis(),
            reducedMotion: Boolean = false
        ): BatteryScreenState = fixture(scenario, nowMs, reducedMotion = reducedMotion)
            .copy(telemetry = FixtureData.nightTelemetry(scenario, nowMs))
    }
}

// ------------------------------------------------------------------ derivations

/** The reading, when there is one. */
val BatteryScreenState.reading: SolarTelemetry? get() = telemetry.valueOrNull()

/** Component-facing state for the value surface, mapped in one shared place. */
val BatteryScreenState.surfaceState: SurfaceState get() = telemetry.toSurfaceState()

/** True when the value is shown but cannot be called live (stale, offline, partial). */
val BatteryScreenState.dimmed: Boolean
    get() = uiState() in setOf(UiState.STALE, UiState.OFFLINE, UiState.PARTIAL)

private fun BatteryScreenState.uiState(): UiState = telemetry.uiState()

/** The classified failure, for the banner and the error surface. */
val BatteryScreenState.failure: ServiceFailure? get() = (telemetry as? Loadable.Failed)?.failure

/** The freshness statement. A simulated system says "Demo data", never "Live" (C1). */
val BatteryScreenState.freshnessText: String get() = telemetry.freshnessLabel()

/** Word-plus-dot role for the freshness stamp. */
val BatteryScreenState.freshnessKind: HeliosStatusKind
    get() = when {
        uiState() == UiState.OFFLINE -> HeliosStatusKind.OFFLINE
        telemetry.metaOrNull()?.simulated == true && uiState() == UiState.READY -> HeliosStatusKind.DEMO
        dimmed -> HeliosStatusKind.STANDBY
        else -> HeliosStatusKind.PRODUCING
    }

/** Charge direction, from the dead band of 30 W either way. */
val BatteryScreenState.ringMode: BatteryRingMode
    get() {
        val powerW = reading?.batteryPowerW ?: return BatteryRingMode.IDLE
        return when {
            powerW > 30 -> BatteryRingMode.CHARGING
            powerW < -30 -> BatteryRingMode.DISCHARGING
            else -> BatteryRingMode.IDLE
        }
    }

/** Usable energy in the pack right now. */
val BatteryScreenState.availableKwh: Double
    get() {
        val value = reading ?: return Double.NaN
        return value.batterySoc / 100.0 * value.batteryCapacityKwh
    }

/**
 * Hours of essentials, the PWA's own formula: available energy over the current home
 * load, floored, with the load clamped at 0.4 kW so a near-zero reading cannot invent a
 * double-digit runtime.
 *
 * NaN when the home load did not report, so the card can say "no data" rather than
 * fabricate hours (STS-053).
 */
val BatteryScreenState.hoursOfEssentials: Double
    get() {
        val value = reading ?: return Double.NaN
        if (value.homeLoadW.isNaN()) return Double.NaN
        val loadKw = (value.homeLoadW / 1000.0).coerceAtLeast(0.4)
        return floor(availableKwh / loadKw)
    }

/** True when the runtime cannot be estimated because the home load is missing. */
val BatteryScreenState.readinessUnavailable: Boolean get() = hoursOfEssentials.isNaN()

/** Share of the pack that is full, for the readiness bar. */
val BatteryScreenState.readinessFraction: Float
    get() {
        val soc = reading?.batterySoc ?: return 0f
        if (soc.isNaN()) return 0f
        return (soc / 100.0).coerceIn(0.0, 1.0).toFloat()
    }

/** SoC at or below 20 percent: attention styling, and the word "Low reserve" (STS-049). */
val BatteryScreenState.lowReserve: Boolean
    get() {
        val soc = reading?.batterySoc ?: return false
        return !soc.isNaN() && soc <= BatteryThreshold.TWENTY.socPct
    }

/** Battery power with an explicit direction sign, because + is charge and - is draw. */
val BatteryScreenState.powerLabel: String
    get() {
        val powerW = reading?.batteryPowerW ?: return HeliosFormat.NO_DATA
        if (powerW.isNaN()) return HeliosFormat.NO_DATA
        val sign = when {
            powerW > 30 -> "+"
            powerW < -30 -> "-"
            else -> ""
        }
        return sign + HeliosFormat.kilowatts(abs(powerW) / 1000.0)
    }

/** Live battery power samples, for the sparkline next to the power tile. */
val BatteryScreenState.trailValues: List<Double>
    get() = (liveTrail.valueOrNull() ?: emptyList())
        .map { it.batteryW }
        .filter { !it.isNaN() }

/** Pack temperature, or "No data" for a register that did not answer (F7). */
val BatteryScreenState.temperatureLabel: String
    get() = HeliosFormat.celsius(reading?.batteryTempC ?: Double.NaN)

/** Capacity in words, for the pack line under the title. */
val BatteryScreenState.packLine: String
    get() {
        val value = reading ?: return "Last known reading"
        return listOf(
            HeliosFormat.kwh(value.batteryCapacityKwh, decimals = 1),
            "${value.batteryCycles} cycles",
            "${HeliosFormat.percent(value.batteryHealthPct, decimals = 1)} health"
        ).joinToString(" \u00B7 ")
    }

/** Health in words, because a percentage alone is not a verdict. */
val BatteryScreenState.healthWord: String
    get() {
        val health = reading?.batteryHealthPct ?: return HeliosFormat.NO_DATA
        if (health.isNaN()) return HeliosFormat.NO_DATA
        return when {
            health >= 95 -> "Excellent"
            health >= 85 -> "Good"
            health >= 70 -> "Fair"
            else -> "Service"
        }
    }

/** One line of the failure banner: what happened, in the words of the failure class. */
val BatteryScreenState.failureMessage: String?
    get() = failure?.message

/** Diagnostic detail for the banner, host and unit id, or the source note. */
val BatteryScreenState.failureDetail: String?
    get() = failure?.detail ?: telemetry.metaOrNull()?.note

/** When the reading was last good, for the offline banner. */
val BatteryScreenState.lastGoodLabel: String?
    get() = failure?.lastGoodAt?.let { "Last good reading ${HeliosFormat.clockTime(it)}" }
        ?: if (dimmed) telemetry.metaOrNull()?.note else null

/** The demo qualifier shown next to every advisory or value from a simulated system. */
val BatteryScreenState.sourceQualifier: String?
    get() = when {
        telemetry.metaOrNull()?.simulated == true -> "Demo data"
        dimmed -> "Last known"
        else -> null
    }

/** Number the ring shows: SoC, capped for the hero at 200 percent text scaling. */
val BatteryScreenState.socPct: Double get() = reading?.batterySoc ?: Double.NaN

/**
 * Remembered fixture state for a screen shell that has no state yet.
 *
 * The default argument of [BatteryScreen], so `HeliosNavGraph` keeps calling
 * `BatteryScreen()` while a caller that owns state (`BatteryScreen(state = ...)`) drives
 * every variant.
 */
@Composable
fun rememberBatteryFixtureState(scenario: FixtureScenario = FixtureState.current()): BatteryScreenState =
    remember(scenario) { BatteryScreenState.fixture(scenario) }
