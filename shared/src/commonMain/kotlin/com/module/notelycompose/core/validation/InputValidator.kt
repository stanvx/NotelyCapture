package com.module.notelycompose.core.validation

/**
 * Input validation utilities for user input with security validation and length limits.
 */
object InputValidator {
    
    // Constants for input limits
    object Limits {
        const val MAX_NOTE_TITLE_LENGTH = 200
        const val MAX_NOTE_CONTENT_LENGTH = 50000 // 50K characters
        const val MAX_SEARCH_QUERY_LENGTH = 100
        const val MAX_TAG_LENGTH = 50
        const val MAX_FILENAME_LENGTH = 255
        const val MIN_SEARCH_QUERY_LENGTH = 1
    }
    
    // Valid theme options
    private val validThemes = setOf("light", "dark", "system")
    
    // Valid language codes (ISO 639-1)
    private val validLanguageCodes = setOf(
        "en", "es", "fr", "de", "it", "pt", "ru", "zh", "ja", "ko", 
        "ar", "hi", "bn", "ur", "id", "ms", "th", "vi", "tr", "pl",
        "nl", "sv", "da", "no", "fi", "he", "cs", "sk", "hu", "ro",
        "bg", "hr", "sr", "sl", "et", "lv", "lt", "mt", "ga", "cy"
    )
    
    /**
     * Validation result containing success status and error message.
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success() = ValidationResult(isValid = true)
            fun failure(message: String) = ValidationResult(isValid = false, errorMessage = message)
        }
    }
    
    /**
     * Validates note title input.
     */
    fun validateNoteTitle(title: String?): ValidationResult {
        if (title == null) {
            return ValidationResult.failure("Note title cannot be null")
        }
        
        if (title.isEmpty()) {
            return ValidationResult.success()
        }
        
        if (title.length > Limits.MAX_NOTE_TITLE_LENGTH) {
            return ValidationResult.failure(
                "Note title too long (${title.length}/${Limits.MAX_NOTE_TITLE_LENGTH} characters)"
            )
        }
        
        if (containsDangerousCharacters(title)) {
            return ValidationResult.failure("Note title contains invalid characters")
        }
        
        return ValidationResult.success()
    }
    
    /**
     * Validates note content input.
     */
    fun validateNoteContent(content: String?): ValidationResult {
        if (content == null) {
            return ValidationResult.failure("Note content cannot be null")
        }
        
        if (content.isEmpty()) {
            return ValidationResult.success()
        }
        
        if (content.length > Limits.MAX_NOTE_CONTENT_LENGTH) {
            return ValidationResult.failure(
                "Note content too long (${content.length}/${Limits.MAX_NOTE_CONTENT_LENGTH} characters)"
            )
        }
        
        return ValidationResult.success()
    }
    
    /**
     * Validates and sanitizes search query input.
     */
    fun validateSearchQuery(query: String?): ValidationResult {
        if (query == null) {
            return ValidationResult.failure("Search query cannot be null")
        }
        
        if (query.isEmpty()) {
            return ValidationResult.success()
        }
        
        if (query.trim().length < Limits.MIN_SEARCH_QUERY_LENGTH) {
            return ValidationResult.failure("Search query too short")
        }
        
        if (query.length > Limits.MAX_SEARCH_QUERY_LENGTH) {
            return ValidationResult.failure(
                "Search query too long (${query.length}/${Limits.MAX_SEARCH_QUERY_LENGTH} characters)"
            )
        }
        
        if (containsDangerousCharacters(query)) {
            return ValidationResult.failure("Search query contains invalid characters")
        }
        
        return ValidationResult.success()
    }
    
