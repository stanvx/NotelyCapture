package com.module.notelycompose.core.security

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test suite for SecurityMonitoringService implementation.
 * Tests the core security monitoring functionality including event reporting,
 * metrics collection, and configuration management.
 */
class SecurityMonitoringServiceTest {

    private class TestSecurityMonitoringService : SecurityMonitoringService {
        private val events = mutableListOf<SecurityMonitoringService.SecurityEvent>()
        private var config = SecurityMonitoringService.SecurityConfig()
        private val eventCounts = mutableMapOf<SecurityMonitoringService.SecurityEventType, Long>()
        private val severityCounts = mutableMapOf<SecurityMonitoringService.SecuritySeverity, Long>()

        override suspend fun reportSecurityEvent(
            type: SecurityMonitoringService.SecurityEventType,
            severity: SecurityMonitoringService.SecuritySeverity,
            message: String,
            details: Map<String, String>,
            userContext: SecurityMonitoringService.UserContext?,
            remediation: String?,
            throwable: Throwable?
        ) {
            if (!config.enabledEventTypes.contains(type)) return
            if (severity.ordinal < config.minSeverityLevel.ordinal) return

            val event = SecurityMonitoringService.SecurityEvent(
                id = "test-${events.size}",
                timestamp = System.currentTimeMillis(),
                type = type,
                severity = severity,
                message = message,
                details = details,
                userContext = userContext,
                systemContext = SecurityMonitoringService.SystemContext(
                    applicationVersion = "test-1.0.0",
                    platformVersion = "test-platform"
                ),
                remediation = remediation,
                stackTrace = throwable?.stackTraceToString()
            )

            synchronized(this) {
                events.add(event)
                eventCounts[type] = eventCounts.getOrDefault(type, 0) + 1
                severityCounts[severity] = severityCounts.getOrDefault(severity, 0) + 1
            }
        }

        override suspend fun reportValidationFailure(
            validationType: String,
            input: String,
            validationError: String,
            userContext: SecurityMonitoringService.UserContext?
        ) {
            val severity = when {
                validationError.contains("path traversal", ignoreCase = true) -> 
                    SecurityMonitoringService.SecuritySeverity.HIGH
                validationError.contains("sql", ignoreCase = true) -> 
                    SecurityMonitoringService.SecuritySeverity.HIGH
                else -> SecurityMonitoringService.SecuritySeverity.MEDIUM
            }

            val eventType = when {
                validationError.contains("path traversal", ignoreCase = true) -> 
                    SecurityMonitoringService.SecurityEventType.PATH_TRAVERSAL_ATTEMPT
                validationError.contains("sql", ignoreCase = true) -> 
                    SecurityMonitoringService.SecurityEventType.SQL_INJECTION_ATTEMPT
                else -> SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE
            }

            reportSecurityEvent(
                type = eventType,
                severity = severity,
                message = "Validation failure: $validationType",
                details = mapOf(
                    "validation_type" to validationType,
                    "input_preview" to input.take(50),
                    "validation_error" to validationError
                ),
                userContext = userContext
            )
        }

        override suspend fun reportFileSystemViolation(
            operation: String,
            filePath: String,
            violation: String,
            userContext: SecurityMonitoringService.UserContext?
        ) {
            reportSecurityEvent(
                type = SecurityMonitoringService.SecurityEventType.FILE_SYSTEM_VIOLATION,
                severity = SecurityMonitoringService.SecuritySeverity.HIGH,
                message = "File system violation: $operation",
                details = mapOf(
                    "operation" to operation,
                    "file_path" to filePath,
                    "violation" to violation
                ),
                userContext = userContext
            )
        }

        override suspend fun reportSuspiciousActivity(
            activityType: String,
            description: String,
            confidence: Double,
            userContext: SecurityMonitoringService.UserContext?
        ) {
            val severity = when {
                confidence >= 0.8 -> SecurityMonitoringService.SecuritySeverity.HIGH
                confidence >= 0.6 -> SecurityMonitoringService.SecuritySeverity.MEDIUM
                else -> SecurityMonitoringService.SecuritySeverity.LOW
            }

            reportSecurityEvent(
                type = SecurityMonitoringService.SecurityEventType.SUSPICIOUS_ACTIVITY,
                severity = severity,
                message = "Suspicious activity: $activityType",
                details = mapOf(
                    "activity_type" to activityType,
                    "description" to description,
                    "confidence" to confidence.toString()
                ),
                userContext = userContext
            )
        }

        override fun getSecurityEvents() = kotlinx.coroutines.flow.flowOf(events.toList())

