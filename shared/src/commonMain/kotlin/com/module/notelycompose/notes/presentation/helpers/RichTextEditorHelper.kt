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
 * Helper class for managing Rich Text Editor state and operations with performance optimizations.
 * 
 * Provides centralized management of rich text editing functionality,
 * including text formatting, content synchronization, and state persistence.
 * Includes performance optimizations like content caching and reduced state updates.
 */
class RichTextEditorHelper {
    
    private val _richTextState = MutableStateFlow(RichTextState())
    val richTextState: StateFlow<RichTextState> = _richTextState.asStateFlow()
    
    // Performance optimization: cache last content to avoid unnecessary updates
    private var lastSetContent: String = ""
    private var lastPlainTextContent: String = ""
    
    /**
     * Sets the content of the rich text editor with performance optimization.
     * 
     * @param content The HTML content to set
     */
    fun setContent(content: String) {
        // Performance optimization: only update if content actually changed
        if (content != lastSetContent) {
            lastSetContent = content
            _richTextState.value = RichTextState().apply {
                setHtml(content)
            }
            // Reset plain text cache when content changes
            lastPlainTextContent = ""
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
     * Gets the current content as plain text with caching for performance.
     * 
     * @return Plain text content
     */
    fun getPlainText(): String {
        val currentText = _richTextState.value.annotatedString.text
        // Performance optimization: cache plain text to avoid repeated conversion
        if (lastPlainTextContent != currentText) {
            lastPlainTextContent = currentText
        }
        return lastPlainTextContent
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
     * Toggles strikethrough formatting on selected text.
     */
    fun toggleStrikethrough() {
        _richTextState.value.toggleSpanStyle(
            SpanStyle(textDecoration = TextDecoration.LineThrough)
        )
    }
    
    /**
     * Toggles code block formatting on selected text.
     */
    fun toggleCodeBlock() {
        _richTextState.value.toggleSpanStyle(
            SpanStyle(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                background = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.3f)
            )
        )
    }
    
    /**
     * Toggles quote block formatting on selected text.
     */
    fun toggleQuoteBlock() {
        _richTextState.value.addParagraphStyle(
            ParagraphStyle(
                textIndent = androidx.compose.ui.text.style.TextIndent(firstLine = 16.sp),
                lineHeight = 1.5.sp
            )
        )
        _richTextState.value.toggleSpanStyle(
            SpanStyle(
                fontStyle = FontStyle.Italic,
                color = androidx.compose.ui.graphics.Color.Gray
            )
        )
    }
    
    /**
     * Checks if the current selection has strikethrough formatting.
     * 
     * @return True if the selection has strikethrough
     */
    fun isSelectionStrikethrough(): Boolean {
        return _richTextState.value.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true
    }
    
    /**
     * Checks if the current selection is in a code block.
     * 
     * @return True if the selection is in a code block
     */
    fun isCodeBlock(): Boolean {
        return _richTextState.value.currentSpanStyle.fontFamily == androidx.compose.ui.text.font.FontFamily.Monospace
    }
    
    /**
     * Checks if the current selection is in a quote block.
     * 
     * @return True if the selection is in a quote block
     */
    fun isQuoteBlock(): Boolean {
        return _richTextState.value.currentSpanStyle.fontStyle == FontStyle.Italic
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
     * Gets the current heading level based on font size.
     * 
     * @return Current heading level (1-3) or null if not a heading
     */
    fun getCurrentHeadingLevel(): Int? {
        val fontSize = _richTextState.value.currentSpanStyle.fontSize
        return when {
            fontSize == 28.sp -> 1
            fontSize == 24.sp -> 2
            fontSize == 20.sp -> 3
            else -> null
        }
    }
    
    /**
     * Sets the text to body/paragraph style (removes heading formatting).
     */
    fun setBodyText() {
        _richTextState.value.toggleSpanStyle(
            SpanStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        )
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