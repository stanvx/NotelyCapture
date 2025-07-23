package com.module.notelycompose.notes.presentation.detail

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.utils.deleteFile
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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
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
    private val richTextEditorHelper: RichTextEditorHelper
) : ViewModel() {

    private val _editorPresentationState = MutableStateFlow(EditorPresentationState())
    val editorPresentationState: StateFlow<EditorPresentationState> = _editorPresentationState
    private var _currentNoteId = MutableStateFlow<Long?>(ID_NOT_SET)

    internal val currentNoteId: StateFlow<Long?> = _currentNoteId.asStateFlow()
    private val _noteIdTrigger = MutableStateFlow<Long?>(null)
    
    // Expose rich text state for UI components
    val richTextState: StateFlow<com.mohamedrejeb.richeditor.model.RichTextState> = richTextEditorHelper.richTextState

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

    fun onGetNoteById(id: String) {
        _noteIdTrigger.value = id.toLong()
    }

    private fun getLastNote() = getLastNoteUseCase.execute()

    fun onUpdateContent(newContent: TextFieldValue) {
        updateContent(newContent)
        // Sync to rich text state
        syncContentToRichText(newContent.text)
        createOrUpdateEvent(
            title = newContent.text,
            content = newContent.text,
            starred = _editorPresentationState.value.starred,
            formatting = _editorPresentationState.value.formats,
            textAlign = _editorPresentationState.value.textAlign,
            recordingPath = _editorPresentationState.value.recording.recordingPath,
        )
    }
    
    /**
     * Handles content updates from the RichTextEditor.
     * This method processes changes from the rich text editor and synchronizes
     * them with the existing text formatting system.
     */
    fun onUpdateRichContent() {
        syncContentFromRichText()
        val currentState = _editorPresentationState.value
        createOrUpdateEvent(
            title = currentState.content.text,
            content = currentState.content.text,
            starred = currentState.starred,
            formatting = currentState.formats,
            textAlign = currentState.textAlign,
            recordingPath = currentState.recording.recordingPath,
        )
    }

    fun onUpdateRecordingPath(recordingPath: String) {
        _editorPresentationState.update {
            it.copy(
                recording = recordingPath(recordingPath)
            )
        }
        onUpdateContent(newContent = _editorPresentationState.value.content)
    }

    fun onDeleteRecord() {
        deleteFile(_editorPresentationState.value.recording.recordingPath)
        _editorPresentationState.update {
            it.copy(
                recording = recordingPath(/*reset record path */"")
            )
        }
        onUpdateContent(newContent = _editorPresentationState.value.content)
    }

    private fun recordingPath(recordingPath: String) = RecordingPathPresentationModel(
        recordingPath = recordingPath,
        isRecordingExist = recordingPath.isNotEmpty()
    )

    private fun loadNote(
        content: String,
        formats: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String,
        starred: Boolean,
        createdAt: String
    ) {
        _editorPresentationState.update {
            it.copy(
                content = TextFieldValue(content),
                formats = formats,
                textAlign = textAlign,
                recording = recordingPath(recordingPath),
                starred = starred,
                createdAt = createdAt
            )
        }
        
        // Synchronize content to rich text state
        syncContentToRichText(content)
    }
    
    /**
     * Synchronizes content from plain text to RichTextState.
     * This ensures both text systems are kept in sync when loading notes.
     */
    private fun syncContentToRichText(content: String) {
        richTextEditorHelper.setContent(content)
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
            currentAlignment = richTextEditorHelper.getCurrentAlignment()
        )
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
