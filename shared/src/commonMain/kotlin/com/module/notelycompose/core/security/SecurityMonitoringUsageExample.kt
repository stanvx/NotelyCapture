package com.module.notelycompose.core.security

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Example usage patterns for the SecurityMonitoringService and SecurityHelper.
 * This file demonstrates how to properly use the security monitoring system
 * throughout the application with proper error handling and user context.
 */
class SecurityMonitoringUsageExample(
    private val securityHelper: SecurityHelper,
    private val securityMonitoringService: SecurityMonitoringService,
    private val scope: CoroutineScope
) {

    /**
     * Example of validating user input with security monitoring.
     */
    suspend fun validateUserInput(
        noteTitle: String,
        noteContent: String,
        sessionId: String
    ): Boolean {
        val userContext = securityHelper.createUserContext(
            sessionId = sessionId,
            userAgent = "NotelyCapture/1.0.0",
            deviceFingerprint = "android_device_123"
        )

        // Validate note title with security monitoring
        val isTitleValid = securityHelper.validateNoteTitle(noteTitle, userContext)
        if (!isTitleValid) {
            return false
        }

        // Validate note content with security monitoring
        val isContentValid = securityHelper.validateNoteContent(noteContent, userContext)
        if (!isContentValid) {
            return false
        }

        return true
    }

    /**
     * Example of secure file operations with monitoring.
     */
    suspend fun performSecureFileOperation(
        filePath: String,
        sessionId: String
    ): SecurityHelper.FileDeleteResult {
        val userContext = securityHelper.createUserContext(sessionId = sessionId)

        // Check if path is safe before any file operation
        val isPathSafe = securityHelper.isPathSafe(filePath, userContext)
        if (!isPathSafe) {
            // Path validation already reported through security monitoring
            return SecurityHelper.FileDeleteResult(
                success = false,
                securityError = "Unsafe file path detected"
            )
        }

        // Perform secure file deletion
        return securityHelper.secureDeleteFile(filePath, userContext)
    }

    /**
     * Example of reporting custom security events.
     */
    fun reportCustomSecurityEvent() {
        scope.launch {
            securityHelper.reportSecurityEvent(
                type = SecurityMonitoringService.SecurityEventType.SUSPICIOUS_ACTIVITY,
                severity = SecurityMonitoringService.SecuritySeverity.MEDIUM,
                message = "Multiple failed login attempts detected",
                details = mapOf(
                    "attempt_count" to "5",
                    "time_window" to "60_seconds",
                    "source" to "login_screen"
                ),
                userContext = securityHelper.createUserContext(
                    sessionId = "session_123",
                    deviceFingerprint = "device_abc"
                )
            )
        }
    }

    /**
     * Example of monitoring search query validation.
     */
    suspend fun validateSearchQuery(
        query: String,
        sessionId: String
    ): Boolean {
        val userContext = securityHelper.createUserContext(sessionId = sessionId)

        // Validate search query with automatic security monitoring
        val isValid = securityHelper.validateSearchQuery(query, userContext)
        
        if (isValid) {
            // Sanitize the query for safe use
            val sanitizedQuery = securityHelper.sanitizeSearchQuery(query)
            // Use sanitizedQuery for actual search...
        }

        return isValid
    }

    /**
     * Example of handling suspicious activity detection.
     */
    suspend fun detectSuspiciousActivity(
        activityType: String,
        confidence: Double,
        sessionId: String
    ) {
        val userContext = securityHelper.createUserContext(sessionId = sessionId)

        securityMonitoringService.reportSuspiciousActivity(
            activityType = activityType,
            description = "Unusual pattern detected in user behavior",
            confidence = confidence,
            userContext = userContext
        )

        // If confidence is high, take additional security measures
        if (confidence >= 0.8) {
            securityHelper.reportSecurityEvent(
                type = SecurityMonitoringService.SecurityEventType.SUSPICIOUS_ACTIVITY,
                severity = SecurityMonitoringService.SecuritySeverity.HIGH,
                message = "High-confidence suspicious activity detected",
                details = mapOf(
                    "activity_type" to activityType,
                    "confidence" to confidence.toString(),
                    "action_taken" to "enhanced_monitoring"
                ),
                userContext = userContext
            )
        }
    }

    /**
     * Example of monitoring security metrics.
     */
    suspend fun monitorSecurityHealth() {
        val metrics = securityMonitoringService.getSecurityMetrics()
        
        // Log security health status
        when (metrics.systemHealth) {
            "CRITICAL" -> {
                securityHelper.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.UNKNOWN_SECURITY_EVENT,
                    severity = SecurityMonitoringService.SecuritySeverity.CRITICAL,
                    message = "System security health is CRITICAL",
                    details = mapOf(
                        "total_events" to metrics.totalEvents.toString(),
                        "recent_events" to metrics.recentEventCount.toString(),
                        "top_threats" to metrics.topThreats.joinToString(", ")
                    )
                )
            }
            "WARNING" -> {
                securityHelper.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.UNKNOWN_SECURITY_EVENT,
                    severity = SecurityMonitoringService.SecuritySeverity.HIGH,
                    message = "System security health shows warnings",
                    details = mapOf(
                        "recent_events" to metrics.recentEventCount.toString(),
                        "avg_events_per_hour" to metrics.averageEventsPerHour.toString()
                    )
                )
            }
            "HEALTHY" -> {
                // System is healthy, no action needed
            }
        }
    }

    /**
     * Example of configuring security monitoring.
     */
    suspend fun configureSecurityMonitoring() {
        val config = SecurityMonitoringService.SecurityConfig(
            enabledEventTypes = setOf(
                SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE,
                SecurityMonitoringService.SecurityEventType.FILE_SYSTEM_VIOLATION,
                SecurityMonitoringService.SecurityEventType.PATH_TRAVERSAL_ATTEMPT,
                SecurityMonitoringService.SecurityEventType.SUSPICIOUS_ACTIVITY
            ),
            minSeverityLevel = SecurityMonitoringService.SecuritySeverity.MEDIUM,
            maxEventsPerHour = 500,
            enableDetailedLogging = true,
            enableRemoteReporting = false, // Keep local for privacy
            gdprCompliant = true,
            retentionPeriodDays = 7 // Shorter retention for privacy
        )

        securityMonitoringService.updateConfiguration(config)
    }

    /**
     * Example of handling file validation failures.
     */
    suspend fun handleFileValidation(filename: String, sessionId: String): String? {
        val userContext = securityHelper.createUserContext(sessionId = sessionId)
        
        // Get safe filename with automatic security monitoring
        val safeFilename = securityHelper.getSafeFilename(filename, userContext)
        
        if (safeFilename == null) {
            // Additional logging for filename validation failure
            securityHelper.reportSecurityEvent(
                type = SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE,
                severity = SecurityMonitoringService.SecuritySeverity.MEDIUM,
                message = "Filename validation failed",
                details = mapOf(
                    "original_filename" to filename.take(50), // Truncated for security
                    "validation_context" to "file_upload",
                    "action_taken" to "filename_rejected"
                ),
                userContext = userContext
            )
        }
        
        return safeFilename
    }

    /**
     * Example of periodic security maintenance.
     */
    suspend fun performSecurityMaintenance() {
        // Clean up old security events
        securityMonitoringService.cleanupOldEvents()
        
        // Report maintenance completion
        securityHelper.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.UNKNOWN_SECURITY_EVENT,
            severity = SecurityMonitoringService.SecuritySeverity.LOW,
            message = "Security maintenance completed",
            details = mapOf(
                "operation" to "cleanup_old_events",
                "timestamp" to System.currentTimeMillis().toString()
            )
        )
    }
}