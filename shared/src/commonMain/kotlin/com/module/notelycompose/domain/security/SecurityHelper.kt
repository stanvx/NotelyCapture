package com.module.notelycompose.domain.security

/**
 * Security helper interface for input validation and HTML sanitization.
 * This interface design allows for easy mocking in tests and different implementations
 * across platforms while maintaining security standards.
 */
interface SecurityHelper {
    /**
     * Sanitizes HTML content by removing potentially dangerous elements and scripts.
     * Uses OWASP HTML Sanitizer for robust security protection.
     *
     * @param input The raw HTML input to sanitize
     * @return Sanitized HTML content safe for display
     */
    fun sanitizeHtml(input: String): String
    
    /**
     * Validates input content for basic security requirements.
     * Checks for common patterns that might indicate malicious content.
     *
     * @param input The input string to validate
     * @return true if input passes validation, false otherwise
     */
    fun validateInput(input: String): Boolean
}