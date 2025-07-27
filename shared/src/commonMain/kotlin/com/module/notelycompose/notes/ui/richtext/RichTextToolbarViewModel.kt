package com.module.notelycompose.notes.ui.richtext

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.notes.presentation.detail.RichTextFormattingState
import com.module.notelycompose.notes.presentation.helpers.RichTextEditorHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for rich text toolbar components providing centralized state management.
 * 
 * Features:
 * - Centralized formatting state management across multiple toolbar variants
 * - Integration with RichTextEditorHelper for consistent state synchronization
 * - Toolbar visibility and positioning logic
 * - Smart keyboard awareness and focus management
 * - Undo/redo history management
 * - Performance optimization with state consolidation
 * 
 * @param richTextEditorHelper The rich text editor helper for formatting operations
 */
class RichTextToolbarViewModel(
    private val richTextEditorHelper: RichTextEditorHelper
) : ViewModel() {

    // Toolbar visibility and positioning state
    private val _isToolbarVisible = MutableStateFlow(false)
    val isToolbarVisible: StateFlow<Boolean> = _isToolbarVisible.asStateFlow()
    
    private val _toolbarMode = MutableStateFlow(ToolbarMode.Bottom)
    val toolbarMode: StateFlow<ToolbarMode> = _toolbarMode.asStateFlow()
    
    // Text field focus and keyboard awareness
    private val _isTextFieldFocused = MutableStateFlow(false)
    val isTextFieldFocused: StateFlow<Boolean> = _isTextFieldFocused.asStateFlow()
    
    private val _isKeyboardVisible = MutableStateFlow(false)
    val isKeyboardVisible: StateFlow<Boolean> = _isKeyboardVisible.asStateFlow()
    
    // Formatting state derived from RichTextEditorHelper
    private val _formattingState = MutableStateFlow(RichTextFormattingState())
    val formattingState: StateFlow<RichTextFormattingState> = _formattingState.asStateFlow()
    
    // Advanced features
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()
    
    // Performance and UX state
    var isPerformingBulkOperation by mutableStateOf(false)
        private set
    
    /**
     * Updates the formatting state by querying the current state from RichTextEditorHelper.
     * This should be called when the text selection changes or formatting is applied.
     */
    fun refreshFormattingState() {
        if (isPerformingBulkOperation) return
        
        viewModelScope.launch {
            val newState = RichTextFormattingState(
                isBold = richTextEditorHelper.isSelectionBold(),
                isItalic = richTextEditorHelper.isSelectionItalic(),
                isUnderlined = richTextEditorHelper.isSelectionUnderlined(),
                isUnorderedList = richTextEditorHelper.isUnorderedList(),
                isOrderedList = richTextEditorHelper.isOrderedList(),
                currentAlignment = richTextEditorHelper.getCurrentAlignment()
            )
            _formattingState.value = newState
        }
    }
    
    /**
     * Sets the focus state of the text field and manages toolbar visibility.
     */
    fun setTextFieldFocused(focused: Boolean) {
        _isTextFieldFocused.value = focused
        updateToolbarVisibility()
    }
    
    /**
     * Sets the keyboard visibility state.
     */
    fun setKeyboardVisible(visible: Boolean) {
        _isKeyboardVisible.value = visible
        updateToolbarVisibility()
    }
    
    /**
     * Sets the toolbar display mode.
     */
    fun setToolbarMode(mode: ToolbarMode) {
        _toolbarMode.value = mode
    }
    
    /**
     * Updates toolbar visibility based on focus and keyboard state.
     */
    private fun updateToolbarVisibility() {
        val shouldShow = when (_toolbarMode.value) {
            ToolbarMode.Bottom -> true // Always show toolbar at bottom in note detail screen
            ToolbarMode.Floating -> _isTextFieldFocused.value
            ToolbarMode.Hidden -> false
        }
        _isToolbarVisible.value = shouldShow
    }
    
    // Formatting operations with state synchronization
    
    fun toggleBold() {
        richTextEditorHelper.toggleBold()
        refreshFormattingState()
    }
    
    fun toggleItalic() {
        richTextEditorHelper.toggleItalic()
        refreshFormattingState()
    }
    
    fun toggleUnderline() {
        richTextEditorHelper.toggleUnderline()
        refreshFormattingState()
    }
    
    fun setAlignment(alignment: TextAlign) {
        richTextEditorHelper.setAlignment(alignment)
        refreshFormattingState()
    }
    
    fun toggleUnorderedList() {
        richTextEditorHelper.toggleUnorderedList()
        refreshFormattingState()
    }
    
    fun toggleOrderedList() {
        richTextEditorHelper.toggleOrderedList()
        refreshFormattingState()
    }
    
    fun addHeading(level: Int) {
        richTextEditorHelper.addHeading(level)
        refreshFormattingState()
    }
    
    fun clearFormatting() {
        richTextEditorHelper.clearFormatting()
        refreshFormattingState()
    }
    
    /**
     * Performs multiple formatting operations efficiently with single state refresh.
     */
    fun performBulkFormatting(operations: () -> Unit) {
        isPerformingBulkOperation = true
        operations()
        isPerformingBulkOperation = false
        refreshFormattingState()
    }
    
    /**
     * Hides the toolbar manually (e.g., when user taps outside).
     */
    fun hideToolbar() {
        _isToolbarVisible.value = false
    }
    
    /**
     * Shows the toolbar manually with specified mode.
     */
    fun showToolbar(mode: ToolbarMode = _toolbarMode.value) {
        setToolbarMode(mode)
        _isToolbarVisible.value = true
    }
    
    /**
     * Toggles between floating and bottom toolbar modes.
     */
    fun toggleToolbarMode() {
        val newMode = when (_toolbarMode.value) {
            ToolbarMode.Bottom -> ToolbarMode.Floating
            ToolbarMode.Floating -> ToolbarMode.Bottom
            ToolbarMode.Hidden -> ToolbarMode.Bottom
        }
        setToolbarMode(newMode)
    }
}

