package com.module.notelycompose.notes.ui.detail

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.module.notelycompose.notes.presentation.detail.RichTextFormattingState
import com.module.notelycompose.notes.ui.theme.Material3ShapeTokens

/**
 * Modern floating rich text toolbar with Material 3 design.
 * 
 * Features:
 * - Floating design with glassmorphism effect
 * - Smooth entrance/exit animations
 * - Rich text formatting controls
 * - Selection-aware button states
 * - Haptic feedback
 * - Adaptive layout for different screen sizes
 */
@Composable
fun ModernRichTextToolbar(
    isVisible: Boolean,
    formattingState: RichTextFormattingState,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onSetAlignment: (TextAlign) -> Unit,
    onToggleOrderedList: () -> Unit,
    onToggleUnorderedList: () -> Unit,
    onAddHeading: (Int) -> Unit,
    onClearFormatting: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
        ) + fadeOut(
            animationSpec = tween(200, easing = FastOutLinearInEasing)
        ),
        modifier = modifier.zIndex(10f)
    ) {
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = Material3ShapeTokens.richTextToolbar,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                )
                .clip(Material3ShapeTokens.richTextToolbar),
            shape = Material3ShapeTokens.richTextToolbar,
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
            tonalElevation = 3.dp
        ) {
            // Glassmorphism overlay
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Primary formatting row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Text formatting buttons
                        FormattingButton(
                            icon = Icons.Rounded.Edit,
                            contentDescription = "Bold",
                            isSelected = formattingState.isBold,
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleBold()
                            }
                        )
                        
                        FormattingButton(
                            icon = Icons.Rounded.Edit,
                            contentDescription = "Italic",
                            isSelected = formattingState.isItalic,
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleItalic()
                            }
                        )
                        
                        FormattingButton(
                            icon = Icons.Rounded.Edit,
                            contentDescription = "Underline",
                            isSelected = formattingState.isUnderlined,
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleUnderline()
                            }
                        )
                        
                        // Divider
                        VerticalDivider()
                        
                        // Text alignment buttons
                        FormattingButton(
                            icon = Icons.Rounded.Menu,
                            contentDescription = "Align Left",
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSetAlignment(TextAlign.Left)
                            }
                        )
                        
                        FormattingButton(
                            icon = Icons.Rounded.Menu,
                            contentDescription = "Align Center",
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSetAlignment(TextAlign.Center)
                            }
                        )
                        
                        FormattingButton(
                            icon = Icons.Rounded.Menu,
                            contentDescription = "Align Right",
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSetAlignment(TextAlign.Right)
                            }
                        )
                    }
                    
                    // Secondary formatting row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // List formatting
                        FormattingButton(
                            icon = Icons.Rounded.List,
                            contentDescription = "Bullet List",
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleUnorderedList()
                            }
                        )
                        
                        FormattingButton(
                            icon = Icons.Rounded.List,
                            contentDescription = "Numbered List",
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleOrderedList()
                            }
                        )
                        
                        // Divider
                        VerticalDivider()
                        
                        // Heading buttons
                        HeadingButton(
                            level = 1,
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAddHeading(1)
                            }
                        )
                        
                        HeadingButton(
                            level = 2,
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAddHeading(2)
                            }
                        )
                        
                        HeadingButton(
                            level = 3,
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAddHeading(3)
                            }
                        )
                        
                        // Divider
                        VerticalDivider()
                        
                        // Clear formatting
                        FormattingButton(
                            icon = Icons.Rounded.Clear,
                            contentDescription = "Clear Formatting",
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onClearFormatting()
                            },
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual formatting button with modern styling.
 */
@Composable
private fun FormattingButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing)
    )
    
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            tint
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing)
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        contentColor = iconTint
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Heading button with level indicator.
 */
@Composable
private fun HeadingButton(
    level: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "H$level",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Vertical divider for separating button groups.
 */
@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
    )
}