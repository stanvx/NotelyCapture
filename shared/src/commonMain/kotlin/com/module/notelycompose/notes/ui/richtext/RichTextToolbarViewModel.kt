package com.module.notelycompose.notes.ui.richtext

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.notes.presentation.detail.RichTextFormattingState
import com.module.notelycompose.notes.presentation.helpers.RichTextEditorHelper
import com.module.notelycompose.notes.ui.components.ColorPickerMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

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
    
    // Color picker state
    private val _isColorPickerVisible = MutableStateFlow(false)
    val isColorPickerVisible: StateFlow<Boolean> = _isColorPickerVisible.asStateFlow()
    
    private val _colorPickerMode = MutableStateFlow(ColorPickerMode.TEXT_COLOR)
    val colorPickerMode: StateFlow<ColorPickerMode> = _colorPickerMode.asStateFlow()
    
    private val _currentTextColor = MutableStateFlow<Color?>(null)
    val currentTextColor: StateFlow<Color?> = _currentTextColor.asStateFlow()
    
    private val _currentHighlightColor = MutableStateFlow<Color?>(null)
    val currentHighlightColor: StateFlow<Color?> = _currentHighlightColor.asStateFlow()
    
    // Keyboard shortcuts overlay state
    private val _isKeyboardShortcutsVisible = MutableStateFlow(false)
    val isKeyboardShortcutsVisible: StateFlow<Boolean> = _isKeyboardShortcutsVisible.asStateFlow()
    
    // Performance and UX state
    var isPerformingBulkOperation by mutableStateOf(false)
        private set
    
    // Performance optimization: debouncing for formatting state refresh
    private var refreshJob: Job? = null
    private companion object {
        const val REFRESH_DEBOUNCE_DELAY = 100L // 100ms debounce for state refresh
    }
    
    /**
     * Updates the formatting state by querying the current state from RichTextEditorHelper.
     * This should be called when the text selection changes or formatting is applied.
     * Uses debouncing to prevent excessive state refresh during rapid user interactions.
     */
    fun refreshFormattingState() {
        if (isPerformingBulkOperation) return
        
        // Cancel previous refresh job if still pending (performance optimization)
        refreshJob?.cancel()
        
        refreshJob = viewModelScope.launch {
            delay(REFRESH_DEBOUNCE_DELAY)
            
            // Update current colors from editor
            val textColor = richTextEditorHelper.getCurrentTextColor()
            val highlightColor = richTextEditorHelper.getCurrentHighlightColor()
            
            _currentTextColor.value = if (textColor != Color.Unspecified) textColor else null
            _currentHighlightColor.value = if (highlightColor != Color.Unspecified) highlightColor else null
            
            val newState = RichTextFormattingState(
                isBold = richTextEditorHelper.isSelectionBold(),
                isItalic = richTextEditorHelper.isSelectionItalic(),
                isUnderlined = richTextEditorHelper.isSelectionUnderlined(),
                isUnorderedList = richTextEditorHelper.isUnorderedList(),
                isOrderedList = richTextEditorHelper.isOrderedList(),
                currentAlignment = richTextEditorHelper.getCurrentAlignment(),
                currentHeadingLevel = richTextEditorHelper.getCurrentHeadingLevel(),
                hasTextColor = richTextEditorHelper.hasTextColor(),
                hasHighlight = richTextEditorHelper.hasHighlight(),
                indentLevel = richTextEditorHelper.getIndentLevel(),
                hasLink = richTextEditorHelper.hasLink(),
                isCodeBlock = richTextEditorHelper.isCodeBlock(),
                isQuoteBlock = richTextEditorHelper.isQuoteBlock()
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
    
    fun toggleStrikethrough() {
        richTextEditorHelper.toggleStrikethrough()
        refreshFormattingState()
    }
    
    fun toggleCodeBlock() {
        richTextEditorHelper.toggleCodeBlock()
        refreshFormattingState()
    }
    
    fun toggleQuoteBlock() {
        richTextEditorHelper.toggleQuoteBlock()
        refreshFormattingState()
    }
    
    fun setBodyText() {
        richTextEditorHelper.setBodyText()
        refreshFormattingState()
    }
    
    fun increaseIndent() {
        richTextEditorHelper.increaseIndent()
        refreshFormattingState()
    }
    
    fun decreaseIndent() {
        richTextEditorHelper.decreaseIndent()
        refreshFormattingState()
    }
    
    fun toggleLink() {
        richTextEditorHelper.toggleLink()
        refreshFormattingState()
    }
    
    fun insertDivider() {
        richTextEditorHelper.insertDivider()
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
     * Shows the color picker for text color selection.
     */
    fun showTextColorPicker() {
        _colorPickerMode.value = ColorPickerMode.TEXT_COLOR
        _isColorPickerVisible.value = true
    }
    
    /**
     * Shows the color picker for highlight color selection.
     */
    fun showHighlightColorPicker() {
        _colorPickerMode.value = ColorPickerMode.HIGHLIGHT_COLOR
        _isColorPickerVisible.value = true
    }
    
    /**
     * Hides the color picker.
     */
    fun hideColorPicker() {
        _isColorPickerVisible.value = false
    }
    
    /**
     * Applies the selected text color and refreshes formatting state.
     */
    fun applyTextColor(color: Color?) {
        if (color != null) {
            richTextEditorHelper.setTextColor(color)
        } else {
            richTextEditorHelper.removeTextColor()
        }
        _currentTextColor.value = color
        refreshFormattingState()
        hideColorPicker()
    }
    
    /**
     * Applies the selected highlight color and refreshes formatting state.
     */
    fun applyHighlightColor(color: Color?) {
        if (color != null) {
            richTextEditorHelper.setHighlightColor(color)
        } else {
            richTextEditorHelper.removeHighlightColor()
        }
        _currentHighlightColor.value = color
        refreshFormattingState()
        hideColorPicker()
    }
    
    /**
     * Shows the keyboard shortcuts overlay.
     */
    fun showKeyboardShortcuts() {
        _isKeyboardShortcutsVisible.value = true
    }
    
    /**
     * Hides the keyboard shortcuts overlay.
     */
    fun hideKeyboardShortcuts() {
        _isKeyboardShortcutsVisible.value = false
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
    
    /**
     * Cleanup method to cancel pending operations and prevent memory leaks.
     * Should be called when the ViewModel is being cleared.
     */
    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
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
           currentAlignment != TextAlign.Start || currentHeadingLevel != null ||
           hasTextColor || hasHighlight || indentLevel > 0 || hasLink ||
           isCodeBlock || isQuoteBlock
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
    currentHeadingLevel?.let { level ->
        activeFormats.add("H$level")
    }
    if (hasTextColor) activeFormats.add("TextColor")
    if (hasHighlight) activeFormats.add("Highlight")
    if (indentLevel > 0) activeFormats.add("Indent:$indentLevel")
    if (hasLink) activeFormats.add("Link")
    if (isCodeBlock) activeFormats.add("Code")
    if (isQuoteBlock) activeFormats.add("Quote")
    return if (activeFormats.isEmpty()) "NoFormatting" else activeFormats.joinToString(", ")
}