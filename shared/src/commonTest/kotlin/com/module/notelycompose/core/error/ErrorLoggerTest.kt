package com.module.notelycompose.core.error

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Comprehensive tests for the ErrorLogger system
 */
class ErrorLoggerTest {

    @Test
    fun testBasicErrorLogging() = runTest {
        ErrorLogger.clearErrors()
        
        val error = RuntimeException("Test error")
        val context = ErrorContext(
            component = "TestComponent",
            operation = "testOperation"
        )
        
        ErrorLogger.logError(
            error = error,
            context = context,
            severity = ErrorSeverity.HIGH,
            userMessage = "Test error occurred"
        )
        
        val recentErrors = ErrorLogger.getRecentErrors(10)
        assertEquals(1, recentErrors.size)
        
        val logEntry = recentErrors.first()
        assertEquals(ErrorSeverity.HIGH, logEntry.severity)
        assertEquals("TestComponent", logEntry.context.component)
        assertEquals("testOperation", logEntry.context.operation)
        assertEquals("Test error", logEntry.errorMessage)
        assertEquals("Test error occurred", logEntry.userMessage)
    }

    @Test
    fun testMalformedDataLogging() = runTest {
        ErrorLogger.clearErrors()
        
        ErrorLogger.logMalformedData(
            noteId = 123L,
            field = "title",
            value = "Very long title that exceeds limits...",
            context = ErrorContext("NoteValidator", "validateTitle")
        )
        
        val recentErrors = ErrorLogger.getRecentErrors(10)
        assertEquals(1, recentErrors.size)
        
        val logEntry = recentErrors.first()
        assertEquals(ErrorSeverity.HIGH, logEntry.severity)
        assertTrue(logEntry.userMessage?.contains("field 'title'") == true)
        assertTrue(logEntry.userMessage?.contains("note 123") == true)
    }

    @Test
    fun testErrorLogSizeLimit() = runTest {
        ErrorLogger.clearErrors()
        
        // Add more than the limit
        repeat(600) { i ->
            ErrorLogger.logError(
                error = RuntimeException("Error $i"),
                context = ErrorContext("TestComponent", "testOperation$i"),
                severity = ErrorSeverity.LOW
            )
        }
        
        val recentErrors = ErrorLogger.getRecentErrors(1000)
        assertTrue(recentErrors.size <= 500, "Error log should respect size limit")
        
        // Verify the most recent errors are kept
        val lastError = recentErrors.last()
        assertTrue(lastError.errorMessage.contains("Error 599"))
    }

    @Test
    fun testErrorStatistics() = runTest {
        ErrorLogger.clearErrors()
        
        // Add various error types and severities
        ErrorLogger.logError(
            error = RuntimeException("Critical error"),
            context = ErrorContext("ComponentA", "operation1"),
            severity = ErrorSeverity.CRITICAL
        )
        
        ErrorLogger.logError(
            error = IllegalArgumentException("High error"),
            context = ErrorContext("ComponentB", "operation2"),
            severity = ErrorSeverity.HIGH
        )
        
        ErrorLogger.logError(
            error = IllegalStateException("Another high error"),
            context = ErrorContext("ComponentA", "operation3"),
            severity = ErrorSeverity.HIGH
        )
        
        val stats = ErrorLogger.getErrorStats()
        assertEquals(3, stats.totalErrors)
        assertEquals(1, stats.criticalErrors)
        assertEquals("ComponentA", stats.mostProblematicComponent)
        assertTrue(stats.mostCommonErrorType.isNotEmpty())
    }

    @Test
    fun testConcurrentLogging() = runTest {
        ErrorLogger.clearErrors()
        
        // This test verifies thread safety of the logging system
        val errors = (1..50).map { i ->
            RuntimeException("Concurrent error $i")
        }
        
        errors.forEach { error ->
            ErrorLogger.logError(
                error = error,
                context = ErrorContext("ConcurrentComponent", "concurrentOperation"),
                severity = ErrorSeverity.MEDIUM
            )
        }
        
        val recentErrors = ErrorLogger.getRecentErrors(100)
        assertEquals(50, recentErrors.size)
        
        // Verify all errors were logged
        val errorMessages = recentErrors.map { it.errorMessage }.toSet()
        assertEquals(50, errorMessages.size)
    }

