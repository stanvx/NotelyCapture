package com.module.notelycompose.notes.presentation.helpers

import com.mohamedrejeb.richeditor.model.RichTextState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Helper class for managing Rich Text Editor state and operations.
 * 
 * Provides centralized management of rich text editing functionality,
 * including text formatting, content synchronization, and state persistence.
 */
class RichTextEditorHelper {
    
    private val _richTextState = MutableStateFlow(RichTextState())
    val richTextState: StateFlow<RichTextState> = _richTextState.asStateFlow()
    
    /**
     * Sets the content of the rich text editor.
     * 
     * @param content The HTML content to set
     */
    fun setContent(content: String) {
        _richTextState.value = RichTextState().apply {
            setHtml(content)
        }
    }
    
    /**
     * Gets the current content as HTML.
     * 
     * @return HTML content string
     */
    fun getContent(): String {
        return _richTextState.value.toHtml()
    }
    
    /**
     * Gets the current content as plain text.
     * 
     * @return Plain text content
     */
    fun getPlainText(): String {
        return _richTextState.value.annotatedString.text
    }
    
    /**
     * Applies bold formatting to selected text.
     */
    fun toggleBold() {
        _richTextState.value.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
    }
    
    /**
     * Applies italic formatting to selected text.
     */
    fun toggleItalic() {
        _richTextState.value.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
    }
    
    /**
     * Applies underline formatting to selected text.
     */
    fun toggleUnderline() {
        _richTextState.value.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
    }
    
    /**
     * Toggles unordered list formatting.
     */
    fun toggleUnorderedList() {
        _richTextState.value.toggleUnorderedList()
    }
    
    /**
     * Toggles ordered list formatting.
     */
    fun toggleOrderedList() {
        _richTextState.value.toggleOrderedList()
    }
    
    /**
     * Adds a heading of the specified level.
     * 
     * @param level The heading level (1-6)
     */
    fun addHeading(level: Int) {
        val fontSize = when (level) {
            1 -> 28.sp
            2 -> 24.sp
            3 -> 20.sp
            4 -> 18.sp
            5 -> 16.sp
            6 -> 14.sp
            else -> 16.sp
        }
        _richTextState.value.toggleSpanStyle(
            SpanStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        )
    }
    
    /**
     * Clears all formatting from selected text.
     */
    fun clearFormatting() {
        _richTextState.value.removeSpanStyle(SpanStyle())
    }
    
    /**
     * Checks if the current selection has bold formatting.
     * 
     * @return True if the selection is bold
     */
    fun isSelectionBold(): Boolean {
        return _richTextState.value.currentSpanStyle.fontWeight == FontWeight.Bold
    }
    
    /**
     * Checks if the current selection has italic formatting.
     * 
     * @return True if the selection is italic
     */
    fun isSelectionItalic(): Boolean {
        return _richTextState.value.currentSpanStyle.fontStyle == FontStyle.Italic
    }
    
    /**
     * Checks if the current selection has underline formatting.
     * 
     * @return True if the selection is underlined
     */
    fun isSelectionUnderlined(): Boolean {
        return _richTextState.value.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true
    }
    
    /**
     * Checks if the current paragraph is an unordered list.
     * 
     * @return True if the current paragraph is an unordered list
     */
    fun isUnorderedList(): Boolean {
        return _richTextState.value.isUnorderedList
    }
    
    /**
     * Checks if the current paragraph is an ordered list.
     * 
     * @return True if the current paragraph is an ordered list
     */
    fun isOrderedList(): Boolean {
        return _richTextState.value.isOrderedList
    }
    
    /**
     * Sets text alignment for the current paragraph.
     * 
     * @param textAlign The text alignment to apply
     */
    fun setAlignment(textAlign: TextAlign) {
        _richTextState.value.addParagraphStyle(ParagraphStyle(textAlign = textAlign))
    }
    
    /**
     * Gets the current text alignment.
     * 
     * @return Current text alignment
     */
    fun getCurrentAlignment(): TextAlign {
        return _richTextState.value.currentParagraphStyle.textAlign ?: TextAlign.Start
    }
    
    /**
     * Creates a new instance with fresh state.
     * 
     * @return New RichTextEditorHelper instance
     */
    fun createNew(): RichTextEditorHelper {
        return RichTextEditorHelper()
    }
}