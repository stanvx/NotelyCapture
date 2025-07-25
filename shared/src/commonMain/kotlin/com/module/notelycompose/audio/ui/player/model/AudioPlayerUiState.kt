package com.module.notelycompose.audio.ui.player.model

import androidx.compose.runtime.Stable

@Stable
data class AudioPlayerUiState(
    val isLoaded: Boolean,
    val isPlaying: Boolean,
    val currentPosition: Int,
    val duration: Int,
    val errorMessage: String,
    val playbackSpeed: Float,
    val waveformAmplitudes: List<Float> = emptyList()
)
