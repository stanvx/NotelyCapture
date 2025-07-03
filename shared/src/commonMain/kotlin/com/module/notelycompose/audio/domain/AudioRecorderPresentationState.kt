package com.module.notelycompose.audio.domain

data class AudioRecorderPresentationState(
    val recordCounterString: String = RECORD_COUNTER_START,
    val recordingPath: String = "",
    val isRecordPaused: Boolean = false
)