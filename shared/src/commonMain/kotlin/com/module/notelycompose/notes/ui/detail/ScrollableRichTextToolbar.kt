package com.module.notelycompose.notes.ui.detail

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.module.notelycompose.notes.presentation.detail.RichTextFormattingState
import com.module.notelycompose.notes.ui.theme.Material3ShapeTokens

/**
 * Scrollable rich text toolbar designed for bottom alignment above the keyboard.
 * Inspired by modern messaging apps with horizontal scrolling for better space utilization.
 * 
 * Features:
 * - Horizontal scrollable design
 * - Grouped formatting options
 * - Keyboard-aware positioning
 * - Material 3 design with glassmorphism effect
 * - Smooth show/hide animations
 * - Haptic feedback
 */
@Composable
fun ScrollableRichTextToolbar(
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
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
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
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                ),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(2.dp)
                        )
                        .align(Alignment.CenterHorizontally)
                )

                // Scrollable formatting options
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    // Text formatting group
                    item {
                        FormattingGroup(title = "Format") {
                            FormattingButton(
                                icon = Icons.Filled.Edit,
                                contentDescription = "Bold",
                                isSelected = formattingState.isBold,
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleBold()
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.Edit,
                                contentDescription = "Italic",
                                isSelected = formattingState.isItalic,
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleItalic()
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.Edit,
                                contentDescription = "Underline",
                                isSelected = formattingState.isUnderlined,
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleUnderline()
                                }
                            )
                        }
                    }

                    item { GroupDivider() }

                    // Alignment group
                    item {
                        FormattingGroup(title = "Align") {
                            FormattingButton(
                                icon = Icons.Filled.Menu,
                                contentDescription = "Align Left",
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSetAlignment(TextAlign.Start)
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.Menu,
                                contentDescription = "Align Center",
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSetAlignment(TextAlign.Center)
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.Menu,
                                contentDescription = "Align Right",
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSetAlignment(TextAlign.End)
                                }
                            )
                        }
                    }

                    item { GroupDivider() }

                    // Lists group
                    item {
                        FormattingGroup(title = "Lists") {
                            FormattingButton(
                                icon = Icons.Filled.List,
                                contentDescription = "Bullet List",
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleUnorderedList()
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.List,
                                contentDescription = "Numbered List",
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleOrderedList()
                                }
                            )
                        }
                    }

                    item { GroupDivider() }

                    // Headings group
                    item {
                        FormattingGroup(title = "Headings") {
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
                        }
                    }

                    item { GroupDivider() }

                    // Actions group
                    item {
                        FormattingGroup(title = "Actions") {
                            FormattingButton(
                                icon = Icons.Filled.Clear,
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
}

/**
 * Formatting group container with title
 */
@Composable
private fun FormattingGroup(
    title: String,
    content: @Composable RowScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
    }
}

/**
 * Individual formatting button with modern styling
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
        modifier = modifier.size(36.dp),
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
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Heading button with level indicator
 */
@Composable
private fun HeadingButton(
    level: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "H$level",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Visual divider between formatting groups
 */
@Composable
private fun GroupDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(48.dp)
            .background(
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
    )
}