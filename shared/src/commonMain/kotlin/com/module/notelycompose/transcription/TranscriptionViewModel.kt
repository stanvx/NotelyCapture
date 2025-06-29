package com.module.notelycompose.transcription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.core.debugPrintln
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.platform.Transcriber
import com.module.notelycompose.summary.Text2Summary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class TranscriptionViewModel(
    private val transcriber: Transcriber,
    private val preferencesRepository: PreferencesRepository
) :ViewModel(){
    private val _uiState = MutableStateFlow(TranscriptionUiState())
    val uiState: StateFlow<TranscriptionUiState> = _uiState

    fun requestAudioPermission() {
        viewModelScope.launch {
            transcriber.requestRecordingPermission()
        }
    }

    fun initRecognizer() {
        viewModelScope.launch {
            transcriber.initialize()
        }
    }


    fun startRecognizer(filePath: String) {
        debugPrintln{"startRecognizer ========================="}
        viewModelScope.launch(Dispatchers.Default) {
            if (transcriber.hasRecordingPermission()) {
                _uiState.update { current ->
                    current.copy(inTranscription = true)
                }
                transcriber.start(
                    filePath, preferencesRepository.getDefaultTranscriptionLanguage().first(), onProgress = { progress ->
                        debugPrintln{"progress ========================= $progress"}
                        _uiState.update { current ->
                            current.copy(
                                progress = progress
                            )
                        }
                    }, onNewSegment = { _, _, text ->
                        
                        val delimiter = if(_uiState.value.originalText.endsWith(".")) "\n\n" else ""
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
            transcriber.stop()
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
            transcriber.finish()
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
        stopRecognizer()
    }
}
