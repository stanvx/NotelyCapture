package com.module.notelycompose.transcription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.core.debugPrintln
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.summary.Text2Summary
import com.module.notelycompose.transcription.domain.repository.TranscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

const val SPACE_STR = " "

class TranscriptionViewModel(
    private val transcriptionRepository: TranscriptionRepository,
    private val preferencesRepository: PreferencesRepository
) :ViewModel(){
    private val _uiState = MutableStateFlow(TranscriptionUiState())
    val uiState: StateFlow<TranscriptionUiState> = _uiState
    
    private val initMutex = Mutex()
    private var recognizerReady = false

    fun requestAudioPermission() {
        viewModelScope.launch {
            transcriptionRepository.requestRecordingPermission()
        }
    }

    suspend fun initRecognizer() {
        if (recognizerReady) return // Fast-path if already initialized
        
        initMutex.withLock {
            if (recognizerReady) return // Double-checked locking
            
            debugPrintln { "speech: initialize model" }
            try {
                val initResult = withTimeoutOrNull(30.seconds) {
                    transcriptionRepository.initialize()
                }
                
                if (initResult == null) {
                    throw Exception("Model initialization timed out after 30 seconds")
                }
                
                recognizerReady = true
                debugPrintln { "TranscriptionViewModel: Model initialization completed" }
            } catch (e: Exception) {
                debugPrintln { "TranscriptionViewModel: Model initialization failed: ${e.message}" }
                throw e // Re-throw to let caller handle the error
            }
        }
    }


    fun startRecognizer(filePath: String) {
        debugPrintln{"startRecognizer ========================="}
        viewModelScope.launch {
            if (transcriptionRepository.hasRecordingPermission()) {
                _uiState.update { current ->
                    current.copy(inTranscription = true)
                }
                transcriptionRepository.start(
                    filePath, preferencesRepository.getDefaultTranscriptionLanguage().first(), onProgress = { progress ->
                        debugPrintln{"progress ========================= $progress"}
                        _uiState.update { current ->
                            current.copy(
                                progress = progress
                            )
                        }
                    }, onNewSegment = { _, _, text ->
                        
                        val delimiter = if(_uiState.value.originalText.endsWith(".")) "\n\n" else SPACE_STR
                        debugPrintln{"\n text ========================= $text"}
                        _uiState.update { current ->
                            current.copy(
                                originalText = "${_uiState.value.originalText}$delimiter${text.trim()}".trim(),
                                partialText = text
                            )
                        }

                    },
                    onComplete = {
                        debugPrintln{"\n completed ========================= "}
                        _uiState.update {current ->
                            current.copy(
                                inTranscription = false
                            )
                        }
                    })
            }
        }

    }

    fun stopRecognizer() {
        _uiState.update { current ->
            current.copy(inTranscription = false)
        }
        viewModelScope.launch {
            transcriptionRepository.stop()
        }

    }

    fun finishRecognizer() {
        _uiState.update { current ->
            current.copy(
                inTranscription = false,
                originalText = "",
                finalText = "",
                partialText = "",
                summarizedText = ""
            )
        }
        viewModelScope.launch {
            transcriptionRepository.finish()
            // Reset ready state so service can be reused
            initMutex.withLock {
                recognizerReady = false
            }
        }
    }

    fun summarize() {
        if (_uiState.value.viewOriginalText) {
            viewModelScope.launch {
                val summarizedText = Text2Summary.summarize(_uiState.value.originalText, 0.7f)
                _uiState.update { current ->
                    current.copy(viewOriginalText = false, summarizedText = summarizedText)
                }

            }
        } else {
            _uiState.update { current ->
                current.copy(viewOriginalText = true)
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        debugPrintln { "TranscriptionViewModel: onCleared called." }
        viewModelScope.launch {
            try {
                // Only perform cleanup if we're not already in a cleaned state
                if (_uiState.value.inTranscription || _uiState.value.originalText.isNotEmpty()) {
                    debugPrintln { "TranscriptionViewModel: Performing cleanup in onCleared" }
                    transcriptionRepository.stop()
                    transcriptionRepository.finish()
                } else {
                    debugPrintln { "TranscriptionViewModel: State already clean, skipping onCleared cleanup" }
                }
            } catch (e: Exception) {
                debugPrintln { "Error during TranscriptionViewModel cleanup: ${e.message}" }
            }
        }
    }
}
