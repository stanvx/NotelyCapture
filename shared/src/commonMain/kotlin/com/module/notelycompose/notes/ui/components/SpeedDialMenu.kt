package com.module.notelycompose.notes.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.module.notelycompose.core.constants.AppConstants
import com.module.notelycompose.notes.ui.theme.LocalCustomColors

data class FabMenuItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * Microsoft FluentUI-inspired speed dial menu component for secondary actions.
 * Follows Material 3 design principles with Microsoft design patterns and clean animations.
 */
@Composable
fun SpeedDialMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    fabActions: List<FabMenuItem>,
    modifier: Modifier = Modifier
) {
    // Material 3 motion specifications
    val expandDuration = AppConstants.Animation.MEDIUM_TRANSITION_DURATION.inWholeMilliseconds.toInt()
    val collapseDuration = AppConstants.Animation.SHORT_TRANSITION_DURATION.inWholeMilliseconds.toInt()
    val scrimFadeDuration = AppConstants.Animation.SCRIM_FADE_DURATION.inWholeMilliseconds.toInt()
    
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(durationMillis = expandDuration, easing = FastOutSlowInEasing),
        label = "fab_rotation"
    )

    Box(modifier = modifier) {
        // Scrim overlay when expanded - click to dismiss
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(durationMillis = scrimFadeDuration, easing = LinearEasing)),
            exit = fadeOut(animationSpec = tween(durationMillis = scrimFadeDuration, easing = LinearEasing))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggle
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            // Sub-FABs rendered from the list with staggered animation
            fabActions.forEachIndexed { index, action ->
                val delay = (fabActions.size - 1 - index) * AppConstants.Animation.FAB_STAGGER_DELAY.inWholeMilliseconds.toInt()
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = scaleIn(
                        animationSpec = tween(durationMillis = expandDuration, delayMillis = delay, easing = FastOutSlowInEasing),
                        initialScale = 0.3f
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = expandDuration, delayMillis = delay, easing = LinearEasing)
                    ),
                    exit = scaleOut(
                        animationSpec = tween(durationMillis = collapseDuration, easing = FastOutSlowInEasing),
                        targetScale = 0.3f
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = collapseDuration, easing = LinearEasing)
                    )
                ) {
                    SubFAB(
                        onClick = action.onClick,
                        icon = {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                tint = LocalCustomColors.current.floatActionButtonIconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = action.label
                    )
                }
            }

            // Main FAB using Material 3 FloatingActionButton with Microsoft-inspired elevation
            FloatingActionButton(
                onClick = onToggle,
                containerColor = LocalCustomColors.current.backgroundViewColor,
                contentColor = LocalCustomColors.current.floatActionButtonIconColor,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp,
                    focusedElevation = 10.dp,
                    hoveredElevation = 10.dp
                ),
                modifier = Modifier.semantics {
                    contentDescription = if (isExpanded) "Close speed dial" else "Open speed dial"
                    stateDescription = if (isExpanded) "Expanded" else "Collapsed"
                    customActions = fabActions.map { action ->
                        CustomAccessibilityAction(
                            label = action.label,
                            action = { 
                                action.onClick()
                                true
                            }
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null, // Handled by parent semantics
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}

@Composable
private fun SubFAB(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp), // Increased spacing for better separation
        modifier = modifier
    ) {
        // Label with Microsoft FluentUI text styling
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = LocalCustomColors.current.bodyContentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .clip(CircleShape)
                .background(LocalCustomColors.current.backgroundViewColor)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )

        // Sub-FAB Button - Material 3 SmallFloatingActionButton with enhanced elevation
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = LocalCustomColors.current.backgroundViewColor,
            contentColor = LocalCustomColors.current.floatActionButtonIconColor,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 10.dp,
                focusedElevation = 8.dp,
                hoveredElevation = 8.dp
            ),
            modifier = Modifier.semantics {
                contentDescription = "$label button. Double tap to activate."
                // Provide clear instructions for screen reader users
            }
        ) {
            icon()
        }
    }
}
