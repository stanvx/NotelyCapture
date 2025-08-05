package com.module.notelycompose.security

import androidx.compose.runtime.Stable
import com.module.notelycompose.security.AudioPathValidator.SecurityThreat
import com.module.notelycompose.security.AudioPathValidator.ValidationResult
import com.module.notelycompose.security.AudioPathValidator.ValidatedAudioPath

/**
 * Secure wrapper for audio player state that enforces path validation
 * and provides safe error handling for security violations.
 * 
 * This addresses the critical security vulnerability identified in Apple QA review
 * where audio file paths were passed directly to CompactAudioPlayer without validation.
 */
@Stable
sealed class SecureAudioPlayerState {
    /**
     * Audio path has been validated and is safe to use
     */
    data class Validated(
        val validatedPath: ValidatedAudioPath,
        val noteId: Long,
        val noteDurationMs: Int
    ) : SecureAudioPlayerState()
    
    /**
     * Audio path validation failed due to security threat
     */
    data class SecurityThreatDetected(
        val threatLevel: SecurityThreat,
        val reason: String,
        val originalPath: String,
        val noteId: Long
    ) : SecureAudioPlayerState() {
        
        /**
         * User-friendly error message that doesn't expose security details
         */
        val userMessage: String get() = when (threatLevel) {
            SecurityThreat.CRITICAL -> "Audio file is not accessible due to security restrictions"
            SecurityThreat.HIGH -> "Audio file format is not supported"
            SecurityThreat.MEDIUM -> "Audio file path is invalid"
            SecurityThreat.LOW -> "Audio file is unavailable"
        }
        
        /**
         * Whether this threat should be logged as a security incident
         */
        val shouldLogIncident: Boolean get() = threatLevel in setOf(
            SecurityThreat.CRITICAL, 
            SecurityThreat.HIGH
        )
    }
    
    /**
     * Audio file is not available (empty path or note has no audio)
     */
    data class NoAudio(
        val noteId: Long
    ) : SecureAudioPlayerState()
    
    companion object {
        /**
         * Factory method to create SecureAudioPlayerState from note data
         * 
         * @param recordingPath The recording path from the note
         * @param noteId The note identifier
         * @param noteDurationMs The audio duration in milliseconds
         * @param allowedBaseDirectory Optional base directory restriction
         * @return SecureAudioPlayerState with validation results
         */
        fun fromNoteData(
            recordingPath: String?,
            noteId: Long,
            noteDurationMs: Int,
            allowedBaseDirectory: String? = null
        ): SecureAudioPlayerState {
            // Handle empty or null paths
            if (recordingPath.isNullOrBlank()) {
                return NoAudio(noteId)
            }
            
            // Validate the audio path
            return when (val validation = AudioPathValidator.validateAudioPath(recordingPath, allowedBaseDirectory)) {
                is ValidationResult.Valid -> {
                    Validated(
                        validatedPath = ValidatedAudioPath(recordingPath),
                        noteId = noteId,
                        noteDurationMs = noteDurationMs
                    )
                }
                is ValidationResult.Invalid -> {
                    SecurityThreatDetected(
                        threatLevel = validation.securityThreat,
                        reason = validation.reason,
                        originalPath = recordingPath,
                        noteId = noteId
                    )
                }
            }
        }
        
        /**
         * Factory method with additional path validation for UI layer
         * 
         * @param recordingPath The recording path to validate
         * @param noteId The note identifier
         * @param noteDurationMs The audio duration
         * @param expectedNoteId Additional validation to ensure path matches expected note
         * @param allowedBaseDirectory Optional base directory restriction
         * @return SecureAudioPlayerState with comprehensive validation
         */
        fun validateForUi(
            recordingPath: String?,
            noteId: Long,
            noteDurationMs: Int,
            expectedNoteId: Long? = null,
            allowedBaseDirectory: String? = null
        ): SecureAudioPlayerState {
            // Additional validation: ensure note ID matches expected
            expectedNoteId?.let { expected ->
                if (noteId != expected) {
                    return SecurityThreatDetected(
                        threatLevel = SecurityThreat.HIGH,
                        reason = "Note ID mismatch in audio path validation",
                        originalPath = recordingPath ?: "",
                        noteId = noteId
                    )
                }
            }
            
            return fromNoteData(recordingPath, noteId, noteDurationMs, allowedBaseDirectory)
        }
    }
}

/**
 * Extension function to safely extract validated path for audio player usage
 */
fun SecureAudioPlayerState.getValidatedPathOrNull(): ValidatedAudioPath? {
    return when (this) {
        is SecureAudioPlayerState.Validated -> validatedPath
        is SecureAudioPlayerState.SecurityThreatDetected -> {
            // Log security incident if high-severity threat
            if (shouldLogIncident) {
                println("[SECURITY-INCIDENT] Blocked audio access: $reason (Threat: $threatLevel)")
            }
            null
        }
        is SecureAudioPlayerState.NoAudio -> null
    }
}

/**
 * Extension function to check if audio is safely available for playback
 */
fun SecureAudioPlayerState.isAudioSafelyAvailable(): Boolean {
    return this is SecureAudioPlayerState.Validated
}

/**
 * Extension function to get user-safe error message
 */
fun SecureAudioPlayerState.getUserErrorMessage(): String? {
    return when (this) {
        is SecureAudioPlayerState.SecurityThreatDetected -> userMessage
        is SecureAudioPlayerState.NoAudio -> null
        is SecureAudioPlayerState.Validated -> null
    }
}