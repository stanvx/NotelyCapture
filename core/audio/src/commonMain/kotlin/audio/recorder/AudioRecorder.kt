package audio.recorder

import kotlinx.coroutines.flow.Flow

expect class AudioRecorder {
    suspend fun setup()
    suspend fun teardown()
    fun startRecording()
    fun stopRecording()
    fun pauseRecording()
    fun resumeRecording()
    fun isPaused(): Boolean
    fun isRecording(): Boolean
    fun hasRecordingPermission(): Boolean
    fun getRecordingFilePath(): String
    suspend fun requestRecordingPermission(): Boolean
    
    /**
     * Provides a flow of normalized amplitude values during recording.
     * Values range from 0.0 to 1.0 where 0.0 is silence and 1.0 is maximum amplitude.
     */
    val amplitudeFlow: Flow<Float>
}
