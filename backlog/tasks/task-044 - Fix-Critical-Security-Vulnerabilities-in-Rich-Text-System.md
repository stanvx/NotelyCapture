---
id: task-044
title: Fix Critical Security Vulnerabilities in Rich Text System
status: To Do
assignee: []
created_date: '2025-07-26'
labels: []
dependencies: []
---

# task-044 - Fix Critical Security Vulnerabilities in Rich Text System

## Description

**CRITICAL SECURITY ISSUE**: The rich text editing system has two severe security vulnerabilities that must be fixed immediately before production deployment:
1. **HTML Injection (XSS)**: Raw content is passed to `setHtml()` without sanitization
2. **Path Traversal**: Audio file operations use unvalidated file paths from database

These vulnerabilities could allow malicious users to execute scripts or access arbitrary files on the device.

**Reference Document**: See [Material 3 Expressive Design Implementation Guide](../docs/doc-001%20-%20Material%203%20Expressive%20Design%20Implementation%20Guide.md) - Section "Rich Text Editor System > Security Enhancements"

## Acceptance Criteria

- [ ] HTML injection vulnerability is completely eliminated
- [ ] Path traversal vulnerability is resolved with proper validation
- [ ] All user inputs are sanitized before processing
- [ ] Security tests pass for all input scenarios
- [ ] Code review confirms no remaining security gaps

## Implementation Plan

### Phase 1: HTML Sanitization Implementation

**Files to Modify:**
- `shared/src/commonMain/kotlin/com/module/notelycompose/notes/presentation/helpers/RichTextEditorHelper.kt`
- `shared/src/commonMain/kotlin/com/module/notelycompose/notes/domain/TextEditCommand.kt`
- `shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/detail/NoteDetailScreen.kt`

**Step 1: Add HTML Sanitizer Dependency**
```kotlin
// Add to shared/build.gradle.kts dependencies
implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20220608.1")
```

**Step 2: Create HTML Sanitization Utility**
```kotlin
// Create new file: shared/src/commonMain/kotlin/com/module/notelycompose/security/HtmlSanitizer.kt
object HtmlSanitizer {
    private val policy = Sanitizers.FORMATTING
        .and(Sanitizers.LINKS)
        .and(Sanitizers.BLOCKS)
        .and(Sanitizers.STYLES)
        .and(Sanitizers.TABLES)
        
    fun sanitize(html: String): String {
        return policy.sanitize(html)
    }
}
```

**Step 3: Fix RichTextEditorHelper.kt**
```kotlin
// BEFORE (VULNERABLE - Line 41)
_richTextState.value = RichTextState().apply {
    setHtml(content) // ⚠️ Raw content injection
}

// AFTER (SECURE)
private var lastSetContent: String? = null

fun setContent(content: String) {
    if (content != lastSetContent) {
        lastSetContent = content
        val sanitizedContent = HtmlSanitizer.sanitize(content)
        _richTextState.value = RichTextState().apply {
            setHtml(sanitizedContent)
        }
    }
}
```

**Step 4: Fix TextEditCommand.kt**
```kotlin
// Fix InsertHtmlCommand (Line 193) and ReplaceHtmlCommand (Line 240)
class InsertHtmlCommand(
    private val html: String,
    private val selection: TextRange
) : TextEditCommand {
    override fun execute(state: TextFieldValue): TextFieldValue {
        val sanitizedHtml = HtmlSanitizer.sanitize(html)
        // Rest of implementation with sanitizedHtml
    }
}
```

### Phase 2: Path Traversal Protection

**Files to Modify:**
- `shared/src/commonMain/kotlin/com/module/notelycompose/notes/presentation/detail/TextEditorViewModel.kt`

**Step 1: Create File Path Validator**
```kotlin
// Add to TextEditorViewModel.kt
private fun isPathSafe(filePath: String): Boolean {
    return try {
        val safeDir = File(getApplicationContext().filesDir, "recordings").canonicalPath
        val requestedFile = File(filePath)
        val canonicalPath = requestedFile.canonicalPath
        canonicalPath.startsWith(safeDir)
    } catch (e: Exception) {
        false // If any error occurs, deny access
    }
}

private fun getSafeRecordingsDirectory(): String {
    return File(getApplicationContext().filesDir, "recordings").absolutePath
}
```

**Step 2: Fix Audio File Operations**
```kotlin
// Fix onDeleteRecord() - Line 180
fun onDeleteRecord() {
    val currentState = _editorPresentationState.value
    val recordingPath = currentState.recording.recordingPath
    
    if (!isPathSafe(recordingPath)) {
        _errorMessages.value = "Invalid file path detected"
        return
    }
    
    deleteFile(recordingPath)
    // Rest of existing implementation
}

// Fix audio player operations - Line 207, 351
fun prepareAudioPlayer(recordingPath: String) {
    if (!isPathSafe(recordingPath)) {
        _errorMessages.value = "Invalid audio file path"
        return
    }
    
    try {
        audioPlayer.prepare(recordingPath)
    } catch (e: Exception) {
        _errorMessages.value = "Failed to prepare audio: ${e.message}"
    }
}
```

