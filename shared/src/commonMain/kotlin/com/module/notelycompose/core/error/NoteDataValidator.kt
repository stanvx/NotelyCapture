package com.module.notelycompose.core.error

import com.module.notelycompose.notes.ui.list.model.NoteUiModel

/**
 * Comprehensive validation system for note data to prevent malformed data issues.
 * Validates all note fields and provides safe fallback values.
 */
object NoteDataValidator {
    
    private const val MAX_TITLE_LENGTH = 1000
    private const val MAX_CONTENT_LENGTH = 50000
    private const val MAX_PATH_LENGTH = 500
    
    /**
     * Validate and sanitize a NoteUiModel, returning a safe version or null if critically invalid
     */
    fun validateAndSanitize(note: NoteUiModel): NoteUiModel? {
        try {
            // Critical validation - these must be valid or note is unusable
            if (note.id < 0) {
                ErrorLogger.logMalformedData(
                    noteId = note.id,
                    field = "id",
                    value = note.id.toString(),
                    context = ErrorContext("NoteDataValidator", "validateId")
                )
                return null
            }
            
            // Sanitize and validate individual fields
            val sanitizedTitle = sanitizeTitle(note.title, note.id)
            val sanitizedContent = sanitizeContent(note.content, note.id)
            val sanitizedPath = sanitizeRecordingPath(note.recordingPath, note.id)
            val sanitizedCreatedAt = sanitizeCreatedAt(note.createdAt, note.id)
            val sanitizedWords = sanitizeWords(note.words, note.id)
            val sanitizedDuration = sanitizeDuration(note.audioDurationMs, note.id)
            
            return note.copy(
                title = sanitizedTitle,
                content = sanitizedContent,
                recordingPath = sanitizedPath,
                createdAt = sanitizedCreatedAt,
                words = sanitizedWords,
                audioDurationMs = sanitizedDuration
            )
            
        } catch (e: Exception) {
            ErrorLogger.logError(
                error = e,
                context = ErrorContext("NoteDataValidator", "validateAndSanitize", 
                    mapOf("noteId" to note.id.toString())),
                severity = ErrorSeverity.HIGH,
                userMessage = "Failed to validate note data"
            )
            return null
        }
    }
    
    /**
     * Sanitize note title with fallback
     */
    private fun sanitizeTitle(title: String, noteId: Long): String {
        return try {
            when {
                title.length > MAX_TITLE_LENGTH -> {
                    ErrorLogger.logMalformedData(
                        noteId = noteId,
                        field = "title",
                        value = "Length: ${title.length}",
                        context = ErrorContext("NoteDataValidator", "sanitizeTitle")
                    )
                    title.take(MAX_TITLE_LENGTH).trim() + "..."
                }
                title.contains('\u0000') -> {
                    ErrorLogger.logMalformedData(
                        noteId = noteId,
                        field = "title",
                        value = "Contains null characters",
                        context = ErrorContext("NoteDataValidator", "sanitizeTitle")
                    )
                    title.replace('\u0000', ' ').trim()
                }
                else -> title.trim()
            }
        } catch (e: Exception) {
            ErrorLogger.logMalformedData(
                noteId = noteId,
                field = "title",
                value = "Sanitization failed",
                context = ErrorContext("NoteDataValidator", "sanitizeTitle"),
                error = e
            )
            "Untitled Note"
        }
    }
    
    /**
     * Sanitize note content with fallback
     */
    private fun sanitizeContent(content: String, noteId: Long): String {
        return try {
            when {
                content.length > MAX_CONTENT_LENGTH -> {
                    ErrorLogger.logMalformedData(
                        noteId = noteId,
                        field = "content",
                        value = "Length: ${content.length}",
                        context = ErrorContext("NoteDataValidator", "sanitizeContent")
                    )
                    content.take(MAX_CONTENT_LENGTH).trim() + "\n\n[Content truncated due to excessive length]"
                }
                content.contains('\u0000') -> {
                    ErrorLogger.logMalformedData(
                        noteId = noteId,
                        field = "content",
                        value = "Contains null characters",
                        context = ErrorContext("NoteDataValidator", "sanitizeContent")
                    )
                    content.replace('\u0000', ' ').trim()
                }
                else -> content
            }
        } catch (e: Exception) {
            ErrorLogger.logMalformedData(
                noteId = noteId,
                field = "content",
                value = "Sanitization failed",
                context = ErrorContext("NoteDataValidator", "sanitizeContent"),
                error = e
            )
            "[Content unavailable due to data corruption]"
        }
    }
    
