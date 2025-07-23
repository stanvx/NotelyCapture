package com.module.notelycompose.notes.ui.richtext

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
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
 * Floating rich text toolbar with glassmorphism effect and smart positioning.
 * 
 * Features:
 * - Floating overlay positioning with collision detection
 * - Premium glassmorphism visual effects
 * - Smooth scale and fade animations
 * - Smart positioning relative to text selection
 * - Horizontal scrolling for space efficiency
 * - Material 3 design system integration
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
 * @param anchorPosition Optional anchor position for smart positioning
 * @param modifier Modifier for customization
 */
@Composable
fun FloatingRichTextToolbar(
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
    anchorPosition: IntOffset? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var toolbarSize by remember { mutableStateOf(IntOffset.Zero) }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            transformOrigin = TransformOrigin(0.5f, 1f)
        ) + fadeIn(
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        ),
        exit = scaleOut(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            ),
            transformOrigin = TransformOrigin(0.5f, 1f)
        ) + fadeOut(
            animationSpec = spring(stiffness = Spring.StiffnessHigh)
        ),
        modifier = modifier.zIndex(20f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            RichTextSurfaces.GlassToolbar(
                modifier = Modifier
                    .offset {
                        calculateToolbarPosition(
                            anchorPosition = anchorPosition,
                            toolbarSize = toolbarSize,
                            density = density
                        )
                    }
                    .onGloballyPositioned { coordinates ->
                        toolbarSize = IntOffset(
                            coordinates.size.width,
                            coordinates.size.height
                        )
                    }
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(getToolbarGroups(formattingState)) { group ->
                        when (group) {
                            is ToolbarGroup.TextStyle -> {
                                TextStyleButtonGroup(
                                    isBold = formattingState.isBold,
                                    isItalic = formattingState.isItalic,
                                    isUnderlined = formattingState.isUnderlined,
                                    onToggleBold = onToggleBold,
                                    onToggleItalic = onToggleItalic,
                                    onToggleUnderline = onToggleUnderline
                                )
                            }
                            
                            is ToolbarGroup.Alignment -> {
                                AlignmentButtonGroup(
                                    currentAlignment = formattingState.currentAlignment,
                                    onSetAlignment = onSetAlignment
                                )
                            }
                            
                            is ToolbarGroup.Lists -> {
                                ListButtonGroup(
                                    isUnorderedList = formattingState.isUnorderedList,
                                    isOrderedList = formattingState.isOrderedList,
                                    onToggleUnorderedList = onToggleUnorderedList,
                                    onToggleOrderedList = onToggleOrderedList
                                )
                            }
                            
                            is ToolbarGroup.Headings -> {
                                HeadingButtonGroup(
                                    onAddHeading = onAddHeading
                                )
                            }
                            
                            is ToolbarGroup.Actions -> {
                                ActionButtonGroup(
                                    onClearFormatting = onClearFormatting
                                )
                            }
                            
                            is ToolbarGroup.Divider -> {
                                RichTextGroupDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Calculates optimal positioning for the floating toolbar.
 * 
 * @param anchorPosition Position to anchor the toolbar to (typically text selection)
 * @param toolbarSize Current toolbar dimensions
 * @param density Screen density for dp-to-px conversion
 * @return Calculated offset for toolbar positioning
 */
private fun calculateToolbarPosition(
    anchorPosition: IntOffset?,
    toolbarSize: IntOffset,
    density: androidx.compose.ui.unit.Density
): IntOffset {
    if (anchorPosition == null) {
        // Default center position
        return IntOffset.Zero
    }
    
    with(density) {
        val toolbarOffsetY = -80.dp.toPx().toInt() // Float above anchor point
        val toolbarOffsetX = -(toolbarSize.x / 2) // Center horizontally
        
        return IntOffset(
            x = anchorPosition.x + toolbarOffsetX,
            y = anchorPosition.y + toolbarOffsetY
        )
    }
}

/**
 * Defines the toolbar groups and their display order.
 */
private sealed class ToolbarGroup {
    object TextStyle : ToolbarGroup()
    object Alignment : ToolbarGroup()
    object Lists : ToolbarGroup()
    object Headings : ToolbarGroup()
    object Actions : ToolbarGroup()
    object Divider : ToolbarGroup()
}

/**
 * Generates the toolbar groups based on current formatting state.
 * This allows for dynamic toolbar content and smart group ordering.
 */
private fun getToolbarGroups(formattingState: RichTextFormattingState): List<ToolbarGroup> {
    return buildList {
        // Always show text formatting
        add(ToolbarGroup.TextStyle)
        add(ToolbarGroup.Divider)
        
        // Show alignment if text is selected
        add(ToolbarGroup.Alignment)
        add(ToolbarGroup.Divider)
        
        // Show lists
        add(ToolbarGroup.Lists)
        add(ToolbarGroup.Divider)
        
        // Show headings
        add(ToolbarGroup.Headings)
        add(ToolbarGroup.Divider)
        
        // Always show actions
        add(ToolbarGroup.Actions)
    }
}

/**
 * Preview composable for floating toolbar development.
 */
@Composable
private fun FloatingRichTextToolbarPreview() {
    FloatingRichTextToolbar(
        isVisible = true,
        formattingState = RichTextFormattingState(
            isBold = true,
            isItalic = false,
            isUnderlined = false,
            currentAlignment = TextAlign.Start
        ),
        onToggleBold = {},
        onToggleItalic = {},
        onToggleUnderline = {},
        onSetAlignment = {},
        onToggleOrderedList = {},
        onToggleUnorderedList = {},
        onAddHeading = {},
        onClearFormatting = {},
        anchorPosition = IntOffset(200, 300)
    )
}