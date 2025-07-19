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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.note_list_add_note
import com.module.notelycompose.resources.note_list_quick_record
import com.module.notelycompose.resources.vectors.IcRecorder
import com.module.notelycompose.resources.vectors.Images
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// Data class to represent each speed dial action
private data class FabAction(
    val labelRes: StringResource,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun SpeedDialFAB(
    onNewNoteClick: () -> Unit,
    onQuickRecordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Define actions in a list for maintainability
    val fabActions = remember {
        listOf(
            FabAction(Res.string.note_list_quick_record, Images.Icons.IcRecorder, onQuickRecordClick),
            FabAction(Res.string.note_list_add_note, Icons.Default.Add, onNewNoteClick)
        )
    }

    // Material 3 motion specifications
    val expandDuration = 300 // Based on M3 guidelines for medium transitions
    val collapseDuration = 150 // Based on M3 guidelines for short transitions
    val scrimFadeDuration = 150 // Based on M3 FAB fade guidelines
    
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(durationMillis = expandDuration, easing = FastOutSlowInEasing),
        label = "fab_rotation"
    )

    Box(modifier = modifier) {
        // Scrim overlay when expanded
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
                        onClick = { isExpanded = false }
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
                val delay = (fabActions.size - 1 - index) * 50 // Staggered delay
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
                        onClick = {
                            isExpanded = false
                            action.onClick()
                        },
                        icon = {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                tint = LocalCustomColors.current.floatActionButtonIconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = stringResource(action.labelRes)
                    )
                }
            }

            // Main FAB using Material 3 FloatingActionButton
            FloatingActionButton(
                onClick = { isExpanded = !isExpanded },
                containerColor = LocalCustomColors.current.backgroundViewColor,
                contentColor = LocalCustomColors.current.floatActionButtonIconColor,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.semantics {
                    contentDescription = if (isExpanded) "Close speed dial" else "Open speed dial"
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
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = LocalCustomColors.current.bodyContentColor,
            modifier = Modifier
                .clip(CircleShape)
                .background(LocalCustomColors.current.backgroundViewColor)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
        
        // Sub-FAB Button - Material 3 SmallFloatingActionButton
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = LocalCustomColors.current.backgroundViewColor,
            contentColor = LocalCustomColors.current.floatActionButtonIconColor,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            modifier = Modifier.semantics {
                this.contentDescription = label
            }
        ) {
            icon()
        }
    }
}