package com.module.notelycompose.android.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.audio.ui.expect.Downloader
import com.module.notelycompose.audio.ui.expect.Transcriber
import com.module.notelycompose.modelDownloader.ModelDownloaderViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidModelDownloaderViewModel @Inject constructor(
    private val downloader: Downloader,
    private val transcriber: Transcriber
) : ViewModel() {

    private val viewModel by lazy {
        ModelDownloaderViewModel(
            transcriber = transcriber,
            downloader = downloader,
            coroutineScope = viewModelScope
        )
    }

    val state = viewModel.uiState
    val effect = viewModel.effects


    fun checkModelAvailability(){
      viewModel.checkTranscriptionAvailability()
    }

    fun startDownload(){
        viewModel.startDownload()
    }

}