        override suspend fun getSecurityMetrics(): SecurityMonitoringService.SecurityMetrics {
            val totalEvents = events.size.toLong()
            val recentEvents = events.count { 
                System.currentTimeMillis() - it.timestamp < 24 * 60 * 60 * 1000 
            }.toLong()

            return SecurityMonitoringService.SecurityMetrics(
                totalEvents = totalEvents,
                eventsByType = eventCounts,
                eventsBySeverity = severityCounts,
                recentEventCount = recentEvents,
                averageEventsPerHour = totalEvents.toDouble() / 24.0,
                topThreats = eventCounts.entries.sortedByDescending { it.value }
                    .take(3).map { "${it.key.name}: ${it.value}" },
                systemHealth = when {
                    severityCounts[SecurityMonitoringService.SecuritySeverity.CRITICAL]?.let { it > 0 } == true -> "CRITICAL"
                    severityCounts[SecurityMonitoringService.SecuritySeverity.HIGH]?.let { it > 5 } == true -> "WARNING"
                    else -> "HEALTHY"
                }
            )
        }

        override suspend fun cleanupOldEvents() {
            synchronized(this) {
                events.clear()
                eventCounts.clear()
                severityCounts.clear()
            }
        }

        override suspend fun updateConfiguration(config: SecurityMonitoringService.SecurityConfig) {
            this.config = config
        }

        override suspend fun getConfiguration(): SecurityMonitoringService.SecurityConfig = config

        fun getEventCount() = events.size
        fun getLastEvent() = events.lastOrNull()
    }

    @Test
    fun testBasicSecurityEventReporting() = runTest {
        val service = TestSecurityMonitoringService()

        service.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE,
            severity = SecurityMonitoringService.SecuritySeverity.MEDIUM,
            message = "Test validation failure",
            details = mapOf("input" to "test_input")
        )

