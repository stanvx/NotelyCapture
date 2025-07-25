package com.module.notelycompose.audio.ui.uicomponents

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import com.module.notelycompose.core.debugPrintln

/**
 * iOS implementation of AudioReactiveLottie using fallback Canvas drawing.
 * 
 * Note: This is a temporary fallback implementation. For full Lottie support on iOS,
 * consider using a cross-platform Lottie solution or native iOS Lottie integration.
 * 
 * Features:
 * - Real-time audio amplitude visualization using animated circles
 * - Gradient-inspired color scheme to match the Android Lottie design
 * - Smooth progress-based animation control for responsive audio feedback
 * - Optimized Canvas drawing for iOS performance
 */
@Composable
actual fun AudioReactiveLottie(
    modifier: Modifier,
    amplitude: Float,
    isRecording: Boolean,
    amplitudeSensitivity: Float,
    minProgress: Float,
    maxProgress: Float
) {
    // Debug amplitude data flow
    LaunchedEffect(amplitude, isRecording) {
        if (isRecording && amplitude > 0f) {
            debugPrintln { "AudioReactiveLottie (iOS): amplitude=$amplitude, isRecording=$isRecording" }
        }
    }
    
    // Smooth amplitude processing to prevent jerky movements
    val smoothAmplitude by animateFloatAsState(
        targetValue = amplitude,
        animationSpec = tween(
            durationMillis = 100,  // Fast response for real-time feel
            easing = FastOutSlowInEasing
        ),
        label = "smoothAmplitude"
    )
    
    // Calculate animation progress based on amplitude and recording state
    val targetProgress = when {
        !isRecording -> minProgress  // Resting state when not recording
        smoothAmplitude <= 0.01f -> minProgress  // Minimal amplitude threshold
        else -> {
            // Map amplitude (0-1) to animation progress range
            val scaledAmplitude = (smoothAmplitude * amplitudeSensitivity).coerceIn(0f, 1f)
            minProgress + (scaledAmplitude * (maxProgress - minProgress))
        }
    }
    
    // Smooth animation progress transitions
    val animationProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(
            durationMillis = 150,  // Slightly slower for smooth transitions
            easing = FastOutSlowInEasing
        ),
        label = "animationProgress"
    )
    
    // Gentle breathing animation when recording but no significant voice input
    val breathingAnimation by rememberInfiniteTransition(label = "breathingTransition").animateFloat(
        initialValue = minProgress,
        targetValue = minProgress + 0.1f,  // Subtle 10% breathing variation
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2500,  // Gentle breathing pace
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingAnimation"
    )
    
    // Choose final progress: use amplitude response or gentle breathing
    val finalProgress = when {
        !isRecording -> minProgress
        smoothAmplitude > 0.03f -> animationProgress  // Use amplitude when voice detected
        else -> breathingAnimation  // Use breathing when recording but quiet
    }
    
    // Debug animation progress
    LaunchedEffect(finalProgress, isRecording) {
        if (isRecording) {
            debugPrintln { "AudioReactiveLottie (iOS): finalProgress=$finalProgress, smoothAmplitude=$smoothAmplitude" }
        }
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Fallback Canvas implementation with gradient-inspired colors
        AudioReactiveCircleFallback(
            progress = finalProgress,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Fallback implementation using Canvas drawing to mimic the gradient orb design.
 * Uses colors inspired by the Lottie animation's gradient palette.
 */
@Composable
private fun AudioReactiveCircleFallback(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
    // Gradient-inspired color palette from the Lottie animation
    val cyanColor = Color(0f, 0.8981f, 1f)     // Bright cyan
    val pinkColor = Color(1f, 0f, 0.4937f)     // Magenta/pink
    val lightBlueColor = Color(0.4975f, 0.6232f, 1f)  // Light blue
    
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = size.minDimension / 2.5f
        
        // Calculate animated radius based on progress
        val currentRadius = maxRadius * progress
        
        // Outer ring - cyan inspired by Lottie gradient
        drawCircle(
            color = cyanColor.copy(alpha = 0.2f),
            center = Offset(centerX, centerY),
            radius = currentRadius,
            style = Stroke(width = 4f)
        )
        
        // Middle ring - light blue inspired by Lottie gradient
        val middleRadius = currentRadius * 0.75f
        drawCircle(
            color = lightBlueColor.copy(alpha = 0.4f),
            center = Offset(centerX, centerY),
            radius = middleRadius,
            style = Stroke(width = 6f)
        )
        
        // Inner filled circle - pink inspired by Lottie gradient
        val innerRadius = currentRadius * 0.5f
        drawCircle(
            color = pinkColor.copy(alpha = 0.6f),
            center = Offset(centerX, centerY),
            radius = innerRadius
        )
        
        // Central core - primary color for consistency
        val coreRadius = currentRadius * 0.25f
        drawCircle(
            color = primaryColor.copy(alpha = 0.9f),
            center = Offset(centerX, centerY),
            radius = coreRadius
        )
    }
}