    @Test
    fun testErrorMessageSanitization() = runTest {
        ErrorLogger.clearErrors()
        
        // Test with potentially sensitive information
        val sensitiveError = RuntimeException("Database password: secret123")
        ErrorLogger.logMalformedData(
            noteId = 456L,
            field = "content",
            value = "User password is secret123 and credit card is 1234-5678-9012-3456",
            context = ErrorContext("DataProcessor", "processContent")
        )
        
        val recentErrors = ErrorLogger.getRecentErrors(10)
        val logEntry = recentErrors.first()
        
        // Verify value is truncated for privacy
        assertTrue(logEntry.stackTrace.length <= 1000, "Stack trace should be truncated")
    }
}

/**
 * Tests for NoteDataValidator
 */
class NoteDataValidatorTest {

    @Test
    fun testValidNotePassesValidation() = runTest {
        val validNote = createValidNote()
        
        val result = NoteDataValidator.validateAndSanitize(validNote)
        assertNotNull(result)
        assertEquals(validNote.id, result.id)
        assertEquals(validNote.title, result.title)
        assertEquals(validNote.content, result.content)
    }

    @Test
    fun testInvalidIdRejectsNote() = runTest {
        val invalidNote = createValidNote().copy(id = -1L)
        
        val result = NoteDataValidator.validateAndSanitize(invalidNote)
        assertEquals(null, result)
    }

    @Test
    fun testLongTitleIsTruncated() = runTest {
        val longTitle = "A".repeat(1500)
        val noteWithLongTitle = createValidNote().copy(title = longTitle)
        
        val result = NoteDataValidator.validateAndSanitize(noteWithLongTitle)
        assertNotNull(result)
        assertTrue(result.title.length <= 1003) // 1000 + "..."
        assertTrue(result.title.endsWith("..."))
    }

    @Test
    fun testNullCharactersAreRemoved() = runTest {
        val titleWithNulls = "Title\u0000with\u0000nulls"
        val contentWithNulls = "Content\u0000with\u0000nulls"
        val noteWithNulls = createValidNote().copy(
            title = titleWithNulls,
            content = contentWithNulls
        )
        
        val result = NoteDataValidator.validateAndSanitize(noteWithNulls)
        assertNotNull(result)
        assertEquals("Title with nulls", result.title)
        assertEquals("Content with nulls", result.content)
    }

    @Test
    fun testSuspiciousPathsAreCleared() = runTest {
        val suspiciousPath = "/path/../../../etc/passwd"
        val noteWithSuspiciousPath = createValidNote().copy(recordingPath = suspiciousPath)
        
        val result = NoteDataValidator.validateAndSanitize(noteWithSuspiciousPath)
        assertNotNull(result)
        assertEquals("", result.recordingPath)
    }

    @Test
    fun testInvalidTimestampIsReplaced() = runTest {
        val invalidTimestamp = "not-a-timestamp"
        val noteWithInvalidTimestamp = createValidNote().copy(createdAt = invalidTimestamp)
        
        val result = NoteDataValidator.validateAndSanitize(noteWithInvalidTimestamp)
        assertNotNull(result)
        assertEquals("1970-01-01T00:00:00Z", result.createdAt)
    }

    @Test
    fun testNegativeValuesAreCorrected() = runTest {
        val noteWithNegativeValues = createValidNote().copy(
            words = -5,
            audioDurationMs = -1000
        )
        
        val result = NoteDataValidator.validateAndSanitize(noteWithNegativeValues)
        assertNotNull(result)
        assertEquals(0, result.words)
        assertEquals(0, result.audioDurationMs)
    }

    @Test
    fun testExcessivelyLargeValuesAreCapped() = runTest {
        val noteWithLargeValues = createValidNote().copy(
            words = 200000,
            audioDurationMs = 48 * 60 * 60 * 1000 // 48 hours
        )
        
        val result = NoteDataValidator.validateAndSanitize(noteWithLargeValues)
        assertNotNull(result)
        assertEquals(100000, result.words)
        assertEquals(24 * 60 * 60 * 1000, result.audioDurationMs) // Capped at 24 hours
    }

    @Test
    fun testQuickValidationMethod() = runTest {
        val validNote = createValidNote()
        val invalidNote = createValidNote().copy(id = -1L)
        
        assertTrue(NoteDataValidator.isValid(validNote))
        assertTrue(!NoteDataValidator.isValid(invalidNote))
    }

    private fun createValidNote() = com.module.notelycompose.notes.ui.list.model.NoteUiModel(
        id = 1L,
        title = "Valid Title",
        content = "Valid content for testing",
        isStarred = false,
        isVoice = false,
        createdAt = "2023-01-01T12:00:00Z",
        recordingPath = "/valid/path/audio.mp3",
        words = 50,
        audioDurationMs = 30000
    )
}