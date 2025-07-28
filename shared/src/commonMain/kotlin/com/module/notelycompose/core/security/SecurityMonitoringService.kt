package com.module.notelycompose.core.security

import kotlinx.coroutines.flow.Flow

/**
 * Security monitoring service interface for tracking and reporting security events.
 * Provides production-ready security monitoring capabilities with proper logging,
 * threat tracking, and incident response functionality.
 */
interface SecurityMonitoringService {

    /**
     * Represents different types of security events that can occur in the application.
     */
    enum class SecurityEventType {
        AUTHENTICATION_FAILURE,
        AUTHORIZATION_VIOLATION,
        INPUT_VALIDATION_FAILURE,
        FILE_SYSTEM_VIOLATION,
        PATH_TRAVERSAL_ATTEMPT,
        SQL_INJECTION_ATTEMPT,
        XSS_ATTEMPT,
        MALICIOUS_CONTENT_DETECTED,
        RATE_LIMIT_EXCEEDED,
        SUSPICIOUS_ACTIVITY,
        DATA_INTEGRITY_VIOLATION,
        PRIVACY_VIOLATION,
        CONFIGURATION_TAMPERING,
        RESOURCE_EXHAUSTION,
        UNKNOWN_SECURITY_EVENT
    }

    /**
     * Severity levels for security events.
     */
    enum class SecuritySeverity {
        LOW,      // Minor security concerns, informational
        MEDIUM,   // Potential security issues requiring monitoring
        HIGH,     // Serious security threats requiring immediate attention
        CRITICAL  // Critical security incidents requiring immediate response
    }

    /**
     * Represents a security event with all relevant context information.
     */
    data class SecurityEvent(
        val id: String,
        val timestamp: Long,
        val type: SecurityEventType,
        val severity: SecuritySeverity,
        val message: String,
        val details: Map<String, String> = emptyMap(),
        val userContext: UserContext? = null,
        val systemContext: SystemContext? = null,
        val remediation: String? = null,
        val stackTrace: String? = null
    )

    /**
     * User context information (anonymized for privacy compliance).
     */
    data class UserContext(
        val sessionId: String,
        val userAgent: String? = null,
        val ipAddress: String? = null, // Only last octet for privacy
        val deviceFingerprint: String? = null
    )

    /**
     * System context information for security events.
     */
    data class SystemContext(
        val applicationVersion: String,
        val platformVersion: String,
        val availableMemory: Long? = null,
        val diskSpace: Long? = null,
        val networkStatus: String? = null
    )

    /**
     * Configuration for security monitoring behavior.
     */
    data class SecurityConfig(
        val enabledEventTypes: Set<SecurityEventType> = SecurityEventType.entries.toSet(),
        val minSeverityLevel: SecuritySeverity = SecuritySeverity.LOW,
        val maxEventsPerHour: Int = 1000,
        val enableDetailedLogging: Boolean = true,
        val enableRemoteReporting: Boolean = false,
        val gdprCompliant: Boolean = true,
        val retentionPeriodDays: Int = 30
    )

    /**
     * Reports a security event with full context information.
     * 
     * @param type The type of security event
     * @param severity The severity level of the event
     * @param message Human-readable description of the security event
     * @param details Additional context information as key-value pairs
     * @param userContext Optional user context information
     * @param remediation Optional remediation steps or actions taken
     * @param throwable Optional exception that triggered this security event
     */
    suspend fun reportSecurityEvent(
        type: SecurityEventType,
        severity: SecuritySeverity,
        message: String,
        details: Map<String, String> = emptyMap(),
        userContext: UserContext? = null,
        remediation: String? = null,
        throwable: Throwable? = null
    )

    /**
     * Reports a security incident based on a validation failure.
     * Convenience method for input validation failures.
     * 
     * @param validationType The type of validation that failed
     * @param input The input that failed validation (sanitized)
     * @param validationError The validation error message
     * @param userContext Optional user context
     */
    suspend fun reportValidationFailure(
        validationType: String,
        input: String,
        validationError: String,
        userContext: UserContext? = null
    )

    /**
     * Reports a file system security violation.
     * Convenience method for file access security events.
     * 
     * @param operation The file operation being attempted
     * @param filePath The file path involved (sanitized)
     * @param violation The specific violation detected
     * @param userContext Optional user context
     */
    suspend fun reportFileSystemViolation(
        operation: String,
        filePath: String,
        violation: String,
        userContext: UserContext? = null
    )

    /**
     * Reports suspicious user activity patterns.
     * 
     * @param activityType The type of suspicious activity
     * @param description Description of the suspicious behavior
     * @param confidence Confidence level (0.0 to 1.0)
     * @param userContext User context information
     */
    suspend fun reportSuspiciousActivity(
        activityType: String,
        description: String,
        confidence: Double,
        userContext: UserContext? = null
    )

    /**
     * Gets security events for monitoring and analysis.
     * Returns a flow of recent security events.
     */
    fun getSecurityEvents(): Flow<List<SecurityEvent>>

    /**
     * Gets security metrics and statistics.
     */
    suspend fun getSecurityMetrics(): SecurityMetrics

    /**
     * Clears old security events based on retention policy.
     */
    suspend fun cleanupOldEvents()

    /**
     * Updates the security monitoring configuration.
     */
    suspend fun updateConfiguration(config: SecurityConfig)

    /**
     * Gets the current security monitoring configuration.
     */
    suspend fun getConfiguration(): SecurityConfig

    /**
     * Security metrics for monitoring dashboard.
     */
    data class SecurityMetrics(
        val totalEvents: Long,
        val eventsByType: Map<SecurityEventType, Long>,
        val eventsBySeverity: Map<SecuritySeverity, Long>,
        val recentEventCount: Long, // Last 24 hours
        val averageEventsPerHour: Double,
        val topThreats: List<String>,
        val systemHealth: String // "HEALTHY", "WARNING", "CRITICAL"
    )
}