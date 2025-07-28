package com.module.notelycompose.notes.presentation.detail

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.core.debugPrintln
import com.module.notelycompose.core.security.SecurityHelper
import com.module.notelycompose.notes.domain.DeleteNoteById
import com.module.notelycompose.notes.domain.GetLastNote
import com.module.notelycompose.notes.domain.GetNoteById
import com.module.notelycompose.notes.domain.InsertNoteUseCase
import com.module.notelycompose.notes.domain.UpdateNoteUseCase
import com.module.notelycompose.notes.presentation.detail.model.TextPresentationFormat
import com.module.notelycompose.notes.presentation.mapper.TextAlignPresentationMapper
import com.module.notelycompose.notes.presentation.mapper.TextFormatPresentationMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class NoteDetailScreenViewModel(
    private val getNoteByIdUseCase: GetNoteById,
    private val insertNoteUseCase: InsertNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteById,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val getLastNoteUseCase: GetLastNote,
    private val textFormatPresentationMapper: TextFormatPresentationMapper,
    private val textAlignPresentationMapper: TextAlignPresentationMapper
):ViewModel() {

    // User feedback state for delete operations
    private val _deleteOperationState = MutableStateFlow<DeleteOperationState>(DeleteOperationState.Idle)
    val deleteOperationState: StateFlow<DeleteOperationState> = _deleteOperationState.asStateFlow()

    /**
     * Represents the state of delete operations for user feedback
     */
    sealed class DeleteOperationState {
        object Idle : DeleteOperationState()
        object InProgress : DeleteOperationState()
        object Success : DeleteOperationState()
        data class Error(val message: String) : DeleteOperationState()
    }

    private fun getNoteById(id: String) = getNoteByIdUseCase.execute(id.toLong())

    private fun getLastNote() = getLastNoteUseCase.execute()

    fun onCreateOrUpdateEvent(
        newContent: TextFieldValue,
        oldContentText: String,
        oldFormats: List<TextPresentationFormat>,
        isUpdate: Boolean,
        textAlign: TextAlign
    ) {
        val updatedFormats = updateFormats(
            formats = oldFormats,
            oldText = oldContentText,
            newText = newContent.text,
            changePosition = newContent.selection.start
        )
        createOrUpdateEvent(
            title = newContent.text,
            content = newContent.text,
            isUpdate = isUpdate,
            formatting = updatedFormats,
            textAlign = textAlign
        )
    }

    private fun createOrUpdateEvent(
        title: String,
        content: String,
        isUpdate: Boolean,
        formatting: List<TextPresentationFormat>,
        textAlign: TextAlign
    ) {
        when {
            content.isEmpty() && isUpdate -> {
                val lastNoteId = getLastNote()?.id ?: 0L
                onEvent(NoteDetailScreenEvent.ClearNoteOnEmptyContent(lastNoteId.toString()))
            }
            isUpdate -> {
                val lastNoteId = getLastNote()?.id ?: 0L
                onEvent(NoteDetailScreenEvent
                    .UpdateNote(lastNoteId, title, content, formatting, textAlign)
                )
            }
            else -> onEvent(NoteDetailScreenEvent
                .NoteSaved(title, content, formatting, textAlign)
            )
        }
    }

    fun onEvent(event: NoteDetailScreenEvent) {
        when (event) {
            is NoteDetailScreenEvent.NoteSaved -> {
                insertNote(
                    title = event.title,
                    content = event.content,
                    starred = true,
                    formatting = event.formatting,
                    textAlign = event.textAlign,
                    recordingPath = ""
                )
            }
            is NoteDetailScreenEvent.DeleteNote -> {
                deleteNote(event.id.toLong())
            }
            is NoteDetailScreenEvent.UpdateNote -> {
                updateNote(
                    noteId = event.id,
                    title = event.title,
                    content = event.content,
                    starred = true,
                    formatting = event.formatting,
                    textAlign = event.textAlign
                )
            }
            is NoteDetailScreenEvent.ClearNoteOnEmptyContent -> {
                deleteNote(event.id.toLong())
            }
        }
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
            insertNoteUseCase.execute(
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
        textAlign: TextAlign
    ) {
        viewModelScope.launch {
            updateNoteUseCase.execute(
                id = noteId,
                title = title,
                content = content,
                starred = starred,
                formatting = formatting.map { textFormatPresentationMapper.mapToDomainModel(it) },
                textAlign = textAlignPresentationMapper.mapToDomainModel(textAlign),
                recordingPath = ""
            )
        }
    }

    private fun deleteNote(id: Long) {
        viewModelScope.launch {
            _deleteOperationState.value = DeleteOperationState.InProgress
            
            try {
                // First, retrieve the note to get the recording path
                val note = getNoteByIdUseCase.execute(id)
                val recordingPath = note?.recordingPath
                
                // Clean up audio file if it exists using SecurityHelper
                if (!recordingPath.isNullOrEmpty()) {
                    val fileDeleteResult = SecurityHelper.secureDeleteFile(recordingPath)
                    
                    if (!fileDeleteResult.success) {
                        // Log the issue but continue with note deletion to avoid orphaned DB records
                        if (fileDeleteResult.securityError != null) {
                            println("Security warning: ${fileDeleteResult.securityError}")
                        }
                        if (fileDeleteResult.fileError != null) {
                            println("File deletion warning: ${fileDeleteResult.fileError}")
                        }
                    }
                }
                
                // Delete the database record on IO dispatcher
                launch(Dispatchers.IO) {
                    deleteNoteUseCase.execute(id)
                }
                
                // Success feedback
                _deleteOperationState.value = DeleteOperationState.Success
                debugPrintln { "Successfully deleted note with id: $id" }
                
                // Reset state after showing success
                launch {
                    kotlinx.coroutines.delay(2000)
                    _deleteOperationState.value = DeleteOperationState.Idle
                }
                
            } catch (e: Exception) {
                // Handle database deletion errors with user feedback
                val errorMessage = "Failed to delete note: ${e.message}"
                _deleteOperationState.value = DeleteOperationState.Error(errorMessage)
                
                println("Database error: Failed to delete note with id $id: ${e.message}")
                debugPrintln { "Delete operation failed for note $id. Exception: ${e::class.simpleName}" }
                
                // Reset error state after showing it
                launch {
                    kotlinx.coroutines.delay(5000)
                    _deleteOperationState.value = DeleteOperationState.Idle
                }
            }
        }
    }
    
    /**
     * Clears any active delete operation state (for UI reset)
     */
    fun clearDeleteOperationState() {
        _deleteOperationState.value = DeleteOperationState.Idle
    }

    fun getNewNoteContentDate(id: String): String {
        val note = getNoteById(id)
        val localDate = note?.createdAt ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val day = localDate.dayOfMonth
        val month = localDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
        val year = localDate.year
        val hour = localDate.hour
        val minute = localDate.minute.toString().padStart(2, '0')
        return "$day $month $year at $hour:$minute"
    }

    private fun updateFormats(
        formats: List<TextPresentationFormat>,
        oldText: String,
        newText: String,
        changePosition: Int
    ): List<TextPresentationFormat> {
        val lengthDiff = newText.length - oldText.length
        return formats.mapNotNull { format ->
            when {
                changePosition <= format.range.first -> {
                    val newStart = (format.range.first + lengthDiff).coerceAtLeast(0)
                    val newEnd = (format.range.last + lengthDiff).coerceAtLeast(newStart)
                    if (newStart < newEnd) {
                        format.copy(range = newStart..newEnd)
                    } else null
                }
                changePosition < format.range.last -> {
                    val newEnd = (format.range.last + lengthDiff).coerceAtLeast(format.range.first)
                    format.copy(range = format.range.first..newEnd)
                }
                else -> format
            }
        }
    }
}