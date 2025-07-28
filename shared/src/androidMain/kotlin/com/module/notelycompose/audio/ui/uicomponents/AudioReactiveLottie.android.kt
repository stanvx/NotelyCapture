package com.module.notelycompose.audio.ui.uicomponents

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.compose.runtime.DotLottieController
import com.lottiefiles.dotlottie.core.util.DotLottieSource

@Composable
actual fun AudioReactiveLottie(
    modifier: Modifier,
    amplitude: Float, // Normalized between 0.0 and 1.0
    isRecording: Boolean,
) {
    // 1. Hoist the source and controller to create them only once.
    val source by remember {
        mutableStateOf(DotLottieSource.Asset("files/animations/recording-visual.lottie"))
    }
    val controller by remember { mutableStateOf(DotLottieController()) }

    // 2. Animate the amplitude to prevent jittery visual feedback.
    val smoothAmplitude by animateFloatAsState(
        targetValue = if (isRecording) amplitude else 0f,
        animationSpec = tween(durationMillis = 180), // Increased by 50% (120ms → 180ms) for slower response
        label = "smoothAmplitude"
    )

    // 3. Use derivedStateOf to efficiently calculate the target speed and scale.
    val targetSpeed by remember {
        derivedStateOf {
            // Reduced by 50% for slower animation (1.0f → 0.5f, 2.5f → 1.25f, 0.5f → 0.25f)
            if (isRecording) 0.5f + (smoothAmplitude * 1.25f) else 0.25f
        }
    }
    val targetScale by remember {
        derivedStateOf {
            1.0f + (smoothAmplitude * 0.15f)
        }
    }

    // 4. Animate transitions to the target speed and scale for a fluid feel.
    val animatedSpeed by animateFloatAsState(
        targetValue = targetSpeed,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing), // Increased by 50% (600ms → 900ms)
        label = "animatedSpeed"
    )
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "animatedScale"
    )

    // 5. Update the animation imperatively inside a LaunchedEffect for best performance.
    LaunchedEffect(animatedSpeed) {
        controller.setSpeed(animatedSpeed)
    }

    // 6. Start/stop animation based on recording state
    LaunchedEffect(isRecording) {
        if (isRecording) {
            controller.play()
        } else {
            controller.pause()
        }
    }

    // 7. Clean up DotLottieController when component leaves composition
    DisposableEffect(controller) {
        onDispose {
            // DotLottieController cleanup is handled automatically
            // No explicit dispose method available in current API
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        DotLottieAnimation(
            source = source,
            autoplay = true,
            loop = true,
            controller = controller,
            modifier = Modifier
                .fillMaxSize()
                // Use graphicsLayer for the most performant application of scale.
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
        )
    }
}