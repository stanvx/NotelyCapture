package com.module.notelycompose.testing

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Custom test matchers and assertion utilities for domain-specific testing.
 * 
 * This object provides high-level assertion methods that encapsulate common
 * testing patterns and make tests more readable and maintainable.
 */
object TestMatchers {
    
    /**
     * Asserts that two TestNote objects are equal in all relevant properties.
     * 
     * @param expected The expected note
     * @param actual The actual note to compare
     */
    fun assertNoteEquals(expected: TestNote, actual: TestNote) {
        assertEquals(expected.id, actual.id, "Note ID should match")
        assertEquals(expected.title, actual.title, "Note title should match")
        assertEquals(expected.content, actual.content, "Note content should match")
        assertEquals(expected.isStarred, actual.isStarred, "Note starred status should match")
        assertEquals(expected.audioPath, actual.audioPath, "Note audio path should match")
        assertEquals(expected.audioDuration, actual.audioDuration, "Note audio duration should match")
        assertEquals(expected.dateCreated, actual.dateCreated, "Note creation date should match")
        assertEquals(expected.dateModified, actual.dateModified, "Note modification date should match")
    }
    
    /**
     * Asserts that a UI state represents a loading condition.
     * 
     * @param state The UI state to check
     */
    fun assertStateLoading(state: TestUiState) {
        assertTrue(state.isLoading, "State should be loading")
        assertNull(state.error, "State should not have error when loading")
    }
    
    /**
     * Asserts that a UI state represents a success condition.
     * 
     * @param state The UI state to check
     */
    fun assertStateSuccess(state: TestUiState) {
        assertFalse(state.isLoading, "State should not be loading")
        assertNull(state.error, "State should not have error on success")
    }
    
    /**
     * Asserts that a UI state represents an error condition.
     * 
     * @param state The UI state to check
     * @param expectedError The expected error message (optional)
     */
    fun assertStateError(state: TestUiState, expectedError: String? = null) {
        assertFalse(state.isLoading, "State should not be loading when error occurred")
        assertNotNull(state.error, "State should have error message")
        
        if (expectedError != null) {
            assertEquals(expectedError, state.error, "Error message should match expected")
        }
    }
    
    /**
     * Asserts that a validation result indicates success.
     * 
     * @param result The validation result to check
     */
    fun assertValidationSuccess(result: TestValidationResult) {
        assertTrue(result.isValid, "Validation should succeed")
        assertNull(result.errorMessage, "Validation should not have error message on success")
    }
    
    /**
     * Asserts that a validation result indicates failure.
     * 
     * @param result The validation result to check
     * @param expectedError The expected error message (optional)
     */
    fun assertValidationFailure(result: TestValidationResult, expectedError: String? = null) {
        assertFalse(result.isValid, "Validation should fail")
        assertNotNull(result.errorMessage, "Validation should have error message on failure")
        
        if (expectedError != null) {
            assertEquals(expectedError, result.errorMessage, "Error message should match expected")
        }
    }
    
    /**
     * Asserts that a list contains notes with specific IDs in the expected order.
     * 
     * @param expectedIds The expected note IDs in order
     * @param actualNotes The actual list of notes
     */
    fun assertNotesOrder(expectedIds: List<Long>, actualNotes: List<TestNote>) {
        assertEquals(
            expectedIds.size, 
            actualNotes.size, 
            "Note list should have expected size"
        )
        
        expectedIds.forEachIndexed { index, expectedId ->
            assertEquals(
                expectedId, 
                actualNotes[index].id, 
                "Note at position $index should have ID $expectedId"
            )
        }
    }
    
    /**
     * Asserts that a note list is properly sorted by modification date (newest first).
     * 
     * @param notes The list of notes to check
     */
    fun assertNotesSortedByDateModified(notes: List<TestNote>) {
        if (notes.size <= 1) return
        
        for (i in 0 until notes.size - 1) {
            assertTrue(
                notes[i].dateModified >= notes[i + 1].dateModified,
                "Notes should be sorted by modification date (newest first). " +
                "Note at index $i (${notes[i].dateModified}) should be >= " +
                "note at index ${i + 1} (${notes[i + 1].dateModified})"
            )
        }
    }
    
    /**
     * Asserts that HTML content has been properly sanitized.
     * 
     * @param sanitizedContent The sanitized HTML content
     * @param originalContent The original potentially unsafe content
     */
    fun assertHtmlSanitized(sanitizedContent: String, originalContent: String) {
        // Should not contain script tags
        assertFalse(
            sanitizedContent.contains("<script", ignoreCase = true),
            "Sanitized content should not contain script tags"
        )
        
        // Should not contain javascript: protocols
        assertFalse(
            sanitizedContent.contains("javascript:", ignoreCase = true),
            "Sanitized content should not contain javascript: protocols"
        )
        
        // Should not contain on* event handlers
        val eventHandlers = listOf("onclick", "onload", "onerror", "onmouseover")
        eventHandlers.forEach { handler ->
            assertFalse(
                sanitizedContent.contains(handler, ignoreCase = true),
                "Sanitized content should not contain $handler event handler"
            )
        }
    }
    
    /**
     * Asserts that a playback speed is within valid range.
     * 
     * @param speed The playback speed to validate
     */
    fun assertValidPlaybackSpeed(speed: Float) {
        val validSpeeds = setOf(1.0f, 1.5f, 2.0f)
        assertTrue(
            speed in validSpeeds,
            "Playback speed $speed should be one of $validSpeeds"
        )
    }
    
    /**
     * Asserts that a timestamp is within a reasonable range of the current time.
     * 
     * @param timestamp The timestamp to check
     * @param toleranceMs Tolerance in milliseconds (default: 5000ms)
     */
    fun assertTimestampRecent(timestamp: Long, toleranceMs: Long = 5000L) {
        val currentTime = System.currentTimeMillis()
        val difference = kotlin.math.abs(currentTime - timestamp)
        
        assertTrue(
            difference <= toleranceMs,
            "Timestamp $timestamp should be within ${toleranceMs}ms of current time $currentTime. " +
            "Difference: ${difference}ms"
        )
    }
}

/**
 * Test representation of a UI state for testing state assertions.
 */
data class TestUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: Any? = null
)