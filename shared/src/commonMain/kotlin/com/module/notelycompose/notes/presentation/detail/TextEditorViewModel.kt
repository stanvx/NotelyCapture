package com.module.notelycompose.notes.presentation.detail

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.core.constants.AppConstants
import com.module.notelycompose.core.debugPrintln
import com.module.notelycompose.core.security.SecurityHelper
import com.module.notelycompose.notes.domain.DeleteNoteById
import com.module.notelycompose.notes.domain.GetLastNote
import com.module.notelycompose.notes.domain.GetNoteById
import com.module.notelycompose.notes.domain.InsertNoteUseCase
import com.module.notelycompose.notes.domain.UpdateNoteUseCase
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private const val ID_NOT_SET = 0L

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
    private val securityHelper: SecurityHelper,
    private val audioPlayer: com.module.notelycompose.platform.PlatformAudioPlayer
) : ViewModel() {

    private val _editorPresentationState = MutableStateFlow(EditorPresentationState())
    val editorPresentationState: StateFlow<EditorPresentationState> = _editorPresentationState
    private var _currentNoteId = MutableStateFlow<Long?>(ID_NOT_SET)

    internal val currentNoteId: StateFlow<Long?> = _currentNoteId.asStateFlow()
    private val _noteIdTrigger = MutableStateFlow<Long?>(null)
    
    
    // Performance optimization fields
    private var saveJob: Job? = null
    private var syncJob: Job? = null
    private var lastContentHash: Int = 0
    
    // Thread-safety for save operations and content synchronization
    private val saveMutex = Mutex()
    private val contentSyncMutex = Mutex()
    
    // Immutable content snapshot for atomic operations (protected by contentSyncMutex)
    private var _contentSnapshot: ContentSnapshot? = null
    
    /**
     * Immutable snapshot of content state for thread-safe operations.
     */
    private data class ContentSnapshot(
        val plainText: String,
        val htmlContent: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    
    // Expose rich text state for UI components
    val richTextState: StateFlow<com.mohamedrejeb.richeditor.model.RichTextState> = richTextEditorHelper.richTextState
    
    
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
        viewModelScope.launch {
            // SECURITY: Validate content input using SecurityHelper
            if (!securityHelper.validateNoteContent(newContent.text)) {
                return@launch
            }
            
            contentSyncMutex.withLock {
                val oldContent = _editorPresentationState.value.content.text
                val sanitizedContent = newContent
                
                updateContent(sanitizedContent)
                
                if (oldContent != sanitizedContent.text) {
                    syncContentToRichText(sanitizedContent.text)
                    
                    
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
        }
    }
    
    
    /**
     * Handles content updates from the RichTextEditor with thread-safe synchronization.
     * This method processes changes from the rich text editor and synchronizes
     * them with the existing text formatting system using atomic operations.
     */
    fun onUpdateRichContent() {
        viewModelScope.launch {
            contentSyncMutex.withLock {
                val oldContent = _editorPresentationState.value.content.text
                syncContentFromRichText()
                val currentState = _editorPresentationState.value
                val snapshot = _contentSnapshot
                
                // Only save if content actually changed and we have a valid snapshot
                if (snapshot != null && (oldContent != currentState.content.text || snapshot.htmlContent.isNotEmpty())) {
                    // Use HTML content for persistence when available, fallback to plain text
                    val contentToSave = snapshot.htmlContent.ifEmpty { snapshot.plainText }
                    
                    // Use first line or first 50 chars as title
                    val titleToSave = snapshot.plainText.lines().firstOrNull()?.take(50) 
                        ?: snapshot.plainText.take(50)
                    
                    debouncedSave(
                        title = titleToSave,
                        content = contentToSave, // Save HTML content for rich formatting
                        starred = currentState.starred,
                        formatting = currentState.formats,
                        textAlign = currentState.textAlign,
                        recordingPath = currentState.recording.recordingPath,
                    )
                }
            }
        }
    }

    fun onUpdateRecordingPath(recordingPath: String) {
        viewModelScope.launch {
            // SECURITY: Validate recording path before updating using SecurityHelper
            if (recordingPath.isNotEmpty() && !securityHelper.isPathSafe(recordingPath)) {
                return@launch
            }
            
            val recordingModel = recordingPath(recordingPath)
            _editorPresentationState.update {
                it.copy(recording = recordingModel)
            }
            onUpdateContent(newContent = _editorPresentationState.value.content)
        }
    }

    fun onDeleteRecord() {
        val recordingPath = _editorPresentationState.value.recording.recordingPath
        
        viewModelScope.launch {
            // Use SecurityHelper for secure file deletion
            val deleteResult = securityHelper.secureDeleteFile(recordingPath)
            
            if (!deleteResult.success) {
                // Log warnings but continue with UI update
                deleteResult.securityError?.let { println("Security warning: $it") }
                deleteResult.fileError?.let { println("File deletion warning: $it") }
            }
            
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
            // SECURITY: Validate path before accessing audio file using SecurityHelper
            if (!securityHelper.isPathSafe(recordingPath)) {
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
        
        // Determine if content is HTML (simple heuristic check)
        val isHtmlContent = content.contains("<") && content.contains(">")
        
        // For display in the UI, extract plain text if HTML content
        val displayContent = if (isHtmlContent) {
            // Extract plain text from HTML for backward compatibility
            content.replace(Regex("<[^>]+>"), "").trim()
        } else {
            content
        }
        
        _editorPresentationState.update {
            it.copy(
                content = TextFieldValue(displayContent),
                formats = formats,
                textAlign = textAlign,
                recording = recordingModel,
                starred = starred,
                createdAt = createdAt
            )
        }
        
        // Synchronize content to rich text state - use original content which may be HTML
        syncContentToRichText(content)
        
        // Create content snapshot if we have HTML content
        if (isHtmlContent) {
            val snapshot = ContentSnapshot(
                plainText = displayContent,
                htmlContent = content
            )
            _contentSnapshot = snapshot
        }
    }
    
    /**
     * Synchronizes content from plain text to RichTextState with debouncing and error handling.
     * This ensures both text systems are kept in sync when loading notes.
     * Uses content equality instead of hash comparison to prevent missed updates.
     */
    private fun syncContentToRichText(content: String) {
        // Cancel previous sync job if still pending
        syncJob?.cancel()
        
        // Use content equality instead of hash comparison to prevent collisions
        if (content == lastSetContent) return
        
        syncJob = viewModelScope.launch {
            delay(AppConstants.Editor.SYNC_DEBOUNCE_DELAY)
            
            try {
                richTextEditorHelper.setContent(content)
                lastSetContent = content // Update after successful sync
            } catch (e: Exception) {
                // Log sync failure but don't crash
                println("Failed to sync content to rich text: ${e.message}")
            }
        }
    }
    
    // Track the last successfully synced content
    private var lastSetContent: String = ""
    
    /**
     * Thread-safe debounced save operation with atomic operations to prevent race conditions.
     * Uses mutex to ensure only one save operation can execute at a time.
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
            delay(AppConstants.Editor.SAVE_DEBOUNCE_DELAY)
            
            // Use mutex to ensure atomic save operations and prevent race conditions
            saveMutex.withLock {
                val currentNoteId = _currentNoteId.value
                when {
                    currentNoteId != null && currentNoteId != ID_NOT_SET -> {
                        try {
                            updateNote(
                                noteId = currentNoteId,
                                title = title,
                                content = content,
                                starred = starred,
                                formatting = formatting,
                                textAlign = textAlign,
                                recordingPath = recordingPath
                            )
                        } catch (e: Exception) {
                            println("Failed to update note $currentNoteId: ${e.message}")
                        }
                    }
                    else -> {
                        // Double-check to prevent duplicate note creation
                        val recentNoteId = _currentNoteId.value
                        if (recentNoteId == null || recentNoteId == ID_NOT_SET) {
                            try {
                                insertNoteUseCase.execute(
                                    title = title,
                                    content = content,
                                    starred = starred,
                                    formatting = formatting.map { textFormatPresentationMapper.mapToDomainModel(it) },
                                    textAlign = textAlignPresentationMapper.mapToDomainModel(textAlign),
                                    recordingPath = recordingPath
                                )
                                // Generate a temporary ID since the use case doesn't return one
                                val newNoteId = Clock.System.now().toEpochMilliseconds()
                                _currentNoteId.value = newNoteId
                            } catch (e: Exception) {
                                println("Failed to create new note: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Synchronizes content from RichTextState back to TextFieldValue using immutable snapshots.
     * This is used when the rich text editor content changes.
     * Creates atomic content snapshots to prevent race conditions during save operations.
     */
    private fun syncContentFromRichText() {
        val richTextHtmlContent = richTextEditorHelper.getContent() // Get HTML for persistence
        val richTextPlainContent = richTextEditorHelper.getPlainText() // Get plain text for display
        val currentState = _editorPresentationState.value
        
        // Create immutable snapshot for thread-safe operations
        val snapshot = ContentSnapshot(
            plainText = richTextPlainContent,
            htmlContent = richTextHtmlContent
        )
        _contentSnapshot = snapshot
        
        // Update the presentation state with plain text for backward compatibility
        if (currentState.content.text != richTextPlainContent) {
            _editorPresentationState.update {
                it.copy(content = TextFieldValue(richTextPlainContent))
            }
        }
    }

    fun onGetUiState(presentationState: EditorPresentationState): EditorUiState {
        return editorPresentationToUiStateMapper.mapToUiState(presentationState)
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
            
            viewModelScope.launch {
                // Use SecurityHelper for secure file deletion
                val deleteResult = securityHelper.secureDeleteFile(path)
                
                if (!deleteResult.success) {
                    // Log warnings but continue with note deletion
                    deleteResult.securityError?.let { println("Security warning: $it") }
                    deleteResult.fileError?.let { println("File deletion warning: $it") }
                }
                
                deleteNote(id = noteId)
            }
        }
    }

    private fun deleteNote(id: Long) {
        viewModelScope.launch {
            try {
                // Execute database operation on IO dispatcher
                launch(Dispatchers.IO) {
                    deleteNoteUseCase.execute(id)
                }
                
                // Success - log for debugging if needed
                debugPrintln { "Successfully deleted note with id: $id" }
                
            } catch (e: Exception) {
                // Handle database deletion errors gracefully
                println("Database error: Failed to delete note with id $id: ${e.message}")
                
                // Consider showing user-facing error in the future
                // For now, we log the error to prevent crashes
                // The UI will remain in current state, user can retry
                debugPrintln { "Delete operation failed for note $id. Exception: ${e::class.simpleName}" }
            }
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
        return createdAt.toInstant(TimeZone.currentSystemDefault()).toString()
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

    fun onToggleItalic() {
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

    fun setTextSize(size: Float) {
        textEditorHelper.toggleFormat(
            currentState = _editorPresentationState.value,
            transform = { it.copy(textSize = size) },
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
        refreshSelection()
    }

    fun onToggleUnderline() {
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

    private fun refreshSelection() {
        textEditorHelper.refreshSelection(
            currentState = _editorPresentationState.value,
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
    }

    fun onSetAlignment(alignment: TextAlign) {
        _editorPresentationState.update { it.copy(textAlign = alignment) }
        // Apply to rich text state as well
        richTextEditorHelper.setAlignment(alignment)
        val content = _editorPresentationState.value.content
        val formats = _editorPresentationState.value.formats
        val textAlign = _editorPresentationState.value.textAlign
        val starred = _editorPresentationState.value.starred
        val recordingPath = _editorPresentationState.value.recording.recordingPath
        if (content.text.isNotEmpty()) {
            debouncedSave(
                title = content.text,
                content = content.text,
                starred = starred,
                formatting = formats,
                textAlign = textAlign,
                recordingPath = recordingPath
            )
        }
    }

    fun onToggleBulletList() {
        textEditorHelper.toggleBulletList(
            currentState = _editorPresentationState.value,
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
        // Apply to rich text state as well
        richTextEditorHelper.toggleUnorderedList()
    }
    
    /**
     * Toggles ordered list formatting using the RichTextEditor.
     */
    fun onToggleOrderedList() {
        richTextEditorHelper.toggleOrderedList()
        // Sync changes back to traditional state
        onUpdateRichContent()
    }
    
    /**
     * Adds a heading of the specified level using the RichTextEditor.
     * 
     * @param level The heading level (1-6)
     */
    fun onAddHeading(level: Int) {
        richTextEditorHelper.addHeading(level)
        // Sync changes back to traditional state
        onUpdateRichContent()
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
        richTextEditorHelper.clearFormatting()
        // Also clear traditional formatting
        _editorPresentationState.update {
            it.copy(formats = emptyList())
        }
        // Sync changes back
        onUpdateRichContent()
    }
    
    /**
     * Toggles strikethrough formatting on selected text.
     */
    fun onToggleStrikethrough() {
        richTextEditorHelper.toggleStrikethrough()
        refreshSelection()
    }
    
    /**
     * Toggles code block formatting on selected text.
     */
    fun onToggleCodeBlock() {
        richTextEditorHelper.toggleCodeBlock()
        // Sync changes back to traditional state
        onUpdateRichContent()
    }
    
    /**
     * Toggles quote block formatting on selected text.
     */
    fun onToggleQuoteBlock() {
        richTextEditorHelper.toggleQuoteBlock()
        // Sync changes back to traditional state
        onUpdateRichContent()
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
            hasTextColor = richTextEditorHelper.hasTextColor(),
            hasHighlight = richTextEditorHelper.hasHighlight(),
            indentLevel = richTextEditorHelper.getIndentLevel(),
            hasLink = richTextEditorHelper.hasLink(),
            isCodeBlock = richTextEditorHelper.isCodeBlock(),
            isQuoteBlock = richTextEditorHelper.isQuoteBlock()
        )
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
     * Cleanup method to cancel pending operations and prevent memory leaks.
     * Should be called when the ViewModel is being cleared.
     */
    override fun onCleared() {
        super.onCleared()
        saveJob?.cancel()
        syncJob?.cancel()
        _contentSnapshot = null
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
