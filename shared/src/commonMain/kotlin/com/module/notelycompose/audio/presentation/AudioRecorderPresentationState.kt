package com.module.notelycompose.audio.presentation

data class AudioRecorderPresentationState(
    val recordCounterString: String = RECORD_COUNTER_START,
    val recordingPath: String = "",
    val isRecordPaused: Boolean = false
)