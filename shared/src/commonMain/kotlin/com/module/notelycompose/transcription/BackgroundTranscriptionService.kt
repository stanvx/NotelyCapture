package com.module.notelycompose.transcription

import androidx.lifecycle.viewModelScope
import com.module.notelycompose.core.debugPrintln
import com.module.notelycompose.notes.domain.InsertNoteUseCase
import com.module.notelycompose.notes.domain.model.TextAlignDomainModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Background transcription service that wraps TranscriptionViewModel
 * for quick record functionality. Handles automatic transcription and note creation.
 */
class BackgroundTranscriptionService(
    private val transcriptionViewModel: TranscriptionViewModel,
    private val insertNoteUseCase: InsertNoteUseCase
) {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val _state = MutableStateFlow<BackgroundTranscriptionState>(BackgroundTranscriptionState.Idle)
    val state: StateFlow<BackgroundTranscriptionState> = _state
    
    /**
     * Start background transcription for a recorded audio file
     * @param audioFilePath Path to the recorded audio file
     * @param onComplete Callback invoked when transcription and note creation complete
     * @param onError Callback invoked if an error occurs
     */
    fun startTranscription(
        audioFilePath: String,
        onComplete: (noteId: Long) -> Unit = {},
        onError: (error: String) -> Unit = {}
    ) {
        debugPrintln { "BackgroundTranscriptionService: Starting transcription for $audioFilePath" }
        
        _state.value = BackgroundTranscriptionState.Processing
        
        serviceScope.launch {
            try {
                // Initialize the transcription ViewModel
                transcriptionViewModel.initRecognizer()
                
                // Monitor transcription progress
                var transcriptionCompleted = false
                val transcriptionJob = launch {
                    transcriptionViewModel.uiState.collect { uiState ->
                        if (!uiState.inTranscription && !transcriptionCompleted && uiState.originalText.isNotEmpty()) {
                            transcriptionCompleted = true
                            
                            // Create note with transcribed content
                            val noteId = createNoteFromTranscription(
                                transcribedText = uiState.originalText,
                                audioFilePath = audioFilePath
                            )
                            
                            // Clean up transcription state
                            transcriptionViewModel.finishRecognizer()
                            
                            _state.value = BackgroundTranscriptionState.Complete
                            onComplete(noteId)
                        }
                    }
                }
                
                // Start transcription
                transcriptionViewModel.startRecognizer(audioFilePath)
                
            } catch (error: Exception) {
                debugPrintln { "BackgroundTranscriptionService: Error during transcription: ${error.message}" }
                _state.value = BackgroundTranscriptionState.Error(error.message ?: "Unknown transcription error")
                onError(error.message ?: "Unknown transcription error")
            }
        }
    }
    
    /**
     * Create a new note with the transcribed content and timestamp-based title
     */
    private suspend fun createNoteFromTranscription(
        transcribedText: String,
        audioFilePath: String
    ): Long {
        val timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val title = "Quick Record ${timestamp.date} ${timestamp.hour}:${timestamp.minute.toString().padStart(2, '0')}"
        
        debugPrintln { "BackgroundTranscriptionService: Creating note with title: $title" }
        
        return insertNoteUseCase.execute(
            title = title,
            content = transcribedText,
            starred = false,
            formatting = emptyList(), // No special formatting for quick records
            textAlign = TextAlignDomainModel.Left,
            recordingPath = audioFilePath
        ) ?: throw Exception("Failed to create note")
    }
    
    /**
     * Reset the service state to idle
     */
    fun reset() {
        _state.value = BackgroundTranscriptionState.Idle
    }
}

/**
 * Represents the state of background transcription processing
 */
sealed class BackgroundTranscriptionState {
    /**
     * No transcription in progress
     */
    data object Idle : BackgroundTranscriptionState()
    
    /**
     * Transcription and note creation in progress
     */
    data object Processing : BackgroundTranscriptionState()
    
    /**
     * Transcription and note creation completed successfully
     */
    data object Complete : BackgroundTranscriptionState()
    
    /**
     * An error occurred during transcription or note creation
     */
    data class Error(val message: String) : BackgroundTranscriptionState()
}