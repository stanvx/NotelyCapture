package com.module.notelycompose.audio.presentation

import com.module.notelycompose.onboarding.data.PreferencesRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for playback speed validation and constants
 */
class PlaybackSpeedValidationTest {

    @Test
    fun `valid playback speeds are accepted`() {
        val validSpeeds = PreferencesRepository.VALID_PLAYBACK_SPEEDS
        
        assertEquals(setOf(1.0f, 1.5f, 2.0f), validSpeeds)
        assertTrue(validSpeeds.contains(PreferencesRepository.DEFAULT_PLAYBACK_SPEED))
    }

    @Test
    fun `default playback speed is valid`() {
        val defaultSpeed = PreferencesRepository.DEFAULT_PLAYBACK_SPEED
        val validSpeeds = PreferencesRepository.VALID_PLAYBACK_SPEEDS
        
        assertEquals(1.0f, defaultSpeed)
        assertTrue(validSpeeds.contains(defaultSpeed))
    }

    @Test
    fun `speed cycling follows correct pattern`() {
        // Test the speed cycling logic that would be in the ViewModel
        fun getNextSpeed(currentSpeed: Float): Float {
            return when (currentSpeed) {
                1.0f -> 1.5f
                1.5f -> 2.0f
                else -> 1.0f
            }
        }

        // Test the complete cycle
        assertEquals(1.5f, getNextSpeed(1.0f))
        assertEquals(2.0f, getNextSpeed(1.5f))
        assertEquals(1.0f, getNextSpeed(2.0f))
        
        // Test edge cases
        assertEquals(1.0f, getNextSpeed(3.0f)) // Invalid speed -> reset to 1.0f
        assertEquals(1.0f, getNextSpeed(0.5f)) // Invalid speed -> reset to 1.0f
    }

    @Test
    fun `playback speed constants are properly defined`() {
        // Ensure our constants match the expected values from the PR review
        assertTrue(PreferencesRepository.VALID_PLAYBACK_SPEEDS.size == 3)
        assertTrue(1.0f in PreferencesRepository.VALID_PLAYBACK_SPEEDS)
        assertTrue(1.5f in PreferencesRepository.VALID_PLAYBACK_SPEEDS)
        assertTrue(2.0f in PreferencesRepository.VALID_PLAYBACK_SPEEDS)
    }

    @Test
    fun `invalid speed detection works correctly`() {
        val validSpeeds = PreferencesRepository.VALID_PLAYBACK_SPEEDS
        
        // Test invalid speeds
        val invalidSpeeds = listOf(0.5f, 0.8f, 2.5f, 3.0f, -1.0f, 0.0f)
        
        for (speed in invalidSpeeds) {
            assertTrue(!validSpeeds.contains(speed), "Speed $speed should not be valid")
        }
    }

    @Test
    fun `finite number validation logic`() {
        // Test the logic that would validate finite numbers
        fun isValidFiniteSpeed(speed: Float): Boolean {
            return speed.isFinite() && speed in PreferencesRepository.VALID_PLAYBACK_SPEEDS
        }
        
        // Valid finite speeds
        assertTrue(isValidFiniteSpeed(1.0f))
        assertTrue(isValidFiniteSpeed(1.5f))
        assertTrue(isValidFiniteSpeed(2.0f))
        
        // Invalid finite speeds
        assertTrue(!isValidFiniteSpeed(0.5f))
        assertTrue(!isValidFiniteSpeed(3.0f))
        
        // Non-finite speeds
        assertTrue(!isValidFiniteSpeed(Float.NaN))
        assertTrue(!isValidFiniteSpeed(Float.POSITIVE_INFINITY))
        assertTrue(!isValidFiniteSpeed(Float.NEGATIVE_INFINITY))
    }

    @Test
    fun `playback speed state management`() {
        // Test state management logic
        data class PlaybackState(val speed: Float = PreferencesRepository.DEFAULT_PLAYBACK_SPEED)
        
        val initialState = PlaybackState()
        assertEquals(1.0f, initialState.speed)
        
        val updatedState = initialState.copy(speed = 1.5f)
        assertEquals(1.5f, updatedState.speed)
        
        // State should remain immutable
        assertEquals(1.0f, initialState.speed)
    }
}