    /**
     * Sanitize recording path
     */
    private fun sanitizeRecordingPath(path: String, noteId: Long): String {
        return try {
            when {
                path.length > MAX_PATH_LENGTH -> {
                    ErrorLogger.logMalformedData(
                        noteId = noteId,
                        field = "recordingPath",
                        value = "Length: ${path.length}",
                        context = ErrorContext("NoteDataValidator", "sanitizeRecordingPath")
                    )
                    ""
                }
                path.contains('\u0000') || path.contains("..") -> {
                    ErrorLogger.logMalformedData(
                        noteId = noteId,
                        field = "recordingPath",
                        value = "Contains suspicious characters",
                        context = ErrorContext("NoteDataValidator", "sanitizeRecordingPath")
                    )
                    ""
                }
                else -> path.trim()
            }
        } catch (e: Exception) {
            ErrorLogger.logMalformedData(
                noteId = noteId,
                field = "recordingPath",
                value = "Sanitization failed",
                context = ErrorContext("NoteDataValidator", "sanitizeRecordingPath"),
                error = e
            )
            ""
        }
    }
    
    /**
     * Sanitize created timestamp
     */
    private fun sanitizeCreatedAt(createdAt: String, noteId: Long): String {
        return try {
            // Basic validation - check if it looks like a timestamp
            when {
                createdAt.isBlank() -> {
                    ErrorLogger.logMalformedData(
                        noteId = noteId,
                        field = "createdAt",
                        value = "Empty timestamp",
                        context = ErrorContext("NoteDataValidator", "sanitizeCreatedAt")
                    )
                    "1970-01-01T00:00:00Z"
                }
                createdAt.length > 50 -> {
                    ErrorLogger.logMalformedData(
                        noteId = noteId,
                        field = "createdAt",
                        value = "Length: ${createdAt.length}",
                        context = ErrorContext("NoteDataValidator", "sanitizeCreatedAt")
                    )
                    "1970-01-01T00:00:00Z"
                }
                !createdAt.matches(Regex("[0-9T:\\-Z.+A-Za-z ]+")) -> {
                    ErrorLogger.logMalformedData(
                        noteId = noteId,
                        field = "createdAt",
                        value = "Invalid format",
                        context = ErrorContext("NoteDataValidator", "sanitizeCreatedAt")
                    )
                    "1970-01-01T00:00:00Z"
                }
                else -> createdAt.trim()
            }
        } catch (e: Exception) {
            ErrorLogger.logMalformedData(
                noteId = noteId,
                field = "createdAt",
                value = "Sanitization failed",
                context = ErrorContext("NoteDataValidator", "sanitizeCreatedAt"),
                error = e
            )
            "1970-01-01T00:00:00Z"
        }
    }
    
    /**
     * Sanitize word count
     */
    private fun sanitizeWords(words: Int, noteId: Long): Int {
        return try {
            when {
                words < 0 -> {
                    ErrorLogger.logMalformedData(
                        noteId = noteId,
                        field = "words",
                        value = words.toString(),
                        context = ErrorContext("NoteDataValidator", "sanitizeWords")
                    )
                    0
                }
                words > 100000 -> {
                    ErrorLogger.logMalformedData(
                        noteId = noteId,
                        field = "words",
                        value = words.toString(),
                        context = ErrorContext("NoteDataValidator", "sanitizeWords")
                    )
                    100000
                }
                else -> words
            }
        } catch (e: Exception) {
            ErrorLogger.logMalformedData(
                noteId = noteId,
                field = "words",
                value = "Sanitization failed",
                context = ErrorContext("NoteDataValidator", "sanitizeWords"),
                error = e
            )
            0
        }
    }
    
    /**
     * Sanitize audio duration
     */
    private fun sanitizeDuration(duration: Int, noteId: Long): Int {
        return try {
            when {
                duration < 0 -> {
                    ErrorLogger.logMalformedData(
                        noteId = noteId,
                        field = "audioDurationMs",
                        value = duration.toString(),
                        context = ErrorContext("NoteDataValidator", "sanitizeDuration")
                    )
                    0
                }
                duration > 24 * 60 * 60 * 1000 -> { // More than 24 hours
                    ErrorLogger.logMalformedData(
                        noteId = noteId,
                        field = "audioDurationMs",
                        value = duration.toString(),
                        context = ErrorContext("NoteDataValidator", "sanitizeDuration")
                    )
                    24 * 60 * 60 * 1000
                }
                else -> duration
            }
        } catch (e: Exception) {
            ErrorLogger.logMalformedData(
                noteId = noteId,
                field = "audioDurationMs",
                value = "Sanitization failed",
                context = ErrorContext("NoteDataValidator", "sanitizeDuration"),
                error = e
            )
            0
        }
    }
    
    /**
     * Quick validation check without full sanitization
     */
    fun isValid(note: NoteUiModel): Boolean {
        return try {
            note.id >= 0 &&
            note.title.length <= MAX_TITLE_LENGTH &&
            note.content.length <= MAX_CONTENT_LENGTH &&
            note.recordingPath.length <= MAX_PATH_LENGTH &&
            note.createdAt.isNotBlank() &&
            note.words >= 0 &&
            note.audioDurationMs >= 0
        } catch (e: Exception) {
            false
        }
    }
}