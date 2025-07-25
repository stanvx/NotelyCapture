package com.module.notelycompose.audio.domain

data class AudioRecorderPresentationState(
    val recordCounterString: String = RECORD_COUNTER_START,
    val recordingPath: String = "",
    val isRecordPaused: Boolean = false,
    val currentAmplitude: Float = 0f,
    val amplitudeHistory: List<Float> = emptyList(),
    val maxAmplitudeHistorySize: Int = 100
)