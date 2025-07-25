package com.module.notelycompose.audio.ui.uicomponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Audio-reactive animation component displaying a beautiful gradient orb
 * that responds to real-time audio amplitude data.
 * 
 * Platform-specific implementation:
 * - Android: Uses Lottie animation with ai-bot-large.json
 * - iOS: Uses fallback implementation (for now)
 * 
 * Features:
 * - Real-time audio amplitude visualization 
 * - Beautiful gradient design with pink/blue/white colors
 * - Smooth progress-based animation control for responsive audio feedback
 * - Full-size display without additional UI elements
 * - Optimized performance for real-time audio visualization
 */
@Composable
expect fun AudioReactiveLottie(
    modifier: Modifier = Modifier,
    amplitude: Float = 0f,
    isRecording: Boolean = false,
    amplitudeSensitivity: Float = 1.2f,
    minProgress: Float = 0.2f,  // Resting state (20% of animation)
    maxProgress: Float = 0.9f   // Max amplitude state (90% of animation)
)

/**
 * Simplified version for quick recording feedback.
 * Uses the same beautiful gradient animation with optimized settings.
 */
@Composable
fun SimpleAudioReactiveLottie(
    modifier: Modifier = Modifier,
    amplitude: Float = 0f,
    isRecording: Boolean = false
) {
    AudioReactiveLottie(
        modifier = modifier,
        amplitude = amplitude,
        isRecording = isRecording,
        amplitudeSensitivity = 1.0f,  // Standard sensitivity
        minProgress = 0.25f,  // Slightly larger resting state
        maxProgress = 0.85f   // Slightly smaller max for subtlety
    )
}