    /**
     * Sanitizes search query by removing potentially dangerous characters.
     */
    fun sanitizeSearchQuery(query: String): String {
        return query
            .trim()
            .replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]"), "")
            .replace(Regex("\\s+"), " ")
            .take(Limits.MAX_SEARCH_QUERY_LENGTH)
    }
    
    /**
     * Validates theme preference value.
     */
    fun validateTheme(theme: String?): ValidationResult {
        if (theme == null || theme.isEmpty()) {
            return ValidationResult.failure("Theme cannot be empty")
        }
        
        if (!validThemes.contains(theme.lowercase())) {
            return ValidationResult.failure(
                "Invalid theme '$theme'. Valid options: ${validThemes.joinToString(", ")}"
            )
        }
        
        return ValidationResult.success()
    }
    
    /**
     * Validates language preference value.
     */
    fun validateLanguage(languageCode: String?): ValidationResult {
        if (languageCode == null || languageCode.isEmpty()) {
            return ValidationResult.failure("Language code cannot be empty")
        }
        
        if (!validLanguageCodes.contains(languageCode.lowercase())) {
            return ValidationResult.failure(
                "Invalid language code '$languageCode'. Must be a valid ISO 639-1 code."
            )
        }
        
        return ValidationResult.success()
    }
    
    /**
     * Validates tag input.
     */
    fun validateTag(tag: String?): ValidationResult {
        if (tag == null) {
            return ValidationResult.failure("Tag cannot be null")
        }
        
        if (tag.isEmpty()) {
            return ValidationResult.failure("Tag cannot be empty")
        }
        
        if (tag.length > Limits.MAX_TAG_LENGTH) {
            return ValidationResult.failure(
                "Tag too long (${tag.length}/${Limits.MAX_TAG_LENGTH} characters)"
            )
        }
        
        if (!tag.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
            return ValidationResult.failure("Tag can only contain letters, numbers, underscores, and hyphens")
        }
        
        return ValidationResult.success()
    }
    
    /**
     * Validates filename input.
     */
    fun validateFilename(filename: String?): ValidationResult {
        if (filename == null || filename.isEmpty()) {
            return ValidationResult.failure("Filename cannot be empty")
        }
        
        if (filename.length > Limits.MAX_FILENAME_LENGTH) {
            return ValidationResult.failure(
                "Filename too long (${filename.length}/${Limits.MAX_FILENAME_LENGTH} characters)"
            )
        }
        
        val dangerousPatterns = listOf(
            "..", "/", "\\", ":", "*", "?", "\"", "<", ">", "|",
            "\u0000"
        )
        
        for (pattern in dangerousPatterns) {
            if (filename.contains(pattern)) {
                return ValidationResult.failure("Filename contains invalid characters")
            }
        }
        
        val reservedNames = setOf(
            "CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5",
            "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4",
            "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
        )
        
        if (reservedNames.contains(filename.uppercase())) {
            return ValidationResult.failure("Filename uses a reserved name")
        }
        
        return ValidationResult.success()
    }
    
    /**
     * Checks if text contains potentially dangerous characters.
     */
    private fun containsDangerousCharacters(text: String): Boolean {
        for (char in text) {
            if (char.isISOControl() && char != '\t' && char != '\n' && char != '\r') {
                return true
            }
        }
        
        val dangerousPatterns = listOf(
            "\u0000",
            "\uFEFF",
            "\u200E", "\u200F",
        )
        
        return dangerousPatterns.any { text.contains(it) }
    }
    
    /**
     * Validates general text input with customizable limits.
     */
    fun validateText(
        text: String?, 
        maxLength: Int, 
        allowEmpty: Boolean = true, 
        fieldName: String = "Text"
    ): ValidationResult {
        if (text == null) {
            return ValidationResult.failure("$fieldName cannot be null")
        }
        
        if (!allowEmpty && text.isEmpty()) {
            return ValidationResult.failure("$fieldName cannot be empty")
        }
        
        if (text.length > maxLength) {
            return ValidationResult.failure(
                "$fieldName too long (${text.length}/$maxLength characters)"
            )
        }
        
        if (containsDangerousCharacters(text)) {
            return ValidationResult.failure("$fieldName contains invalid characters")
        }
        
        return ValidationResult.success()
    }
    
    /**
     * Safely truncates text to the specified maximum length.
     */
    fun truncateText(text: String, maxLength: Int): String {
        return if (text.length <= maxLength) {
            text
        } else {
            text.take(maxLength - 3) + "..."
        }
    }
}