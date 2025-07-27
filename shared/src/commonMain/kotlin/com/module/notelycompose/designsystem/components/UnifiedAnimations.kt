package com.module.notelycompose.designsystem.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

/**
 * Unified animation and motion components following Material 3 Expressive design principles
 * 
 * Provides consistent motion, timing, and transition patterns across the application.
 * Based on Material Motion guidelines for fluid, purposeful animations.
 */

/**
 * Material 3 motion tokens for consistent animation timing
 */
object Material3MotionTokens {
    // Duration tokens
    val durationShort = 150
    val durationMedium = 300
    val durationLong = 500
    val durationExtraLong = 700
    
    // Easing tokens
    val easingStandard = CubicBezierEasing(0.2f, 0.0f, 0f, 1.0f)
    val easingDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val easingAccelerate = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    val easingEmphasized = CubicBezierEasing(0.2f, 0.0f, 0f, 1.0f)
}

/**
 * Fade transition for content appearing/disappearing
 */
@Composable
fun FadeTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    duration: Int = Material3MotionTokens.durationMedium,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = duration,
                easing = Material3MotionTokens.easingStandard
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = duration,
                easing = Material3MotionTokens.easingStandard
            )
        ),
        content = content
    )
}

/**
 * Slide transition for content entering from different directions
 */
@Composable
fun SlideTransition(
    visible: Boolean,
    direction: SlideDirection = SlideDirection.Up,
    modifier: Modifier = Modifier,
    duration: Int = Material3MotionTokens.durationMedium,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val (enter, exit) = when (direction) {
        SlideDirection.Up -> slideInVertically(
            animationSpec = tween(duration, easing = Material3MotionTokens.easingDecelerate),
            initialOffsetY = { it }
        ) to slideOutVertically(
            animationSpec = tween(duration, easing = Material3MotionTokens.easingAccelerate),
            targetOffsetY = { it }
        )
        SlideDirection.Down -> slideInVertically(
            animationSpec = tween(duration, easing = Material3MotionTokens.easingDecelerate),
            initialOffsetY = { -it }
        ) to slideOutVertically(
            animationSpec = tween(duration, easing = Material3MotionTokens.easingAccelerate),
            targetOffsetY = { -it }
        )
        SlideDirection.Left -> slideInHorizontally(
            animationSpec = tween(duration, easing = Material3MotionTokens.easingDecelerate),
            initialOffsetX = { it }
        ) to slideOutHorizontally(
            animationSpec = tween(duration, easing = Material3MotionTokens.easingAccelerate),
            targetOffsetX = { it }
        )
        SlideDirection.Right -> slideInHorizontally(
            animationSpec = tween(duration, easing = Material3MotionTokens.easingDecelerate),
            initialOffsetX = { -it }
        ) to slideOutHorizontally(
            animationSpec = tween(duration, easing = Material3MotionTokens.easingAccelerate),
            targetOffsetX = { -it }
        )
    }
    
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter + fadeIn(tween(duration)),
        exit = exit + fadeOut(tween(duration)),
        content = content
    )
}

/**
 * Scale transition for content growing/shrinking
 */
@Composable
fun ScaleTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    duration: Int = Material3MotionTokens.durationMedium,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = scaleIn(
            animationSpec = tween(
                durationMillis = duration,
                easing = Material3MotionTokens.easingEmphasized
            ),
            initialScale = 0.8f
        ) + fadeIn(tween(duration)),
        exit = scaleOut(
            animationSpec = tween(
                durationMillis = duration,
                easing = Material3MotionTokens.easingEmphasized
            ),
            targetScale = 0.8f
        ) + fadeOut(tween(duration)),
        content = content
    )
}

/**
 * Shared element-style transition for content transforming
 */
@Composable
fun SharedElementTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            animationSpec = tween(
                Material3MotionTokens.durationLong,
                easing = Material3MotionTokens.easingEmphasized
            ),
            initialOffsetY = { it / 4 }
        ) + fadeIn(
            tween(Material3MotionTokens.durationMedium)
        ) + scaleIn(
            animationSpec = tween(
                Material3MotionTokens.durationLong,
                easing = Material3MotionTokens.easingEmphasized
            ),
            initialScale = 0.9f
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                Material3MotionTokens.durationMedium,
                easing = Material3MotionTokens.easingAccelerate
            ),
            targetOffsetY = { -it / 4 }
        ) + fadeOut(
            tween(Material3MotionTokens.durationShort)
        ) + scaleOut(
            animationSpec = tween(
                Material3MotionTokens.durationMedium,
                easing = Material3MotionTokens.easingAccelerate
            ),
            targetScale = 0.9f
        ),
        content = content
    )
}

/**
 * Floating action button scale animation
 */
@Composable
fun AnimatedFAB(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ScaleTransition(
        visible = visible,
        modifier = modifier,
        duration = Material3MotionTokens.durationShort    ) {
        FABSurface(
            onClick = onClick,
            content = content
        )
    }
}

/**
 * Animated card with hover/press effects
 */
@Composable
fun AnimatedCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed && onClick != null) 2.dp else 4.dp,
        animationSpec = tween(
            Material3MotionTokens.durationShort        ),
        label = "card_elevation"
    )
    
    CardSurface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        content()
    }
}

/**
 * Animated progress indicator
 */
@Composable
fun AnimatedProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    duration: Int = Material3MotionTokens.durationLong) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = duration,
            easing = Material3MotionTokens.easingStandard
        ),
        label = "progress_animation"
    )
    
    LinearProgressIndicator(
        progress = animatedProgress,
        modifier = modifier
    )
}

/**
 * Slide directions for slide transitions
 */
enum class SlideDirection {
    Up, Down, Left, Right
}

/**
 * Animated content crossfade for switching between different content
 */
@Composable
fun <T> AnimatedContentCrossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(
        Material3MotionTokens.durationMedium    ),
    content: @Composable (T) -> Unit
) {
    Crossfade(
        targetState = targetState,
        modifier = modifier,
        animationSpec = animationSpec,
        content = content
    )
}

/**
 * Animated list item for smooth list updates
 */
@Composable
fun AnimatedListItem(
    modifier: Modifier = Modifier,
    enterDelay: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(enterDelay.toLong())
        visible = true
    }
    
    SlideTransition(
        visible = visible,
        direction = SlideDirection.Up,
        modifier = modifier,
        content = { content() }
    )
}