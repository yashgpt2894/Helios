package com.helios.core.designsystem.haptics

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService

/**
 * Haptic feedback wrappers from shared-spec/design-tokens.json haptics section.
 * Maps Android HapticFeedbackConstants + VibrationEffect.Composition.
 */
object HeliosHaptics {

    /**
     * Perform a selection tick haptic (CLOCK_TICK).
     */
    fun selection(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    /**
     * Light impact haptic (CONTEXT_CLICK).
     */
    fun light(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }

    /**
     * Medium impact haptic (VIRTUAL_KEY).
     */
    fun medium(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    /**
     * Heavy impact haptic (LONG_PRESS).
     */
    fun heavy(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    /**
     * Success notification haptic (CONFIRM).
     */
    fun success(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    /**
     * Warning/error haptic (REJECT).
     */
    fun warning(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }

    /**
     * Continuous haptic texture using VibrationEffect.Composition (API 31+).
     * Falls back to LONG_PRESS on older devices.
     */
    fun continuous(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = view.context.getSystemService<VibratorManager>()
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                view.context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.vibrate(
                VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f, 100)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.3f, 80)
                    .compose()
            )
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /**
     * Convenience: dispatch the right haptic for the semantic action from the mapping table.
     */
    fun perform(view: View, action: HapticAction) {
        when (action) {
            HapticAction.TILE_TAP -> light(view)
            HapticAction.PICKER_SCRUB -> selection(view)
            HapticAction.PRIMARY_CTA -> medium(view)
            HapticAction.SHARE_COPY -> success(view)
            HapticAction.FAULT_STATE -> warning(view)
            HapticAction.CONNECTION_FAILURE -> warning(view)
            HapticAction.BATTERY_80 -> light(view)
            HapticAction.BATTERY_50 -> medium(view)
            HapticAction.BATTERY_20 -> heavy(view)
            HapticAction.LONG_PRESS_TEXTURE -> continuous(view)
        }
    }
}

enum class HapticAction {
    TILE_TAP,
    PICKER_SCRUB,
    PRIMARY_CTA,
    SHARE_COPY,
    FAULT_STATE,
    CONNECTION_FAILURE,
    BATTERY_80,
    BATTERY_50,
    BATTERY_20,
    LONG_PRESS_TEXTURE
}
