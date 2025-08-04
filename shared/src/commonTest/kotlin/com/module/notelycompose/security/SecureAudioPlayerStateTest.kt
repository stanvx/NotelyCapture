package com.module.notelycompose.security

import com.module.notelycompose.security.AudioPathValidator.SecurityThreat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test suite for SecureAudioPlayerState security wrapper
 * 
 * Validates that the secure audio player state correctly handles
 * validation results and provides appropriate security responses.
 */
class SecureAudioPlayerStateTest {
    
    @Test
    fun `fromNoteData should create Validated state for safe audio paths`() {
        val safeRecordingPath = "recordings/audio.mp3"
        val noteId = 123L
        val noteDurationMs = 30000
        
        val state = SecureAudioPlayerState.fromNoteData(
            recordingPath = safeRecordingPath,
            noteId = noteId,
            noteDurationMs = noteDurationMs
        )
        
        assertIs<SecureAudioPlayerState.Validated>(state)
        assertEquals(safeRecordingPath, state.validatedPath.path)
        assertEquals(noteId, state.noteId)
        assertEquals(noteDurationMs, state.noteDurationMs)
    }
    
    @Test
    fun `fromNoteData should create NoAudio state for empty paths`() {
        val noteId = 123L
        val noteDurationMs = 0
        
        val emptyPaths = listOf(null, "", "   ", "\t")
        
        emptyPaths.forEach { path ->
            val state = SecureAudioPlayerState.fromNoteData(
                recordingPath = path,
                noteId = noteId,
                noteDurationMs = noteDurationMs
            )
            
            assertIs<SecureAudioPlayerState.NoAudio>(state, "Empty path should create NoAudio state: '$path'")
            assertEquals(noteId, state.noteId)
        }
    }
    
    @Test
    fun `fromNoteData should create SecurityThreatDetected for malicious paths`() {
        val maliciousPath = "../../../etc/passwd"
        val noteId = 123L
        val noteDurationMs = 30000
        
        val state = SecureAudioPlayerState.fromNoteData(
            recordingPath = maliciousPath,
            noteId = noteId,
            noteDurationMs = noteDurationMs
        )
        
        assertIs<SecureAudioPlayerState.SecurityThreatDetected>(state)
        assertEquals(SecurityThreat.CRITICAL, state.threatLevel)
        assertEquals(maliciousPath, state.originalPath)
        assertEquals(noteId, state.noteId)
        assertTrue(state.reason.contains("path traversal"))
    }
    
    @Test
    fun `validateForUi should perform additional note ID validation`() {
        val safeRecordingPath = "recordings/audio.mp3"
        val noteId = 123L
        val expectedNoteId = 456L // Different from actual
        val noteDurationMs = 30000
        
        val state = SecureAudioPlayerState.validateForUi(
            recordingPath = safeRecordingPath,
            noteId = noteId,
            noteDurationMs = noteDurationMs,
            expectedNoteId = expectedNoteId
        )
        
        assertIs<SecureAudioPlayerState.SecurityThreatDetected>(state)
        assertEquals(SecurityThreat.HIGH, state.threatLevel)
        assertTrue(state.reason.contains("Note ID mismatch"))
        assertEquals(noteId, state.noteId)
    }
    
    @Test
    fun `validateForUi should succeed when note IDs match`() {
        val safeRecordingPath = "recordings/audio.mp3"
        val noteId = 123L
        val expectedNoteId = 123L // Same as actual
        val noteDurationMs = 30000
        
        val state = SecureAudioPlayerState.validateForUi(
            recordingPath = safeRecordingPath,
            noteId = noteId,
            noteDurationMs = noteDurationMs,
            expectedNoteId = expectedNoteId
        )
        
        assertIs<SecureAudioPlayerState.Validated>(state)
        assertEquals(safeRecordingPath, state.validatedPath.path)
        assertEquals(noteId, state.noteId)
    }
    
    @Test
    fun `getValidatedPathOrNull should return path for validated state`() {
        val safeRecordingPath = "recordings/audio.mp3"
        val state = SecureAudioPlayerState.fromNoteData(
            recordingPath = safeRecordingPath,
            noteId = 123L,
            noteDurationMs = 30000
        )
        
        val validatedPath = state.getValidatedPathOrNull()
        assertNotNull(validatedPath)
        assertEquals(safeRecordingPath, validatedPath.path)
    }
    
    @Test
    fun `getValidatedPathOrNull should return null for security threats`() {
        val maliciousPath = "http://malicious.com/audio.mp3"
        val state = SecureAudioPlayerState.fromNoteData(
            recordingPath = maliciousPath,
            noteId = 123L,
            noteDurationMs = 30000
        )
        
        val validatedPath = state.getValidatedPathOrNull()
        assertNull(validatedPath)
    }
    
    @Test
    fun `getValidatedPathOrNull should return null for no audio state`() {
        val state = SecureAudioPlayerState.fromNoteData(
            recordingPath = null,
            noteId = 123L,
            noteDurationMs = 0
        )
        
        val validatedPath = state.getValidatedPathOrNull()
        assertNull(validatedPath)
    }
    
