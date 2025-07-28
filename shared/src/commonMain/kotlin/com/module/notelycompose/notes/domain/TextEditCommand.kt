package com.module.notelycompose.notes.domain

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.mohamedrejeb.richeditor.model.RichTextState
import com.module.notelycompose.security.HtmlSanitizer
import kotlinx.datetime.Clock

/**
 * Result type for text edit command operations.
 */
sealed class TextEditCommandResult {
    data class Success(val newContent: String) : TextEditCommandResult()
    data class Error(val error: String) : TextEditCommandResult()
}

/**
 * Command pattern interface for implementing undo/redo functionality in rich text editing.
 * 
 * This interface represents all text editing operations that can be undone and redone,
 * providing a consistent way to manage editing history and enable advanced features
 * like batch operations and command merging for performance optimization.
 */
interface TextEditCommand {
    /**
     * Executes the command, applying the text editing operation.
     */
    suspend fun execute()
    
    /**
     * Undoes the command, reverting the text editing operation.
     */
    suspend fun undo()
    
    /**
     * Gets a human-readable description of the command for debugging and UI display.
     */
    fun getDescription(): String
    
    /**
     * Determines if this command can be merged with another command for optimization.
     * Commands can typically be merged if they operate on the same text range or
     * are of the same type and occur within a short time window.
     */
    fun canMergeWith(other: TextEditCommand): Boolean
    
    /**
     * Merges this command with another command, returning a new command that
     * represents both operations. Returns null if merging is not possible.
     */
    fun mergeWith(other: TextEditCommand): TextEditCommand?
    
    /**
     * Gets the timestamp when this command was created for merging and history management.
     */
    val timestamp: Long
}

/**
 * Represents the state of text formatting at a specific point.
 */
data class FormattingSnapshot(
    val range: TextRange,
    val fontWeight: FontWeight?,
    val fontStyle: FontStyle?,
    val textDecoration: TextDecoration?,
    val textAlign: TextAlign?
)

/**
 * Command for applying text formatting changes (bold, italic, underline, alignment).
 */
class FormatCommand(
    private val richTextState: RichTextState,
    private val formatType: FormatType,
    private val range: TextRange,
    private val previousSnapshot: FormattingSnapshot,
    private val newSnapshot: FormattingSnapshot,
    override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) : TextEditCommand {
    
    override suspend fun execute() {
        when (formatType) {
            FormatType.Bold -> {
                newSnapshot.fontWeight?.let { weight ->
                    richTextState.toggleSpanStyle(SpanStyle(fontWeight = weight))
                }
            }
            FormatType.Italic -> {
                newSnapshot.fontStyle?.let { style ->
                    richTextState.toggleSpanStyle(SpanStyle(fontStyle = style))
                }
            }
            FormatType.Underline -> {
                newSnapshot.textDecoration?.let { decoration ->
                    richTextState.toggleSpanStyle(SpanStyle(textDecoration = decoration))
                }
            }
            FormatType.Alignment -> {
                newSnapshot.textAlign?.let { align ->
                    richTextState.toggleParagraphStyle(ParagraphStyle(textAlign = align))
                }
            }
        }
    }
    
    override suspend fun undo() {
        when (formatType) {
            FormatType.Bold -> {
                previousSnapshot.fontWeight?.let { weight ->
                    richTextState.toggleSpanStyle(SpanStyle(fontWeight = weight))
                }
            }
            FormatType.Italic -> {
                previousSnapshot.fontStyle?.let { style ->
                    richTextState.toggleSpanStyle(SpanStyle(fontStyle = style))
                }
            }
            FormatType.Underline -> {
                previousSnapshot.textDecoration?.let { decoration ->
                    richTextState.toggleSpanStyle(SpanStyle(textDecoration = decoration))
                }
            }
            FormatType.Alignment -> {
                previousSnapshot.textAlign?.let { align ->
                    richTextState.toggleParagraphStyle(ParagraphStyle(textAlign = align))
                }
            }
        }
    }
    
    override fun getDescription(): String {
        return when (formatType) {
            FormatType.Bold -> "Toggle Bold"
            FormatType.Italic -> "Toggle Italic"
            FormatType.Underline -> "Toggle Underline"
            FormatType.Alignment -> "Change Alignment"
        }
    }
    
    override fun canMergeWith(other: TextEditCommand): Boolean {
        return other is FormatCommand && 
               other.formatType == formatType &&
               other.range == range &&
               (timestamp - other.timestamp) < MERGE_WINDOW_MS
    }
    
    override fun mergeWith(other: TextEditCommand): TextEditCommand? {
        if (!canMergeWith(other) || other !is FormatCommand) return null
        
        // Return a new command that combines both operations
        return FormatCommand(
            richTextState = richTextState,
            formatType = formatType,
            range = range,
            previousSnapshot = previousSnapshot, // Keep original previous state
            newSnapshot = other.newSnapshot, // Use the latest new state
            timestamp = other.timestamp // Use the latest timestamp
        )
    }
    
    companion object {
        private const val MERGE_WINDOW_MS = 1000L // 1 second window for merging
    }
}

