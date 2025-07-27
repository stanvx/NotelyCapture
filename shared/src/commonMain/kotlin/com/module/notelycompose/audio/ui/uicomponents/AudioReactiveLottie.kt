package com.module.notelycompose.audio.ui.uicomponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Modern AudioReactiveLottie component with high-performance audio visualization.
 * 
 * Features:
 * - Real-time audio amplitude visualization using recording-visual.lottie
 * - Beautiful gradient design with reactive colors and smooth animations
 * - Optimized performance with state hoisting and efficient recomposition
 * - Two-state behavior: idle breathing effect and reactive recording visualization
 * - Smooth transitions with amplitude-based speed and scale animations
 * - Software rendering for proper gradient and blur effect support
 * 
 * Platform-specific implementation:
 * - Android: Uses dotLottie with modern Compose state management
 * - iOS: Uses fallback implementation (for now)
 */
@Composable
expect fun AudioReactiveLottie(
    modifier: Modifier = Modifier,
    amplitude: Float = 0f,        // Normalized amplitude value (0.0f to 1.0f)
    isRecording: Boolean = false  // Recording state for behavior switching
)