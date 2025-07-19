package com.module.notelycompose.platform

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformAudioPlayerIosTest {

    @Test
    fun `setPlaybackSpeed does not crash with valid speeds`() = runTest {
        val audioPlayer = PlatformAudioPlayer()
        val validSpeeds = listOf(1.0f, 1.5f, 2.0f)
        
        // Should not crash even without prepared media
        validSpeeds.forEach { speed ->
            audioPlayer.setPlaybackSpeed(speed)
        }
        
        assertTrue(true, "No crash occurred with valid speeds")
    }

    @Test
    fun `setPlaybackSpeed handles iOS edge cases`() = runTest {
        val audioPlayer = PlatformAudioPlayer()
        val iosSpeeds = listOf(0.5f, 2.0f, 0.25f, 4.0f)
        
        // iOS supports wider range than our app uses
        iosSpeeds.forEach { speed ->
            audioPlayer.setPlaybackSpeed(speed)
        }
        
        assertTrue(true, "No crash occurred with iOS-specific speeds")
    }

    @Test
    fun `audio player initializes correctly on iOS`() = runTest {
        val audioPlayer = PlatformAudioPlayer()
        
        // Basic state checks
        assertTrue(!audioPlayer.isPlaying(), "Should not be playing initially")
        assertTrue(audioPlayer.getCurrentPosition() == 0, "Initial position should be 0")
    }

    @Test
    fun `prepare with non-existent file returns error duration on iOS`() = runTest {
        val audioPlayer = PlatformAudioPlayer()
        val duration = audioPlayer.prepare("/non/existent/file.mp3")
        
        assertTrue(duration == 0, "Should return 0 duration for non-existent file")
    }

    @Test
    fun `release cleans up iOS audio resources safely`() = runTest {
        val audioPlayer = PlatformAudioPlayer()
        
        // Should not crash
        audioPlayer.release()
        audioPlayer.release() // Multiple calls should be safe
        
        assertTrue(true, "Release should handle multiple calls safely on iOS")
    }

    @Test
    fun `iOS rate property accepts floating point values`() = runTest {
        val audioPlayer = PlatformAudioPlayer()
        val preciseRates = listOf(1.25f, 1.75f, 0.875f)
        
        // iOS AVAudioPlayer supports precise rate values
        preciseRates.forEach { rate ->
            audioPlayer.setPlaybackSpeed(rate)
        }
        
        assertTrue(true, "iOS should handle precise rate values")
    }

    @Test
    fun `iOS playback speed with null audio player handles gracefully`() = runTest {
        val audioPlayer = PlatformAudioPlayer()
        
        // Before any audio is loaded, audioPlayer should be null internally
        audioPlayer.setPlaybackSpeed(1.5f)
        
        assertTrue(true, "Should handle null audioPlayer gracefully")
    }
}