/**
 * Toolbar display modes for different UI contexts.
 */
enum class ToolbarMode {
    /**
     * Bottom-aligned toolbar that appears above the keyboard.
     */
    Bottom,
    
    /**
     * Floating toolbar that appears near text selection.
     */
    Floating,
    
    /**
     * Hidden toolbar (formatting via other means).
     */
    Hidden
}

/**
 * Configuration for toolbar behavior and appearance.
 */
data class ToolbarConfig(
    val showAdvancedFormatting: Boolean = true,
    val enableHeadings: Boolean = true,
    val enableLists: Boolean = true,
    val enableAlignment: Boolean = true,
    val autoHideOnKeyboardDismiss: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val compactMode: Boolean = false
)

/**
 * Factory function for creating RichTextToolbarViewModel with dependencies.
 */
fun createRichTextToolbarViewModel(
    richTextEditorHelper: RichTextEditorHelper
): RichTextToolbarViewModel {
    return RichTextToolbarViewModel(richTextEditorHelper)
}

/**
 * Extension functions for easier ViewModel integration.
 */

/**
 * Creates a formatting state snapshot for immediate UI updates.
 */
fun RichTextToolbarViewModel.createFormattingSnapshot(): RichTextFormattingState {
    return formattingState.value.copy()
}

/**
 * Checks if any formatting is currently applied.
 */
fun RichTextFormattingState.hasAnyFormatting(): Boolean {
    return isBold || isItalic || isUnderlined || isUnorderedList || isOrderedList ||
           currentAlignment != TextAlign.Start
}

/**
 * Creates a compact representation of formatting state for debugging.
 */
fun RichTextFormattingState.toDebugString(): String {
    val activeFormats = mutableListOf<String>()
    if (isBold) activeFormats.add("Bold")
    if (isItalic) activeFormats.add("Italic") 
    if (isUnderlined) activeFormats.add("Underlined")
    if (isUnorderedList) activeFormats.add("BulletList")
    if (isOrderedList) activeFormats.add("NumberedList")
    if (currentAlignment != TextAlign.Start) {
        activeFormats.add("Align:${currentAlignment.toString()}")
    }
    return if (activeFormats.isEmpty()) "NoFormatting" else activeFormats.joinToString(", ")
}