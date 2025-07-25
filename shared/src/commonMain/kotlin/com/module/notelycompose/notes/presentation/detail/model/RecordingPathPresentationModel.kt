package com.module.notelycompose.notes.presentation.detail.model

data class RecordingPathPresentationModel(
    val recordingPath: String = "",
    val isRecordingExist: Boolean = false,
    val audioDurationMs: Int = 0
)
