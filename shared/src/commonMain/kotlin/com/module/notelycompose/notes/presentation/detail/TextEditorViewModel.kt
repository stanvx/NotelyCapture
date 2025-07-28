package com.module.notelycompose.notes.presentation.detail

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.utils.deleteFile
import com.module.notelycompose.notes.domain.DeleteNoteById
import com.module.notelycompose.notes.domain.GetLastNote
import com.module.notelycompose.notes.domain.GetNoteById
import com.module.notelycompose.notes.domain.InsertNoteUseCase
import com.module.notelycompose.notes.domain.UpdateNoteUseCase
import com.module.notelycompose.notes.domain.TextEditCommandResult
import com.module.notelycompose.notes.domain.UndoRedoManager
import com.module.notelycompose.notes.domain.FormatCommand
import com.module.notelycompose.notes.domain.CompositeCommand
import com.module.notelycompose.notes.domain.TextEditCommand
import com.module.notelycompose.notes.domain.InsertTextCommand
import com.module.notelycompose.notes.domain.DeleteTextCommand
import com.module.notelycompose.notes.domain.model.NoteDomainModel
import com.module.notelycompose.notes.presentation.detail.model.EditorPresentationState
import com.module.notelycompose.notes.presentation.detail.model.RecordingPathPresentationModel
import com.module.notelycompose.notes.presentation.detail.model.TextPresentationFormat
import com.module.notelycompose.notes.presentation.helpers.TextEditorHelper
import com.module.notelycompose.notes.presentation.helpers.RichTextEditorHelper
import com.module.notelycompose.notes.presentation.helpers.formattedDate
import com.module.notelycompose.notes.presentation.mapper.EditorPresentationToUiStateMapper
import com.module.notelycompose.notes.presentation.mapper.TextAlignPresentationMapper
import com.module.notelycompose.notes.presentation.mapper.TextFormatPresentationMapper
import com.module.notelycompose.notes.ui.detail.EditorUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.module.notelycompose.core.validation.InputValidator

private const val ID_NOT_SET = 0L
private const val SAVE_DEBOUNCE_DELAY = 500L // 500ms debounce for save operations
private const val SYNC_DEBOUNCE_DELAY = 150L // 150ms debounce for rich text sync