    @Test
    fun `isAudioSafelyAvailable should return true only for validated state`() {
        // Validated state
        val validatedState = SecureAudioPlayerState.fromNoteData(
            recordingPath = "recordings/audio.mp3",
            noteId = 123L,
            noteDurationMs = 30000
        )
        assertTrue(validatedState.isAudioSafelyAvailable())
        
        // Security threat state
        val threatState = SecureAudioPlayerState.fromNoteData(
            recordingPath = "../../../etc/passwd",
            noteId = 123L,
            noteDurationMs = 30000
        )
        assertFalse(threatState.isAudioSafelyAvailable())
        
        // No audio state
        val noAudioState = SecureAudioPlayerState.fromNoteData(
            recordingPath = null,
            noteId = 123L,
            noteDurationMs = 0
        )
        assertFalse(noAudioState.isAudioSafelyAvailable())
    }
    
    @Test
    fun `getUserErrorMessage should return appropriate messages for different threat levels`() {
        val testCases = mapOf(
            "../../../etc/passwd" to "Audio file is not accessible due to security restrictions",
            "document.txt" to "Audio file format is not supported",
            "a".repeat(300) + ".mp3" to "Audio file path is invalid"
        )
        
        testCases.forEach { (maliciousPath, expectedMessage) ->
            val state = SecureAudioPlayerState.fromNoteData(
                recordingPath = maliciousPath,
                noteId = 123L,
                noteDurationMs = 30000
            )
            
            val errorMessage = state.getUserErrorMessage()
            assertEquals(expectedMessage, errorMessage, "Wrong error message for path: $maliciousPath")
        }
    }
    
    @Test
    fun `getUserErrorMessage should return null for valid and no audio states`() {
        // Valid state
        val validState = SecureAudioPlayerState.fromNoteData(
            recordingPath = "recordings/audio.mp3",
            noteId = 123L,
            noteDurationMs = 30000
        )
        assertNull(validState.getUserErrorMessage())
        
        // No audio state
        val noAudioState = SecureAudioPlayerState.fromNoteData(
            recordingPath = null,
            noteId = 123L,
            noteDurationMs = 0
        )
        assertNull(noAudioState.getUserErrorMessage())
    }
    
    @Test
    fun `shouldLogIncident should be true for high severity threats`() {
        val highSeverityPaths = listOf(
            "../../../etc/passwd", // CRITICAL
            "http://malicious.com/audio.mp3", // CRITICAL
            "audio<script>.mp3" // HIGH
        )
        
        highSeverityPaths.forEach { path ->
            val state = SecureAudioPlayerState.fromNoteData(
                recordingPath = path,
                noteId = 123L,
                noteDurationMs = 30000
            )
            
            assertIs<SecureAudioPlayerState.SecurityThreatDetected>(state)
            assertTrue(
                state.shouldLogIncident, 
                "High severity threat should trigger incident logging: $path"
            )
        }
    }
    
    @Test
    fun `shouldLogIncident should be false for low severity threats`() {
        val lowSeverityPath = "" // Empty path is LOW severity
        val state = SecureAudioPlayerState.fromNoteData(
            recordingPath = lowSeverityPath,
            noteId = 123L,
            noteDurationMs = 0
        )
        
        // Empty path creates NoAudio state, not SecurityThreatDetected
        assertIs<SecureAudioPlayerState.NoAudio>(state)
    }
    
    @Test
    fun `userMessage should not expose security details`() {
        val maliciousPath = "../../../etc/passwd"
        val state = SecureAudioPlayerState.fromNoteData(
            recordingPath = maliciousPath,
            noteId = 123L,
            noteDurationMs = 30000
        )
        
        assertIs<SecureAudioPlayerState.SecurityThreatDetected>(state)
        
        // User message should not contain the malicious path
        assertFalse(
            state.userMessage.contains("passwd"),
            "User message should not expose attempted target: ${state.userMessage}"
        )
        
        // User message should not contain technical security terms
        assertFalse(
            state.userMessage.contains("traversal"),
            "User message should not contain technical security terms: ${state.userMessage}"
        )
        
        // User message should be user-friendly
        assertTrue(
            state.userMessage.contains("security") || 
            state.userMessage.contains("not accessible") ||
            state.userMessage.contains("not supported"),
            "User message should be user-friendly: ${state.userMessage}"
        )
    }
    
    @Test
    fun `base directory validation should work correctly`() {
        val allowedBaseDirectory = "/app/audio/"
        
        // Path within allowed directory
        val allowedPath = "/app/audio/recording.mp3"
        val allowedState = SecureAudioPlayerState.fromNoteData(
            recordingPath = allowedPath,
            noteId = 123L,
            noteDurationMs = 30000,
            allowedBaseDirectory = allowedBaseDirectory
        )
        assertIs<SecureAudioPlayerState.Validated>(allowedState)
        
        // Path outside allowed directory
        val disallowedPath = "/other/directory/audio.mp3"
        val disallowedState = SecureAudioPlayerState.fromNoteData(
            recordingPath = disallowedPath,
            noteId = 123L,
            noteDurationMs = 30000,
            allowedBaseDirectory = allowedBaseDirectory
        )
        assertIs<SecureAudioPlayerState.SecurityThreatDetected>(disallowedState)
        assertEquals(SecurityThreat.CRITICAL, disallowedState.threatLevel)
    }
}