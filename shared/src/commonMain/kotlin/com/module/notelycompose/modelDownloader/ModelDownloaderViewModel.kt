package com.module.notelycompose.modelDownloader

import com.module.notelycompose.audio.ui.expect.Downloader
import com.module.notelycompose.audio.ui.expect.Transcriber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ModelDownloaderViewModel(
    private val downloader: Downloader,
    private val transcriber: Transcriber,
    coroutineScope: CoroutineScope? = null
) {
    private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.Main)
    private val _uiState = MutableStateFlow(DownloaderUiState("ggml-base.bin"))
    val uiState: StateFlow<DownloaderUiState> = _uiState

    private val _effects = MutableSharedFlow<DownloaderEffect>()
    val effects: SharedFlow<DownloaderEffect> = _effects


    fun checkTranscriptionAvailability() {
        viewModelScope.launch {
            if (downloader.hasRunningDownload()) {
                trackDownload()
            } else {
                if (!transcriber.doesModelExists() || !transcriber.isValidModel() ) {
                    downloader.startDownload(
                        "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin",
                        _uiState.value.fileName
                    )
                    trackDownload()
                } else {
                    _effects.emit(DownloaderEffect.ModelsAreReady())
                }
            }
        }
    }

    private suspend fun trackDownload() {
        _effects.emit(DownloaderEffect.DownloadEffect())
        downloader.trackDownloadProgress(
            _uiState.value.fileName,
            onProgressUpdated = { progress, downloadedMB, totalMB ->
            _uiState.update { current ->
                current.copy(
                    progress = progress.toFloat(),
                    downloaded = downloadedMB,
                    total = totalMB
                )
            }
        }, onSuccess = {
            viewModelScope.launch {
                transcriber.initialize()
                _effects.emit(DownloaderEffect.ModelsAreReady()) }

        }, onFailed = {
            viewModelScope.launch { _effects.emit(DownloaderEffect.ErrorEffect()) }
        })
    }
}
