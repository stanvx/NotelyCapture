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
import com.module.notelycompose.notes.ui.richtext.AnimatedBottomToolbar
import com.module.notelycompose.notes.ui.richtext.rememberRichTextHapticManager
import com.module.notelycompose.notes.ui.richtext.RichTextHaptics.boldToggled
import com.module.notelycompose.notes.ui.richtext.RichTextHaptics.italicToggled
import com.module.notelycompose.notes.ui.richtext.RichTextHaptics.underlineToggled
import com.module.notelycompose.notes.ui.richtext.RichTextHaptics.listToggled
import com.module.notelycompose.notes.ui.richtext.RichTextHaptics.alignmentChanged
import com.module.notelycompose.notes.ui.richtext.RichTextHaptics.headingApplied
import com.module.notelycompose.notes.ui.richtext.RichTextHaptics.formattingCleared
import com.module.notelycompose.notes.ui.richtext.AccessibleToolbarContainer
import com.module.notelycompose.notes.ui.richtext.AccessibleRichTextButton
import com.module.notelycompose.notes.ui.richtext.RichTextAccessibilityManager
import com.module.notelycompose.notes.ui.richtext.AccessibilityAction
import com.module.notelycompose.notes.ui.richtext.RichTextButtonType
import com.module.notelycompose.notes.ui.richtext.rememberRichTextFocusManager

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
    onToggleTextColor: () -> Unit = {},
    onToggleHighlight: () -> Unit = {},
    onIncreaseIndent: () -> Unit = {},
    onDecreaseIndent: () -> Unit = {},
    onToggleCodeBlock: () -> Unit = {},
    onToggleQuoteBlock: () -> Unit = {},
    onInsertDivider: () -> Unit = {},
    onToggleLink: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val accessibilityManager = remember { RichTextAccessibilityManager() }
    val focusManager = rememberRichTextFocusManager()
    
    // Convert RichTextFormattingState to RichTextState for accessibility
    val mockRichTextState = com.mohamedrejeb.richeditor.model.rememberRichTextState()
    
    val handleAccessibilityAction = { action: AccessibilityAction ->
        when (action) {
            AccessibilityAction.ToggleBold -> onToggleBold()
            AccessibilityAction.ToggleItalic -> onToggleItalic()
            AccessibilityAction.ToggleUnderline -> onToggleUnderline()
            AccessibilityAction.ToggleUnorderedList -> onToggleUnorderedList()
            AccessibilityAction.ToggleOrderedList -> onToggleOrderedList()
            AccessibilityAction.AlignLeft -> onSetAlignment(TextAlign.Start)
            AccessibilityAction.AlignCenter -> onSetAlignment(TextAlign.Center)
            AccessibilityAction.AlignRight -> onSetAlignment(TextAlign.End)
            is AccessibilityAction.ApplyHeading -> onAddHeading(action.level)
            AccessibilityAction.ClearFormatting -> onClearFormatting()
            else -> { /* Handle other actions */ }
        }
    }
    
    AnimatedBottomToolbar(
        visible = isVisible,
        modifier = modifier.zIndex(10f)
    ) {
        AccessibleToolbarContainer(
            isVisible = isVisible,
            formattingState = mockRichTextState,
            onKeyboardShortcut = handleAccessibilityAction,
            accessibilityManager = accessibilityManager
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
                    // Group 1: Headings (most structural)
                    item {
                        FormattingGroup(title = "Headings") {
                            HeadingButton(
                                level = 1,
                                isSelected = formattingState.currentHeadingLevel == 1,
                                onClick = {
                                    onAddHeading(1)
                                    hapticFeedback.headingApplied(1)
                                }
                            )
                            
                            HeadingButton(
                                level = 2,
                                isSelected = formattingState.currentHeadingLevel == 2,
                                onClick = {
                                    onAddHeading(2)
                                    hapticFeedback.headingApplied(2)
                                }
                            )
                            
                            HeadingButton(
                                level = 3,
                                isSelected = formattingState.currentHeadingLevel == 3,
                                onClick = {
                                    onAddHeading(3)
                                    hapticFeedback.headingApplied(3)
                                }
                            )
                            
                            HeadingButton(
                                level = 4,
                                isSelected = formattingState.currentHeadingLevel == 4,
                                onClick = {
                                    onAddHeading(4)
                                    hapticFeedback.headingApplied(4)
                                }
                            )
                            
                            HeadingButton(
                                level = 5,
                                isSelected = formattingState.currentHeadingLevel == 5,
                                onClick = {
                                    onAddHeading(5)
                                    hapticFeedback.headingApplied(5)
                                }
                            )
                            
                            HeadingButton(
                                level = 6,
                                isSelected = formattingState.currentHeadingLevel == 6,
                                onClick = {
                                    onAddHeading(6)
                                    hapticFeedback.headingApplied(6)
                                }
                            )
                        }
                    }

                    item { GroupDivider() }

                    // Group 2: Basic Format
                    item {
                        FormattingGroup(title = "Format") {
                            FormattingButton(
                                icon = Icons.Filled.FormatBold,
                                contentDescription = "Bold",
                                isSelected = formattingState.isBold,
                                onClick = {
                                    onToggleBold()
                                    hapticFeedback.boldToggled(!formattingState.isBold)
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.FormatItalic,
                                contentDescription = "Italic",
                                isSelected = formattingState.isItalic,
                                onClick = {
                                    onToggleItalic()
                                    hapticFeedback.italicToggled(!formattingState.isItalic)
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.FormatUnderlined,
                                contentDescription = "Underline",
                                isSelected = formattingState.isUnderlined,
                                onClick = {
                                    onToggleUnderline()
                                    hapticFeedback.underlineToggled(!formattingState.isUnderlined)
                                }
                            )
                        }
                    }

                    item { GroupDivider() }

                    // Group 3: Highlight
                    item {
                        FormattingGroup(title = "Highlight") {
                            FormattingButton(
                                icon = Icons.Filled.FormatColorText,
                                contentDescription = "Text Color",
                                isSelected = formattingState.hasTextColor,
                                onClick = {
                                    onToggleTextColor()
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.FormatColorFill,
                                contentDescription = "Highlight",
                                isSelected = formattingState.hasHighlight,
                                onClick = {
                                    onToggleHighlight()
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            )
                        }
                    }

                    item { GroupDivider() }

                    // Group 4: Lists
                    item {
                        FormattingGroup(title = "Lists") {
                            FormattingButton(
                                icon = Icons.Filled.FormatListBulleted,
                                contentDescription = "Bullet List",
                                isSelected = formattingState.isUnorderedList,
                                onClick = {
                                    onToggleUnorderedList()
                                    hapticFeedback.listToggled(!formattingState.isUnorderedList)
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.FormatListNumbered,
                                contentDescription = "Numbered List",
                                isSelected = formattingState.isOrderedList,
                                onClick = {
                                    onToggleOrderedList()
                                    hapticFeedback.listToggled(!formattingState.isOrderedList)
                                }
                            )
                        }
                    }

                    item { GroupDivider() }

                    // Group 5: Indent/Outdent
                    item {
                        FormattingGroup(title = "Indent") {
                            FormattingButton(
                                icon = Icons.Filled.FormatIndentDecrease,
                                contentDescription = "Decrease Indent",
                                onClick = {
                                    onDecreaseIndent()
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                                enabled = formattingState.indentLevel > 0
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.FormatIndentIncrease,
                                contentDescription = "Increase Indent",
                                onClick = {
                                    onIncreaseIndent()
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                                enabled = formattingState.indentLevel < 5
                            )
                        }
                    }

                    item { GroupDivider() }

                    // Group 6: Code/Special
                    item {
                        FormattingGroup(title = "Special") {
                            CodeBlockButton(
                                isSelected = formattingState.isCodeBlock,
                                onClick = {
                                    onToggleCodeBlock()
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.FormatQuote,
                                contentDescription = "Quote Block",
                                isSelected = formattingState.isQuoteBlock,
                                onClick = {
                                    onToggleQuoteBlock()
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.Link,
                                contentDescription = "Link",
                                isSelected = formattingState.hasLink,
                                onClick = {
                                    onToggleLink()
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            )
                        }
                    }

                    item { GroupDivider() }

                    // Group 7: Alignment
                    item {
                        FormattingGroup(title = "Align") {
                            FormattingButton(
                                icon = Icons.Filled.FormatAlignLeft,
                                contentDescription = "Align Left",
                                isSelected = formattingState.currentAlignment == TextAlign.Start,
                                onClick = {
                                    onSetAlignment(TextAlign.Start)
                                    hapticFeedback.alignmentChanged()
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.FormatAlignCenter,
                                contentDescription = "Align Center",
                                isSelected = formattingState.currentAlignment == TextAlign.Center,
                                onClick = {
                                    onSetAlignment(TextAlign.Center)
                                    hapticFeedback.alignmentChanged()
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.FormatAlignRight,
                                contentDescription = "Align Right",
                                isSelected = formattingState.currentAlignment == TextAlign.End,
                                onClick = {
                                    onSetAlignment(TextAlign.End)
                                    hapticFeedback.alignmentChanged()
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.FormatAlignJustify,
                                contentDescription = "Justify",
                                isSelected = formattingState.currentAlignment == TextAlign.Justify,
                                onClick = {
                                    onSetAlignment(TextAlign.Justify)
                                    hapticFeedback.alignmentChanged()
                                }
                            )
                        }
                    }

                    item { GroupDivider() }

                    // Group 8: Actions
                    item {
                        FormattingGroup(title = "Actions") {
                            FormattingButton(
                                icon = Icons.Filled.HorizontalRule,
                                contentDescription = "Insert Divider",
                                onClick = {
                                    onInsertDivider()
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            )
                            
                            FormattingButton(
                                icon = Icons.Filled.Clear,
                                contentDescription = "Clear Formatting",
                                onClick = {
                                    onClearFormatting()
                                    hapticFeedback.formattingCleared()
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
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> Color.Transparent
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing)
    )
    
    val iconTint by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> tint
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing)
    )
    
    Surface(
        onClick = { if (enabled) onClick() },
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
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing)
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing)
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier.size(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "H$level",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Code block button with special styling
 */
@Composable
private fun CodeBlockButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing)
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing)
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier.size(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "</>",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
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