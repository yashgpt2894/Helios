package com.helios.core.designsystem.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Motion tokens from `design/tokens.json` (`motion`) and
 * `shared-spec/motion-language.md`.
 *
 * Springs are the default for every transition. A cubic curve is only correct for an
 * opacity cross-fade, an indeterminate loader, or the skeleton shimmer, so the curves
 * below are named for those three jobs only.
 */
object HeliosMotion {

    /** Gentle: dampingRatio=0.85, stiffness=120. Hero cards, page entrance, SoC trim. */
    val gentle: SpringSpec<Float> = spring<Float>(dampingRatio = 0.85f, stiffness = 120f)

    /** Default: dampingRatio=0.8, stiffness=200. Standard transitions. */
    val defaultSpring: SpringSpec<Float> = spring<Float>(dampingRatio = 0.8f, stiffness = 200f)

    /** Snappy: dampingRatio=0.7, stiffness=380. Toggles, tickers, nav. */
    val snappy: SpringSpec<Float> = spring<Float>(dampingRatio = 0.7f, stiffness = 380f)

    /** Bouncy: dampingRatio=0.5, stiffness=280. Success moments and empty-state entrance. */
    val bouncy: SpringSpec<Float> = spring<Float>(dampingRatio = 0.5f, stiffness = 280f)

    /** Named spring lookup, for tables and the gallery. */
    fun spring(preset: Preset): SpringSpec<Float> = when (preset) {
        Preset.GENTLE -> gentle
        Preset.DEFAULT -> defaultSpring
        Preset.SNAPPY -> snappy
        Preset.BOUNCY -> bouncy
    }

    enum class Preset { GENTLE, DEFAULT, SNAPPY, BOUNCY }

    /** Duration tokens in milliseconds, the unit Compose needs. */
    object DurationMs {
        const val instant = 0
        const val fast = 150
        const val normal = 250
        const val slow = 400
        const val deliberate = 600
        const val draw = 600
        const val ringTrim = 800
        const val dischargePulse = 1000
        const val chargeGlowPulse = 1600
        const val nodePulse = 2400
        const val shimmer = 2500
        const val screenEnter = 350
        const val reduceMotionCrossfade = 200
    }

    /** Duration tokens in seconds, kept for callers that already use them. */
    object Duration {
        const val instant = 0.0f
        const val fast = 0.15f
        const val normal = 0.25f
        const val slow = 0.4f
        const val deliberate = 0.6f
        const val draw = 0.6f
    }

    /** Stagger delays in milliseconds (entrance choreography). */
    object StaggerMs {
        const val tight = 30
        const val normal = 50
        const val loose = 80
    }

    /** Stagger delays in seconds, kept for callers that already use them. */
    object Stagger {
        const val tight = 0.03f
        const val normal = 0.05f
        const val loose = 0.08f
    }

    /** Screen entrance order from motion-language section 3. */
    object Entrance {
        const val heroDelayMs = 0
        const val statsStaggerMs = 40
        const val sectionHeaderDelayMs = 200
        const val chartDelayMs = 300
        const val chromeDelayMs = 400
        const val chartDrawMs = 600
    }

    /**
     * The only cubic curves in the app. [CURVES.linear] belongs to indeterminate
     * loaders and the shimmer; the other three to opacity cross-fades.
     */
    object Curves {
        val linear: Easing = LinearEasing
        val standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        val decelerate: Easing = CubicBezierEasing(0f, 0f, 0f, 1f)
        val accelerate: Easing = CubicBezierEasing(0.3f, 0f, 1f, 1f)
    }

    /**
     * Reduce Motion collapses every spring to a short opacity cross-fade
     * (motion-language section 11). Components read this flag rather than re-deciding.
     */
    object ReduceMotion {
        const val crossfadeMs = DurationMs.reduceMotionCrossfade
        const val stopsEnergyFlowDots = true
        const val snapsTickers = true
        const val disablesParallax = true
    }
}
