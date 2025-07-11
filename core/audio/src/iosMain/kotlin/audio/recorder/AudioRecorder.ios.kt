package audio.recorder

import audio.utils.generateNewAudioFile
import io.github.aakira.napier.Napier
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioQualityHigh
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatLinearPCM
import platform.Foundation.NSURL
import kotlin.coroutines.resume

actual class AudioRecorder {

    private var audioRecorder: AVAudioRecorder? = null
    private var recordingSession: AVAudioSession = AVAudioSession.sharedInstance()
    private var recordingURL: NSURL? = null
    private var isCurrentlyPaused = false

    /**
     * Call when entering recording screen
     */
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun setup() {
        try {
            recordingSession.setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                withOptions = AVAudioSessionCategoryOptionDefaultToSpeaker,
                null
            )
            recordingSession.setActive(true, null)
        } catch (e: Exception) {
            Napier.d { "Audio session setup failed: ${e.message}" }
        }
    }

    /**
     * Call when leaving recording screen
     */
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun teardown() {
        // 1. Stop any active recording
        if (isRecording()) {
            stopRecording()
        }

        // 2. Deactivate audio session
        try {
            recordingSession.setActive(false, null)
        } catch (e: Exception) {
            Napier.d { "Audio session teardown failed: ${e.message}" }
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual fun startRecording() {
        // 1. Request permissions early
        if (!hasRecordingPermission()) {
            Napier.d { "Recording permission not granted" }
            return
        }

        this.recordingURL = generateNewAudioFile() ?: run {
            Napier.e { "Failed to create recording URL" }
            return
        }

        val settings = mapOf<Any?, Any?>(
            AVFormatIDKey to kAudioFormatLinearPCM,
            AVSampleRateKey to 16000.0,
            AVNumberOfChannelsKey to 1,
            AVEncoderAudioQualityKey to AVAudioQualityHigh,
        )
        audioRecorder = AVAudioRecorder(recordingURL!!, settings, null)
        if (audioRecorder?.prepareToRecord() == true) {
            val isRecording = audioRecorder?.record()
            isCurrentlyPaused = false
            Napier.d { "Recording started successfully $isRecording" }
        } else {
            Napier.d { "Failed to prepare recording" }
            audioRecorder = null
        }

    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun stopRecording() {
        audioRecorder?.let { recorder ->
            if (recorder.isRecording()) {
                recorder.stop()
            }
        }

        audioRecorder = null
        isCurrentlyPaused = false
    }

    actual fun isRecording(): Boolean {
        return audioRecorder?.isRecording() ?: false
    }

    actual fun hasRecordingPermission(): Boolean {
        return recordingSession.recordPermission() == AVAudioSessionRecordPermissionGranted
    }

    actual suspend fun requestRecordingPermission(): Boolean {
        if (hasRecordingPermission()) return true

        return suspendCancellableCoroutine { continuation ->
            recordingSession.requestRecordPermission { granted ->
                continuation.resume(granted)
            }
        }
    }

    actual fun getRecordingFilePath(): String {
        return recordingURL?.path.orEmpty()
    }

    actual fun pauseRecording() {
        if (isRecording() && !isCurrentlyPaused) {
            audioRecorder?.let { recorder ->
                recorder.pause()
                isCurrentlyPaused = true
                Napier.d { "Recording paused successfully" }
            }
        }
    }

    actual fun resumeRecording() {
        if (isCurrentlyPaused) {
            audioRecorder?.let { recorder ->
                recorder.record()
                isCurrentlyPaused = false
                Napier.d { "Recording resumed successfully" }
            }
        }
    }

    actual fun isPaused(): Boolean {
        return isCurrentlyPaused
    }
}