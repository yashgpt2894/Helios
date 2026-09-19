package com.helios.core.format

import java.util.Locale
import kotlin.math.abs

/**
 * The single formatting source (SVC-17). Every number the UI shows goes through here, so
 * a value cannot be rounded one way on the dashboard and another way in a snapshot.
 *
 * A register read that failed is represented as [Double.NaN], and every function returns
 * [NO_DATA] for it. That distinction is deliberate: showing 0 W for a failed read is a
 * fault-diagnosis lie (FLW-03 hard rules).
 *
 * All output is Locale.US so a capture, a test or a snapshot link reads the same
 * everywhere. Digits use tabular figures where the type token says so; that is a style
 * decision, not a formatting one.
 */
object HeliosFormat {

    /** Shown when a value could not be read. Never shown for a real zero. */
    const val NO_DATA = "No data"

    /** Grid import rate and export rate used by the savings maths (SVC-08). */
    const val IMPORT_RATE = 0.32
    const val EXPORT_RATE = 0.08

    fun watts(value: Double, decimals: Int = 0): String =
        if (value.isNaN()) NO_DATA else "%s W".format(Locale.US, fixed(value, decimals))

    fun kilowatts(value: Double, decimals: Int = 2): String =
        if (value.isNaN()) NO_DATA else "%s kW".format(Locale.US, fixed(value, decimals))

    fun wattsToKilowatts(value: Double, decimals: Int = 2): String =
        if (value.isNaN()) NO_DATA else kilowatts(value / 1000.0, decimals)

    fun kwh(value: Double, decimals: Int = 1): String =
        if (value.isNaN()) NO_DATA else "%s kWh".format(Locale.US, fixed(value, decimals))

    fun percent(value: Double, decimals: Int = 0): String =
        if (value.isNaN()) NO_DATA else "%s%%".format(Locale.US, fixed(value, decimals))

    fun celsius(value: Double, decimals: Int = 1): String =
        if (value.isNaN()) NO_DATA else "%s\u00B0C".format(Locale.US, fixed(value, decimals))

    fun currency(value: Double, decimals: Int = 2): String =
        if (value.isNaN()) NO_DATA else "\u0024%s".format(Locale.US, fixed(value, decimals))

    /** Signed delta for a tile or an insight: "+1.4 kWh", "-3.2%". */
    fun signed(value: Double, unit: String, decimals: Int = 1): String {
        if (value.isNaN()) return NO_DATA
        val sign = if (value >= 0) "+" else "-"
        return "%s%s%s".format(Locale.US, sign, fixed(abs(value), decimals), unit)
    }

    /** Relative age for a freshness stamp: "now", "12 s", "3 min", "2 h". */
    fun age(ageMs: Long): String {
        val seconds = ageMs / 1000
        return when {
            seconds < 2 -> "now"
            seconds < 60 -> "$seconds s"
            seconds < 3600 -> "${seconds / 60} min"
            else -> "${seconds / 3600} h"
        }
    }

    /** Wall clock for "Updated 14:32:06". */
    fun clockTime(epochMs: Long): String =
        java.time.Instant.ofEpochMilli(epochMs)
            .atZone(java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US))

    /** Short weekday for a chart axis or a forecast row. */
    fun weekday(epochMs: Long): String =
        java.time.Instant.ofEpochMilli(epochMs)
            .atZone(java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ofPattern("EEE", Locale.US))

    fun hostPort(host: String, port: Int): String = "$host:$port"

    /** Savings from production: self-consumed energy at the import rate, the rest at the export rate. */
    fun savings(productionKwh: Double, selfConsumedKwh: Double): Double {
        if (productionKwh.isNaN() || selfConsumedKwh.isNaN()) return Double.NaN
        val exported = (productionKwh - selfConsumedKwh).coerceAtLeast(0.0)
        return selfConsumedKwh * IMPORT_RATE + exported * EXPORT_RATE
    }

    fun fixed(value: Double, decimals: Int): String = "%.${decimals}f".format(Locale.US, value)
}
