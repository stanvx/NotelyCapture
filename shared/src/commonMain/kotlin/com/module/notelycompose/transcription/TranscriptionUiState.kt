package com.module.notelycompose.transcription

data class TranscriptionUiState(
    val inTranscription: Boolean = false,
    val viewOriginalText: Boolean = true,
    val finalText: String = "",
    val partialText: String = "",
    val summarizedText: String = "",
    val originalText: String = "",
    val progress: Int = 0,
    val downloaded: String = "0 MB ",
    val total: String = "0 MB"
)

sealed class TranscriptionEffect() {
     object DownloadEffect : TranscriptionEffect()
}