/**
 * Command for text insertion operations.
 */
class InsertTextCommand(
    private val richTextState: RichTextState,
    private val insertPosition: Int,
    private val text: String,
    override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) : TextEditCommand {
    
    override suspend fun execute() {
        // SECURITY: Sanitize text content to prevent XSS attacks during command execution
        val sanitizedText = HtmlSanitizer.sanitize(text)
        richTextState.insertHtml(sanitizedText, insertPosition)
    }
    
    override suspend fun undo() {
        val currentText = richTextState.annotatedString.text
        val newText = currentText.removeRange(insertPosition, insertPosition + text.length)
        // SECURITY: Clear with empty string (already safe) and rebuild with plain text
        richTextState.setHtml("") // Clear and rebuild - this is a simplified approach
        richTextState.insertHtml(newText, 0) // insertHtml is safe for plain text
    }
    
    override fun getDescription(): String = "Insert Text"
    
    override fun canMergeWith(other: TextEditCommand): Boolean {
        return other is InsertTextCommand &&
               other.insertPosition == insertPosition + text.length &&
               (other.timestamp - timestamp) < MERGE_WINDOW_MS
    }
    
    override fun mergeWith(other: TextEditCommand): TextEditCommand? {
        if (!canMergeWith(other) || other !is InsertTextCommand) return null
        
        return InsertTextCommand(
            richTextState = richTextState,
            insertPosition = insertPosition,
            text = text + other.text,
            timestamp = other.timestamp
        )
    }
    
    companion object {
        private const val MERGE_WINDOW_MS = 2000L // 2 seconds for text insertion merging
    }
}

/**
 * Command for text deletion operations.
 */
class DeleteTextCommand(
    private val richTextState: RichTextState,
    private val range: TextRange,
    private val deletedText: String,
    override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) : TextEditCommand {
    
    override suspend fun execute() {
        // Delete text by rebuilding content without the deleted range
        val currentText = richTextState.annotatedString.text
        val newText = currentText.removeRange(range.start, range.end)
        richTextState.setHtml("")
        richTextState.insertHtml(newText, 0)
    }
    
    override suspend fun undo() {
        val currentText = richTextState.annotatedString.text
        val newText = currentText.substring(0, range.start) + 
                     deletedText + 
                     currentText.substring(range.start)
        // SECURITY: Clear with empty string (already safe) and rebuild with plain text
        richTextState.setHtml("") // Clear and rebuild
        richTextState.insertHtml(newText, 0) // insertHtml is safe for plain text
    }
    
    override fun getDescription(): String = "Delete Text"
    
    override fun canMergeWith(other: TextEditCommand): Boolean {
        return other is DeleteTextCommand &&
               (other.range.end == range.start || other.range.start == range.end) &&
               (other.timestamp - timestamp) < MERGE_WINDOW_MS
    }
    
    override fun mergeWith(other: TextEditCommand): TextEditCommand? {
        if (!canMergeWith(other) || other !is DeleteTextCommand) return null
        
        val mergedRange = if (other.range.end == range.start) {
            TextRange(other.range.start, range.end)
        } else {
            TextRange(range.start, other.range.end)
        }
        
        val mergedText = if (other.range.end == range.start) {
            other.deletedText + deletedText
        } else {
            deletedText + other.deletedText
        }
        
        return DeleteTextCommand(
            richTextState = richTextState,
            range = mergedRange,
            deletedText = mergedText,
            timestamp = other.timestamp
        )
    }
    
    companion object {
        private const val MERGE_WINDOW_MS = 1000L // 1 second for deletion merging
    }
}

/**
 * Command for list operations (ordered/unordered lists).
 */
