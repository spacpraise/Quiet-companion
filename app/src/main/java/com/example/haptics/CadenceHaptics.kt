package com.example.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object CadenceHaptics {

    enum class HapticType {
        BUTTON_CLICK,
        TASK_COMPLETE,
        BOOK_COMPLETE,
        IMPORTANT_ACTION,
        ALARM_PULSE,
        PAGE_FLIP,
        SELECTION
    }

    fun performHaptic(context: Context, type: HapticType, isEnabled: Boolean = true) {
        if (!isEnabled) return

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return

            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    HapticType.BUTTON_CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    HapticType.SELECTION -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    HapticType.TASK_COMPLETE -> {
                        // Subtle double pulse
                        VibrationEffect.createWaveform(longArrayOf(0, 30, 60, 45), intArrayOf(0, 140, 0, 200), -1)
                    }
                    HapticType.BOOK_COMPLETE -> {
                        // Gentle celebratory rhythm
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 40, 50, 40, 50, 70),
                            intArrayOf(0, 120, 0, 160, 0, 240),
                            -1
                        )
                    }
                    HapticType.IMPORTANT_ACTION -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    HapticType.ALARM_PULSE -> {
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 200, 150, 200),
                            intArrayOf(0, 180, 0, 220),
                            -1
                        )
                    }
                    HapticType.PAGE_FLIP -> {
                        VibrationEffect.createOneShot(12, 60)
                    }
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = when (type) {
                    HapticType.BUTTON_CLICK, HapticType.PAGE_FLIP, HapticType.SELECTION -> longArrayOf(0, 20)
                    HapticType.TASK_COMPLETE -> longArrayOf(0, 30, 60, 45)
                    HapticType.BOOK_COMPLETE -> longArrayOf(0, 40, 50, 40, 50, 70)
                    HapticType.IMPORTANT_ACTION -> longArrayOf(0, 50)
                    HapticType.ALARM_PULSE -> longArrayOf(0, 200, 150, 200)
                }
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (_: Exception) {
            // Gracefully handle devices where vibration permissions or hardware fail
        }
    }
}
