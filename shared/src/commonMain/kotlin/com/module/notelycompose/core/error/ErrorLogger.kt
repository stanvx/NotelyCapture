package com.module.notelycompose.core.error

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Centralized error logging system for malformed note data and UI errors.
 * Provides structured error reporting without exposing sensitive information.
 */
object ErrorLogger {
    
    private val errorLog = mutableListOf<ErrorLogEntry>()
    private const val MAX_LOG_ENTRIES = 500
    
    /**
     * Log an error with structured context information
     */
    fun logError(
        error: Throwable,
        context: ErrorContext,
        severity: ErrorSeverity = ErrorSeverity.MEDIUM,
        userMessage: String? = null
    ) {
        val entry = ErrorLogEntry(
            timestamp = Clock.System.now(),
            severity = severity,
            context = context,
            errorType = error::class.simpleName ?: "UnknownError",
            errorMessage = error.message ?: "No message",
            userMessage = userMessage,
            stackTrace = error.stackTraceToString().take(1000) // Limit stack trace size
        )
        
        synchronized(errorLog) {
            errorLog.add(entry)
            
            // Keep log size manageable
            if (errorLog.size > MAX_LOG_ENTRIES) {
                errorLog.removeFirstOrNull()
            }
        }
        
        // Print to console for debugging (replace with proper logging in production)
        when (severity) {
            ErrorSeverity.CRITICAL -> println("[CRITICAL] ${context.component}: $userMessage - ${error.message}")
            ErrorSeverity.HIGH -> println("[ERROR] ${context.component}: $userMessage - ${error.message}")
            ErrorSeverity.MEDIUM -> println("[WARN] ${context.component}: $userMessage - ${error.message}")
            ErrorSeverity.LOW -> println("[INFO] ${context.component}: $userMessage - ${error.message}")
        }
    }
    
    /**
     * Log a malformed data error specifically
     */
    fun logMalformedData(
        noteId: Long?,
        field: String,
        value: String,
        context: ErrorContext,
        error: Throwable? = null
    ) {
        val sanitizedValue = value.take(100) // Limit value length for privacy
        val userMessage = "Malformed data in field '$field' for note ${noteId ?: "unknown"}"
        
        val contextualError = error ?: MalformedDataException("Invalid $field: $sanitizedValue")
        
        logError(
            error = contextualError,
            context = context,
            severity = ErrorSeverity.HIGH,
            userMessage = userMessage
        )
    }
    
    /**
     * Get recent error entries for debugging
     */
    fun getRecentErrors(limit: Int = 50): List<ErrorLogEntry> {
        return synchronized(errorLog) {
            errorLog.takeLast(limit).toList()
        }
    }
    
    /**
     * Get error statistics
     */
    fun getErrorStats(): ErrorStats {
        return synchronized(errorLog) {
            val now = Clock.System.now()
            val last24Hours = errorLog.filter { 
                (now - it.timestamp).inWholeHours <= 24 
            }
            
            ErrorStats(
                totalErrors = errorLog.size,
                criticalErrors = errorLog.count { it.severity == ErrorSeverity.CRITICAL },
                errorsLast24Hours = last24Hours.size,
                mostCommonErrorType = errorLog.groupBy { it.errorType }
                    .maxByOrNull { it.value.size }?.key ?: "None",
                mostProblematicComponent = errorLog.groupBy { it.context.component }
                    .maxByOrNull { it.value.size }?.key ?: "None"
            )
        }
    }
    
    /**
     * Clear error log (for testing or memory management)
     */
    fun clearErrors() {
        synchronized(errorLog) {
            errorLog.clear()
        }
    }
}

/**
 * Error severity levels
 */
enum class ErrorSeverity {
    CRITICAL,  // App crashes or data corruption
    HIGH,      // UI failures or data inconsistencies  
    MEDIUM,    // Unexpected behavior but recoverable
    LOW        // Minor issues or warnings
}

/**
 * Error context for categorizing and tracking issues
 */
data class ErrorContext(
    val component: String,
    val operation: String,
    val additionalInfo: Map<String, String> = emptyMap()
)

/**
 * Individual error log entry
 */
data class ErrorLogEntry(
    val timestamp: Instant,
    val severity: ErrorSeverity,
    val context: ErrorContext,
    val errorType: String,
    val errorMessage: String,
    val userMessage: String?,
    val stackTrace: String
)

/**
 * Error statistics summary
 */
data class ErrorStats(
    val totalErrors: Int,
    val criticalErrors: Int,
    val errorsLast24Hours: Int,
    val mostCommonErrorType: String,
    val mostProblematicComponent: String
)

/**
 * Custom exception for malformed data
 */
class MalformedDataException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Custom exception for UI rendering errors
 */
class UIRenderingException(
    message: String,
    val component: String,
    cause: Throwable? = null
) : Exception(message, cause)