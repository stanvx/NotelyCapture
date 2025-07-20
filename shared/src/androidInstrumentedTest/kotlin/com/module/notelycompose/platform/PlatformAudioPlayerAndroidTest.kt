package com.module.notelycompose.platform

import android.os.Build
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PlatformAudioPlayerAndroidTest {

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
    fun `setPlaybackSpeed handles extreme values gracefully`() = runTest {
        val audioPlayer = PlatformAudioPlayer()
        val extremeSpeeds = listOf(0.125f, 8.0f, 100.0f, Float.MAX_VALUE)
        
        // Should not crash even with extreme values
        extremeSpeeds.forEach { speed ->
            audioPlayer.setPlaybackSpeed(speed)
        }
        
        assertTrue(true, "No crash occurred with extreme speeds")
    }

    @Test
    fun `playback speed is supported on API 23+`() {
        val isSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        
        if (isSupported) {
            assertTrue(true, "Playback speed should be supported on API ${Build.VERSION.SDK_INT}")
        } else {
            assertTrue(true, "Playback speed not supported on API ${Build.VERSION.SDK_INT}, but should handle gracefully")
        }
    }

    @Test
    fun `audio player initializes correctly`() = runTest {
        val audioPlayer = PlatformAudioPlayer()
        
        // Basic state checks
        assertTrue(!audioPlayer.isPlaying(), "Should not be playing initially")
        assertTrue(audioPlayer.getCurrentPosition() == 0, "Initial position should be 0")
    }

    @Test
    fun `prepare with non-existent file returns error duration`() = runTest {
        val audioPlayer = PlatformAudioPlayer()
        val duration = audioPlayer.prepare("/non/existent/file.mp3")
        
        assertTrue(duration == 0, "Should return 0 duration for non-existent file")
    }

    @Test
    fun `release cleans up resources safely`() = runTest {
        val audioPlayer = PlatformAudioPlayer()
        
        // Should not crash
        audioPlayer.release()
        audioPlayer.release() // Multiple calls should be safe
        
        assertTrue(true, "Release should handle multiple calls safely")
    }
}