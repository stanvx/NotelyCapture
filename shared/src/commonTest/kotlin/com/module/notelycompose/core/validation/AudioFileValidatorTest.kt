package com.module.notelycompose.core.validation

import com.module.notelycompose.transcription.error.TranscriptionError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Test suite for AudioFileValidator
 * 
 * Tests cover:
 * - File format validation
 * - Path security validation
 * - File existence and access validation
 * - Error message generation
 * - Security utilities
 */
class AudioFileValidatorTest {
    
    @Test
    fun `validateAudioFile should reject empty file path`() {
        // When: Validating empty path
        val result = AudioFileValidator.validateAudioFile("")
        
        // Then: Should fail with validation error
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<TranscriptionError.AudioFileValidationError>(error)
        assertEquals("Audio file path cannot be empty", error.message)
    }
    
    @Test
    fun `validateAudioFile should reject blank file path`() {
        // When: Validating blank path
        val result = AudioFileValidator.validateAudioFile("   ")
        
        // Then: Should fail with validation error
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<TranscriptionError.AudioFileValidationError>(error)
        assertEquals("Audio file path cannot be empty", error.message)
    }
    
    @Test
    fun `validateAudioFile should reject unsupported file extensions`() {
        val unsupportedExtensions = listOf("txt", "pdf", "doc", "xyz", "")
        
        unsupportedExtensions.forEach { extension ->
            // When: Validating unsupported extension
            val fileName = if (extension.isEmpty()) "audio" else "audio.$extension"
            val result = AudioFileValidator.validateAudioFile("/test/path/$fileName")
            
            // Then: Should fail with format error
            assertTrue(result.isFailure, "Should reject .$extension files")
            val error = result.exceptionOrNull()
            assertIs<TranscriptionError.AudioFileValidationError>(error)
            assertTrue(
                error.message.contains("extension") || error.message.contains("format"),
                "Error message should mention file format issue: ${error.message}"
            )
        }
    }
    
    @Test
    fun `validateAudioFile should accept supported file extensions`() {
        val supportedExtensions = listOf("wav", "mp3", "m4a", "aac", "flac", "ogg", "mp4", "wma")
        
        supportedExtensions.forEach { extension ->
            // When: Validating supported extension (without platform-specific checks)
            val result = AudioFileValidator.validateAudioFile("/test/path/audio.$extension")
            
            // Note: This may still fail due to file not existing, but should not fail on format
            val error = result.exceptionOrNull()
            if (error is TranscriptionError.AudioFileValidationError) {
                assertFalse(
                    error.message.contains("Unsupported audio format"),
                    "Should not reject .$extension files for format reasons: ${error.message}"
                )
            }
        }
    }
    
    @Test
    fun `validateAudioFile should handle case insensitive extensions`() {
        val extensions = listOf("WAV", "Mp3", "M4A", "AAC")
        
        extensions.forEach { extension ->
            // When: Validating uppercase/mixed case extension
            val result = AudioFileValidator.validateAudioFile("/test/path/audio.$extension")
            
            // Then: Should not fail on format (case insensitive)
            val error = result.exceptionOrNull()
            if (error is TranscriptionError.AudioFileValidationError) {
                assertFalse(
                    error.message.contains("Unsupported audio format"),
                    "Should handle case insensitive extensions: $extension"
                )
            }
        }
    }
    
    @Test
    fun `validateAudioFile should reject directory traversal attempts`() {
        val maliciousPaths = listOf(
            "/app/data/../../../etc/passwd",
            "/app/data/./../../secrets.txt",
            "/app/data/audio/../../../system.wav",
            "/app/data/subdir/../../outside.mp3"
        )
        
        maliciousPaths.forEach { path ->
            // When: Validating path with traversal
            val result = AudioFileValidator.validateAudioFile(path, "/app/data")
            
            // Then: Should fail with security error
            assertTrue(result.isFailure, "Should reject traversal path: $path")
            val error = result.exceptionOrNull()
            assertIs<TranscriptionError.AudioFileValidationError>(error)
            assertTrue(
                error.message.contains("directory traversal") || error.message.contains("Invalid file path"),
                "Should detect directory traversal: ${error.message}"
            )
        }
    }
    
    @Test
    fun `validateAudioFile should reject paths outside app directory`() {
        // When: Validating path outside app directory
        val result = AudioFileValidator.validateAudioFile("/system/bin/audio.wav", "/app/data")
        
        // Then: Should fail with security error
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<TranscriptionError.AudioFileValidationError>(error)
        assertTrue(
            error.message.contains("within app data directory"),
            "Should reject paths outside app directory: ${error.message}"
        )
    }
    
    @Test
    fun `validateAudioFile should accept valid paths within app directory`() {
        // When: Validating valid path within app directory (format-wise)
        val result = AudioFileValidator.validateAudioFile("/app/data/recordings/audio.wav", "/app/data")
        
        // Then: Should not fail on security validation (may fail on file access)
        val error = result.exceptionOrNull()
        if (error is TranscriptionError.AudioFileValidationError) {
            assertFalse(
                error.message.contains("directory traversal") || 
                error.message.contains("within app data directory"),
                "Should accept valid paths within app directory: ${error.message}"
            )
        }
    }
    
    @Test
    fun `getSecureFileName should return just filename for short paths`() {
        // When: Getting secure filename for short path
        val result = AudioFileValidator.getSecureFileName("/path/to/audio.wav")
        
        // Then: Should return just the filename
        assertEquals("audio.wav", result)
    }
    
    @Test
    fun `getSecureFileName should truncate long filenames for security`() {
        // Given: Very long filename
        val longFilename = "a".repeat(60) + ".wav"
        val path = "/path/to/$longFilename"
        
        // When: Getting secure filename
        val result = AudioFileValidator.getSecureFileName(path)
        
        // Then: Should be truncated with ellipsis
        assertTrue(result.length <= 50, "Should truncate long filenames")
        assertTrue(result.contains("..."), "Should contain ellipsis for truncated names")
        assertTrue(result.endsWith(".wav"), "Should preserve extension")
    }
    
    @Test
    fun `getSecureFileName should handle paths without directory separators`() {
        // When: Getting secure filename for filename only
        val result = AudioFileValidator.getSecureFileName("audio.wav")
        
        // Then: Should return the filename as-is
        assertEquals("audio.wav", result)
    }
    
    @Test
    fun `validation should work without app directory parameter`() {
        // When: Validating without app directory (skips security validation)
        val result = AudioFileValidator.validateAudioFile("/any/path/audio.wav")
        
        // Then: Should not fail on security checks (may fail on file access)
        val error = result.exceptionOrNull()
        if (error is TranscriptionError.AudioFileValidationError) {
            assertFalse(
                error.message.contains("directory traversal") || 
                error.message.contains("within app data directory"),
                "Should skip security validation when app directory not provided: ${error.message}"
            )
        }
    }
    
    @Test
    fun `error messages should be user friendly`() {
        // Test various error scenarios produce helpful messages
        val testCases = listOf(
            "" to "empty",
            "audio.txt" to "format",
            "audio" to "extension"
        )
        
        testCases.forEach { (path, expectedTopic) ->
            val result = AudioFileValidator.validateAudioFile(path)
            assertTrue(result.isFailure)
            
            val error = result.exceptionOrNull()
            assertIs<TranscriptionError.AudioFileValidationError>(error)
            assertTrue(
                error.message.isNotEmpty(),
                "Error message should not be empty for: $path"
            )
            assertTrue(
                error.filePath == path,
                "Error should preserve original file path"
            )
        }
    }
}