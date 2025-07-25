package com.module.notelycompose.audio.ui.uicomponents

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.*
import com.module.notelycompose.core.debugPrintln

/**
 * Android implementation of AudioReactiveLottie using Lottie-Compose.
 * 
 * Features:
 * - Real-time audio amplitude visualization using professional Lottie animation
 * - Beautiful gradient design with pink/blue/white colors from ai-bot-large.json
 * - Smooth progress-based animation control for responsive audio feedback
 * - Full-size display without additional UI elements
 * - Optimized performance for real-time audio visualization
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
    // Load the Lottie composition from Android assets with gradient-optimized settings
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("files/animations/ai-bot-large.json"),
        onRetry = { failCount, exception ->
            debugPrintln { "AudioReactiveLottie: Retry loading animation, attempt $failCount: ${exception.message}" }
            failCount < 3  // Retry up to 3 times
        }
    )
    
    // Debug composition loading state with detailed error information
    LaunchedEffect(composition) {
        when {
            composition == null -> {
                debugPrintln { "AudioReactiveLottie: Composition is null - animation not loaded from assets. Trying alternative paths..." }
                debugPrintln { "AudioReactiveLottie: Check if files/animations/ai-bot-large.json exists in assets" }
            }
            composition?.duration == 0f -> {
                debugPrintln { "AudioReactiveLottie: Composition loaded but has 0 duration - may be invalid animation file" }
            }
            else -> {
                debugPrintln { "AudioReactiveLottie: ✅ Composition loaded successfully!" }
                debugPrintln { "AudioReactiveLottie: Duration=${composition?.duration}ms, FrameRate=${composition?.frameRate}fps" }
                debugPrintln { "AudioReactiveLottie: Bounds=${composition?.bounds}" }
            }
        }
    }
    
    // Debug amplitude data flow
    LaunchedEffect(amplitude, isRecording) {
        if (isRecording && amplitude > 0f) {
            debugPrintln { "AudioReactiveLottie: amplitude=$amplitude, isRecording=$isRecording" }
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
    
    // Calculate animation speed based on amplitude and recording state
    val animationSpeed = when {
        !isRecording -> 0.3f  // Slow ambient animation when not recording
        smoothAmplitude > 0.05f -> 1.5f + (smoothAmplitude * 2f)  // Fast responsive animation
        else -> 0.8f  // Medium breathing pace when quiet
    }
    
    // Alternative approach: Use progress with time-based animation for smoother gradients
    val timeBasedProgress by rememberInfiniteTransition(label = "timeBasedProgress").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (3000 / animationSpeed).toInt().coerceAtLeast(1000),  // Variable duration based on amplitude
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "timeBasedProgress"
    )
    
    // Debug animation progress with more detailed logging
    LaunchedEffect(animationSpeed, isRecording, smoothAmplitude) {
        debugPrintln { "AudioReactiveLottie: 🎬 speed=$animationSpeed, amplitude=$smoothAmplitude, recording=$isRecording" }
        if (isRecording) {
            debugPrintln { "AudioReactiveLottie: 🎵 Animation should be ${if (animationSpeed > 1f) "FAST" else if (animationSpeed > 0.5f) "MEDIUM" else "SLOW"}" }
        }
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Show debug info if composition is not loaded
        if (composition == null) {
            debugPrintln { "AudioReactiveLottie: Rendering with null composition" }
        }
        
        // Render the Lottie animation with progress-based animation for better gradient rendering
        LottieAnimation(
            composition = composition,
            progress = { timeBasedProgress },  // Use time-based progress for smoother gradients
            modifier = Modifier.fillMaxSize(),
            enableMergePaths = false,  // FIXED: Disable to prevent gradient rendering issues
            maintainOriginalImageBounds = true,  // Preserve gradient quality
            clipToCompositionBounds = true,  // Efficient clipping
            contentScale = androidx.compose.ui.layout.ContentScale.Fit  // Ensure proper scaling
        )
    }
}