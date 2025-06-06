package com.module.notelycompose

import com.module.notelycompose.audio.ui.expect.Downloader
import com.module.notelycompose.audio.ui.expect.Transcriber
import com.module.notelycompose.modelDownloader.ModelDownloaderViewModel

class IOSModelDownloaderViewModel (
        private val downloader: Downloader,
        private val transcriber: Transcriber
    ){

        private val viewModel by lazy {
            ModelDownloaderViewModel(
                transcriber = transcriber,
                downloader = downloader,
            )
        }

        val state = viewModel.uiState
        val effect = viewModel.effects


        fun checkModelAvailability(){
            viewModel.checkTranscriptionAvailability()
        }

}