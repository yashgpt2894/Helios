package com.helios.core.data.repository

import kotlin.math.*

/**
 * Solar-curve math ported byte-for-byte from PWA src/lib/solarCurve.ts.
 */
object SolarCurve {

    private const val SUNRISE_HOUR = 6.2
    private const val SUNSET_HOUR = 19.8
    private const val PEAK_HOUR = 13.0

    fun solarFractionAt(hourFloat: Double): Double {
        if (hourFloat <= SUNRISE_HOUR || hourFloat >= SUNSET_HOUR) return 0.0
        val x = (hourFloat - SUNRISE_HOUR) / (SUNSET_HOUR - SUNRISE_HOUR)
        val bell = sin(PI * x)
        val skew = 1 - abs(hourFloat - PEAK_HOUR) / 8.0
        return clamp(bell * skew, 0.0, 1.0)
    }

    fun irradianceAt(hourFloat: Double, cloudCover: Double): Double {
        val clear = solarFractionAt(hourFloat) * 1000.0
        val transmittance = 1.0 - cloudCover * 0.7
        return max(0.0, clear * transmittance)
    }

    fun consumptionFractionAt(hourFloat: Double): Double {
        val morning = exp(-(hourFloat - 7.5).pow(2) / 2.5) * 0.7
        val evening = exp(-(hourFloat - 19.0).pow(2) / 4.0) * 1.0
        val baseline = 0.18
        val midday = exp(-(hourFloat - 13.0).pow(2) / 18.0) * 0.25
        return clamp(baseline + morning + evening + midday, 0.12, 1.2)
    }

    fun nowAsHourFloat(now: Long = System.currentTimeMillis()): Double {
        val hours = ((now / 3600000) % 24).toInt()
        val minutes = ((now / 60000) % 60).toInt()
        val seconds = ((now / 1000) % 60).toInt()
        return hours + minutes / 60.0 + seconds / 3600.0
    }

    fun clamp(value: Double, min: Double, max: Double): Double {
        return value.coerceIn(min, max)
    }
}
