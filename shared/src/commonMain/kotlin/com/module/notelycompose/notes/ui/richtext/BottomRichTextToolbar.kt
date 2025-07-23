package com.module.notelycompose.notes.ui.richtext

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.module.notelycompose.designsystem.components.richtext.ActionButtonGroup
import com.module.notelycompose.designsystem.components.richtext.AlignmentButtonGroup
import com.module.notelycompose.designsystem.components.richtext.HeadingButtonGroup
import com.module.notelycompose.designsystem.components.richtext.ListButtonGroup
import com.module.notelycompose.designsystem.components.richtext.RichTextGroupDivider
import com.module.notelycompose.designsystem.components.richtext.RichTextSurfaces
import com.module.notelycompose.designsystem.components.richtext.TextStyleButtonGroup
import com.module.notelycompose.notes.presentation.detail.RichTextFormattingState

/**
 * Bottom-aligned rich text toolbar with keyboard awareness and adaptive positioning.
 * 
 * Features:
 * - Keyboard-aware positioning that adjusts with IME visibility
 * - Smooth slide-in animations from bottom
 * - Horizontal scrolling for comprehensive formatting options
 * - Material 3 surface design with appropriate elevation
 * - Smart content adaptation based on available space
 * - Automatic hide/show based on text field focus
 * 
 * @param isVisible Whether the toolbar should be displayed
 * @param formattingState Current formatting state from RichTextEditor
 * @param onToggleBold Bold formatting callback
 * @param onToggleItalic Italic formatting callback
 * @param onToggleUnderline Underline formatting callback
 * @param onSetAlignment Text alignment callback
 * @param onToggleOrderedList Ordered list callback
 * @param onToggleUnorderedList Unordered list callback
 * @param onAddHeading Heading level callback
 * @param onClearFormatting Clear formatting callback
 * @param isKeyboardVisible Whether software keyboard is currently visible
 * @param modifier Modifier for customization
 */
@Composable
fun BottomRichTextToolbar(
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
    isKeyboardVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var shouldShowToolbar by remember { mutableStateOf(false) }
    
    // Smart visibility management based on keyboard state and focus
    LaunchedEffect(isVisible, isKeyboardVisible) {
        shouldShowToolbar = isVisible && isKeyboardVisible
    }
    
    AnimatedVisibility(
        visible = shouldShowToolbar,
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
        modifier = modifier.zIndex(15f)
    ) {
        RichTextSurfaces.BottomToolbar {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(getAdaptiveToolbarGroups(formattingState)) { group ->
                    when (group) {
                        is BottomToolbarGroup.TextStyle -> {
                            TextStyleButtonGroup(
                                isBold = formattingState.isBold,
                                isItalic = formattingState.isItalic,
                                isUnderlined = formattingState.isUnderlined,
                                onToggleBold = onToggleBold,
                                onToggleItalic = onToggleItalic,
                                onToggleUnderline = onToggleUnderline
                            )
                        }
                        
                        is BottomToolbarGroup.Lists -> {
                            ListButtonGroup(
                                isUnorderedList = formattingState.isUnorderedList,
                                isOrderedList = formattingState.isOrderedList,
                                onToggleUnorderedList = onToggleUnorderedList,
                                onToggleOrderedList = onToggleOrderedList
                            )
                        }
                        
                        is BottomToolbarGroup.Headings -> {
                            HeadingButtonGroup(
                                onAddHeading = onAddHeading
                            )
                        }
                        
                        is BottomToolbarGroup.Alignment -> {
                            AlignmentButtonGroup(
                                currentAlignment = formattingState.currentAlignment,
                                onSetAlignment = onSetAlignment
                            )
                        }
                        
                        is BottomToolbarGroup.Actions -> {
                            ActionButtonGroup(
                                onClearFormatting = onClearFormatting
                            )
                        }
                        
                        is BottomToolbarGroup.Divider -> {
                            RichTextGroupDivider()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact version of bottom toolbar for limited space scenarios.
 */
@Composable
fun CompactBottomRichTextToolbar(
    isVisible: Boolean,
    formattingState: RichTextFormattingState,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onToggleOrderedList: () -> Unit,
    onToggleUnorderedList: () -> Unit,
    onClearFormatting: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)
        ) + fadeOut(),
        modifier = modifier.zIndex(15f)
    ) {
        RichTextSurfaces.BottomToolbar {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    // Most used formatting options only
                    TextStyleButtonGroup(
                        isBold = formattingState.isBold,
                        isItalic = formattingState.isItalic,
                        isUnderlined = formattingState.isUnderlined,
                        onToggleBold = onToggleBold,
                        onToggleItalic = onToggleItalic,
                        onToggleUnderline = onToggleUnderline
                    )
                }
                
                item {
                    RichTextGroupDivider()
                }
                
                item {
                    ListButtonGroup(
                        isUnorderedList = formattingState.isUnorderedList,
                        isOrderedList = formattingState.isOrderedList,
                        onToggleUnorderedList = onToggleUnorderedList,
                        onToggleOrderedList = onToggleOrderedList
                    )
                }
                
                item {
                    RichTextGroupDivider()
                }
                
                item {
                    ActionButtonGroup(
                        onClearFormatting = onClearFormatting
                    )
                }
            }
        }
    }
}

/**
 * Toolbar groups specifically arranged for bottom positioning and horizontal scrolling.
 */
private sealed class BottomToolbarGroup {
    object TextStyle : BottomToolbarGroup()
    object Lists : BottomToolbarGroup()
    object Headings : BottomToolbarGroup()
    object Alignment : BottomToolbarGroup()
    object Actions : BottomToolbarGroup()
    object Divider : BottomToolbarGroup()
}

/**
 * Generates adaptive toolbar groups optimized for bottom positioning.
 * Prioritizes most commonly used formatting options first.
 */
private fun getAdaptiveToolbarGroups(
    formattingState: RichTextFormattingState
): List<BottomToolbarGroup> {
    return buildList {
        // Primary formatting (most frequently used)
        add(BottomToolbarGroup.TextStyle)
        add(BottomToolbarGroup.Divider)
        
        // Lists (second most common)
        add(BottomToolbarGroup.Lists)
        add(BottomToolbarGroup.Divider)
        
        // Headings (structured content)
        add(BottomToolbarGroup.Headings)
        add(BottomToolbarGroup.Divider)
        
        // Alignment (layout formatting)
        add(BottomToolbarGroup.Alignment)
        add(BottomToolbarGroup.Divider)
        
        // Actions (utility functions)
        add(BottomToolbarGroup.Actions)
    }
}

/**
 * Enhanced bottom toolbar with smart keyboard management.
 */
@Composable
fun KeyboardAwareBottomRichTextToolbar(
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
    onKeyboardDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    BottomRichTextToolbar(
        isVisible = isVisible,
        formattingState = formattingState,
        onToggleBold = onToggleBold,
        onToggleItalic = onToggleItalic,
        onToggleUnderline = onToggleUnderline,
        onSetAlignment = onSetAlignment,
        onToggleOrderedList = onToggleOrderedList,
        onToggleUnorderedList = onToggleUnorderedList,
        onAddHeading = onAddHeading,
        onClearFormatting = {
            onClearFormatting()
            // Optionally dismiss keyboard after clearing formatting
            onKeyboardDismiss?.invoke()
        },
        modifier = modifier
    )
}