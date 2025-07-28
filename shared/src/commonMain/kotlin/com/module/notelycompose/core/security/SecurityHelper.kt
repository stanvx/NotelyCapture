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
class SecurityHelper(
    private val securityMonitoringService: SecurityMonitoringService
) {
    
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
     * @param userContext Optional user context for security monitoring
     * @return True if the path is safe, false otherwise
     */
    suspend fun isPathSafe(
        filePath: String, 
        userContext: SecurityMonitoringService.UserContext? = null
    ): Boolean {
        if (filePath.isBlank()) return true // Empty path is safe
        
        val validationResult = InputValidator.validateFilePath(filePath)
        
        if (!validationResult.isValid) {
            securityMonitoringService.reportFileSystemViolation(
                operation = "path_validation",
                filePath = filePath,
                violation = validationResult.errorMessage ?: "Unknown validation error",
                userContext = userContext
            )
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
     * @param userContext Optional user context for security monitoring
     * @return FileDeleteResult indicating success/failure and any error details
     */
    suspend fun secureDeleteFile(
        filePath: String?,
        userContext: SecurityMonitoringService.UserContext? = null
    ): FileDeleteResult {
        if (filePath.isNullOrEmpty()) {
            return FileDeleteResult(success = true) // No file to delete
        }
        
        return withContext(Dispatchers.Default) {
            try {
                // Security validation to prevent path traversal attacks
                if (!isPathSafe(filePath, userContext)) {
                    return@withContext FileDeleteResult(
                        success = false,
                        securityError = "Invalid file path detected during deletion: $filePath"
                    )
                }
                
                // Log successful file deletion for security audit
                securityMonitoringService.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.FILE_SYSTEM_VIOLATION,
                    severity = SecurityMonitoringService.SecuritySeverity.LOW,
                    message = "File deletion requested",
                    details = mapOf(
                        "operation" to "delete",
                        "file_path" to filePath,
                        "status" to "approved"
                    ),
                    userContext = userContext
                )
                
                // Perform actual file deletion on IO dispatcher
                withContext(Dispatchers.IO) {
                    deleteFile(filePath)
                }
                
                FileDeleteResult(success = true)
                
            } catch (e: Exception) {
                // Report file deletion failure as security event
                securityMonitoringService.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.FILE_SYSTEM_VIOLATION,
                    severity = SecurityMonitoringService.SecuritySeverity.MEDIUM,
                    message = "File deletion failed",
                    details = mapOf(
                        "operation" to "delete",
                        "file_path" to filePath,
                        "error" to (e.message ?: "Unknown error")
                    ),
                    userContext = userContext,
                    throwable = e
                )
                
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
     * @param userContext Optional user context for security monitoring
     * @return True if content is valid, false otherwise
     */
    suspend fun validateNoteContent(
        content: String?,
        userContext: SecurityMonitoringService.UserContext? = null
    ): Boolean {
        val validation = InputValidator.validateNoteContent(content)
        if (!validation.isValid) {
            securityMonitoringService.reportValidationFailure(
                validationType = "note_content",
                input = content ?: "",
                validationError = validation.errorMessage ?: "Unknown validation error",
                userContext = userContext
            )
            return false
        }
        return true
    }
    
    /**
     * Validates note title input with security checks.
     * 
     * @param title The note title to validate
     * @param userContext Optional user context for security monitoring
     * @return True if title is valid, false otherwise
     */
    suspend fun validateNoteTitle(
        title: String?,
        userContext: SecurityMonitoringService.UserContext? = null
    ): Boolean {
        val validation = InputValidator.validateNoteTitle(title)
        if (!validation.isValid) {
            securityMonitoringService.reportValidationFailure(
                validationType = "note_title",
                input = title ?: "",
                validationError = validation.errorMessage ?: "Unknown validation error",
                userContext = userContext
            )
            return false
        }
        return true
    }
    
    /**
     * Validates search query input with security checks.
     * 
     * @param query The search query to validate
     * @param userContext Optional user context for security monitoring
     * @return True if query is valid, false otherwise
     */
    suspend fun validateSearchQuery(
        query: String?,
        userContext: SecurityMonitoringService.UserContext? = null
    ): Boolean {
        val validation = InputValidator.validateSearchQuery(query)
        if (!validation.isValid) {
            securityMonitoringService.reportValidationFailure(
                validationType = "search_query",
                input = query ?: "",
                validationError = validation.errorMessage ?: "Unknown validation error",
                userContext = userContext
            )
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
     * Gets a safe, sanitized filename from user input.
     * 
     * @param filename The filename to sanitize
     * @param userContext Optional user context for security monitoring
     * @return Sanitized filename or null if invalid
     */
    suspend fun getSafeFilename(
        filename: String?,
        userContext: SecurityMonitoringService.UserContext? = null
    ): String? {
        if (filename.isNullOrEmpty()) return null
        
        val validation = InputValidator.validateFilename(filename)
        
        if (!validation.isValid) {
            securityMonitoringService.reportValidationFailure(
                validationType = "filename",
                input = filename,
                validationError = validation.errorMessage ?: "Unknown validation error",
                userContext = userContext
            )
            return null
        }
        
        return filename
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
    
    /**
     * Reports a general security event with custom details.
     * Convenience method for reporting custom security events.
     * 
     * @param type The type of security event
     * @param severity The severity level
     * @param message The security event message
     * @param details Additional context details
     * @param userContext Optional user context
     * @param throwable Optional exception that triggered this event
     */
    suspend fun reportSecurityEvent(
        type: SecurityMonitoringService.SecurityEventType,
        severity: SecurityMonitoringService.SecuritySeverity,
        message: String,
        details: Map<String, String> = emptyMap(),
        userContext: SecurityMonitoringService.UserContext? = null,
        throwable: Throwable? = null
    ) {
        securityMonitoringService.reportSecurityEvent(
            type = type,
            severity = severity,
            message = message,
            details = details,
            userContext = userContext,
            throwable = throwable
        )
    }
    
    /**
     * Creates a user context from available session information.
     * Helper method to create consistent user context objects.
     * 
     * @param sessionId The current session identifier
     * @param userAgent Optional user agent string
     * @param ipAddress Optional IP address (will be sanitized)
     * @param deviceFingerprint Optional device fingerprint
     * @return UserContext object for security monitoring
     */
    fun createUserContext(
        sessionId: String,
        userAgent: String? = null,
        ipAddress: String? = null,
        deviceFingerprint: String? = null
    ): SecurityMonitoringService.UserContext {
        return SecurityMonitoringService.UserContext(
            sessionId = sessionId,
            userAgent = userAgent,
            ipAddress = ipAddress,
            deviceFingerprint = deviceFingerprint
        )
    }
}