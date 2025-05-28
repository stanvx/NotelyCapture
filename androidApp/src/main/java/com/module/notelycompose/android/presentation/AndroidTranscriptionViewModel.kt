package com.module.notelycompose.android.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.transcription.TranscriptionViewModel
import com.module.notelycompose.audio.ui.expect.Downloader
import com.module.notelycompose.audio.ui.expect.Transcriper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidTranscriptionViewModel @Inject constructor(
    private val transcriper: Transcriper,
    private val downloader: Downloader
) : ViewModel() {

    private val viewModel by lazy {
        TranscriptionViewModel(
            transcriper = transcriper,
            downloader = downloader,
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
    fun startRecognizer(filePath:String, language: String) {
        viewModel.startRecognizer(filePath, language)
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