        assertEquals(1, service.getEventCount())
        val event = service.getLastEvent()
        assertNotNull(event)
        assertEquals(SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE, event.type)
        assertEquals(SecurityMonitoringService.SecuritySeverity.MEDIUM, event.severity)
        assertEquals("Test validation failure", event.message)
        assertEquals("test_input", event.details["input"])
    }

    @Test
    fun testValidationFailureReporting() = runTest {
        val service = TestSecurityMonitoringService()
        val userContext = SecurityMonitoringService.UserContext(sessionId = "test-session")

        service.reportValidationFailure(
            validationType = "note_title",
            input = "malicious input with path traversal",
            validationError = "Contains path traversal attempt",
            userContext = userContext
        )

        assertEquals(1, service.getEventCount())
        val event = service.getLastEvent()
        assertNotNull(event)
        assertEquals(SecurityMonitoringService.SecurityEventType.PATH_TRAVERSAL_ATTEMPT, event.type)
        assertEquals(SecurityMonitoringService.SecuritySeverity.HIGH, event.severity)
        assertEquals(userContext, event.userContext)
    }

    @Test
    fun testFileSystemViolationReporting() = runTest {
        val service = TestSecurityMonitoringService()

        service.reportFileSystemViolation(
            operation = "delete",
            filePath = "/etc/passwd",
            violation = "Attempting to access system file"
        )

        assertEquals(1, service.getEventCount())
        val event = service.getLastEvent()
        assertNotNull(event)
        assertEquals(SecurityMonitoringService.SecurityEventType.FILE_SYSTEM_VIOLATION, event.type)
        assertEquals(SecurityMonitoringService.SecuritySeverity.HIGH, event.severity)
        assertEquals("/etc/passwd", event.details["file_path"])
    }

    @Test
    fun testSuspiciousActivityReporting() = runTest {
        val service = TestSecurityMonitoringService()

        // Test high confidence suspicious activity
        service.reportSuspiciousActivity(
            activityType = "rapid_requests",
            description = "Too many requests in short time",
            confidence = 0.9
        )

        assertEquals(1, service.getEventCount())
        val event = service.getLastEvent()
        assertNotNull(event)
        assertEquals(SecurityMonitoringService.SecurityEventType.SUSPICIOUS_ACTIVITY, event.type)
        assertEquals(SecurityMonitoringService.SecuritySeverity.HIGH, event.severity)
        assertEquals("0.9", event.details["confidence"])

        // Test low confidence suspicious activity
        service.reportSuspiciousActivity(
            activityType = "unusual_pattern",
            description = "Slightly unusual behavior",
            confidence = 0.3
        )

        assertEquals(2, service.getEventCount())
        val lowConfidenceEvent = service.getLastEvent()
        assertNotNull(lowConfidenceEvent)
        assertEquals(SecurityMonitoringService.SecuritySeverity.LOW, lowConfidenceEvent.severity)
    }

    @Test
    fun testSecurityMetrics() = runTest {
        val service = TestSecurityMonitoringService()

        // Report various events
        service.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE,
            severity = SecurityMonitoringService.SecuritySeverity.MEDIUM,
            message = "Test 1"
        )

        service.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.FILE_SYSTEM_VIOLATION,
            severity = SecurityMonitoringService.SecuritySeverity.HIGH,
            message = "Test 2"
        )

        service.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE,
            severity = SecurityMonitoringService.SecuritySeverity.LOW,
            message = "Test 3"
        )

        val metrics = service.getSecurityMetrics()

        assertEquals(3, metrics.totalEvents)
        assertEquals(2L, metrics.eventsByType[SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE])
        assertEquals(1L, metrics.eventsByType[SecurityMonitoringService.SecurityEventType.FILE_SYSTEM_VIOLATION])
        assertEquals(1L, metrics.eventsBySeverity[SecurityMonitoringService.SecuritySeverity.HIGH])
        assertEquals(1L, metrics.eventsBySeverity[SecurityMonitoringService.SecuritySeverity.MEDIUM])
        assertEquals(1L, metrics.eventsBySeverity[SecurityMonitoringService.SecuritySeverity.LOW])
        assertEquals("HEALTHY", metrics.systemHealth)
    }

    @Test
    fun testConfigurationManagement() = runTest {
        val service = TestSecurityMonitoringService()

        val newConfig = SecurityMonitoringService.SecurityConfig(
            enabledEventTypes = setOf(SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE),
            minSeverityLevel = SecurityMonitoringService.SecuritySeverity.HIGH,
            maxEventsPerHour = 100,
            enableDetailedLogging = false,
            gdprCompliant = true
        )

        service.updateConfiguration(newConfig)
        val retrievedConfig = service.getConfiguration()

        assertEquals(newConfig.enabledEventTypes, retrievedConfig.enabledEventTypes)
        assertEquals(newConfig.minSeverityLevel, retrievedConfig.minSeverityLevel)
        assertEquals(newConfig.maxEventsPerHour, retrievedConfig.maxEventsPerHour)
        assertFalse(retrievedConfig.enableDetailedLogging)
        assertTrue(retrievedConfig.gdprCompliant)
    }

    @Test
    fun testEventFiltering() = runTest {
        val service = TestSecurityMonitoringService()

        // Configure to only accept HIGH severity events
        service.updateConfiguration(
            SecurityMonitoringService.SecurityConfig(
                minSeverityLevel = SecurityMonitoringService.SecuritySeverity.HIGH
            )
        )

        // Report medium severity event (should be filtered out)
        service.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE,
            severity = SecurityMonitoringService.SecuritySeverity.MEDIUM,
            message = "Should be filtered"
        )

        assertEquals(0, service.getEventCount())

        // Report high severity event (should be accepted)
        service.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE,
            severity = SecurityMonitoringService.SecuritySeverity.HIGH,
            message = "Should be accepted"
        )

        assertEquals(1, service.getEventCount())
    }

    @Test
    fun testEventTypeFiltering() = runTest {
        val service = TestSecurityMonitoringService()

        // Configure to only accept INPUT_VALIDATION_FAILURE events
        service.updateConfiguration(
            SecurityMonitoringService.SecurityConfig(
                enabledEventTypes = setOf(SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE)
            )
        )

        // Report different event type (should be filtered out)
        service.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.FILE_SYSTEM_VIOLATION,
            severity = SecurityMonitoringService.SecuritySeverity.HIGH,
            message = "Should be filtered"
        )

        assertEquals(0, service.getEventCount())

        // Report allowed event type (should be accepted)
        service.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE,
            severity = SecurityMonitoringService.SecuritySeverity.HIGH,
            message = "Should be accepted"
        )

        assertEquals(1, service.getEventCount())
    }

    @Test
    fun testEventCleanup() = runTest {
        val service = TestSecurityMonitoringService()

        // Add some events
        service.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE,
            severity = SecurityMonitoringService.SecuritySeverity.MEDIUM,
            message = "Test event 1"
        )

        service.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.FILE_SYSTEM_VIOLATION,
            severity = SecurityMonitoringService.SecuritySeverity.HIGH,
            message = "Test event 2"
        )

        assertEquals(2, service.getEventCount())

        // Clean up events
        service.cleanupOldEvents()

        assertEquals(0, service.getEventCount())
        val metrics = service.getSecurityMetrics()
        assertEquals(0, metrics.totalEvents)
    }

    @Test
    fun testSecurityEventFlow() = runTest {
        val service = TestSecurityMonitoringService()

        service.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE,
            severity = SecurityMonitoringService.SecuritySeverity.MEDIUM,
            message = "Test event"
        )

        val events = service.getSecurityEvents().first()
        assertEquals(1, events.size)
        assertEquals("Test event", events.first().message)
    }
}