class TextEditorViewModel(
    private val getNoteByIdUseCase: GetNoteById,
    private val insertNoteUseCase: InsertNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteById,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val getLastNoteUseCase: GetLastNote,
    private val editorPresentationToUiStateMapper: EditorPresentationToUiStateMapper,
    private val textFormatPresentationMapper: TextFormatPresentationMapper,
    private val textAlignPresentationMapper: TextAlignPresentationMapper,
    private val textEditorHelper: TextEditorHelper,
    private val richTextEditorHelper: RichTextEditorHelper,
    private val audioPlayer: com.module.notelycompose.platform.PlatformAudioPlayer
) : ViewModel() {

    private val _editorPresentationState = MutableStateFlow(EditorPresentationState())
    val editorPresentationState: StateFlow<EditorPresentationState> = _editorPresentationState
    private var _currentNoteId = MutableStateFlow<Long?>(ID_NOT_SET)

    internal val currentNoteId: StateFlow<Long?> = _currentNoteId.asStateFlow()
    private val _noteIdTrigger = MutableStateFlow<Long?>(null)
    
    // Undo/Redo functionality
    private val undoRedoManager = UndoRedoManager()
    
    // Performance optimization fields
    private var saveJob: Job? = null
    private var syncJob: Job? = null
    private var lastContentHash: Int = 0
    
    // Expose undo/redo state for UI
    val canUndo: StateFlow<Boolean> = undoRedoManager.canUndo
    val canRedo: StateFlow<Boolean> = undoRedoManager.canRedo
    val undoDescription: StateFlow<String?> = undoRedoManager.undoDescription
    val redoDescription: StateFlow<String?> = undoRedoManager.redoDescription
    
    // Expose rich text state for UI components
    val richTextState: StateFlow<com.mohamedrejeb.richeditor.model.RichTextState> = richTextEditorHelper.richTextState
    
    // Security: Error handling for security violations
    private val _securityErrors = MutableStateFlow<String?>(null)
    val securityErrors: StateFlow<String?> = _securityErrors.asStateFlow()
    
    // Security: Safe recordings directory - platform-specific implementation needed
    private val safeRecordingsDirectory: String by lazy {
        createSafeRecordingsDirectory()
    }

    init {
        viewModelScope.launch {
            _noteIdTrigger
                .filterNotNull()
                .take(1)
                .collect { id ->
                    val note = getNoteByIdUseCase.execute(id)
                    note?.let { retrievedNote ->
                        processNote(retrievedNote)
                        _currentNoteId.value = id
                    }
                }
        }
    }

    private fun processNote(retrievedNote: NoteDomainModel) {
        viewModelScope.launch {
            loadNote(
                content = retrievedNote.content,
                formats = retrievedNote.formatting.map {
                    textFormatPresentationMapper.mapToPresentationModel(it)
                },
                textAlign = textAlignPresentationMapper.mapToComposeTextAlign(
                    retrievedNote.textAlign
                ),
                recordingPath = retrievedNote.recordingPath,
                starred = retrievedNote.starred,
                createdAt = getFormattedDate(retrievedNote.createdAt)
            )
        }
    }

    fun onGetNoteById(id: String) {
        _noteIdTrigger.value = id.toLong()
    }

    private fun getLastNote() = getLastNoteUseCase.execute()

    fun onUpdateContent(newContent: TextFieldValue) {
        val oldContent = _editorPresentationState.value.content.text
        
        // SECURITY: Validate content input
        val validation = InputValidator.validateNoteContent(newContent.text)
        if (!validation.isValid) {
            reportSecurityError("Invalid note content: ${validation.errorMessage}")
            return
        }
        
        val sanitizedContent = newContent
        
        updateContent(sanitizedContent)
        
        // Optimized sync: only sync if content actually changed
        if (oldContent != sanitizedContent.text) {
            syncContentToRichText(sanitizedContent.text)
            
            // Create undo/redo command for content changes
            createContentUpdateCommand(oldContent, sanitizedContent.text)
            
            // Debounced save operation
            debouncedSave(
                title = sanitizedContent.text,
                content = sanitizedContent.text,
                starred = _editorPresentationState.value.starred,
                formatting = _editorPresentationState.value.formats,
                textAlign = _editorPresentationState.value.textAlign,
                recordingPath = _editorPresentationState.value.recording.recordingPath,
            )
        }
    }
    
    /**
     * Handles content updates from the RichTextEditor with performance optimizations.
     * This method processes changes from the rich text editor and synchronizes
     * them with the existing text formatting system.
     */
    fun onUpdateRichContent() {
        val oldContent = _editorPresentationState.value.content.text
        syncContentFromRichText()
        val currentState = _editorPresentationState.value
        
        // Only save if content actually changed
        if (oldContent != currentState.content.text) {
            debouncedSave(
                title = currentState.content.text,
                content = currentState.content.text,
                starred = currentState.starred,
                formatting = currentState.formats,
                textAlign = currentState.textAlign,
                recordingPath = currentState.recording.recordingPath,
            )
        }
    }

    fun onUpdateRecordingPath(recordingPath: String) {
        // SECURITY: Validate recording path before updating
        if (recordingPath.isNotEmpty() && !isPathSafe(recordingPath)) {
            reportSecurityError("Invalid recording path provided")
            return
        }
        
        viewModelScope.launch {
            val recordingModel = recordingPath(recordingPath)
            _editorPresentationState.update {
                it.copy(recording = recordingModel)
            }
            onUpdateContent(newContent = _editorPresentationState.value.content)
        }
    }

    fun onDeleteRecord() {
        val recordingPath = _editorPresentationState.value.recording.recordingPath
        
        // SECURITY: Validate path before deletion to prevent path traversal attacks
        if (!isPathSafe(recordingPath)) {
            reportSecurityError("Invalid recording path detected during deletion")
            return
        }
        
        deleteFile(recordingPath)
        viewModelScope.launch {
            val recordingModel = recordingPath(/*reset record path */"")
            _editorPresentationState.update {
                it.copy(recording = recordingModel)
            }
            onUpdateContent(newContent = _editorPresentationState.value.content)
        }
    }

    private suspend fun recordingPath(recordingPath: String): RecordingPathPresentationModel {
        val audioDuration = if (recordingPath.isNotEmpty()) {
            getAudioDuration(recordingPath)
        } else {
            0
        }
        
        return RecordingPathPresentationModel(
            recordingPath = recordingPath,
            isRecordingExist = recordingPath.isNotEmpty(),
            audioDurationMs = audioDuration
        )
    }
    
    private suspend fun getAudioDuration(recordingPath: String): Int {
        return if (recordingPath.isNotEmpty()) {
            // SECURITY: Validate path before accessing audio file
            if (!isPathSafe(recordingPath)) {
                reportSecurityError("Invalid recording path detected during audio duration check")
                return 0
            }
            
            try {
                audioPlayer.prepare(recordingPath)
            } catch (e: Exception) {
                println("Failed to get audio duration for $recordingPath: ${e.message}")
                0
            }
        } else {
            0
        }
    }

    private suspend fun loadNote(
        content: String,
        formats: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String,
        starred: Boolean,
        createdAt: String
    ) {
        val recordingModel = recordingPath(recordingPath)
        _editorPresentationState.update {
            it.copy(
                content = TextFieldValue(content),
                formats = formats,
                textAlign = textAlign,
                recording = recordingModel,
                starred = starred,
                createdAt = createdAt
            )
        }
        
        // Synchronize content to rich text state
        syncContentToRichText(content)
    }
    
    /**
     * Synchronizes content from plain text to RichTextState with debouncing.
     * This ensures both text systems are kept in sync when loading notes.
     */
    private fun syncContentToRichText(content: String) {
        // Cancel previous sync job if still pending
        syncJob?.cancel()
        
        // Only sync if content actually changed (performance optimization)
        val contentHash = content.hashCode()
        if (contentHash == lastContentHash) return
        lastContentHash = contentHash
        
        syncJob = viewModelScope.launch {
            delay(SYNC_DEBOUNCE_DELAY)
            richTextEditorHelper.setContent(content)
        }
    }
    
    /**
     * Debounced save operation to improve performance during rapid text changes.
     */
    private fun debouncedSave(
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String
    ) {
        // Cancel previous save job if still pending
        saveJob?.cancel()
        
        saveJob = viewModelScope.launch {
            delay(SAVE_DEBOUNCE_DELAY)
            createOrUpdateEvent(
                title = title,
                content = content,
                starred = starred,
                formatting = formatting,
                textAlign = textAlign,
                recordingPath = recordingPath
            )
        }
    }
    
    /**
     * Synchronizes content from RichTextState back to TextFieldValue.
     * This is used when the rich text editor content changes.
     */
    private fun syncContentFromRichText() {
        val richTextContent = richTextEditorHelper.getPlainText()
        val currentState = _editorPresentationState.value
        
        if (currentState.content.text != richTextContent) {
            _editorPresentationState.update {
                it.copy(content = TextFieldValue(richTextContent))
            }
        }
    }

    fun onGetUiState(presentationState: EditorPresentationState): EditorUiState {
        return editorPresentationToUiStateMapper.mapToUiState(presentationState)
    }

    private fun insertNote(
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String
    ) {
        viewModelScope.launch {
            _currentNoteId.value = insertNoteUseCase.execute(
                title = title,
                content = content,
                starred = starred,
                formatting = formatting.map { textFormatPresentationMapper.mapToDomainModel(it) },
                textAlign = textAlignPresentationMapper.mapToDomainModel(textAlign),
                recordingPath = recordingPath
            )
        }
    }

    private fun updateNote(
        noteId: Long,
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String
    ) {
        viewModelScope.launch {
            updateNoteUseCase.execute(
                id = noteId,
                title = title,
                content = content,
                starred = starred,
                formatting = formatting.map { textFormatPresentationMapper.mapToDomainModel(it) },
                textAlign = textAlignPresentationMapper.mapToDomainModel(textAlign),
                recordingPath = recordingPath
            )
        }
    }

    fun onDeleteNote() {
        _currentNoteId.value?.let { noteId ->
            val path = _editorPresentationState.value.recording.recordingPath
            
            // SECURITY: Validate path before deletion to prevent path traversal attacks
            if (path.isNotEmpty() && !isPathSafe(path)) {
                reportSecurityError("Invalid recording path detected during note deletion")
                // Still proceed with note deletion but skip file deletion
                deleteNote(id = noteId)
                return@let
            }
            
            deleteFile(filePath = path)
            deleteNote(id = noteId)
        }
    }

    private fun deleteNote(id: Long) {
        viewModelScope.launch {
            deleteNoteUseCase.execute(id)
        }
    }

    fun onToggleStar() {
        val starred = _editorPresentationState.value.starred
        _editorPresentationState.update {
            it.copy(
                starred = !starred
            )
        }
        onUpdateContent(newContent = _editorPresentationState.value.content)
    }

    private fun getFormattedDate(
        createdAt: LocalDateTime = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
    ): String {
        return createdAt.formattedDate()
    }

    private fun createOrUpdateEvent(
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String
    ) {
        val currentNoteId = _currentNoteId.value
        when {
            currentNoteId != null && currentNoteId != ID_NOT_SET -> {
                updateNote(
                    noteId = currentNoteId,
                    title = title,
                    content = content,
                    starred = starred,
                    formatting = formatting,
                    textAlign = textAlign,
                    recordingPath = recordingPath
                )
            }

            else -> {
                insertNote(
                    title = title,
                    content = content,
                    starred = starred,
                    formatting = formatting,
                    textAlign = textAlign,
                    recordingPath = recordingPath
                )
            }
        }
    }

    private fun updateContent(newContent: TextFieldValue) {
        textEditorHelper.updateContent(
            newContent = newContent,
            currentState = _editorPresentationState.value,
            getFormattedDate = { getFormattedDate() },
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
    }

    fun onToggleBold() {
        executeFormattingCommand("Toggle Bold") {
            textEditorHelper.toggleFormat(
                currentState = _editorPresentationState.value,
                transform = { it.copy(isBold = !it.isBold) },
                updateState = { newState ->
                    _editorPresentationState.update { newState }
                }
            )
            // Apply to rich text state as well
            richTextEditorHelper.toggleBold()
            refreshSelection()
        }
    }

    fun onToggleItalic() {
        executeFormattingCommand("Toggle Italic") {
            textEditorHelper.toggleFormat(
                currentState = _editorPresentationState.value,
                transform = { it.copy(isItalic = !it.isItalic) },
                updateState = { newState ->
                    _editorPresentationState.update { newState }
                }
            )
            // Apply to rich text state as well
            richTextEditorHelper.toggleItalic()
            refreshSelection()
        }
    }

    fun setTextSize(size: Float) {
        executeFormattingCommand("Set Text Size $size") {
            textEditorHelper.toggleFormat(
                currentState = _editorPresentationState.value,
                transform = { it.copy(textSize = size) },
                updateState = { newState ->
                    _editorPresentationState.update { newState }
                }
            )
            refreshSelection()
        }
    }

    fun onToggleUnderline() {
        executeFormattingCommand("Toggle Underline") {
            textEditorHelper.toggleFormat(
                currentState = _editorPresentationState.value,
                transform = { it.copy(isUnderline = !it.isUnderline) },
                updateState = { newState ->
                    _editorPresentationState.update { newState }
                }
            )
            // Apply to rich text state as well
            richTextEditorHelper.toggleUnderline()
            refreshSelection()
        }
    }

    private fun refreshSelection() {
        textEditorHelper.refreshSelection(
            currentState = _editorPresentationState.value,
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
    }

    fun onSetAlignment(alignment: TextAlign) {
        executeFormattingCommand("Set Alignment $alignment") {
            _editorPresentationState.update { it.copy(textAlign = alignment) }
            // Apply to rich text state as well
            richTextEditorHelper.setAlignment(alignment)
            val content = _editorPresentationState.value.content
            val formats = _editorPresentationState.value.formats
            val textAlign = _editorPresentationState.value.textAlign
            val starred = _editorPresentationState.value.starred
            val recordingPath = _editorPresentationState.value.recording.recordingPath
            if (content.text.isNotEmpty()) {
                createOrUpdateEvent(
                    title = content.text,
                    content = content.text,
                    starred = starred,
                    formatting = formats,
                    textAlign = textAlign,
                    recordingPath = recordingPath
                )
            }
        }
    }

    fun onToggleBulletList() {
        executeFormattingCommand("Toggle Bullet List") {
            textEditorHelper.toggleBulletList(
                currentState = _editorPresentationState.value,
                updateState = { newState ->
                    _editorPresentationState.update { newState }
                }
            )
            // Apply to rich text state as well
            richTextEditorHelper.toggleUnorderedList()
        }
    }
    
    /**
     * Toggles ordered list formatting using the RichTextEditor.
     */
    fun onToggleOrderedList() {
        executeFormattingCommand("Toggle Ordered List") {
            richTextEditorHelper.toggleOrderedList()
            // Sync changes back to traditional state
            onUpdateRichContent()
        }
    }
    
    /**
     * Adds a heading of the specified level using the RichTextEditor.
     * 
     * @param level The heading level (1-6)
     */
    fun onAddHeading(level: Int) {
        executeFormattingCommand("Add Heading $level") {
            richTextEditorHelper.addHeading(level)
            // Sync changes back to traditional state
            onUpdateRichContent()
        }
    }
    
    /**
     * Sets the current text to body/paragraph style.
     */
    fun onSetBodyText() {
        richTextEditorHelper.setBodyText()
        // Sync changes back to traditional state
        onUpdateRichContent()
    }
    
    /**
     * Clears all rich text formatting.
     */
    fun onClearFormatting() {
        executeFormattingCommand("Clear Formatting") {
            richTextEditorHelper.clearFormatting()
            // Also clear traditional formatting
            _editorPresentationState.update {
                it.copy(formats = emptyList())
            }
            // Sync changes back
            onUpdateRichContent()
        }
    }
    
    /**
     * Toggles strikethrough formatting on selected text.
     */
    fun onToggleStrikethrough() {
        executeFormattingCommand("Toggle Strikethrough") {
            richTextEditorHelper.toggleStrikethrough()
            refreshSelection()
        }
    }
    
    /**
     * Toggles code block formatting on selected text.
     */
    fun onToggleCodeBlock() {
        executeFormattingCommand("Toggle Code Block") {
            richTextEditorHelper.toggleCodeBlock()
            // Sync changes back to traditional state
            onUpdateRichContent()
        }
    }
    
    /**
     * Toggles quote block formatting on selected text.
     */
    fun onToggleQuoteBlock() {
        executeFormattingCommand("Toggle Quote Block") {
            richTextEditorHelper.toggleQuoteBlock()
            // Sync changes back to traditional state
            onUpdateRichContent()
        }
    }
    
    /**
     * Gets the current formatting state from the RichTextEditor.
     * This can be used to update toolbar button states.
     */
    fun getRichTextFormattingState(): RichTextFormattingState {
        return RichTextFormattingState(
            isBold = richTextEditorHelper.isSelectionBold(),
            isItalic = richTextEditorHelper.isSelectionItalic(),
            isUnderlined = richTextEditorHelper.isSelectionUnderlined(),
            isUnorderedList = richTextEditorHelper.isUnorderedList(),
            isOrderedList = richTextEditorHelper.isOrderedList(),
            currentAlignment = richTextEditorHelper.getCurrentAlignment(),
            currentHeadingLevel = richTextEditorHelper.getCurrentHeadingLevel(),
            isCodeBlock = richTextEditorHelper.isCodeBlock(),
            isQuoteBlock = richTextEditorHelper.isQuoteBlock()
        )
    }
    
    /**
     * Undoes the last text editing operation.
     */
    fun onUndo() {
        viewModelScope.launch {
            val success = undoRedoManager.undo()
            if (!success) {
                // Handle undo failure - could show error message
                println("Undo failed")
            }
        }
    }
    
    /**
     * Redoes the last undone text editing operation.
     */
    fun onRedo() {
        viewModelScope.launch {
            val success = undoRedoManager.redo()
            if (!success) {
                // Handle redo failure - could show error message
                println("Redo failed")
            }
        }
    }
    
    
    /**
     * Executes a formatting command with undo/redo support and performance optimizations.
     * @param description Description of the command for undo/redo
     * @param action The formatting action to execute
     */
    private fun executeFormattingCommand(description: String, action: () -> Unit) {
        val beforeState = _editorPresentationState.value
        val beforeContent = beforeState.content.text
        val beforeFormats = beforeState.formats
        val beforeAlignment = beforeState.textAlign
        
        // Execute the formatting action
        action()
        
        val afterState = _editorPresentationState.value
        val afterContent = afterState.content.text
        val afterFormats = afterState.formats
        val afterAlignment = afterState.textAlign
        
        // Only create command if there were actual changes (performance optimization)
        val hasContentChange = beforeContent != afterContent
        val hasFormatChange = beforeFormats != afterFormats
        val hasAlignmentChange = beforeAlignment != afterAlignment
        
        if (hasContentChange || hasFormatChange || hasAlignmentChange) {
            val commands = mutableListOf<TextEditCommand>()
            
            if (hasContentChange) {
                // Determine if this is an insertion or deletion
                if (afterContent.length > beforeContent.length) {
                    // Text was inserted
                    val insertPosition = beforeContent.length // Simplified - assuming append
                    val insertedText = afterContent.substring(beforeContent.length)
                    commands.add(
                        InsertTextCommand(
                            richTextState = richTextEditorHelper.richTextState.value,
                            insertPosition = insertPosition,
                            text = insertedText
                        )
                    )
                } else if (beforeContent.length > afterContent.length) {
                    // Text was deleted
                    val deleteRange = TextRange(afterContent.length, beforeContent.length)
                    val deletedText = beforeContent.substring(afterContent.length)
                    commands.add(
                        DeleteTextCommand(
                            richTextState = richTextEditorHelper.richTextState.value,
                            range = deleteRange,
                            deletedText = deletedText
                        )
                    )
                }
            }
            
            if (commands.isNotEmpty()) {
                val compositeCommand = if (commands.size == 1) {
                    commands[0]
                } else {
                    CompositeCommand(commands)
                }
                viewModelScope.launch {
                    undoRedoManager.executeCommand(compositeCommand)
                }
            }
        }
    }
    
    /**
     * Creates a command for content updates with undo/redo support.
     */
    private fun createContentUpdateCommand(oldContent: String, newContent: String) {
        if (oldContent != newContent) {
            val command = if (newContent.length > oldContent.length) {
                // Text was inserted
                val insertPosition = oldContent.length // Simplified - assuming append
                val insertedText = newContent.substring(oldContent.length)
                InsertTextCommand(
                    richTextState = richTextEditorHelper.richTextState.value,
                    insertPosition = insertPosition,
                    text = insertedText
                )
            } else {
                // Text was deleted
                val deleteRange = TextRange(newContent.length, oldContent.length)
                val deletedText = oldContent.substring(newContent.length)
                DeleteTextCommand(
                    richTextState = richTextEditorHelper.richTextState.value,
                    range = deleteRange,
                    deletedText = deletedText
                )
            }
            
            viewModelScope.launch {
                undoRedoManager.executeCommand(command)
            }
        }
    }
    
    /**
     * Security: Validates if a file path is safe to access (prevents path traversal attacks).
     * 
     * @param filePath The file path to validate
     * @return True if the path is safe, false otherwise
     */
    private fun isPathSafe(filePath: String): Boolean {
        if (filePath.isBlank()) return true // Empty path is safe
        
        val validationResult = InputValidator.validateFilePath(filePath)
        
        if (!validationResult.isValid) {
            reportSecurityError("Invalid file path detected: ${validationResult.errorMessage}")
            return false
        }
        
        return true
    }
    
    /**
     * Security: Gets the safe recordings directory path.
     * This should be overridden by platform-specific implementations.
     */
    private fun createSafeRecordingsDirectory(): String {
        // Platform-specific implementation needed
        // For now, return a placeholder - this should be implemented in platform modules
        return "/safe/recordings/directory"
    }
    
    /**
     * Security: Reports security errors for monitoring and user feedback.
     * 
     * @param message The security error message
     */
    private fun reportSecurityError(message: String) {
        _securityErrors.value = message
        // Log security incident for monitoring
        println("SECURITY_ALERT: $message")
        
        // Clear error after showing it
        viewModelScope.launch {
            delay(5000) // Show error for 5 seconds
            _securityErrors.value = null
        }
    }
    
    /**
     * Security: Clears any active security error messages.
     */
    fun clearSecurityError() {
        _securityErrors.value = null
    }
    
    /**
     * Cleanup method to cancel pending operations and prevent memory leaks.
     * Should be called when the ViewModel is being cleared.
     */
    override fun onCleared() {
        super.onCleared()
        saveJob?.cancel()
        syncJob?.cancel()
    }
}

/**
 * Data class representing the current formatting state of the rich text editor.
 */
data class RichTextFormattingState(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderlined: Boolean = false,
    val isUnorderedList: Boolean = false,
    val isOrderedList: Boolean = false,
    val currentAlignment: TextAlign = TextAlign.Start,
    val currentHeadingLevel: Int? = null,
    val hasTextColor: Boolean = false,
    val hasHighlight: Boolean = false,
    val indentLevel: Int = 0,
    val hasLink: Boolean = false,
    val isCodeBlock: Boolean = false,
    val isQuoteBlock: Boolean = false
)
