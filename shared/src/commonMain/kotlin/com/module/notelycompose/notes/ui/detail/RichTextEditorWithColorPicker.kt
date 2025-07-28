package com.module.notelycompose.notes.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.presentation.detail.RichTextFormattingState
import com.module.notelycompose.notes.ui.components.ColorPickerBottomSheet
import com.module.notelycompose.notes.ui.components.ColorPickerMode
import com.module.notelycompose.notes.ui.richtext.RichTextToolbarViewModel
import com.module.notelycompose.notes.ui.richtext.RichTextShortcutsOverlay

/**
 * Complete rich text editor integration with color picker bottom sheet.
 * 
 * This composable demonstrates how to integrate the ColorPickerBottomSheet
 * with the rich text toolbar system for a complete color selection experience.
 * 
 * Features:
 * - Rich text toolbar with color picker buttons
 * - Color picker bottom sheet with Material 3 design
 * - State management for text and highlight colors
 * - Proper integration with RichTextEditorHelper
 * 
 * @param toolbarViewModel The rich text toolbar view model
 * @param formattingState Current formatting state
 * @param onToggleBold Bold formatting callback
 * @param onToggleItalic Italic formatting callback
 * @param onToggleUnderline Underline formatting callback
 * @param onSetAlignment Text alignment callback
 * @param onToggleOrderedList Ordered list callback
 * @param onToggleUnorderedList Unordered list callback
 * @param onAddHeading Heading level callback
 * @param onSetBodyText Body text callback
 * @param onClearFormatting Clear formatting callback
 * @param modifier Modifier for the container
 */
@Composable
fun RichTextEditorWithColorPicker(
    toolbarViewModel: RichTextToolbarViewModel,
    formattingState: RichTextFormattingState,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onSetAlignment: (TextAlign) -> Unit,
    onToggleOrderedList: () -> Unit,
    onToggleUnorderedList: () -> Unit,
    onAddHeading: (Int) -> Unit,
    onSetBodyText: () -> Unit,
    onClearFormatting: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect color picker state from ViewModel
    val isColorPickerVisible by toolbarViewModel.isColorPickerVisible.collectAsState()
    val colorPickerMode by toolbarViewModel.colorPickerMode.collectAsState()
    val currentTextColor by toolbarViewModel.currentTextColor.collectAsState()
    val currentHighlightColor by toolbarViewModel.currentHighlightColor.collectAsState()
    
    // Collect keyboard shortcuts overlay state
    val isKeyboardShortcutsVisible by toolbarViewModel.isKeyboardShortcutsVisible.collectAsState()
    
    Box(modifier = modifier.fillMaxSize()) {
        // Rich text toolbar with integrated color picker buttons
        ScrollableRichTextToolbar(
            isVisible = true, // You would manage this based on your app's logic
            formattingState = formattingState,
            onToggleBold = onToggleBold,
            onToggleItalic = onToggleItalic,
            onToggleUnderline = onToggleUnderline,
            onSetAlignment = onSetAlignment,
            onToggleOrderedList = onToggleOrderedList,
            onToggleUnorderedList = onToggleUnorderedList,
            onAddHeading = onAddHeading,
            onSetBodyText = onSetBodyText,
            onClearFormatting = onClearFormatting,
            // Color picker integration
            onShowTextColorPicker = {
                toolbarViewModel.showTextColorPicker()
            },
            onShowHighlightColorPicker = {
                toolbarViewModel.showHighlightColorPicker()
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Color picker bottom sheet
        ColorPickerBottomSheet(
            isVisible = isColorPickerVisible,
            mode = colorPickerMode,
            selectedColor = when (colorPickerMode) {
                ColorPickerMode.TEXT_COLOR -> currentTextColor
                ColorPickerMode.HIGHLIGHT_COLOR -> currentHighlightColor
            },
            onColorSelected = { color ->
                when (colorPickerMode) {
                    ColorPickerMode.TEXT_COLOR -> {
                        toolbarViewModel.applyTextColor(color)
                    }
                    ColorPickerMode.HIGHLIGHT_COLOR -> {
                        toolbarViewModel.applyHighlightColor(color)
                    }
                }
            },
            onDismiss = {
                toolbarViewModel.hideColorPicker()
            }
        )
        
        // Keyboard shortcuts overlay
        RichTextShortcutsOverlay(
            isVisible = isKeyboardShortcutsVisible,
            onDismiss = {
                toolbarViewModel.hideKeyboardShortcuts()
            }
        )
    }
}

/**
 * Example usage of the RichTextEditorWithColorPicker in a screen.
 * 
 * This shows how you would integrate the color picker system in your app.
 */
@Composable
fun ExampleRichTextScreen(
    toolbarViewModel: RichTextToolbarViewModel,
    modifier: Modifier = Modifier
) {
    // Example formatting state - in real usage, this would come from your editor
    val formattingState by toolbarViewModel.formattingState.collectAsState()
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Your rich text editor would go here
        // For example: BasicTextField, RichTextEditor, etc.
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Rich text toolbar with color picker integration
        RichTextEditorWithColorPicker(
            toolbarViewModel = toolbarViewModel,
            formattingState = formattingState,
            onToggleBold = { toolbarViewModel.toggleBold() },
            onToggleItalic = { toolbarViewModel.toggleItalic() },
            onToggleUnderline = { toolbarViewModel.toggleUnderline() },
            onSetAlignment = { alignment -> toolbarViewModel.setAlignment(alignment) },
            onToggleOrderedList = { toolbarViewModel.toggleOrderedList() },
            onToggleUnorderedList = { toolbarViewModel.toggleUnorderedList() },
            onAddHeading = { level -> toolbarViewModel.addHeading(level) },
            onSetBodyText = { toolbarViewModel.setBodyText() },
            onClearFormatting = { toolbarViewModel.clearFormatting() }
        )
    }
}

/**
 * Preview demonstration of the color picker integration.
 * 
 * This preview shows the complete system in action with proper state management.
 */
/*
@Preview(showBackground = true)
@Composable
fun PreviewRichTextEditorWithColorPicker() {
    MyApplicationTheme {
        // Create a mock RichTextEditorHelper for preview
        val richTextEditorHelper = remember { RichTextEditorHelper() }
        val toolbarViewModel = remember { RichTextToolbarViewModel(richTextEditorHelper) }
        
        ExampleRichTextScreen(
            toolbarViewModel = toolbarViewModel,
            modifier = Modifier.padding(16.dp)
        )
    }
}
*/