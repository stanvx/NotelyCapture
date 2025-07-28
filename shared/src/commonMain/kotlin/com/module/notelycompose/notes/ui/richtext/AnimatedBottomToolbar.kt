package com.module.notelycompose.notes.ui.richtext

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Animated bottom toolbar container with smooth show/hide animations.
 * 
 * Features:
 * - Smooth slide up/down animations
 * - Proper keyboard positioning
 * - Performance-optimized transitions
 * 
 * @param visible Whether the toolbar should be visible
 * @param modifier Modifier for the container
 * @param content The toolbar content to display
 */
@Composable
fun AnimatedBottomToolbar(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 200,
                delayMillis = 100
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(
                durationMillis = 250,
                easing = FastOutLinearInEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = 150
            )
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            content()
        }
    }
}

/**
 * Animated toolbar container with customizable positioning.
 * 
 * @param visible Whether the toolbar should be visible
 * @param alignment How to align the toolbar within its container
 * @param modifier Modifier for the container
 * @param content The toolbar content to display
 */
@Composable
fun AnimatedToolbarContainer(
    visible: Boolean,
    alignment: Alignment = Alignment.BottomCenter,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(
                durationMillis = 200,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 150
            )
        ),
        exit = scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(
                durationMillis = 150,
                easing = FastOutLinearInEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = 100
            )
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = alignment
        ) {
            content()
        }
    }
}