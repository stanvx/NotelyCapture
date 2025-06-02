package com.module.notelycompose

import com.module.notelycompose.audio.ui.expect.Downloader
import com.module.notelycompose.audio.ui.expect.PlatformUtils
import com.module.notelycompose.transcription.TranscriptionViewModel
import com.module.notelycompose.audio.ui.expect.Transcriber

class IOSTranscriptionViewModel(
    private val transcriber: Transcriber,
    private val platformUtils: PlatformUtils
) {
    private val viewModel by lazy {
        TranscriptionViewModel(
            platformUtils = platformUtils,
            transcriber = transcriber
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

    private fun formatText(text: String): String {
        val chunks = mutableListOf<String>()
        var remaining = text

        while (remaining.length > 250) {
            val chunk = remaining.take(250)
            val lastDot = chunk.lastIndexOf(".")

            if (lastDot > 0) {
                chunks.add(remaining.substring(0, lastDot + 1))
                remaining = remaining.substring(lastDot + 1)
            } else {
                chunks.add(remaining.substring(0, 250))
                remaining = remaining.substring(250)
            }
        }

        chunks.add(remaining)
        return chunks.joinToString("\n\n")
    }

    fun stopRecognizer() {
        viewModel.stopRecognizer()
    }
    fun summarize(){
        viewModel.summarize()
    }

    fun onCleared() {
        viewModel.onCleared()
    }
}