package com.module.notelycompose.audio.domain

import com.module.notelycompose.audio.ui.recorder.AudioRecorderUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

const val RECORD_COUNTER_START = "00:00"
const val SECONDS_IN_MINUTE = 60
const val LEADING_ZERO_THRESHOLD = 10
const val INITIAL_SECOND = 0

interface AudioRecorderInteractor {
    val state: StateFlow<AudioRecorderPresentationState>

    fun initState()
    fun setupRecorder(coroutineScope: CoroutineScope)
    fun onStartRecording(coroutineScope: CoroutineScope, updateUI: () -> Unit)
    fun onPauseRecording(coroutineScope: CoroutineScope)
    fun onResumeRecording(coroutineScope: CoroutineScope)
    fun onStopRecording(coroutineScope: CoroutineScope)
    fun onCleared()
    fun onRequestAudioPermission(coroutineScope: CoroutineScope)
    fun onGetUiState(presentationState: AudioRecorderPresentationState): AudioRecorderUiState
    fun finishRecorder(coroutineScope: CoroutineScope)
}
