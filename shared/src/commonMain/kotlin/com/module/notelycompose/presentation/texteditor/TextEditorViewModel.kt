package com.module.notelycompose.presentation.texteditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.platform.PlatformAudioPlayer
import com.module.notelycompose.domain.model.Note
import com.module.notelycompose.domain.repository.NoteRepository
import com.module.notelycompose.domain.security.SecurityHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * ViewModel for the text editor screen that is designed to be fully testable.
 * 
 * Key testability improvements:
 * 1. Uses constructor injection for all dependencies
 * 2. Exposes public clearViewModel() method instead of relying on protected onCleared()
 * 3. Uses interface-based dependencies for easy mocking
 * 4. Accepts optional CoroutineScope for testing with TestScope
 * 5. Proper error handling and state management
 */
class TextEditorViewModel(
    private val securityHelper: SecurityHelper,
    private val audioPlayer: PlatformAudioPlayer,
    private val noteRepository: NoteRepository,
    private val coroutineScope: CoroutineScope? = null // Optional for testing
) : ViewModel() {
    
    private val effectiveScope = coroutineScope ?: viewModelScope
    
    private val _uiState = MutableStateFlow(TextEditorUiState())
    val uiState: StateFlow<TextEditorUiState> = _uiState.asStateFlow()
    
    private var longRunningJob: Job? = null
    
    /**
     * Processes user intents/actions for the text editor.
     */
    fun onProcessIntent(intent: TextEditorIntent) {
        when (intent) {
            is TextEditorIntent.UpdateContent -> updateContent(intent.content)
            is TextEditorIntent.SaveNote -> saveNote()
            is TextEditorIntent.PlayAudio -> playAudio(intent.audioPath)
            is TextEditorIntent.LoadNote -> loadNote(intent.noteId)
            is TextEditorIntent.ToggleStar -> toggleStar()
            is TextEditorIntent.StartLongRunningTask -> startLongRunningTask()
            is TextEditorIntent.ClearError -> clearError()
        }
    }
    
    private fun updateContent(content: String) {
        val sanitizedContent = securityHelper.sanitizeHtml(content)
        val isValid = securityHelper.validateInput(sanitizedContent)
        
        _uiState.value = _uiState.value.copy(
            content = sanitizedContent,
            isValid = isValid,
            error = if (!isValid) "Invalid content detected" else null
        )
    }
    
    private fun saveNote() {
        val currentState = _uiState.value
        if (currentState.content.isBlank()) {
            _uiState.value = currentState.copy(error = "Cannot save empty note")
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, error = null)
        
        effectiveScope.launch {
            try {
                val noteId = currentState.noteId?.toLongOrNull() ?: generateNoteId()
                val note = Note(
                    id = noteId,
                    title = extractTitle(currentState.content),
                    content = currentState.content,
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    isStarred = currentState.isStarred
                )
                
                if (currentState.noteId != null) {
                    // Update existing note
                    noteRepository.updateNote(note)
                } else {
                    // Insert new note
                    noteRepository.insertNote(note)
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true,
                    noteId = note.id.toString()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to save note: ${e.message}"
                )
            }
        }
    }
    
    private fun playAudio(audioPath: String) {
        effectiveScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isPlayingAudio = true)
                audioPlayer.prepare(audioPath)
                audioPlayer.play()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPlayingAudio = false,
                    error = "Failed to play audio: ${e.message}"
                )
            }
        }
    }
    
    private fun loadNote(noteId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        effectiveScope.launch {
            try {
                val noteIdLong = noteId.toLongOrNull()
                if (noteIdLong == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Invalid note ID format"
                    )
                    return@launch
                }
                
                val note = noteRepository.getNoteById(noteIdLong)
                if (note != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        noteId = note.id.toString(),
                        content = note.content,
                        isStarred = note.isStarred,
                        isSaved = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Note not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load note: ${e.message}"
                )
            }
        }
    }
    
    private fun toggleStar() {
        _uiState.value = _uiState.value.copy(
            isStarred = !_uiState.value.isStarred
        )
    }
    
    private fun startLongRunningTask() {
        longRunningJob = effectiveScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Simulate long-running task
                delay(5000)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Long running task failed: ${e.message}"
                )
            }
        }
    }
    
    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Public method to clear the ViewModel, replacing the need to access protected onCleared().
     * This method should be called from tests to properly simulate ViewModel lifecycle.
     */
    fun clearViewModel() {
        longRunningJob?.cancel()
        audioPlayer.release()
        // Call the actual onCleared() if needed
        onCleared()
    }
    
    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
        longRunningJob?.cancel()
    }
    
    private fun extractTitle(content: String): String {
        return content.lines()
            .firstOrNull { it.isNotBlank() }
            ?.take(50)
            ?: "Untitled Note"
    }
    
    private fun generateNoteId(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }
}

/**
 * UI state for the text editor screen.
 */
data class TextEditorUiState(
    val content: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isStarred: Boolean = false,
    val isPlayingAudio: Boolean = false,
    val isValid: Boolean = true,
    val error: String? = null,
    val noteId: String? = null
)

/**
 * User intents for the text editor.
 */
sealed class TextEditorIntent {
    data class UpdateContent(val content: String) : TextEditorIntent()
    data class LoadNote(val noteId: String) : TextEditorIntent()
    data class PlayAudio(val audioPath: String) : TextEditorIntent()
    data object SaveNote : TextEditorIntent()
    data object ToggleStar : TextEditorIntent()
    data object StartLongRunningTask : TextEditorIntent()
    data object ClearError : TextEditorIntent()
}