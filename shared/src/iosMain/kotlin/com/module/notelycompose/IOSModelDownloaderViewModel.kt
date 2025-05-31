package com.module.notelycompose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.audio.ui.expect.Downloader
import com.module.notelycompose.audio.ui.expect.Transcriper
import com.module.notelycompose.modelDownloader.ModelDownloaderViewModel

class IOSModelDownloaderViewModel (
        private val downloader: Downloader,
        private val transcriper: Transcriper
    ){

        private val viewModel by lazy {
            ModelDownloaderViewModel(
                transcriper = transcriper,
                downloader = downloader,
            )
        }

        val state = viewModel.uiState
        val effect = viewModel.effects


        fun checkModelAvailability(){
            viewModel.checkTranscriptionAvailability()
        }

}