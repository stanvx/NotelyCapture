package com.module.notelycompose.platform

import android.content.Context
import android.content.Intent
import com.module.notelycompose.audio.presentation.AudioRecorderInteractor
import com.module.notelycompose.audio.presentation.AudioRecorderPresentationState
import com.module.notelycompose.audio.presentation.INITIAL_SECOND
import com.module.notelycompose.audio.presentation.LEADING_ZERO_THRESHOLD
import com.module.notelycompose.audio.presentation.RECORD_COUNTER_START
import com.module.notelycompose.audio.presentation.SECONDS_IN_MINUTE
import com.module.notelycompose.audio.presentation.mappers.AudioRecorderPresentationToUiMapper
import com.module.notelycompose.audio.ui.recorder.AudioRecorderUiState
import com.module.notelycompose.core.debugPrintln
import com.module.notelycompose.service.AudioRecordingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class AudioRecorderInteractorImpl(
    private val context: Context,
    private val audioRecorder: AudioRecorder,
    private val mapper: AudioRecorderPresentationToUiMapper,
) : AudioRecorderInteractor {
    private val _audioRecorderPresentationState = MutableStateFlow(AudioRecorderPresentationState())
    override val state = _audioRecorderPresentationState

    private val isPaused = MutableStateFlow(false)
    private var counterJob: Job? = null
    private var recordingTimeSeconds = INITIAL_SECOND
    private var elapsedTimeBeforePause = 0

    override fun onStartRecording(coroutineScope: CoroutineScope, updateUI: () -> Unit) {
        coroutineScope.launch {
            if (!audioRecorder.hasRecordingPermission()) {
                audioRecorder.requestRecordingPermission()
                if (!audioRecorder.hasRecordingPermission()) {
                    return@launch
                }
            }

            if (!audioRecorder.isRecording()) {
//                audioRecorder.startRecording()
                context.startRecordingService(AudioRecordingService.ACTION_START)

                updateUI()
                startCounter(coroutineScope)
            }
        }
    }

    private fun startCounter(coroutineScope: CoroutineScope) {
        // Reset counter
        recordingTimeSeconds = INITIAL_SECOND
        _audioRecorderPresentationState.value = _audioRecorderPresentationState.value.copy(
            recordCounterString = RECORD_COUNTER_START
        )

        counterJob?.cancel()
        counterJob = coroutineScope.launch {
            while (true) {
                delay(1.seconds)
                recordingTimeSeconds++
                updateCounterString()
            }
        }
    }

    private fun updateCounterString() {
        val minutes = recordingTimeSeconds / SECONDS_IN_MINUTE
        val seconds = recordingTimeSeconds % SECONDS_IN_MINUTE
        val minutesStr = if (minutes < LEADING_ZERO_THRESHOLD) "0$minutes" else "$minutes"
        val secondsStr = if (seconds < LEADING_ZERO_THRESHOLD) "0$seconds" else "$seconds"
        val counterString = "$minutesStr:$secondsStr"

        _audioRecorderPresentationState.update { current ->
            current.copy(recordCounterString = counterString)
        }
    }

    override fun onStopRecording() {
        debugPrintln { "inside stop recording ${audioRecorder.isRecording()}" }
        if (audioRecorder.isRecording()) {
//            audioRecorder.stopRecording()
            context.startRecordingService(AudioRecordingService.ACTION_STOP)
            val recordingPath = audioRecorder.getRecordingFilePath()
            debugPrintln { "%%%%%%%%%%% 2${recordingPath}" }
            stopCounter()
            _audioRecorderPresentationState.update { current ->
                current.copy(recordingPath = recordingPath)
            }
        }
    }

    override fun setupRecorder(coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            audioRecorder.setup()
        }
    }

    override fun finishRecorder(coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            audioRecorder.teardown()
        }
    }

    override fun onPauseRecording(coroutineScope: CoroutineScope) {
//        audioRecorder.pauseRecording()
        context.startRecordingService(AudioRecordingService.ACTION_PAUSE)
        coroutineScope.launch {
            delay(100L)
            updatePausedState()
            pauseCounter()
        }
    }

    override fun onResumeRecording(coroutineScope: CoroutineScope) {
//        audioRecorder.resumeRecording()
        context.startRecordingService(AudioRecordingService.ACTION_RESUME)
        coroutineScope.launch {
            delay(100L)
            updatePausedState()
            resumeCounter(coroutineScope)
        }
    }

    private fun stopCounter() {
        counterJob?.cancel()
        counterJob = null
    }

    override fun onCleared() {
        stopCounter()
        if (audioRecorder.isRecording()) {
//            audioRecorder.stopRecording()
            context.startRecordingService(AudioRecordingService.ACTION_STOP)

        }
    }

    override fun onRequestAudioPermission(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            if (!audioRecorder.hasRecordingPermission()) {
                audioRecorder.requestRecordingPermission()
                if (!audioRecorder.hasRecordingPermission()) {
                    return@launch
                }
            }
        }
    }

    private fun updatePausedState() {
        _audioRecorderPresentationState.value = _audioRecorderPresentationState.value.copy(
            isRecordPaused = audioRecorder.isPaused()
        )
    }

    private fun pauseCounter() {
        counterJob?.cancel()
        counterJob = null
        elapsedTimeBeforePause = recordingTimeSeconds
    }

    private fun resumeCounter(coroutineScope: CoroutineScope) {
        counterJob?.cancel()
        counterJob = coroutineScope.launch {
            // Start counting from the last saved time
            recordingTimeSeconds = elapsedTimeBeforePause
            updateCounterString()

            while (true) {
                delay(1.seconds)
                recordingTimeSeconds++
                updateCounterString()
            }
        }
    }

    override fun onGetUiState(presentationState: AudioRecorderPresentationState): AudioRecorderUiState {
        return mapper.mapToUiState(presentationState)
    }

    override fun release() {
        stopCounter()
        _audioRecorderPresentationState.value = AudioRecorderPresentationState()
        if (audioRecorder.isRecording()) {
//            audioRecorder.stopRecording()
            context.startRecordingService(AudioRecordingService.ACTION_STOP)
        }
    }
}

private fun Context.startRecordingService(recordingAction: String){
    val intent = Intent(this, AudioRecordingService::class.java).apply {
        action = recordingAction
    }
    startForegroundService(intent)
}