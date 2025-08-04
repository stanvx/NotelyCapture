package com.module.notelycompose.testing

/**
 * Test data builder utilities for creating consistent test data across the application.
 * 
 * This object provides factory methods for creating test instances of domain objects
 * with sensible defaults and the ability to override specific properties.
 */
object TestDataBuilder {
    
    /**
     * Creates a test Note with configurable properties.
     * 
     * @param id The note ID (default: 1L)
     * @param title The note title (default: "Test Note")
     * @param content The note content (default: "Test Content")
     * @param dateCreated Creation timestamp (default: current time)
     * @param dateModified Modification timestamp (default: current time)
     * @param isStarred Whether the note is starred (default: false)
     * @param audioPath Path to audio file (default: null)
     * @param audioDuration Audio duration in milliseconds (default: null)
     * @return A Note instance with the specified properties
     */
    fun createTestNote(
        id: Long = 1L,
        title: String = "Test Note",
        content: String = "Test Content",
        dateCreated: Long = System.currentTimeMillis(),
        dateModified: Long = System.currentTimeMillis(),
        isStarred: Boolean = false,
        audioPath: String? = null,
        audioDuration: Long? = null
    ): TestNote = TestNote(
        id = id,
        title = title,
        content = content,
        dateCreated = dateCreated,
        dateModified = dateModified,
        isStarred = isStarred,
        audioPath = audioPath,
        audioDuration = audioDuration
    )
    
    /**
     * Creates a test ValidationResult with configurable properties.
     * 
     * @param isValid Whether the validation passed (default: true)
     * @param errorMessage Error message if validation failed (default: null)
     * @return A ValidationResult instance
     */
    fun createTestValidationResult(
        isValid: Boolean = true,
        errorMessage: String? = null
    ): TestValidationResult = TestValidationResult(isValid, errorMessage)
    
    /**
     * Creates a test User for authentication scenarios.
     * 
     * @param id User ID (default: 1L)
     * @param username Username (default: "testuser")
     * @param email Email address (default: "test@example.com")
     * @return A User instance
     */
    fun createTestUser(
        id: Long = 1L,
        username: String = "testuser",
        email: String = "test@example.com"
    ): TestUser = TestUser(
        id = id,
        username = username,
        email = email
    )
    
    /**
     * Generates long content for testing scenarios with large text.
     * 
     * @param length Approximate length of content to generate
     * @return A string of the specified approximate length
     */
    fun generateLongContent(length: Int): String {
        return buildString {
            val baseText = "This is a long content note for testing purposes. "
            repeat(length / baseText.length + 1) {
                if (this.length + baseText.length <= length) {
                    append(baseText)
                }
            }
        }.take(length)
    }
    
    /**
     * Creates HTML content for rich text testing.
     * 
     * @param includeUnsafeContent Whether to include potentially unsafe HTML (default: false)
     * @return HTML content string
     */
    fun createHtmlContent(includeUnsafeContent: Boolean = false): String {
        val safeContent = """
            <h1>Test Heading</h1>
            <p>This is a <strong>bold</strong> paragraph with <em>italic</em> text.</p>
            <ul>
                <li>List item 1</li>
                <li>List item 2</li>
            </ul>
        """.trimIndent()
        
        return if (includeUnsafeContent) {
            """
                $safeContent
                <script>alert('XSS attempt');</script>
                <img src="javascript:alert('XSS')" onerror="alert('XSS')">
            """.trimIndent()
        } else {
            safeContent
        }
    }
}

/**
 * Test representation of a Note for testing purposes.
 * This avoids dependency on actual domain models that might not be available in tests.
 */
data class TestNote(
    val id: Long,
    val title: String,
    val content: String,
    val dateCreated: Long,
    val dateModified: Long,
    val isStarred: Boolean,
    val audioPath: String? = null,
    val audioDuration: Long? = null
)

/**
 * Test representation of a ValidationResult.
 */
data class TestValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
)

/**
 * Test representation of a User.
 */
data class TestUser(
    val id: Long,
    val username: String,
    val email: String
)