package com.module.notelycompose.notes.presentation.helpers

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.module.notelycompose.notes.presentation.detail.model.EditorPresentationState
import com.module.notelycompose.notes.presentation.detail.model.TextFormatPresentationOption
import com.module.notelycompose.notes.presentation.detail.model.TextPresentationFormat
import com.module.notelycompose.notes.presentation.detail.model.TextPresentationFormats
import com.module.notelycompose.notes.presentation.helpers.TextFormatHelper.updateFormats
import com.module.notelycompose.notes.domain.TextContentPredictor
import com.module.notelycompose.notes.domain.TextCompletion
import com.module.notelycompose.notes.domain.FormattingSuggestion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TextEditorHelper(
    private val contentPredictor: TextContentPredictor? = null
) {
    
    // Performance optimization: cache for reduced allocations
    private var lastProcessedLength = 0
    private var formatUpdateCache = mutableListOf<TextPresentationFormat>()
    
    // Content prediction state
    private var lastPredictionText = ""
    private var lastPredictionCursor = -1

    fun updateContent(
        newContent: TextFieldValue,
        currentState: EditorPresentationState,
        getFormattedDate: () -> String,
        updateState: (EditorPresentationState) -> Unit
    ) {
        try {
            val oldText = currentState.content.text
            val newText = newContent.text
            val selection = newContent.selection

            // Performance optimization: only update formats if text length changed significantly
            val updatedFormats = if (shouldUpdateFormats(oldText.length, newText.length)) {
                currentState.formats.updateFormats(oldText, newText, selection.start)
            } else {
                currentState.formats
            }

            // Handle Enter key press and bullet points
            if (newText.length > oldText.length && selection.start > 0 &&
                selection.start <= newText.length &&
                newText[selection.start - 1] == '\n'
            ) {
                val bulletResult = handleBulletListContinuation(newText, selection, updatedFormats, currentState)
                if (bulletResult != null) {
                    updateState(
                        bulletResult.copy(
                            selectionSize = getSizeLabel(newContent, updatedFormats)
                        )
                    )
                    return
                }
            }

            updateState(
                currentState.copy(
                    content = newContent,
                    formats = updatedFormats,
                    selectionSize = getSizeLabel(newContent, updatedFormats),
                    createdAt = getFormattedDate()
                )
            )
        } catch (e: Exception) {
            updateState(
                currentState.copy(
                    content = newContent,
                    selectionSize = getSizeLabel(newContent, currentState.formats),
                    createdAt = getFormattedDate()
                )
            )
        }
    }

    private fun handleBulletListContinuation(
        newText: String,
        selection: TextRange,
        updatedFormats: List<TextPresentationFormat>,
        currentState: EditorPresentationState
    ): EditorPresentationState? {
        val previousLineEnd = (selection.start - 1).coerceIn(0, newText.length)
        val textBeforeCursor = newText.substring(0, previousLineEnd)
        val lastNewLineIndex = textBeforeCursor.lastIndexOf('\n')
        val previousLineStart = if (lastNewLineIndex == -1) 0 else (lastNewLineIndex + 1)
        val previousLine = textBeforeCursor.substring(previousLineStart, previousLineEnd)

        // Check if the previous line was an empty bullet point
        if (previousLine.trim() == "•" || previousLine.trim() == "• ") {
            // Remove the empty bullet point and add a new line
            val textWithoutEmptyBullet = newText.substring(0, previousLineStart) +
                    "\n" +
                    newText.substring(selection.start)

            return currentState.copy(
                content = TextFieldValue(
                    text = textWithoutEmptyBullet,
                    selection = TextRange(previousLineStart + 1)
                ),
                formats = updatedFormats
            )
        }

        // Handle normal bullet point continuation
        if (previousLine.trimStart().startsWith("• ")) {
            val indentation = previousLine.takeWhile { it.isWhitespace() }
            val beforeCursor = newText.substring(0, selection.start)
            val afterCursor = if (selection.start < newText.length) {
                newText.substring(selection.start)
            } else ""

            val textWithNewBullet = beforeCursor + indentation + "• " + afterCursor
            val newCursorPosition = (selection.start + indentation.length + 2)
                .coerceIn(0, textWithNewBullet.length)

            return currentState.copy(
                content = TextFieldValue(
                    text = textWithNewBullet,
                    selection = TextRange(newCursorPosition)
                ),
                formats = updatedFormats
            )
        }

        return null
    }

    fun toggleFormat(
        currentState: EditorPresentationState,
        transform: (TextPresentationFormat) -> TextPresentationFormat,
        updateState: (EditorPresentationState) -> Unit
    ) {
        val selection = currentState.content.selection
        if (selection.start == selection.end) return

        val start = selection.start.coerceIn(0, currentState.content.text.length)
        val end = selection.end.coerceIn(0, currentState.content.text.length)

        val existingFormat = currentState.formats.find {
            it.range.contains(start) && it.range.contains(end - 1)
        }

        val newFormat = transform(existingFormat ?: TextPresentationFormat(start..end))

        updateState(
            currentState.copy(
                formats = currentState.formats.filter {
                    !it.range.overlaps(start..end)
                } + newFormat
            )
        )
    }

    fun getSizeLabel(
        content: TextFieldValue,
        formats: List<TextPresentationFormat>
    ): TextFormatPresentationOption {
        return if (content.selection.start == content.selection.end) {
            TextPresentationFormats.NoSelection
        } else {
            formats.find { it.range.contains(content.selection.start) }
                ?.textSize?.let { size ->
                    when (size) {
                        24f -> TextPresentationFormats.Title
                        20f -> TextPresentationFormats.Heading
                        16f -> TextPresentationFormats.SubHeading
                        else -> TextPresentationFormats.Body
                    }
                } ?: TextPresentationFormats.Body
        }
    }

    fun toggleBulletList(
        currentState: EditorPresentationState,
        updateState: (EditorPresentationState) -> Unit
    ) {
        val selection = currentState.content.selection
        val text = currentState.content.text

        try {
            // Handle case when no text is selected
            if (selection.start == selection.end) {
                // Get the current line
                val lineStart = text.lastIndexOf('\n', selection.start - 1).let {
                    if (it == -1) 0 else it + 1
                }
                val lineEnd = text.indexOf('\n', selection.start).let {
                    if (it == -1) text.length else it
                }
                val currentLine = text.substring(lineStart, lineEnd)

                // Create new text with bullet point
                val newText = buildString {
                    append(text.substring(0, lineStart))
                    // Only add bullet if line doesn't already have one
                    if (!currentLine.trimStart().startsWith("• ")) {
                        append("• ")
                        append(currentLine)
                    } else {
                        append(currentLine.replaceFirst("• ", ""))
                    }
                    if (lineEnd < text.length) {
                        append(text.substring(lineEnd))
                    }
                }

                val newCursorPosition = if (!currentLine.trimStart().startsWith("• ")) {
                    lineStart + 2 + currentLine.length
                } else {
                    lineStart + currentLine.length - 2
                }

                updateState(
                    currentState.copy(
                        content = TextFieldValue(
                            text = newText,
                            selection = TextRange(newCursorPosition)
                        )
                    )
                )
                return
            }

            // Original logic for selected text
            val selectedText = text.substring(selection.start, selection.end)
            val lines = selectedText.split("\n")

            val processedLines = lines.map { line ->
                if (line.trim().startsWith("• ")) {
                    line.replaceFirst("• ", "")
                } else if (line.isNotEmpty()) {
                    "• $line"
                } else {
                    line
                }
            }

            val newText = buildString {
                append(text.substring(0, selection.start))
                append(processedLines.joinToString("\n"))
                if (selection.end < text.length) {
                    append(text.substring(selection.end))
                }
            }

            updateState(
                currentState.copy(
                    content = TextFieldValue(
                        text = newText,
                        selection = TextRange(
                            selection.start,
                            (selection.start + processedLines.joinToString("\n").length)
                                .coerceIn(0, newText.length)
                        )
                    )
                )
            )
        } catch (e: Exception) {
            // If any error occurs, keep the current state
            return
        }
    }

    fun refreshSelection(
        currentState: EditorPresentationState,
        updateState: (EditorPresentationState) -> Unit
    ) {
        updateState(
            currentState.copy(
                content = currentState.content.copy(
                    selection = TextRange(
                        currentState.content.selection.start,
                        currentState.content.selection.end
                    )
                )
            )
        )
    }

    /**
     * Performance optimization: determines if format updates are necessary.
     * Avoids expensive format calculations for minor text changes.
     */
    private fun shouldUpdateFormats(oldLength: Int, newLength: Int): Boolean {
        val lengthDifference = kotlin.math.abs(newLength - oldLength)
        
        // Update formats if:
        // 1. Significant length change (more than 10 characters)
        // 2. Text became empty or was empty
        // 3. First time processing this length
        return lengthDifference > 10 || 
               oldLength == 0 || 
               newLength == 0 || 
               lastProcessedLength != newLength.also { lastProcessedLength = it }
    }
    
    /**
     * Performance optimization: batch format operations to reduce state updates.
     */
    private fun batchFormatUpdates(
        formats: List<TextPresentationFormat>,
        operation: (List<TextPresentationFormat>) -> List<TextPresentationFormat>
    ): List<TextPresentationFormat> {
        // Reuse cache list to avoid allocations
        formatUpdateCache.clear()
        formatUpdateCache.addAll(formats)
        return operation(formatUpdateCache)
    }

    /**
     * Gets text completion suggestions for the current cursor position.
     * 
     * @param content Current text field value
     * @param limit Maximum number of suggestions
     * @return Flow of text completions
     */
    fun getTextCompletions(
        content: TextFieldValue,
        limit: Int = 5
    ): Flow<List<TextCompletion>> {
        return if (contentPredictor != null && shouldUpdatePredictions(content)) {
            contentPredictor.getTextCompletions(
                currentText = content.text,
                cursorPosition = content.selection.start,
                limit = limit
            )
        } else {
            flowOf(emptyList())
        }
    }
    
    /**
     * Gets formatting suggestions based on current context.
     * 
     * @param content Current text field value
     * @return List of formatting suggestions
     */
    fun getFormattingSuggestions(content: TextFieldValue): List<FormattingSuggestion> {
        return contentPredictor?.getFormattingSuggestions(
            text = content.text,
            cursorPosition = content.selection.start
        ) ?: emptyList()
    }
    
    /**
     * Applies a text completion to the current content.
     * 
     * @param content Current text field value
     * @param completion The completion to apply
     * @return Updated text field value
     */
    fun applyTextCompletion(
        content: TextFieldValue,
        completion: TextCompletion
    ): TextFieldValue {
        val cursorPosition = content.selection.start
        val textBeforeCursor = content.text.substring(0, cursorPosition)
        val textAfterCursor = content.text.substring(cursorPosition)
        
        // Find the word being completed
        val currentWord = extractCurrentWord(textBeforeCursor)
        val wordStartPosition = cursorPosition - currentWord.length
        
        // Replace the current word with the completion
        val newText = content.text.substring(0, wordStartPosition) +
                completion.text +
                textAfterCursor
        
        val newCursorPosition = wordStartPosition + completion.text.length
        
        return TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition)
        )
    }
    
    /**
     * Applies a formatting suggestion to the current content.
     * 
     * @param content Current text field value
     * @param suggestion The formatting suggestion to apply
     * @return Updated text field value
     */
    fun applyFormattingSuggestion(
        content: TextFieldValue,
        suggestion: FormattingSuggestion
    ): TextFieldValue {
        val cursorPosition = content.selection.start
        val textBeforeCursor = content.text.substring(0, cursorPosition)
        val textAfterCursor = content.text.substring(cursorPosition)
        
        val newText = textBeforeCursor + suggestion.action + textAfterCursor
        val newCursorPosition = cursorPosition + suggestion.action.length
        
        return TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition)
        )
    }
    
    /**
     * Records the current text for learning user patterns.
     * 
     * @param content Current text content
     */
    suspend fun recordTextPattern(content: String) {
        contentPredictor?.recordTextPattern(content)
    }
    
    // Private helper methods for content prediction
    
    private fun shouldUpdatePredictions(content: TextFieldValue): Boolean {
        val textChanged = content.text != lastPredictionText
        val cursorChanged = content.selection.start != lastPredictionCursor
        
        // Update prediction state
        lastPredictionText = content.text
        lastPredictionCursor = content.selection.start
        
        // Only update if cursor is at end of a word and text has meaningful changes
        return textChanged && content.selection.start == content.selection.end &&
                content.text.length >= lastProcessedLength + 2
    }
    
    private fun extractCurrentWord(textBeforeCursor: String): String {
        val words = textBeforeCursor.split("\\s+".toRegex())
        return words.lastOrNull()?.trim() ?: ""
    }
    
    /**
     * Enhanced bullet list toggle with smart formatting suggestions.
     */
    fun toggleBulletListWithSuggestions(
        currentState: EditorPresentationState,
        updateState: (EditorPresentationState) -> Unit
    ) {
        // First check if we should suggest formatting
        val suggestions = getFormattingSuggestions(currentState.content)
        val listSuggestion = suggestions.find { 
            it.type == com.module.notelycompose.notes.domain.FormattingSuggestionType.BULLET_LIST 
        }
        
        if (listSuggestion != null) {
            // Apply the smart suggestion
            val updatedContent = applyFormattingSuggestion(currentState.content, listSuggestion)
            updateState(currentState.copy(content = updatedContent))
        } else {
            // Fall back to original toggle logic
            toggleBulletList(currentState, updateState)
        }
    }

    // Extension function for IntRange
    private fun IntRange.overlaps(other: IntRange): Boolean =
        first <= other.last && other.first <= last
}