### Phase 3: Input Validation Layer

**Step 1: Create Input Validator**
```kotlin
// Create new file: shared/src/commonMain/kotlin/com/module/notelycompose/security/InputValidator.kt
object InputValidator {
    fun validateNoteTitle(title: String): String {
        return title.take(200).trim() // Max 200 chars, trim whitespace
    }
    
    fun validateNoteContent(content: String): String {
        return if (content.length > 100000) { // Max 100KB content
            content.take(100000)
        } else {
            content
        }
    }
    
    fun validateFileName(fileName: String): Boolean {
        val validPattern = Regex("^[a-zA-Z0-9._-]+$")
        return fileName.isNotEmpty() && 
               fileName.length <= 255 && 
               validPattern.matches(fileName) &&
               !fileName.startsWith(".") &&
               !fileName.contains("..")
    }
}
```

**Step 2: Apply Validation in ViewModels**
```kotlin
// In TextEditorViewModel.kt - onTitleChange method
fun onTitleChange(newTitle: String) {
    val validatedTitle = InputValidator.validateNoteTitle(newTitle)
    updateEditorState { currentState ->
        currentState.copy(
            title = validatedTitle
        )
    }
}
```

### Phase 4: Error Handling Enhancement

**Step 1: Implement User-Facing Error System**
```kotlin
// Add to TextEditorViewModel.kt
private val _securityErrors = MutableStateFlow<String?>(null)
val securityErrors: StateFlow<String?> = _securityErrors.asStateFlow()

private fun reportSecurityError(message: String) {
    _securityErrors.value = message
    // Log security incident for monitoring
    println("SECURITY_ALERT: $message")
}
```

### Phase 5: Security Testing

**Step 1: Create Security Test Cases**
```kotlin
// Create: shared/src/commonTest/kotlin/security/SecurityTests.kt
class SecurityTests {
    @Test
    fun testHtmlInjectionPrevention() {
        val maliciousInput = "<script>alert('XSS')</script><p>Normal content</p>"
        val sanitized = HtmlSanitizer.sanitize(maliciousInput)
        assertFalse(sanitized.contains("<script>"))
        assertTrue(sanitized.contains("<p>Normal content</p>"))
    }
    
    @Test
    fun testPathTraversalPrevention() {
        val maliciousPaths = listOf(
            "../../../etc/passwd",
            "..\\..\\Windows\\System32",
            "/etc/shadow",
            "C:\\Windows\\System32\\config\\SAM"
        )
        
        maliciousPaths.forEach { path ->
            assertFalse("Path should be rejected: $path", isPathSafe(path))
        }
    }
}
```

## Junior Developer Guidelines

### Understanding the Security Issues

**HTML Injection (XSS)**:
- **What it is**: When user input containing HTML/JavaScript is directly inserted into the app without sanitization
- **Why it's dangerous**: Allows malicious users to execute scripts, steal data, or manipulate the UI
- **How we fix it**: Use OWASP HTML Sanitizer to remove dangerous elements while keeping safe formatting

**Path Traversal**:
- **What it is**: When file paths from user input or database aren't validated, allowing access to files outside intended directories
- **Why it's dangerous**: Could allow reading sensitive files or deleting important system files
- **How we fix it**: Validate all file paths against a safe directory before file operations

### Testing Your Changes

1. **Manual Security Testing**:
   ```kotlin
   // Test malicious HTML input
   val testInputs = listOf(
       "<script>alert('test')</script>",
       "<img src=x onerror=alert('xss')>",
       "<iframe src='javascript:alert(1)'></iframe>"
   )
   ```

2. **File Path Testing**:
   ```kotlin
   // Test malicious file paths
   val testPaths = listOf(
       "../../../etc/passwd",
       "..\\Windows\\System32",
       "/dev/null"
   )
   ```

3. **Run Security Tests**:
   ```bash
   ./gradlew :shared:testDebugUnitTest --tests "*SecurityTests*"
   ```

### Common Mistakes to Avoid

1. **Don't bypass sanitization** - Always sanitize HTML input, even if you think it's "safe"
2. **Don't trust database content** - Sanitize content from database too, as it might have been compromised
3. **Don't use string concatenation** for file paths - Use proper File() constructors
4. **Don't ignore validation failures** - Always handle invalid input gracefully

### Code Review Checklist

- [ ] All `setHtml()` calls use sanitized input
- [ ] All file operations validate paths with `isPathSafe()`
- [ ] User inputs are validated before processing
- [ ] Error cases are handled gracefully
- [ ] Security tests cover edge cases
- [ ] No hardcoded credentials or sensitive data in code

## Implementation Notes

This is a **CRITICAL SECURITY TASK** that must be completed before any production deployment. The vulnerabilities identified pose significant security risks to user data and device security. 

**Estimated Time**: 8-12 hours for complete implementation and testing
**Priority**: P0 - Critical
**Dependencies**: None - can be implemented immediately

The implementation follows security best practices and uses industry-standard libraries (OWASP HTML Sanitizer) for maximum protection. All changes maintain backward compatibility while significantly improving security posture.