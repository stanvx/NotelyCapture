package com.module.notelycompose.presentation.viewmodels

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SecureCompactAudioPlayerTest {

    // Mock implementation of audio player state
    data class AudioPlayerState(
        val isPlaying: Boolean = false,
        val currentPosition: Long = 0L,
        val duration: Long = 0L,
        val audioFilePath: String? = null,
        val playbackSpeed: Float = 1.0f,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    // Mock audio player that would be injected
    private class MockSecureAudioPlayer {
        private var _state = AudioPlayerState()
        val state get() = _state
        
        private var isValidPath = true
        
        fun setPathValidation(isValid: Boolean) {
            isValidPath = isValid
        }
        
        suspend fun loadAudio(filePath: String): Result<Unit> {
            return if (isValidPath && isValidAudioPath(filePath)) {
                _state = _state.copy(
                    audioFilePath = filePath,
                    duration = 30000L, // 30 seconds mock duration
                    isLoading = false,
                    error = null
                )
                Result.success(Unit)
            } else {
                _state = _state.copy(
                    error = "Invalid audio file path or security violation",
                    isLoading = false
                )
                Result.failure(SecurityException("Invalid audio path"))
            }
        }
        
        suspend fun play(): Result<Unit> {
            return if (_state.audioFilePath != null && _state.error == null) {
                _state = _state.copy(isPlaying = true)
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("No audio loaded or error present"))
            }
        }
        
        suspend fun pause(): Result<Unit> {
            _state = _state.copy(isPlaying = false)
            return Result.success(Unit)
        }
        
        suspend fun seekTo(position: Long): Result<Unit> {
            return if (position >= 0 && position <= _state.duration) {
                _state = _state.copy(currentPosition = position)
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Invalid seek position"))
            }
        }
        
        suspend fun setPlaybackSpeed(speed: Float): Result<Unit> {
            return if (speed in 0.5f..3.0f) {
                _state = _state.copy(playbackSpeed = speed)
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Invalid playback speed"))
            }
        }
        
        suspend fun stop() {
            _state = _state.copy(
                isPlaying = false,
                currentPosition = 0L,
                audioFilePath = null,
                error = null
            )
        }
        
        private fun isValidAudioPath(path: String): Boolean {
            if (path.isBlank()) return false
            val validExtensions = setOf("wav", "mp3", "m4a", "aac", "flac", "ogg")
            val extension = path.substringAfterLast('.', "").lowercase()
            return validExtensions.contains(extension) && !path.contains("../")
        }
    }

    private lateinit var audioPlayer: MockSecureAudioPlayer
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        audioPlayer = MockSecureAudioPlayer()
    }

    @Test
    fun `initial state should be stopped with no audio loaded`() = runTest {
        // When
        val state = audioPlayer.state

        // Then
        assertFalse(state.isPlaying)
        assertEquals(0L, state.currentPosition)
        assertEquals(0L, state.duration)
        assertNull(state.audioFilePath)
        assertEquals(1.0f, state.playbackSpeed)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadAudio should successfully load valid audio file`() = runTest {
        // Given
        val validAudioPath = "/storage/recordings/voice_note.wav"

        // When
        val result = audioPlayer.loadAudio(validAudioPath)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(result.isSuccess)
        val state = audioPlayer.state
        assertEquals(validAudioPath, state.audioFilePath)
        assertEquals(30000L, state.duration)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadAudio should reject invalid audio file paths`() = runTest {
        // Given
        val invalidPaths = listOf(
            "/storage/documents/file.txt",
            "../../../etc/passwd",
            "/path/without/extension",
            "",
            "/storage/recordings/../../../secrets.wav"
        )

        // When & Then
        invalidPaths.forEach { path ->
            val result = audioPlayer.loadAudio(path)
            testDispatcher.scheduler.advanceUntilIdle()
            
            assertTrue(result.isFailure, "Expected failure for path: $path")
            val state = audioPlayer.state
            assertNotNull(state.error, "Expected error for path: $path")
            assertTrue(
                state.error!!.contains("Invalid audio file path") || 
                state.error!!.contains("security violation"),
                "Expected security-related error for path: $path"
            )
        }
    }

    @Test
    fun `play should start playback when audio is loaded`() = runTest {
        // Given
        audioPlayer.loadAudio("/storage/test.wav")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val result = audioPlayer.play()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(result.isSuccess)
        assertTrue(audioPlayer.state.isPlaying)
    }

    @Test
    fun `play should fail when no audio is loaded`() = runTest {
        // When
        val result = audioPlayer.play()

        // Then
        assertTrue(result.isFailure)
        assertFalse(audioPlayer.state.isPlaying)
    }

    @Test
    fun `pause should stop playback`() = runTest {
        // Given
        audioPlayer.loadAudio("/storage/test.wav")
        audioPlayer.play()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val result = audioPlayer.pause()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(result.isSuccess)
        assertFalse(audioPlayer.state.isPlaying)
    }

    @Test
    fun `seekTo should update position within valid range`() = runTest {
        // Given
        audioPlayer.loadAudio("/storage/test.wav")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val result = audioPlayer.seekTo(15000L) // Seek to 15 seconds
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(15000L, audioPlayer.state.currentPosition)
    }

    @Test
    fun `seekTo should reject invalid positions`() = runTest {
        // Given
        audioPlayer.loadAudio("/storage/test.wav")
        testDispatcher.scheduler.advanceUntilIdle()

        // When & Then
        val negativeResult = audioPlayer.seekTo(-1000L)
        assertTrue(negativeResult.isFailure)

        val tooLargeResult = audioPlayer.seekTo(50000L) // Beyond duration
        assertTrue(tooLargeResult.isFailure)
    }

    @Test
    fun `setPlaybackSpeed should accept valid speeds`() = runTest {
        // Given
        val validSpeeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f)

        // When & Then
        validSpeeds.forEach { speed ->
            val result = audioPlayer.setPlaybackSpeed(speed)
            assertTrue(result.isSuccess, "Expected success for speed: $speed")
            assertEquals(speed, audioPlayer.state.playbackSpeed)
        }
    }

    @Test
    fun `setPlaybackSpeed should reject invalid speeds`() = runTest {
        // Given
        val invalidSpeeds = listOf(0.0f, 0.25f, 4.0f, -1.0f, Float.NaN, Float.POSITIVE_INFINITY)

        // When & Then
        invalidSpeeds.forEach { speed ->
            val result = audioPlayer.setPlaybackSpeed(speed)
            assertTrue(result.isFailure, "Expected failure for speed: $speed")
        }
    }

    @Test
    fun `stop should reset player state`() = runTest {
        // Given
        audioPlayer.loadAudio("/storage/test.wav")
        audioPlayer.play()
        audioPlayer.seekTo(10000L)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        audioPlayer.stop()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = audioPlayer.state
        assertFalse(state.isPlaying)
        assertEquals(0L, state.currentPosition)
        assertNull(state.audioFilePath)
        assertNull(state.error)
    }

    @Test
    fun `security validation should prevent path traversal attacks`() = runTest {
        // Given
        val maliciousPaths = listOf(
            "../../../system/config.wav",
            "/home/user/../../secrets.mp3",
            "../../../../etc/passwd.m4a",
            "/storage/../../../root/.ssh/id_rsa.wav"
        )

        // When & Then
        maliciousPaths.forEach { path ->
            val result = audioPlayer.loadAudio(path)
            testDispatcher.scheduler.advanceUntilIdle()
            
            assertTrue(result.isFailure, "Expected security failure for path: $path")
            assertNotNull(audioPlayer.state.error)
            assertTrue(
                audioPlayer.state.error!!.contains("security") || 
                audioPlayer.state.error!!.contains("Invalid"),
                "Expected security error for path: $path"
            )
        }
    }

    @Test
    fun `player should handle state transitions correctly`() = runTest {
        // Test complete workflow: load -> play -> pause -> seek -> stop
        
        // Load
        var result = audioPlayer.loadAudio("/storage/test.wav")
        assertTrue(result.isSuccess)
        assertEquals("/storage/test.wav", audioPlayer.state.audioFilePath)
        
        // Play
        result = audioPlayer.play()
        assertTrue(result.isSuccess)
        assertTrue(audioPlayer.state.isPlaying)
        
        // Pause
        result = audioPlayer.pause()
        assertTrue(result.isSuccess)
        assertFalse(audioPlayer.state.isPlaying)
        
        // Seek
        result = audioPlayer.seekTo(20000L)
        assertTrue(result.isSuccess)
        assertEquals(20000L, audioPlayer.state.currentPosition)
        
        // Stop
        audioPlayer.stop()
        assertFalse(audioPlayer.state.isPlaying)
        assertEquals(0L, audioPlayer.state.currentPosition)
        assertNull(audioPlayer.state.audioFilePath)
    }
}