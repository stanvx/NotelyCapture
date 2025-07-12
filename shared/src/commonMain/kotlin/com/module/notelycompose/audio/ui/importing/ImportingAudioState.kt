package com.module.notelycompose.audio.ui.importing

sealed interface ImportingAudioState {
    object Idle : ImportingAudioState
    data class Importing(val progress: Float = 0f) : ImportingAudioState
    data class Success(val path: String) : ImportingAudioState
    data class Failure(val message: String) : ImportingAudioState
}
