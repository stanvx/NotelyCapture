package com.module.notelycompose.core.security

import com.module.notelycompose.core.validation.InputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import audio.utils.deleteFile

/**
 * Centralized security utility for handling secure operations throughout the application.
 * This class provides common security validation and file operation methods to prevent
 * code duplication and ensure consistent security practices across ViewModels.
 */
object SecurityHelper {
    
    /**
     * Represents the result of a secure file deletion operation.
     */
    data class FileDeleteResult(
        val success: Boolean,
        val securityError: String? = null,
        val fileError: String? = null
    )
    
    /**
     * Validates if a file path is safe to access (prevents path traversal attacks).
     * 
     * @param filePath The file path to validate
     * @return True if the path is safe, false otherwise
     */
    fun isPathSafe(filePath: String): Boolean {
        if (filePath.isBlank()) return true // Empty path is safe
        
        val validationResult = InputValidator.validateFilePath(filePath)
        
        if (!validationResult.isValid) {
            reportSecurityError("Invalid file path detected: ${validationResult.errorMessage}")
            return false
        }
        
        return true
    }
    
    /**
     * Securely deletes a file with proper validation and error handling.
     * This method runs on the IO dispatcher for better performance and handles
     * both security validation and file deletion errors gracefully.
     * 
     * @param filePath The path of the file to delete
     * @return FileDeleteResult indicating success/failure and any error details
     */
    suspend fun secureDeleteFile(filePath: String?): FileDeleteResult {
        if (filePath.isNullOrEmpty()) {
            return FileDeleteResult(success = true) // No file to delete
        }
        
        return withContext(Dispatchers.Default) {
            try {
                // Security validation to prevent path traversal attacks
                if (!isPathSafe(filePath)) {
                    return@withContext FileDeleteResult(
                        success = false,
                        securityError = "Invalid file path detected during deletion: $filePath"
                    )
                }
                
                // Perform actual file deletion on IO dispatcher
                withContext(Dispatchers.IO) {
                    deleteFile(filePath)
                }
                
                FileDeleteResult(success = true)
                
            } catch (e: Exception) {
                FileDeleteResult(
                    success = false,
                    fileError = "Failed to delete file $filePath: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Validates note content input with security checks.
     * 
     * @param content The note content to validate
     * @return True if content is valid, false otherwise
     */
    fun validateNoteContent(content: String?): Boolean {
        val validation = InputValidator.validateNoteContent(content)
        if (!validation.isValid) {
            reportSecurityError("Invalid note content: ${validation.errorMessage}")
            return false
        }
        return true
    }
    
    /**
     * Validates note title input with security checks.
     * 
     * @param title The note title to validate
     * @return True if title is valid, false otherwise
     */
    fun validateNoteTitle(title: String?): Boolean {
        val validation = InputValidator.validateNoteTitle(title)
        if (!validation.isValid) {
            reportSecurityError("Invalid note title: ${validation.errorMessage}")
            return false
        }
        return true
    }
    
    /**
     * Validates search query input with security checks.
     * 
     * @param query The search query to validate
     * @return True if query is valid, false otherwise
     */
    fun validateSearchQuery(query: String?): Boolean {
        val validation = InputValidator.validateSearchQuery(query)
        if (!validation.isValid) {
            reportSecurityError("Invalid search query: ${validation.errorMessage}")
            return false
        }
        return true
    }
    
    /**
     * Sanitizes search query input by removing potentially dangerous characters.
     * 
     * @param query The search query to sanitize
     * @return Sanitized search query
     */
    fun sanitizeSearchQuery(query: String): String {
        return InputValidator.sanitizeSearchQuery(query)
    }
    
    /**
     * Reports security errors for monitoring and logging.
     * In a production environment, this should integrate with a proper
     * security monitoring system.
     * 
     * @param message The security error message
     */
    private fun reportSecurityError(message: String) {
        // Log security incident for monitoring
        println("SECURITY_ALERT: $message")
        
        // TODO: In production, integrate with proper security monitoring
        // - Send to security monitoring service
        // - Log to secure audit trail
        // - Alert security team if needed
    }
    
    /**
     * Gets a safe, sanitized filename from user input.
     * 
     * @param filename The filename to sanitize
     * @return Sanitized filename or null if invalid
     */
    fun getSafeFilename(filename: String?): String? {
        if (filename.isNullOrEmpty()) return null
        
        val validation = InputValidator.validateFilename(filename)
        return if (validation.isValid) filename else null
    }
    
    /**
     * Truncates text to a safe length while preserving readability.
     * 
     * @param text The text to truncate
     * @param maxLength The maximum allowed length
     * @return Truncated text
     */
    fun truncateText(text: String, maxLength: Int): String {
        return InputValidator.truncateText(text, maxLength)
    }
}