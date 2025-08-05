package com.module.notelycompose.security

import com.module.notelycompose.security.AudioPathValidator.SecurityThreat
import com.module.notelycompose.security.AudioPathValidator.ValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive test suite for AudioPathValidator security validation
 * 
 * This test suite validates that all identified security vulnerabilities
 * are properly prevented by the AudioPathValidator implementation.
 */
class AudioPathValidatorTest {
    
    @Test
    fun `validateAudioPath should accept valid audio files`() {
        val validPaths = listOf(
            "audio.mp3",
            "recording.wav",
            "voice_note.m4a",
            "audio/recording.aac",
            "files/audio.ogg",
            "recordings/voice.flac"
        )
        
        validPaths.forEach { path ->
            val result = AudioPathValidator.validateAudioPath(path)
            assertIs<ValidationResult.Valid>(result, "Valid path should pass: $path")
        }
    }
    
    @Test
    fun `validateAudioPath should reject null and empty paths`() {
        val invalidPaths = listOf(null, "", "   ", "\t", "\n")
        
        invalidPaths.forEach { path ->
            val result = AudioPathValidator.validateAudioPath(path)
            assertIs<ValidationResult.Invalid.EmptyPath>(result, "Empty path should be rejected: '$path'")
            assertEquals(SecurityThreat.LOW, result.securityThreat)
        }
    }
    
    @Test
    fun `validateAudioPath should reject path traversal attacks`() {
        val pathTraversalAttacks = listOf(
            "../../../etc/passwd",
            "..\\..\\Windows\\System32\\config\\SAM",
            "audio/../../../sensitive.txt",
            "..\\audio.mp3",
            "recordings/../../system.wav",
            "audio/../../../data/data/other.app/files/secret.m4a"
        )
        
        pathTraversalAttacks.forEach { path ->
            val result = AudioPathValidator.validateAudioPath(path)
            assertIs<ValidationResult.Invalid.PathTraversal>(result, "Path traversal should be blocked: $path")
            assertEquals(SecurityThreat.CRITICAL, result.securityThreat)
        }
    }
    
    @Test
    fun `validateAudioPath should reject protocol injection attacks`() {
        val protocolInjections = listOf(
            "file:///etc/passwd",
            "http://malicious.com/audio.mp3",
            "https://attacker.evil/malware.wav",
            "ftp://server.com/audio.m4a",
            "content://provider/audio.aac",
            "javascript:alert('xss').mp3"
        )
        
        protocolInjections.forEach { path ->
            val result = AudioPathValidator.validateAudioPath(path)
            assertIs<ValidationResult.Invalid.ProtocolInjection>(result, "Protocol injection should be blocked: $path")
            assertEquals(SecurityThreat.CRITICAL, result.securityThreat)
        }
    }
    
    @Test
    fun `validateAudioPath should reject system path access attempts`() {
        val systemPaths = listOf(
            "/system/bin/audio.mp3",
            "/data/data/other.app/audio.wav",
            "C:\\Windows\\System32\\audio.m4a",
            "/etc/audio.aac",
            "/usr/bin/audio.ogg",
            "\\Windows\\System32\\config\\audio.flac"
        )
        
        systemPaths.forEach { path ->
            val result = AudioPathValidator.validateAudioPath(path)
            assertIs<ValidationResult.Invalid.SystemPathAccess>(result, "System path access should be blocked: $path")
            assertEquals(SecurityThreat.CRITICAL, result.securityThreat)
        }
    }
    
    @Test
    fun `validateAudioPath should reject malicious characters`() {
        val maliciousCharacters = listOf(
            "audio<script>.mp3",
            "recording>output.wav",
            "audio|pipe.m4a",
            "recording&command.aac",
            "audio;injection.ogg",
            "recording`backtick`.flac",
            "audio\$variable.mp3",
            "recording\"quote.wav",
            "audio'quote.m4a"
        )
        
        maliciousCharacters.forEach { path ->
            val result = AudioPathValidator.validateAudioPath(path)
            assertIs<ValidationResult.Invalid.MaliciousCharacters>(result, "Malicious characters should be blocked: $path")
            assertEquals(SecurityThreat.HIGH, result.securityThreat)
        }
    }
    
    @Test
    fun `validateAudioPath should reject invalid file extensions`() {
        val invalidExtensions = listOf(
            "document.txt",
            "script.js",
            "executable.exe",
            "config.xml",
            "data.json",
            "image.jpg",
            "video.mp4",
            "noextension",
            "audio.",
            "audio.unknown"
        )
        
        invalidExtensions.forEach { path ->
            val result = AudioPathValidator.validateAudioPath(path)
            assertIs<ValidationResult.Invalid.InvalidExtension>(result, "Invalid extension should be blocked: $path")
            assertEquals(SecurityThreat.HIGH, result.securityThreat)
        }
    }
    
