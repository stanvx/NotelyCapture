package com.module.notelycompose.notes.ui.richtext

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext

/**
 * Android-specific implementation of haptic feedback for rich text editing.
 */
actual class PlatformHapticFeedbackManager actual constructor() {
    
    private var vibrator: Vibrator? = null
    
    fun initialize(context: Context) {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    
    actual fun performHapticFeedback(type: HapticFeedbackType) {
        val androidVibrator = vibrator ?: return
        
        val duration = when (type) {
            HapticFeedbackType.LongPress -> 50L
            HapticFeedbackType.TextHandleMove -> 20L
            else -> 30L
        }
        
        if (androidVibrator.hasVibrator()) {
            val effect = VibrationEffect.createOneShot(
                duration,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            androidVibrator.vibrate(effect)
        }
    }
    
    actual fun performCustomHaptic(duration: Long, intensity: Float) {
        val androidVibrator = vibrator ?: return
        
        if (androidVibrator.hasVibrator()) {
            val amplitude = (intensity * 255).toInt().coerceIn(1, 255)
            val effect = VibrationEffect.createOneShot(
                duration,
                amplitude
            )
            androidVibrator.vibrate(effect)
        }
    }
    
    actual fun isHapticFeedbackEnabled(): Boolean {
        return vibrator?.hasVibrator() ?: false
    }
}