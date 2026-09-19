package com.helios.feature.dashboard

import com.helios.core.domain.model.HistoryPoint
import com.helios.core.format.HeliosFormat
import kotlin.math.max
import kotlin.math.min

/**
 * The four node wattages of the hero instrument, after [DashboardMetrics.flowSample] has made
 * them describe one system: `solar = home + battery + grid`, by construction.
 *
 * Sign convention, as everywhere in this app: a positive battery is charging, a positive grid
 * is exporting, a negative grid is importing.
 */
data class FlowSample(
    val solarW: Double,
    val homeW: Double,
    val batteryW: Double,
    val gridW: Double
) {
    /** True when the four paths add up to the array's output. */
    fun conserves(toleranceW: Double = 1.0): Boolean {
        if (solarW.isNaN() || homeW.isNaN() || batteryW.isNaN() || gridW.isNaN()) return false
        return kotlin.math.abs(homeW + batteryW + gridW - solarW) <= toleranceW
    }
}

/**
 * The Dashboard's derived numbers, as pure functions.
 *
 * They live here rather than in a screen so a test can assert them without a Compose host,
 * and so the Dashboard and the snapshot share one definition of "self-use". Every function
 * returns [Double.NaN] for a value it cannot compute, which the UI renders as "No data"
 * instead of a fabricated zero.
 */
object DashboardMetrics {

    /** The PWA factor for avoided CO2 per produced kWh (`src/pages/Dashboard.tsx`). */
    const val CO2_KG_PER_KWH = 0.42

    /** One mature tree takes up about 21 kg of CO2 a year (`src/pages/Dashboard.tsx`). */
    const val KG_CO2_PER_TREE_YEAR = 21.0

    private const val HALF_HOUR_H = 0.5

    /**
     * A number for display, with the one rule the design repeats: a value that did not report
     * is [HeliosFormat.NO_DATA], never 0 and never the literal "NaN". `HeliosFormat.fixed`
     * cannot know whether a caller has already checked, so the screens call this instead.
     */
    fun number(value: Double, decimals: Int): String =
        if (value.isNaN()) HeliosFormat.NO_DATA else HeliosFormat.fixed(value, decimals)

    /** Energy in kWh behind a half-hour series of watts. */
    fun kwhFromSeries(points: List<HistoryPoint>, watts: (HistoryPoint) -> Double): Double {
        if (points.isEmpty()) return Double.NaN
        val total = points.sumOf { if (watts(it).isNaN()) 0.0 else watts(it) }
        return total * HALF_HOUR_H / 1000.0
    }

    /**
     * Self-use: the share of today's production that never left the house, computed the way a
     * solar app defines it — produced minus exported, divided by produced.
     *
     * The definition matters: an earlier "minimum of production and consumption per interval"
     * version under-counts, because the energy the house uses in the evening comes from the
     * battery, not from the current production, and that energy was solar when it was stored.
     * With the grid export in the subtraction the number agrees with the advisory copy the
     * design ships ("82 percent of your solar is powering your home directly").
     */
    fun selfUsePercent(points: List<HistoryPoint>): Double {
        if (points.isEmpty()) return Double.NaN
        val produced = points.sumOf { if (it.productionW.isNaN()) 0.0 else max(it.productionW, 0.0) }
        if (produced <= 0.0) return Double.NaN
        val exported = points.sumOf { if (it.gridW.isNaN()) 0.0 else max(it.gridW, 0.0) }
        return ((produced - exported) / produced * 100.0).coerceIn(0.0, 100.0)
    }

    /**
     * Exported energy behind a series, in kWh: the grid side of the same subtraction.
     */
    fun exportedKwh(points: List<HistoryPoint>): Double = kwhFromSeries(points) { max(it.gridW, 0.0) }

    /**
     * The four node wattages of the hero instrument, as one system that conserves energy.
     *
     * Solar and the house load are the reported registers, unchanged. The battery takes what
     * is left after the house, and the grid carries the residual, so
     * `solar = home + battery + grid` holds for every reading by construction. The rule is
     * needed because an inverter reports its registers independently and a fixture (or a real
     * device at a different sample instant) can report a battery charge and a grid export that
     * add up to more than the array is making; a picture of energy moving between four
     * endpoints must not show 8.6 kW of flow out of a 4.2 kW array.
     *
     * Sign convention, as everywhere in this app: a positive battery is charging, a positive
     * grid is exporting and a negative grid is importing.
     */
    fun flowSample(
        solarW: Double,
        homeW: Double,
        batteryW: Double,
        gridW: Double
    ): FlowSample {
        if (solarW.isNaN() || homeW.isNaN() || batteryW.isNaN() || gridW.isNaN()) {
            return FlowSample(Double.NaN, Double.NaN, Double.NaN, Double.NaN)
        }
        val surplus = solarW - homeW
        val battery = when {
            batteryW > 0 -> min(batteryW, max(surplus, 0.0))
            else -> max(batteryW, min(surplus, 0.0))
        }
        return FlowSample(
            solarW = solarW,
            homeW = homeW,
            batteryW = battery,
            gridW = surplus - battery
        )
    }

    /** Avoided CO2 for a produced energy in kWh. */
    fun co2Kg(producedKwh: Double): Double =
        if (producedKwh.isNaN()) Double.NaN else producedKwh * CO2_KG_PER_KWH

    /** The same CO2 expressed the way the app's summary line does. */
    fun treesEquivalent(producedKwh: Double): Double {
        val co2 = co2Kg(producedKwh)
        return if (co2.isNaN()) Double.NaN else co2 / KG_CO2_PER_TREE_YEAR
    }

    /**
     * Downsample the day's production for a tile sparkline: a tile is 64 dp wide, so 12
     * points carry the shape and the rest is noise.
     */
    fun sparkline(points: List<HistoryPoint>, buckets: Int = 12): List<Double> {
        if (points.size < 2) return emptyList()
        val size = points.size
        return (0 until buckets).map { bucket ->
            val start = bucket * size / buckets
            val end = (((bucket + 1) * size / buckets) - 1).coerceAtLeast(start)
            val slice = points.subList(start, end + 1)
            slice.maxOfOrNull { if (it.productionW.isNaN()) 0.0 else it.productionW } ?: 0.0
        }
    }
}
