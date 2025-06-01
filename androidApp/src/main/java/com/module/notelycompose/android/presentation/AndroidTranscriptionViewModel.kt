package com.module.notelycompose.android.presentation

import androidx.appcompat.widget.ViewUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.transcription.TranscriptionViewModel
import com.module.notelycompose.audio.ui.expect.Downloader
import com.module.notelycompose.audio.ui.expect.PlatformUtils
import com.module.notelycompose.audio.ui.expect.Transcriber
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidTranscriptionViewModel @Inject constructor(
    private val transcriber: Transcriber,
    private val platformUtils: PlatformUtils
) : ViewModel() {

    private val viewModel by lazy {
        TranscriptionViewModel(
            transcriber = transcriber,
            platformUtils = platformUtils,
            coroutineScope = viewModelScope
        )
    }

    val state = viewModel.uiState
    fun requestAudioPermission() {
        viewModel.requestAudioPermission()
    }

    fun initRecognizer() {
        viewModel.initRecognizer()
    }
    fun finishRecognizer(){
        viewModel.finishRecognizer()
    }
    fun startRecognizer(filePath:String) {
        viewModel.startRecognizer(filePath)
    }

    fun stopRecognizer() {
        viewModel.stopRecognizer()
    }
    fun summarize(){
        viewModel.summarize()
    }


    override fun onCleared() {
        viewModel.onCleared()
    }


}
