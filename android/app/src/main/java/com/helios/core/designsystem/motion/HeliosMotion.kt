package com.helios.core.designsystem.motion

import androidx.compose.animation.core.spring

/**
 * Named spring presets from shared-spec/design-tokens.json motion section.
 * Android variants use dampingRatio + stiffness.
 */
object HeliosMotion {

    /** Gentle: dampingRatio=0.85, stiffness=120 */
    val gentle: SpringSpec = SpringSpec(
        spring = spring(dampingRatio = 0.85f, stiffness = 120f)
    )

    /** Default: dampingRatio=0.8, stiffness=200 */
    val defaultSpring: SpringSpec = SpringSpec(
        spring = spring(dampingRatio = 0.8f, stiffness = 200f)
    )

    /** Snappy: dampingRatio=0.7, stiffness=380 */
    val snappy: SpringSpec = SpringSpec(
        spring = spring(dampingRatio = 0.7f, stiffness = 380f)
    )

    /** Bouncy: dampingRatio=0.5, stiffness=280 */
    val bouncy: SpringSpec = SpringSpec(
        spring = spring(dampingRatio = 0.5f, stiffness = 280f)
    )

    /** Duration tokens in seconds */
    object Duration {
        const val instant = 0.0f
        const val fast = 0.15f
        const val normal = 0.25f
        const val slow = 0.4f
        const val deliberate = 0.6f
        const val draw = 0.6f
    }

    /** Stagger delays in seconds */
    object Stagger {
        const val tight = 0.03f
        const val normal = 0.05f
        const val loose = 0.08f
    }
}

/** Wrapper so spring specs read cleanly at call sites. */
data class SpringSpec(val spring: androidx.compose.animation.core.SpringSpec<Float>)
