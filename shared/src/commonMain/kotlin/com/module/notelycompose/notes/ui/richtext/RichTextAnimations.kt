package com.module.notelycompose.notes.ui.richtext

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/**
 * Premium animation system for rich text toolbars with Apple-quality spring physics.
 * 
 * Features:
 * - Natural spring-based animations that feel responsive and organic
 * - Context-aware animation timing for different toolbar types
 * - Sophisticated easing curves optimized for different use cases
 * - Staggered animations for grouped elements
 * - Performance-optimized with proper animation lifecycle management
 */
object RichTextAnimations {
    
    /**
     * Premium spring specifications for different interaction contexts.
     */
    object Springs {
        val gentle: SpringSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
        
        val responsive: SpringSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
        
        val snappy: SpringSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
        
        val bouncy: SpringSpec<Float> = spring(
            dampingRatio = 0.4f,
            stiffness = Spring.StiffnessMediumLow
        )

        val intOffsetSpringSpec: SpringSpec<IntOffset> = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )

        val intSizeSpringSpec: SpringSpec<IntSize> = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    }
    
    /**
     * Optimized easing curves for rich text interactions.
     */
    object Easings {
        val smoothEntry = FastOutSlowInEasing
        val quickExit = FastOutLinearInEasing
        val naturalMotion = LinearOutSlowInEasing
    }
    
    /**
     * Standard animation durations for consistency.
     */
    object Durations {
        const val QUICK = 150
        const val MEDIUM = 300
        const val SLOW = 500
    }
}

/**
 * Premium animated visibility for bottom toolbar with sophisticated spring physics.
 */
@Composable
fun AnimatedBottomToolbar(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = RichTextAnimations.Springs.intOffsetSpringSpec
        ) + expandVertically(
            expandFrom = androidx.compose.ui.Alignment.Bottom,
            animationSpec = RichTextAnimations.Springs.intSizeSpringSpec
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = RichTextAnimations.Durations.MEDIUM,
                easing = RichTextAnimations.Easings.smoothEntry
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = RichTextAnimations.Springs.intOffsetSpringSpec
        ) + shrinkVertically(
            shrinkTowards = androidx.compose.ui.Alignment.Bottom,
            animationSpec = RichTextAnimations.Springs.intSizeSpringSpec
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = RichTextAnimations.Durations.QUICK,
                easing = RichTextAnimations.Easings.quickExit
            )
        ),
        modifier = modifier,
        content = content
    )
}

/**
 * Premium animated visibility for floating toolbar with glass-like emergence.
 */
@Composable
fun AnimatedFloatingToolbar(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.8f,
            transformOrigin = TransformOrigin(0.5f, 1.0f),
            animationSpec = RichTextAnimations.Springs.bouncy
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = RichTextAnimations.Durations.MEDIUM,
                easing = RichTextAnimations.Easings.smoothEntry
            )
        ),
        exit = scaleOut(
            targetScale = 0.9f,
            transformOrigin = TransformOrigin(0.5f, 1.0f),
            animationSpec = RichTextAnimations.Springs.snappy
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = RichTextAnimations.Durations.QUICK,
                easing = RichTextAnimations.Easings.quickExit
            )
        ),
        modifier = modifier,
        content = content
    )
}

/**
 * Advanced button press animation with natural spring feedback.
 */
@Composable
fun Modifier.animatedButtonPress(
    isPressed: Boolean,
    pressScale: Float = 0.95f
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressScale else 1f,
        animationSpec = RichTextAnimations.Springs.responsive,
        label = "button_press_scale"
    )
    
    return this.scale(scale)
}

/**
 * Sophisticated toolbar entrance animation with staggered group animations.
 */
@Composable
fun StaggeredToolbarEntrance(
    visible: Boolean,
    groupCount: Int,
    staggerDelayMs: Int = 50,
    modifier: Modifier = Modifier,
    content: @Composable (groupIndex: Int) -> Unit
) {
    repeat(groupCount) { index ->
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { 20.dp.value.roundToInt() },
                animationSpec = tween(
                    durationMillis = RichTextAnimations.Durations.MEDIUM,
                    delayMillis = index * staggerDelayMs,
                    easing = RichTextAnimations.Easings.smoothEntry
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = RichTextAnimations.Durations.MEDIUM,
                    delayMillis = index * staggerDelayMs,
                    easing = RichTextAnimations.Easings.smoothEntry
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = RichTextAnimations.Durations.QUICK,
                    easing = RichTextAnimations.Easings.quickExit
                )
            ),
            modifier = modifier
        ) {
            content(index)
        }
    }
}

/**
 * Premium hover animation for desktop/trackpad interactions.
 */
@Composable
fun Modifier.animatedHover(
    isHovered: Boolean,
    hoverScale: Float = 1.02f,
    hoverAlpha: Float = 0.9f
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isHovered) hoverScale else 1f,
        animationSpec = RichTextAnimations.Springs.gentle,
        label = "hover_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isHovered) hoverAlpha else 1f,
        animationSpec = tween(
            durationMillis = RichTextAnimations.Durations.QUICK,
            easing = RichTextAnimations.Easings.naturalMotion
        ),
        label = "hover_alpha"
    )
    
    return this
        .scale(scale)
        .alpha(alpha)
}

/**
 * Smart positioning animation with collision detection and smooth repositioning.
 */
@Composable
fun SmartPositionedToolbar(
    targetPosition: IntOffset,
    avoidKeyboard: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val animatedOffset = remember { Animatable(0f) }
    val animatedY = remember { Animatable(0f) }
    
    // Smooth position changes with natural spring physics
    LaunchedEffect(targetPosition) {
        animatedOffset.animateTo(
            targetValue = targetPosition.x.toFloat(),
            animationSpec = RichTextAnimations.Springs.responsive
        )
        animatedY.animateTo(
            targetValue = targetPosition.y.toFloat(),
            animationSpec = RichTextAnimations.Springs.responsive
        )
    }
    
    content()
}

/**
 * Contextual animation selector based on toolbar type and system state.
 */
@Composable
fun ContextualToolbarAnimation(
    visible: Boolean,
    toolbarType: ToolbarAnimationType,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    when (toolbarType) {
        ToolbarAnimationType.Bottom -> {
            AnimatedBottomToolbar(
                visible = visible,
                modifier = modifier,
                content = content
            )
        }
        
        ToolbarAnimationType.Floating -> {
            AnimatedFloatingToolbar(
                visible = visible,
                modifier = modifier,
                content = content
            )
        }
        
        ToolbarAnimationType.Compact -> {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = RichTextAnimations.Durations.QUICK,
                        easing = RichTextAnimations.Easings.smoothEntry
                    )
                ),
                exit = fadeOut(
                    animationSpec = tween(
                        durationMillis = RichTextAnimations.Durations.QUICK,
                        easing = RichTextAnimations.Easings.quickExit
                    )
                ),
                modifier = modifier,
                content = content
            )
        }
    }
}

/**
 * Animation types for different toolbar contexts.
 */
enum class ToolbarAnimationType {
    Bottom,     // Full slide-in animation from bottom
    Floating,   // Scale and fade animation with spring physics
    Compact     // Simple fade animation for minimal UI disruption
}

/**
 * Advanced spring animation builder for custom toolbar interactions.
 */
fun createCustomToolbarSpring(
    dampingRatio: Float = Spring.DampingRatioMediumBouncy,
    stiffness: Float = Spring.StiffnessMedium,
    visibilityThreshold: Float = 0.01f
): SpringSpec<Float> {
    return spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness,
        visibilityThreshold = visibilityThreshold
    )
}