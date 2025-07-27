package security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import com.module.notelycompose.security.HtmlSanitizer
import com.module.notelycompose.security.InputValidator

/**
 * Comprehensive security tests for HTML sanitization and input validation.
 * 
 * These tests ensure that the security vulnerabilities identified in task-044
 * have been properly addressed and that malicious inputs are safely handled.
 */
class SecurityTests {
    
    // HTML Injection (XSS) Prevention Tests
    
    @Test
    fun testBasicScriptInjectionPrevention() {
        val maliciousInput = "<script>alert('XSS')</script><p>Normal content</p>"
        val sanitized = HtmlSanitizer.sanitize(maliciousInput)
        
        assertFalse(sanitized.contains("<script>"), "Script tags should be removed")
        assertFalse(sanitized.contains("alert"), "JavaScript should be removed")
        assertTrue(sanitized.contains("<p>Normal content</p>"), "Safe HTML should be preserved")
    }
    
    @Test
    fun testAdvancedScriptInjectionPrevention() {
        val maliciousInputs = listOf(
            "<img src='x' onerror='alert(1)'>",
            "<div onclick='maliciousFunction()'>Click me</div>",
            "<iframe src='javascript:alert(1)'></iframe>",
            "<object data='javascript:alert(1)'></object>",
            "<embed src='javascript:alert(1)'>",
            "<link rel='stylesheet' href='javascript:alert(1)'>",
            "<style>body{background:url('javascript:alert(1)')}</style>",
            "<svg onload='alert(1)'><circle/></svg>",
            "<body onload='alert(1)'>",
            "<form action='javascript:alert(1)'>"
        )
        
        maliciousInputs.forEach { input ->
            val sanitized = HtmlSanitizer.sanitize(input)
            assertFalse(
                sanitized.contains("javascript:") || 
                sanitized.contains("alert") || 
                sanitized.contains("onerror") ||
                sanitized.contains("onclick") ||
                sanitized.contains("onload"),
                "Dangerous attributes should be removed from: $input"
            )
        }
    }
    
    @Test
    fun testSafeHtmlPreservation() {
        val safeInputs = listOf(
            "<p>Normal paragraph</p>",
            "<strong>Bold text</strong>",
            "<em>Italic text</em>",
            "<ul><li>List item</li></ul>",
            "<ol><li>Numbered item</li></ol>",
            "<h1>Heading</h1>",
            "<blockquote>Quote</blockquote>",
            "<a href='https://example.com'>Safe link</a>"
        )
        
        safeInputs.forEach { input ->
            val sanitized = HtmlSanitizer.sanitize(input)
            assertEquals(input, sanitized, "Safe HTML should be preserved: $input")
        }
    }
    
    @Test
    fun testHtmlValidationResult() {
        val safeContent = "<p>Safe content</p>"
        val unsafeContent = "<script>alert('xss')</script><p>Content</p>"
        
        val safeResult = HtmlSanitizer.validateAndSanitize(safeContent)
        assertTrue(safeResult.isSafe, "Safe content should be marked as safe")
        assertFalse(safeResult.wasModified, "Safe content should not be modified")
        
        val unsafeResult = HtmlSanitizer.validateAndSanitize(unsafeContent)
        assertFalse(unsafeResult.isSafe, "Unsafe content should be marked as unsafe")
        assertTrue(unsafeResult.wasModified, "Unsafe content should be modified")
        assertNotEquals(unsafeContent, unsafeResult.sanitizedContent, "Unsafe content should be different after sanitization")
    }
    
    @Test
    fun testEmptyAndBlankContent() {
        assertEquals("", HtmlSanitizer.sanitize(""), "Empty string should remain empty")
        assertEquals("   ", HtmlSanitizer.sanitize("   "), "Whitespace should be preserved")
        assertEquals("", HtmlSanitizer.sanitize("   "), "Blank string should remain blank")
    }
    
    // Path Traversal Prevention Tests
    
    @Test
    fun testBasicPathTraversalPrevention() {
        val maliciousPaths = listOf(
            "../../../etc/passwd",
            "..\\..\\Windows\\System32",
            "/etc/shadow",
            "C:\\Windows\\System32\\config\\SAM",
            "../../sensitive/file.txt",
            "recordings/../../../etc/passwd",
            "recordings\\..\\..\\windows\\system32"
        )
        
        val safeRecordingsDir = "/app/recordings"
        
        maliciousPaths.forEach { path ->
            val result = InputValidator.validateRecordingPath(path, safeRecordingsDir)
            assertFalse(result.isValid, "Malicious path should be rejected: $path")
        }
    }
    
    @Test
    fun testSafePathAcceptance() {
        val safePaths = listOf(
            "/app/recordings/audio.mp3",
            "/app/recordings/subfolder/recording.wav",
            "/app/recordings/user_note_123.m4a"
        )
        
        val safeRecordingsDir = "/app/recordings"
        
        safePaths.forEach { path ->
            val result = InputValidator.validateRecordingPath(path, safeRecordingsDir)
            assertTrue(result.isValid, "Safe path should be accepted: $path")
        }
    }
    
