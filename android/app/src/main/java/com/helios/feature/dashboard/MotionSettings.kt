package com.helios.feature.dashboard

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.helios.core.designsystem.motion.HeliosMotion

/**
 * The two motion settings every animated component on the Dashboard and the Production
 * screen reads, resolved once per screen and passed down.
 *
 * Why this type exists rather than a raw boolean: `design/tokens.json` fixes the durations
 * (`motion.durationMs`), and the device supplies two more inputs that change them:
 *
 *  - Reduce Motion (Android "Remove animations", animator duration scale 0) collapses a
 *    spring to [HeliosMotion.ReduceMotion.crossfadeMs] and stops the repeating loops
 *    (`motion.reduceMotion` in the token file).
 *  - The animator duration scale multiplies a token duration, so a device or an
 *    instrumented test that slows animation down slows these components with it.
 *
 * It lives in `feature/dashboard` because `core/designsystem` is owned by another step of
 * this plan; the Production screen imports it from here. A later step can move it into the
 * design system without changing a call site, because every component takes it as a
 * parameter.
 */
@Immutable
data class HeliosMotionSettings(
    val reduceMotion: Boolean = false,
    val animationScale: Float = 1f
) {

    /** True when a repeating animation may run at all. */
    fun animates(): Boolean = !reduceMotion && animationScale > 0f

    /** A token duration in milliseconds, with Reduce Motion and the device scale applied. */
    fun durationMs(tokenMs: Int): Int = when {
        reduceMotion -> HeliosMotion.ReduceMotion.crossfadeMs
        else -> (tokenMs * animationScale).toInt().coerceAtLeast(1)
    }

    /** A token duration as a spec, for transitions that cannot be spring-interpolated. */
    fun <T> tweenMs(tokenMs: Int): FiniteAnimationSpec<T> = tween(durationMillis = durationMs(tokenMs))

    /**
     * A spring from the token set, or the short cross-fade when Reduce Motion is on.
     * Springs are the default; a cubic curve is only for opacity and indeterminate loops.
     */
    fun springOr(spec: SpringSpec<Float>): FiniteAnimationSpec<Float> =
        if (animates()) spec else tween(durationMillis = HeliosMotion.ReduceMotion.crossfadeMs)

    companion object {
        /** Full motion. The default for a preview, a screenshot or a JVM test. */
        val Default = HeliosMotionSettings()

        /** Reduce Motion on: no loops, token springs replaced by a 200 ms cross-fade. */
        val ReduceMotion = HeliosMotionSettings(reduceMotion = true)
    }
}

/** The device setting, without a host overriding it. */
fun heliosMotionSettingsFrom(context: Context): HeliosMotionSettings {
    val scale = runCatching {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    }.getOrDefault(1f)
    return HeliosMotionSettings(
        reduceMotion = scale <= 0f,
        animationScale = if (scale <= 0f) 1f else scale
    )
}

/**
 * Resolve the device motion settings. A caller that pins them (a capture host, a preview,
 * a test) passes [HeliosMotionSettings] explicitly instead of calling this.
 */
@Composable
fun rememberHeliosMotionSettings(): HeliosMotionSettings {
    val context = LocalContext.current
    return remember(context) { heliosMotionSettingsFrom(context) }
}
