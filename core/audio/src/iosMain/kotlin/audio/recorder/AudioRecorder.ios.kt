package audio.recorder

import audio.utils.generateNewAudioFile
import io.github.aakira.napier.Napier
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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
import kotlin.math.pow

actual class AudioRecorder {

    private var audioRecorder: AVAudioRecorder? = null
    private var recordingSession: AVAudioSession = AVAudioSession.sharedInstance()
    private var recordingURL: NSURL? = null
    private var isCurrentlyPaused = false
    
    // Amplitude flow management
    private val _amplitudeFlow = MutableStateFlow(0f)
    actual val amplitudeFlow: Flow<Float> = _amplitudeFlow.asStateFlow()
    
    private var amplitudeCollectionJob: Job? = null
    private val amplitudeScope = CoroutineScope(Dispatchers.Main)

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
            audioRecorder?.meteringEnabled = true // Enable metering for amplitude data
            val isRecording = audioRecorder?.record()
            isCurrentlyPaused = false
            startAmplitudeCollection()
            Napier.d { "Recording started successfully $isRecording" }
        } else {
            Napier.d { "Failed to prepare recording" }
            audioRecorder = null
        }

    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun stopRecording() {
        stopAmplitudeCollection()
        audioRecorder?.let { recorder ->
            if (recorder.isRecording()) {
                recorder.stop()
            }
        }

        audioRecorder = null
        _amplitudeFlow.value = 0f
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
            stopAmplitudeCollection()
            audioRecorder?.let { recorder ->
                recorder.pause()
                isCurrentlyPaused = true
                _amplitudeFlow.value = 0f
                Napier.d { "Recording paused successfully" }
            }
        }
    }

    actual fun resumeRecording() {
        if (isCurrentlyPaused) {
            audioRecorder?.let { recorder ->
                recorder.record()
                isCurrentlyPaused = false
                startAmplitudeCollection()
                Napier.d { "Recording resumed successfully" }
            }
        }
    }

    actual fun isPaused(): Boolean {
        return isCurrentlyPaused
    }
    
    /**
     * Starts collecting amplitude data from the AVAudioRecorder.
     */
    private fun startAmplitudeCollection() {
        stopAmplitudeCollection() // Ensure no duplicate jobs
        
        amplitudeCollectionJob = amplitudeScope.launch {
            while (isRecording() && !isCurrentlyPaused) {
                try {
                    audioRecorder?.updateMeters()
                    val averagePower = audioRecorder?.averagePowerForChannel(0u) ?: -160f
                    val normalizedAmplitude = normalizeAmplitude(averagePower)
                    _amplitudeFlow.value = normalizedAmplitude
                    delay(50) // Update every 50ms for smooth visualization
                } catch (e: Exception) {
                    // Handle potential exceptions from accessing averagePower
                    _amplitudeFlow.value = 0f
                    break
                }
            }
        }
    }
    
    /**
     * Stops collecting amplitude data.
     */
    private fun stopAmplitudeCollection() {
        amplitudeCollectionJob?.cancel()
        amplitudeCollectionJob = null
    }
    
    /**
     * Normalizes the average power value from AVAudioRecorder to a 0-1 range.
     * AVAudioRecorder.averagePower returns dB values typically from -160dB to 0dB.
     */
    private fun normalizeAmplitude(averagePowerDb: Float): Float {
        if (averagePowerDb <= -160f) return 0f
        
        // Convert dB range (-60dB to 0dB) to 0-1 range for better sensitivity
        val clampedDb = averagePowerDb.coerceIn(-60f, 0f)
        val normalizedDb = (clampedDb + 60f) / 60f
        
        return normalizedDb.coerceIn(0f, 1f)
    }
}