    @Test
    fun `validateAudioPath should reject excessively long paths`() {
        val longPath = "a".repeat(300) + ".mp3"
        
        val result = AudioPathValidator.validateAudioPath(longPath)
        assertIs<ValidationResult.Invalid.PathTooLong>(result)
        assertEquals(SecurityThreat.MEDIUM, result.securityThreat)
    }
    
    @Test
    fun `validateAudioPath should reject URL encoded attacks`() {
        val urlEncodedAttacks = listOf(
            "audio%2e%2e%2fpasswd.mp3",
            "recording%2f%2e%2e%2fconfig.wav",
            "audio%5c%2e%2e%5csystem.m4a"
        )
        
        urlEncodedAttacks.forEach { path ->
            val result = AudioPathValidator.validateAudioPath(path)
            assertIs<ValidationResult.Invalid.MaliciousCharacters>(result, "URL encoded attack should be blocked: $path")
            assertEquals(SecurityThreat.HIGH, result.securityThreat)
        }
    }
    
    @Test
    fun `validateAudioPath should reject template injection patterns`() {
        val templateInjections = listOf(
            "audio\${injection}.mp3",
            "recording#{expression}.wav",
            "audio{{template}}.m4a"
        )
        
        templateInjections.forEach { path ->
            val result = AudioPathValidator.validateAudioPath(path)
            assertIs<ValidationResult.Invalid.MaliciousCharacters>(result, "Template injection should be blocked: $path")
            assertEquals(SecurityThreat.HIGH, result.securityThreat)
        }
    }
    
    @Test
    fun `validateAudioPath should validate against base directory when provided`() {
        val allowedBaseDirectory = "/app/audio/"
        
        // Valid path within base directory
        val validPath = "/app/audio/recording.mp3"
        val validResult = AudioPathValidator.validateAudioPath(validPath, allowedBaseDirectory)
        assertIs<ValidationResult.Valid>(validResult)
        
        // Invalid path outside base directory
        val invalidPath = "/other/directory/audio.mp3"
        val invalidResult = AudioPathValidator.validateAudioPath(invalidPath, allowedBaseDirectory)
        assertIs<ValidationResult.Invalid.SystemPathAccess>(invalidResult)
        assertEquals(SecurityThreat.CRITICAL, invalidResult.securityThreat)
    }
    
    @Test
    fun `getValidatedPath should return ValidatedAudioPath for safe paths`() {
        val safePath = "recording.mp3"
        val validatedPath = AudioPathValidator.getValidatedPath(safePath)
        
        assertNotNull(validatedPath)
        assertEquals(safePath, validatedPath.path)
    }
    
    @Test
    fun `getValidatedPath should return null for dangerous paths`() {
        val dangerousPaths = listOf(
            "../../../etc/passwd",
            "http://malicious.com/audio.mp3",
            "audio<script>.mp3",
            null,
            ""
        )
        
        dangerousPaths.forEach { path ->
            val validatedPath = AudioPathValidator.getValidatedPath(path)
            assertNull(validatedPath, "Dangerous path should return null: $path")
        }
    }
    
    @Test
    fun `validation should handle edge cases correctly`() {
        val edgeCases = mapOf(
            "AUDIO.MP3" to true, // Case insensitive extension
            "recording.MP3" to true,
            "audio.Mp3" to true,
            ".mp3" to false, // No filename
            "mp3" to false, // No extension separator
            "audio..mp3" to false, // Double dots in filename
            "audio/.mp3" to false, // Invalid filename
            "/audio.mp3" to true, // Root path (if allowed)
            "\\audio.mp3" to true // Windows path separator
        )
        
        edgeCases.forEach { (path, shouldBeValid) ->
            val result = AudioPathValidator.validateAudioPath(path)
            if (shouldBeValid) {
                assertIs<ValidationResult.Valid>(result, "Edge case should be valid: $path")
            } else {
                assertIs<ValidationResult.Invalid>(result, "Edge case should be invalid: $path")
            }
        }
    }
    
    @Test
    fun `validation messages should be user-friendly and not expose security details`() {
        val dangerousPath = "../../../etc/passwd"
        val result = AudioPathValidator.validateAudioPath(dangerousPath)
        
        assertIs<ValidationResult.Invalid.PathTraversal>(result)
        assertTrue(
            result.reason.contains("path traversal"),
            "Security message should indicate path traversal without exposing system details"
        )
        
        // Ensure the message doesn't contain the actual dangerous path
        assertTrue(
            !result.reason.contains("/etc/passwd"),
            "Security message should not expose the attempted target path"
        )
    }
}