class ListCommand(
    private val richTextState: RichTextState,
    private val range: TextRange,
    private val listType: ListType,
    private val isAdding: Boolean, // true for adding list, false for removing
    override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) : TextEditCommand {
    
    override suspend fun execute() {
        when (listType) {
            ListType.Unordered -> {
                if (isAdding) {
                    richTextState.toggleUnorderedList()
                } else {
                    richTextState.toggleUnorderedList() // Toggle to remove
                }
            }
            ListType.Ordered -> {
                if (isAdding) {
                    richTextState.toggleOrderedList()
                } else {
                    richTextState.toggleOrderedList() // Toggle to remove
                }
            }
        }
    }
    
    override suspend fun undo() {
        // Reverse the operation
        when (listType) {
            ListType.Unordered -> richTextState.toggleUnorderedList()
            ListType.Ordered -> richTextState.toggleOrderedList()
        }
    }
    
    override fun getDescription(): String {
        val action = if (isAdding) "Add" else "Remove"
        val type = when (listType) {
            ListType.Unordered -> "Bullet List"
            ListType.Ordered -> "Numbered List"
        }
        return "$action $type"
    }
    
    override fun canMergeWith(other: TextEditCommand): Boolean = false // Lists typically don't merge
    
    override fun mergeWith(other: TextEditCommand): TextEditCommand? = null
}

/**
 * Composite command that combines multiple commands into a single undoable operation.
 */
class CompositeCommand(
    private val commands: List<TextEditCommand>,
    override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) : TextEditCommand {
    
    override suspend fun execute() {
        commands.forEach { it.execute() }
    }
    
    override suspend fun undo() {
        // Undo in reverse order
        commands.asReversed().forEach { it.undo() }
    }
    
    override fun getDescription(): String {
        return when (commands.size) {
            0 -> "Empty Operation"
            1 -> commands.first().getDescription()
            else -> "Batch Operation (${commands.size} actions)"
        }
    }
    
    override fun canMergeWith(other: TextEditCommand): Boolean = false // Composite commands don't merge
    
    override fun mergeWith(other: TextEditCommand): TextEditCommand? = null
}

/**
 * Types of formatting that can be applied.
 */
enum class FormatType {
    Bold,
    Italic,
    Underline,
    Alignment
}

/**
 * Types of lists that can be applied.
 */
enum class ListType {
    Ordered,
    Unordered
}

/**
 * Extension function to create a formatting snapshot from current RichTextState.
 */
fun RichTextState.createFormattingSnapshot(range: TextRange): FormattingSnapshot {
    return FormattingSnapshot(
        range = range,
        fontWeight = currentSpanStyle.fontWeight,
        fontStyle = currentSpanStyle.fontStyle,
        textDecoration = currentSpanStyle.textDecoration,
        textAlign = currentParagraphStyle.textAlign
    )
}

/**
 * Factory functions for creating common commands.
 */
object CommandFactory {
    
    fun createBoldCommand(
        richTextState: RichTextState,
        range: TextRange,
        previousSnapshot: FormattingSnapshot,
        newWeight: FontWeight
    ): FormatCommand {
        val newSnapshot = previousSnapshot.copy(fontWeight = newWeight)
        return FormatCommand(
            richTextState = richTextState,
            formatType = FormatType.Bold,
            range = range,
            previousSnapshot = previousSnapshot,
            newSnapshot = newSnapshot
        )
    }
    
    fun createItalicCommand(
        richTextState: RichTextState,
        range: TextRange,
        previousSnapshot: FormattingSnapshot,
        newStyle: FontStyle
    ): FormatCommand {
        val newSnapshot = previousSnapshot.copy(fontStyle = newStyle)
        return FormatCommand(
            richTextState = richTextState,
            formatType = FormatType.Italic,
            range = range,
            previousSnapshot = previousSnapshot,
            newSnapshot = newSnapshot
        )
    }
    
    fun createUnderlineCommand(
        richTextState: RichTextState,
        range: TextRange,
        previousSnapshot: FormattingSnapshot,
        newDecoration: TextDecoration
    ): FormatCommand {
        val newSnapshot = previousSnapshot.copy(textDecoration = newDecoration)
        return FormatCommand(
            richTextState = richTextState,
            formatType = FormatType.Underline,
            range = range,
            previousSnapshot = previousSnapshot,
            newSnapshot = newSnapshot
        )
    }
    
    fun createAlignmentCommand(
        richTextState: RichTextState,
        range: TextRange,
        previousSnapshot: FormattingSnapshot,
        newAlignment: TextAlign
    ): FormatCommand {
        val newSnapshot = previousSnapshot.copy(textAlign = newAlignment)
        return FormatCommand(
            richTextState = richTextState,
            formatType = FormatType.Alignment,
            range = range,
            previousSnapshot = previousSnapshot,
            newSnapshot = newSnapshot
        )
    }
}