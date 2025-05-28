package com.module.notelycompose.transcription

import com.module.notelycompose.audio.ui.expect.Downloader
import com.module.notelycompose.audio.ui.expect.Transcriper
import com.module.notelycompose.summary.Text2Summary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class TranscriptionViewModel(
    private val transcriper: Transcriper,
    private val downloader: Downloader,
    coroutineScope: CoroutineScope? = null
) {
    private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.Main)
    private val _uiState = MutableStateFlow(TranscriptionUiState())
    val uiState: StateFlow<TranscriptionUiState> = _uiState

    fun requestAudioPermission() {
        viewModelScope.launch {
            transcriper.requestRecordingPermission()
        }
    }

    fun initRecognizer() {
        viewModelScope.launch {
            transcriper.initialize()
        }
    }


    fun startRecognizer(filePath: String, language: String) {
        viewModelScope.launch {
            if (transcriper.hasRecordingPermission()) {
                _uiState.update { current ->
                    current.copy(inTranscription = true)
                }
                transcriper.start(
                    filePath, language, onProgress = { progress ->
                        println("progress ========================= $progress")
                        _uiState.update { current ->
                            current.copy(
                                progress = progress
                            )
                        }
                    }, onNewSegment = { _, _, text ->
                        println("text ========================= $text")
                        _uiState.update { current ->
                            current.copy(
                                originalText = "${_uiState.value.originalText}\n\n${text}".trim(),
                                partialText = text
                            )
                        }

                    },
                    onComplete = {

                    })
            }
        }

    }

    fun stopRecognizer() {
        _uiState.update { current ->
            current.copy(inTranscription = false)
        }
        viewModelScope.launch {
            transcriper.stop()
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
            transcriper.finish()
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

    fun onCleared() {
        stopRecognizer()
    }
}
