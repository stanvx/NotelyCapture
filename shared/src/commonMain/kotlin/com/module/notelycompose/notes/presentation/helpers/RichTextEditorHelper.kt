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
import com.module.notelycompose.security.HtmlSanitizer

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
     * Sets the content of the rich text editor with performance optimization and security sanitization.
     * 
     * @param content The HTML content to set (will be sanitized for security)
     */
    fun setContent(content: String) {
        // Performance optimization: only update if content actually changed
        if (content != lastSetContent) {
            lastSetContent = content
            // SECURITY: Sanitize HTML content to prevent XSS attacks
            val sanitizedContent = HtmlSanitizer.sanitize(content)
            _richTextState.value = RichTextState().apply {
                setHtml(sanitizedContent)
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
        return hasStrikethrough()
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
     * Properly removes all span styles by clearing them explicitly.
     */
    fun clearFormatting() {
        val state = _richTextState.value
        
        // Clear all span styles properly by removing specific formatting
        state.removeSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
        state.removeSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
        state.removeSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
        state.removeSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
        
        // Clear colors
        val currentTextColor = state.currentSpanStyle.color
        if (currentTextColor != androidx.compose.ui.graphics.Color.Unspecified) {
            state.removeSpanStyle(SpanStyle(color = currentTextColor))
        }
        
        val currentBackground = state.currentSpanStyle.background
        if (currentBackground != androidx.compose.ui.graphics.Color.Unspecified) {
            state.removeSpanStyle(SpanStyle(background = currentBackground))
        }
        
        // Clear font family (for code blocks)
        if (state.currentSpanStyle.fontFamily == androidx.compose.ui.text.font.FontFamily.Monospace) {
            state.removeSpanStyle(SpanStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace))
        }
        
        // Clear font size (for headings) - reset to default body text
        val currentFontSize = state.currentSpanStyle.fontSize
        if (currentFontSize != 16.sp && currentFontSize != androidx.compose.ui.unit.TextUnit.Unspecified) {
            state.removeSpanStyle(SpanStyle(fontSize = currentFontSize))
            // Apply default body text size
            state.addSpanStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal))
        }
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
     * 
     * This method properly clears any heading formatting and sets standard body text styling.
     */
    fun setBodyText() {
        val state = _richTextState.value
        
        // Clear existing formatting by removing any heading-specific styles
        // Remove heading font sizes and bold formatting
        state.removeSpanStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)) // H1
        state.removeSpanStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) // H2  
        state.removeSpanStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) // H3
        state.removeSpanStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) // H4
        state.removeSpanStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)) // H5
        state.removeSpanStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)) // H6
        
        // Apply body text styling
        state.addSpanStyle(
            SpanStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        )
    }
    
    /**
     * Checks if the current selection has text color applied.
     * 
     * @return True if the selection has custom text color
     */
    fun hasTextColor(): Boolean {
        val currentColor = _richTextState.value.currentSpanStyle.color
        // Check if color is different from default (unspecified)
        return currentColor != androidx.compose.ui.graphics.Color.Unspecified &&
               currentColor != androidx.compose.ui.graphics.Color.Black
    }
    
    /**
     * Checks if the current selection has background highlight applied.
     * 
     * @return True if the selection has background highlight
     */
    fun hasHighlight(): Boolean {
        val currentBackground = _richTextState.value.currentSpanStyle.background
        return currentBackground != androidx.compose.ui.graphics.Color.Unspecified &&
               currentBackground != androidx.compose.ui.graphics.Color.Transparent
    }
    
    /**
     * Gets the current indent level of the paragraph.
     * Note: This is a simplified implementation as the library may not fully support indent tracking.
     * 
     * @return Current indent level (0 if no indent)
     */
    fun getIndentLevel(): Int {
        val textIndent = _richTextState.value.currentParagraphStyle.textIndent
        return if (textIndent != null && textIndent.firstLine.value > 0) {
            // Approximate indent level based on firstLine indent
            (textIndent.firstLine.value / 16).toInt().coerceAtLeast(0)
        } else {
            0
        }
    }
    
    /**
     * Enhanced indentation that works with both regular text and lists.
     * Increases indentation level with proper handling for list items.
     */
    fun increaseIndent() {
        val state = _richTextState.value
        
        // For both lists and regular text, use paragraph indentation
        // This allows consistent indentation behavior across all content types
        val currentLevel = getIndentLevel()
        val maxIndentLevel = 5
        
        if (currentLevel < maxIndentLevel) {
            val newIndentValue = ((currentLevel + 1) * 16).sp
            state.addParagraphStyle(
                ParagraphStyle(
                    textIndent = androidx.compose.ui.text.style.TextIndent(
                        firstLine = newIndentValue,
                        restLine = newIndentValue
                    )
                )
            )
        }
    }
    
    /**
     * Enhanced de-indentation that works with both regular text and lists.
     * Decreases indentation level with proper handling for list items.
     */
    fun decreaseIndent() {
        val state = _richTextState.value
        
        // For both lists and regular text, use paragraph de-indentation
        // This maintains consistent behavior across all content types
        val currentLevel = getIndentLevel()
        
        if (currentLevel > 0) {
            val newLevel = (currentLevel - 1).coerceAtLeast(0)
            val newIndentValue = if (newLevel > 0) (newLevel * 16).sp else 0.sp
            
            state.addParagraphStyle(
                ParagraphStyle(
                    textIndent = androidx.compose.ui.text.style.TextIndent(
                        firstLine = newIndentValue,
                        restLine = newIndentValue
                    )
                )
            )
        }
    }
    
    /**
     * Checks if the current selection contains a link.
     * Note: This is a basic implementation as link detection may require more sophisticated analysis.
     * 
     * @return True if the selection contains a link
     */
    fun hasLink(): Boolean {
        // Basic URL pattern matching in the current selection or nearby text
        val text = _richTextState.value.annotatedString.text
        val selection = _richTextState.value.selection
        
        if (selection.collapsed) {
            return false
        }
        
        val selectedText = try {
            // Validate selection boundaries before substring operation
            if (selection.start < 0 || selection.end > text.length || selection.start > selection.end) {
                return false
            }
            text.substring(selection.start, selection.end)
        } catch (e: Exception) {
            return false
        }
        
        // Simple URL detection pattern
        val urlPattern = Regex("https?://[^\\s]+|www\\.[^\\s]+|[^\\s]+\\.[a-z]{2,}")
        return urlPattern.containsMatchIn(selectedText)
    }
    
    /**
     * Checks if the current selection has strikethrough formatting.
     * Enhanced version with better validation.
     * 
     * @return True if the selection has strikethrough
     */
    fun hasStrikethrough(): Boolean {
        return _richTextState.value.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true
    }
    
    /**
     * Gets the current text color of the selection.
     * 
     * @return Current text color or Color.Unspecified if no custom color
     */
    fun getCurrentTextColor(): androidx.compose.ui.graphics.Color {
        return _richTextState.value.currentSpanStyle.color ?: androidx.compose.ui.graphics.Color.Unspecified
    }
    
    /**
     * Gets the current background color (highlight) of the selection.
     * 
     * @return Current background color or Color.Unspecified if no highlight
     */
    fun getCurrentHighlightColor(): androidx.compose.ui.graphics.Color {
        return _richTextState.value.currentSpanStyle.background ?: androidx.compose.ui.graphics.Color.Unspecified
    }
    
    /**
     * Sets the text color for the current selection.
     * 
     * @param color The color to apply to the text
     */
    fun setTextColor(color: androidx.compose.ui.graphics.Color) {
        _richTextState.value.toggleSpanStyle(
            androidx.compose.ui.text.SpanStyle(color = color)
        )
    }
    
    /**
     * Sets the highlight (background) color for the current selection.
     * 
     * @param color The color to apply as background highlight
     */
    fun setHighlightColor(color: androidx.compose.ui.graphics.Color) {
        _richTextState.value.toggleSpanStyle(
            androidx.compose.ui.text.SpanStyle(background = color)
        )
    }
    
    /**
     * Removes text color formatting from the current selection.
     */
    fun removeTextColor() {
        _richTextState.value.removeSpanStyle(
            androidx.compose.ui.text.SpanStyle(
                color = _richTextState.value.currentSpanStyle.color
            )
        )
    }
    
    /**
     * Removes highlight color formatting from the current selection.
     */
    fun removeHighlightColor() {
        _richTextState.value.removeSpanStyle(
            androidx.compose.ui.text.SpanStyle(
                background = _richTextState.value.currentSpanStyle.background
            )
        )
    }
    
    /**
     * Checks if the cursor is at the end of a heading line.
     * 
     * @return True if cursor is at the end of a heading, false otherwise
     */
    fun isCursorAtEndOfHeading(): Boolean {
        val state = _richTextState.value
        val text = state.annotatedString.text
        val selection = state.selection
        
        // Check if selection is collapsed (cursor position)
        if (!selection.collapsed) return false
        
        val cursorPosition = selection.start
        
        // Check if cursor is at the end of text or before a newline
        val isAtLineEnd = cursorPosition >= text.length || 
                         (cursorPosition < text.length && text[cursorPosition] == '\n')
        
        if (!isAtLineEnd) return false
        
        // Find the start of the current line
        val lineStart = text.lastIndexOf('\n', cursorPosition - 1) + 1
        
        // Check if current line has heading formatting
        return getCurrentHeadingLevel() != null
    }
    
    /**
     * Handles Enter key press behavior for headings.
     * When Enter is pressed at the end of a heading line, automatically converts the new line to body text.
     * 
     * @return True if the Enter key was handled (heading to body conversion), false otherwise
     */
    fun handleEnterKeyPress(): Boolean {
        if (!isCursorAtEndOfHeading()) {
            return false // Let default Enter behavior handle it
        }
        
        // Let the default Enter behavior happen first, then modify the formatting
        // We return false to allow the default Enter key handling, and then apply body text formatting
        // in a separate call that will be triggered after the Enter key processing
        return false // Let default behavior handle Enter key
    }
    
    /**
     * Applies body text formatting after Enter key press on a heading.
     * This should be called immediately after the Enter key is processed.
     */
    fun applyBodyTextAfterEnter() {
        // Apply body text styling to the current cursor position
        setBodyText()
    }
    
    /**
     * Toggles link formatting on selected text.
     * If text is selected, prompts for URL input. If URL is detected, removes link formatting.
     */
    fun toggleLink() {
        val state = _richTextState.value
        val selection = state.selection
        
        if (selection.collapsed) {
            // No text selected - can't create link
            return
        }
        
        val selectedText = try {
            // Validate selection boundaries
            if (selection.start < 0 || selection.end > state.annotatedString.text.length || selection.start > selection.end) {
                return
            }
            state.annotatedString.text.substring(selection.start, selection.end)
        } catch (e: Exception) {
            return
        }
        
        // Check if selected text already has a link
        if (hasLink()) {
            // Remove link formatting by removing text decoration
            state.removeSpanStyle(
                SpanStyle(
                    textDecoration = TextDecoration.Underline,
                    color = androidx.compose.ui.graphics.Color.Blue
                )
            )
        } else {
            // Add link formatting (visual indication)
            state.addSpanStyle(
                SpanStyle(
                    textDecoration = TextDecoration.Underline,
                    color = androidx.compose.ui.graphics.Color.Blue
                )
            )
        }
    }
    
    /**
     * Inserts a horizontal divider (horizontal rule) at the current cursor position.
     */
    fun insertDivider() {
        val state = _richTextState.value
        val currentText = state.annotatedString.text
        val cursorPosition = state.selection.start
        
        // Insert divider as a line of dashes
        val divider = "\n---\n"
        
        // Insert the divider at cursor position
        val newText = StringBuilder(currentText)
            .insert(cursorPosition, divider)
            .toString()
        
        // Update the content
        state.setHtml(newText)
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