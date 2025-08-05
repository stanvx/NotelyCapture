package com.module.notelycompose.testing

/**
 * Predefined test fixtures for common testing scenarios.
 * 
 * This object provides ready-to-use test data that represents common use cases
 * and edge cases that appear frequently in tests.
 */
object TestFixtures {
    
    /**
     * Standard note with typical content for basic testing scenarios.
     */
    val standardNote = TestDataBuilder.createTestNote(
        id = 1L,
        title = "Standard Note",
        content = "This is a standard test note with regular content that represents typical user input."
    )
    
    /**
     * Empty note for testing empty state scenarios.
     */
    val emptyNote = TestDataBuilder.createTestNote(
        id = 2L,
        title = "",
        content = ""
    )
    
    /**
     * Note with rich HTML content for testing rich text scenarios.
     */
    val richTextNote = TestDataBuilder.createTestNote(
        id = 3L,
        title = "Rich Text Note",
        content = TestDataBuilder.createHtmlContent(includeUnsafeContent = false)
    )
    
    /**
     * Note with malicious HTML content for security testing.
     */
    val maliciousContentNote = TestDataBuilder.createTestNote(
        id = 4L,
        title = "Malicious Content Note",
        content = TestDataBuilder.createHtmlContent(includeUnsafeContent = true)
    )
    
    /**
     * Note with very long content for performance and UI testing.
     */
    val longContentNote = TestDataBuilder.createTestNote(
        id = 5L,
        title = "Long Content Note",
        content = TestDataBuilder.generateLongContent(5000)
    )
    
    /**
     * Starred note for testing starred functionality.
     */
    val starredNote = TestDataBuilder.createTestNote(
        id = 6L,
        title = "Starred Note",
        content = "This note is starred for priority.",
        isStarred = true
    )
    
    /**
     * Note with audio attachment for testing audio functionality.
     */
    val audioNote = TestDataBuilder.createTestNote(
        id = 7L,
        title = "Audio Note",
        content = "This note has an audio recording attached.",
        audioPath = "/test/path/audio.wav",
        audioDuration = 30000L // 30 seconds
    )
    
    /**
     * List of sample notes for testing list scenarios.
     */
    val sampleNotes = listOf(
        standardNote,
        richTextNote,
        starredNote,
        audioNote,
        TestDataBuilder.createTestNote(
            id = 8L,
            title = "Another Note",
            content = "Additional note for list testing."
        )
    )
    
    /**
     * Valid validation result for success scenarios.
     */
    val validResult = TestDataBuilder.createTestValidationResult(
        isValid = true,
        errorMessage = null
    )
    
    /**
     * Invalid validation result for error scenarios.
     */
    val invalidResult = TestDataBuilder.createTestValidationResult(
        isValid = false,
        errorMessage = "Validation failed: Content contains invalid characters"
    )
    
    /**
     * Standard test user for authentication scenarios.
     */
    val standardUser = TestDataBuilder.createTestUser(
        id = 1L,
        username = "testuser",
        email = "test@example.com"
    )
    
    /**
     * Test cases for parameterized input validation tests.
     */
    val inputValidationTestCases = listOf(
        InputValidationTestCase("Valid input", "Hello World", true),
        InputValidationTestCase("Empty input", "", false),
        InputValidationTestCase("Whitespace only", "   ", false),
        InputValidationTestCase("Too long input", "a".repeat(10000), false),
        InputValidationTestCase("HTML injection", "<script>alert('xss')</script>", false),
        InputValidationTestCase("SQL injection", "'; DROP TABLE notes; --", false),
        InputValidationTestCase("Unicode content", "こんにちは世界", true),
        InputValidationTestCase("Special characters", "!@#$%^&*()", true),
        InputValidationTestCase("Mixed content", "Title with <em>emphasis</em>", true)
    )
    
    /**
     * Test cases for different playback speeds.
     */
    val playbackSpeedTestCases = listOf(
        PlaybackSpeedTestCase("Normal speed", 1.0f, true),
        PlaybackSpeedTestCase("Fast speed", 1.5f, true),
        PlaybackSpeedTestCase("Fastest speed", 2.0f, true),
        PlaybackSpeedTestCase("Invalid slow speed", 0.5f, false),
        PlaybackSpeedTestCase("Invalid fast speed", 3.0f, false),
        PlaybackSpeedTestCase("Zero speed", 0.0f, false),
        PlaybackSpeedTestCase("Negative speed", -1.0f, false)
    )
}

/**
 * Test case for input validation scenarios.
 */
data class InputValidationTestCase(
    val name: String,
    val input: String,
    val expectedValid: Boolean
)

/**
 * Test case for playback speed validation scenarios.
 */
data class PlaybackSpeedTestCase(
    val name: String,
    val speed: Float,
    val expectedValid: Boolean
)