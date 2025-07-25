package com.module.notelycompose.transcription

import androidx.lifecycle.viewModelScope
import com.module.notelycompose.core.debugPrintln
import com.module.notelycompose.core.validation.AudioFileValidator
import com.module.notelycompose.notes.domain.InsertNoteUseCase
import com.module.notelycompose.notes.domain.model.TextAlignDomainModel
import com.module.notelycompose.transcription.error.TranscriptionError
import com.module.notelycompose.transcription.error.isRecoverable
import com.module.notelycompose.transcription.error.userMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Background transcription service that wraps TranscriptionViewModel
 * for quick record functionality. Handles automatic transcription and note creation.
 * 
 * Implements proper resource management with lifecycle awareness.
 */
class BackgroundTranscriptionService(
    private val transcriptionViewModel: TranscriptionViewModel,
    private val insertNoteUseCase: InsertNoteUseCase
) {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val _state = MutableStateFlow<BackgroundTranscriptionState>(BackgroundTranscriptionState.Idle)
    val state: StateFlow<BackgroundTranscriptionState> = _state
    
    private var isDisposed = false
    
    /**
     * Start background transcription for a recorded audio file
     * @param audioFilePath Path to the recorded audio file
     * @param onComplete Callback invoked when transcription and note creation complete
     * @param onError Callback invoked if an error occurs
     */
    fun startTranscription(
        audioFilePath: String,
        onComplete: (noteId: Long) -> Unit = {},
        onError: (error: TranscriptionError) -> Unit = {}
    ) {
        if (isDisposed) {
            debugPrintln { "BackgroundTranscriptionService: Cannot start transcription - service is disposed" }
            val error = TranscriptionError.ServiceDisposedError()
            _state.value = BackgroundTranscriptionState.Error(error)
            onError(error)
            return
        }
        
        debugPrintln { "BackgroundTranscriptionService: Starting transcription for ${AudioFileValidator.getSecureFileName(audioFilePath)}" }
        
        // Validate audio file before processing
        AudioFileValidator.validateAudioFile(audioFilePath).onFailure { error ->
            val transcriptionError = error as? TranscriptionError ?: TranscriptionError.AudioFileValidationError(
                message = "Audio file validation failed: ${error.message}",
                filePath = audioFilePath
            )
            debugPrintln { "BackgroundTranscriptionService: File validation failed: ${transcriptionError.message}" }
            _state.value = BackgroundTranscriptionState.Error(transcriptionError)
            onError(transcriptionError)
            return
        }
        
        _state.value = BackgroundTranscriptionState.Processing
        
        serviceScope.launch {
            var recognizerInitialized = false
            var cleanupCompleted = false
            
            try {
                // Initialize the transcription ViewModel and wait for completion
                try {
                    transcriptionViewModel.initRecognizer()
                    recognizerInitialized = true
                    debugPrintln { "BackgroundTranscriptionService: Model initialization completed, starting transcription" }
                } catch (initError: Exception) {
                    val error = TranscriptionError.InitializationError(
                        message = "Failed to initialize transcription: ${initError.message}",
                        cause = initError
                    )
                    debugPrintln { "BackgroundTranscriptionService: Initialization failed: ${error.message}" }
                    _state.value = BackgroundTranscriptionState.Error(error)
                    withContext(Dispatchers.Main) {
                        onError(error)
                    }
                    return@launch
                }
                
                // Start transcription - model is now guaranteed to be ready
                transcriptionViewModel.startRecognizer(audioFilePath)
                
                // Monitor transcription progress and wait for completion
                transcriptionViewModel.uiState.collect { uiState ->
                    if (!uiState.inTranscription && !cleanupCompleted && uiState.originalText.isNotEmpty()) {
                        cleanupCompleted = true
                        
                        // Create note with transcribed content
                        val noteId = try {
                            createNoteFromTranscription(
                                transcribedText = uiState.originalText,
                                audioFilePath = audioFilePath
                            )
                        } catch (noteError: Exception) {
                            val error = TranscriptionError.NoteCreationError(
                                message = "Failed to create note: ${noteError.message}",
                                cause = noteError
                            )
                            debugPrintln { "BackgroundTranscriptionService: Note creation failed: ${error.message}" }
                            _state.value = BackgroundTranscriptionState.Error(error)
                            withContext(Dispatchers.Main) {
                                onError(error)
                            }
                            return@collect
                        }
                        
                        // Clean up transcription state
                        transcriptionViewModel.finishRecognizer()
                        debugPrintln { "BackgroundTranscriptionService: Cleanup completed in success path" }
                        
                        _state.value = BackgroundTranscriptionState.Complete
                        
                        // Switch to main thread before invoking callback to ensure UI updates are safe
                        withContext(Dispatchers.Main) {
                            onComplete(noteId)
                        }
                        
                        // Cancel the collection since we're done
                        return@collect
                    }
                }
                
            } catch (error: Exception) {
                val transcriptionError = when (error) {
                    is TranscriptionError -> error
                    else -> TranscriptionError.UnknownError(
                        message = "Unknown transcription error: ${error.message}",
                        cause = error
                    )
                }
                debugPrintln { "BackgroundTranscriptionService: Error during transcription: ${transcriptionError.message}" }
                _state.value = BackgroundTranscriptionState.Error(transcriptionError)
                
                // Switch to main thread before invoking callback to ensure UI updates are safe
                withContext(Dispatchers.Main) {
                    onError(transcriptionError)
                }
            } finally {
                // Ensure cleanup happens only if not already done
                try {
                    if (recognizerInitialized && !cleanupCompleted) {
                        cleanupCompleted = true
                        debugPrintln { "BackgroundTranscriptionService: Performing cleanup in finally block" }
                        transcriptionViewModel.finishRecognizer()
                    } else if (cleanupCompleted) {
                        debugPrintln { "BackgroundTranscriptionService: Cleanup already completed, skipping finally block cleanup" }
                    }
                } catch (cleanupError: Exception) {
                    debugPrintln { "BackgroundTranscriptionService: Error during cleanup: ${cleanupError.message}" }
                }
            }
        }
    }
    
    /**
     * Create a new note with the transcribed content and empty title for smart UI-based naming
     */
    private suspend fun createNoteFromTranscription(
        transcribedText: String,
        audioFilePath: String
    ): Long {
        // Use empty title to leverage existing UI smart title generation from content
        val title = ""
        
        debugPrintln { "BackgroundTranscriptionService: Creating note with empty title for smart naming" }
        
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
        if (!isDisposed) {
            _state.value = BackgroundTranscriptionState.Idle
        }
    }
    
    /**
     * Dispose of the service and cancel all ongoing operations.
     * This should be called when the service is no longer needed to prevent memory leaks.
     */
    fun dispose() {
        if (isDisposed) {
            debugPrintln { "BackgroundTranscriptionService: Already disposed" }
            return
        }
        
        debugPrintln { "BackgroundTranscriptionService: Disposing service" }
        isDisposed = true
        
        // Cancel all ongoing coroutines
        serviceScope.cancel()
        
        // Reset state to idle
        _state.value = BackgroundTranscriptionState.Idle
    }
    
    /**
     * Check if the service has been disposed
     */
    val disposed: Boolean get() = isDisposed
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
    data class Error(val error: TranscriptionError) : BackgroundTranscriptionState() {
        /**
         * Legacy message property for backward compatibility
         */
        val message: String get() = error.userMessage
        
        /**
         * Check if this error is recoverable
         */
        val isRecoverable: Boolean get() = error.isRecoverable
    }
}