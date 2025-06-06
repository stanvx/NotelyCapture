package com.module.notelycompose.transcription

import com.module.notelycompose.audio.ui.expect.PlatformUtils
import com.module.notelycompose.audio.ui.expect.Transcriber
import com.module.notelycompose.summary.Text2Summary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class TranscriptionViewModel(
    private val transcriber: Transcriber,
    private val platformUtils: PlatformUtils,
    coroutineScope: CoroutineScope? = null
) {
    private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.IO)
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
        println("startRecognizer =========================")
        viewModelScope.launch(Dispatchers.Default) {
            if (transcriber.hasRecordingPermission()) {
                _uiState.update { current ->
                    current.copy(inTranscription = true)
                }
                transcriber.start(
                    filePath, platformUtils.getDefaultTranscriptionLanguage(), onProgress = { progress ->
                        println("progress ========================= $progress")
                        _uiState.update { current ->
                            current.copy(
                                progress = progress
                            )
                        }
                    }, onNewSegment = { _, _, text ->
                        
                        val delimiter = if(_uiState.value.originalText.endsWith(".")) "\n\n" else ""
                        println("\n text ========================= $text")
                        _uiState.update { current ->
                            current.copy(
                                originalText = "${_uiState.value.originalText}$delimiter${text.trim()}".trim(),
                                partialText = text
                            )
                        }

                    },
                    onComplete = {
                        println("\n completed ========================= ")
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

    fun onCleared() {
        stopRecognizer()
    }
}
