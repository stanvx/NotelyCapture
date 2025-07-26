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
        animationSpec = tween(durationMillis = 120), // Fast response
        label = "smoothAmplitude"
    )

    // 3. Use derivedStateOf to efficiently calculate the target speed and scale.
    val targetSpeed by remember {
        derivedStateOf {
            if (isRecording) 1.0f + (smoothAmplitude * 2.5f) else 0.5f
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
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
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