    @Test
    fun testInvalidAudioFileExtensions() {
        val invalidFiles = listOf(
            "/app/recordings/malicious.exe",
            "/app/recordings/script.bat",
            "/app/recordings/config.ini",
            "/app/recordings/document.pdf"
        )
        
        val safeRecordingsDir = "/app/recordings"
        
        invalidFiles.forEach { path ->
            val result = InputValidator.validateRecordingPath(path, safeRecordingsDir)
            assertFalse(result.isValid, "Non-audio file should be rejected: $path")
        }
    }
    
    @Test
    fun testValidAudioFileExtensions() {
        val validFiles = listOf(
            "/app/recordings/audio.mp3",
            "/app/recordings/recording.wav",
            "/app/recordings/voice.m4a",
            "/app/recordings/speech.aac",
            "/app/recordings/note.opus"
        )
        
        val safeRecordingsDir = "/app/recordings"
        
        validFiles.forEach { path ->
            val result = InputValidator.validateRecordingPath(path, safeRecordingsDir)
            assertTrue(result.isValid, "Valid audio file should be accepted: $path")
        }
    }
    
    // Input Validation Tests
    
    @Test
    fun testNoteTitleValidation() {
        val longTitle = "a".repeat(300)
        val validatedTitle = InputValidator.validateNoteTitle(longTitle)
        
        assertTrue(validatedTitle.length <= 200, "Title should be truncated to max length")
        
        val titleWithNewlines = "Title\nwith\nnewlines\tand\ttabs"
        val cleanTitle = InputValidator.validateNoteTitle(titleWithNewlines)
        assertFalse(cleanTitle.contains("\n") || cleanTitle.contains("\t"), "Newlines and tabs should be replaced")
    }
    
    @Test
    fun testNoteContentValidation() {
        val longContent = "a".repeat(150000)
        val validatedContent = InputValidator.validateNoteContent(longContent)
        
        assertTrue(validatedContent.length <= 100000, "Content should be truncated to max length")
        
        val normalContent = "Normal content"
        assertEquals(normalContent, InputValidator.validateNoteContent(normalContent), "Normal content should be unchanged")
    }
    
    @Test
    fun testFilenameValidation() {
        val validFilenames = listOf(
            "valid_filename.txt",
            "123-test.mp3",
            "recording.wav"
        )
        
        validFilenames.forEach { filename ->
            assertTrue(InputValidator.validateFileName(filename), "Valid filename should be accepted: $filename")
        }
        
        val invalidFilenames = listOf(
            "../malicious.txt",
            "file with spaces.txt",
            "file*with*wildcards.txt",
            ".hidden_file",
            "file..with..dots",
            ""
        )
        
        invalidFilenames.forEach { filename ->
            assertFalse(InputValidator.validateFileName(filename), "Invalid filename should be rejected: $filename")
        }
    }
    
    @Test
    fun testTextInputSanitization() {
        val maliciousText = "Normal text\u0000with\u0001control\u0002characters"
        val sanitized = InputValidator.validateTextInput(maliciousText)
        
        assertFalse(sanitized.contains("\u0000"), "Control characters should be removed")
        assertTrue(sanitized.contains("Normal text"), "Normal text should be preserved")
        assertTrue(sanitized.contains("with"), "Normal words should be preserved")
    }
    
    @Test
    fun testSafeFilenameCreation() {
        val inputs = mapOf(
            "Valid Name" to "Valid_Name",
            "file/with\\slashes" to "file_with_slashes",
            "multiple___underscores" to "multiple_underscores",
            "  spaced  " to "spaced",
            "" to "untitled",
            "!@#$%^&*()" to "untitled"
        )
        
        inputs.forEach { (input, expected) ->
            val result = InputValidator.createSafeFilename(input)
            assertEquals(expected, result, "Input '$input' should become '$expected'")
        }
    }
    
    @Test
    fun testEmptyPathValidation() {
        val result = InputValidator.validateRecordingPath("", "/app/recordings")
        assertTrue(result.isValid, "Empty path should be valid (represents no recording)")
        assertEquals("", result.sanitizedPath, "Empty path should remain empty")
    }
    
    @Test
    fun testPathValidationErrors() {
        val invalidInputs = listOf(
            "../../../etc/passwd",
            "malicious.exe",
            "/absolute/path/outside/recordings/file.mp3"
        )
        
        invalidInputs.forEach { path ->
            val result = InputValidator.validateRecordingPath(path, "/app/recordings")
            assertFalse(result.isValid, "Invalid path should fail validation: $path")
            assertTrue(result.errorMessage != null, "Error message should be provided for invalid path: $path")
        }
    }
}