package com.module.notelycompose.platform

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants

actual class HapticFeedback {
    private var context: Context? = null
    
    fun initialize(context: Context) {
        this.context = context
    }

    actual fun light() {
        performHapticFeedback(HapticFeedbackType.LIGHT)
    }

    actual fun medium() {
        performHapticFeedback(HapticFeedbackType.MEDIUM)
    }

    actual fun heavy() {
        performHapticFeedback(HapticFeedbackType.HEAVY)
    }

    actual fun success() {
        performHapticFeedback(HapticFeedbackType.SUCCESS)
    }

    actual fun error() {
        performHapticFeedback(HapticFeedbackType.ERROR)
    }

    private fun performHapticFeedback(type: HapticFeedbackType) {
        val context = this.context ?: return
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ - Use VibratorManager
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.let { performModernHapticFeedback(it, type) }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ - Use VibrationEffect
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.let { performModernHapticFeedback(it, type) }
            } else {
                // Legacy Android - Use simple vibration
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.let { performLegacyHapticFeedback(it, type) }
            }
        } catch (e: Exception) {
            android.util.Log.w("HapticFeedback", "Failed to perform haptic feedback: ${e.message}")
        }
    }

    private fun performModernHapticFeedback(vibrator: Vibrator, type: HapticFeedbackType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (type) {
                HapticFeedbackType.LIGHT -> VibrationEffect.createOneShot(50, 50)
                HapticFeedbackType.MEDIUM -> VibrationEffect.createOneShot(100, 100)
                HapticFeedbackType.HEAVY -> VibrationEffect.createOneShot(150, 200)
                HapticFeedbackType.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 100), -1)
                HapticFeedbackType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100, 50, 100), -1)
            }
            
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(effect)
            }
        }
    }

    private fun performLegacyHapticFeedback(vibrator: Vibrator, type: HapticFeedbackType) {
        if (vibrator.hasVibrator()) {
            val duration = when (type) {
                HapticFeedbackType.LIGHT -> 50L
                HapticFeedbackType.MEDIUM -> 100L
                HapticFeedbackType.HEAVY -> 150L
                HapticFeedbackType.SUCCESS -> 200L
                HapticFeedbackType.ERROR -> 300L
            }
            
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private enum class HapticFeedbackType {
        LIGHT, MEDIUM, HEAVY, SUCCESS, ERROR
    }
}