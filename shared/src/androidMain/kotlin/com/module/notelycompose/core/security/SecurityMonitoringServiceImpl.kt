package com.module.notelycompose.core.security

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Android implementation of SecurityMonitoringService.
 * Provides comprehensive security monitoring with local logging, threat detection,
 * and GDPR-compliant data handling.
 */
class SecurityMonitoringServiceImpl(
    private val context: Context,
    private val appVersion: String
) : SecurityMonitoringService {

    companion object {
        private const val TAG = "SecurityMonitoring"
        private const val LOG_FILE_NAME = "security_events.log"
        private const val MAX_LOG_FILE_SIZE = 10 * 1024 * 1024 // 10MB
        private const val MAX_EVENTS_IN_MEMORY = 1000
        private const val RATE_LIMIT_WINDOW_MS = 3600_000L // 1 hour
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    
    // In-memory event storage for recent events
    private val recentEvents = mutableListOf<SecurityMonitoringService.SecurityEvent>()
    private val eventsFlow = MutableStateFlow<List<SecurityMonitoringService.SecurityEvent>>(emptyList())
    
    // Configuration
    private var config = SecurityMonitoringService.SecurityConfig()
    
    // Rate limiting
    private val eventCounts = ConcurrentHashMap<SecurityMonitoringService.SecurityEventType, AtomicLong>()
    private val lastRateLimitReset = AtomicLong(System.currentTimeMillis())
    
    // Metrics tracking
    private val totalEventsCount = AtomicLong(0)
    private val eventTypeCounters = ConcurrentHashMap<SecurityMonitoringService.SecurityEventType, AtomicLong>()
    private val severityCounters = ConcurrentHashMap<SecurityMonitoringService.SecuritySeverity, AtomicLong>()
    
    // System context cache
    private val systemContext by lazy {
        SecurityMonitoringService.SystemContext(
            applicationVersion = appVersion,
            platformVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            availableMemory = getAvailableMemory(),
            diskSpace = getAvailableDiskSpace(),
            networkStatus = "Unknown" // Could be enhanced with network state
        )
    }

    init {
        // Initialize event type counters
        SecurityMonitoringService.SecurityEventType.entries.forEach { type ->
            eventTypeCounters[type] = AtomicLong(0)
        }
        
        // Initialize severity counters
        SecurityMonitoringService.SecuritySeverity.entries.forEach { severity ->
            severityCounters[severity] = AtomicLong(0)
        }
    }

    override suspend fun reportSecurityEvent(
        type: SecurityMonitoringService.SecurityEventType,
        severity: SecurityMonitoringService.SecuritySeverity,
        message: String,
        details: Map<String, String>,
        userContext: SecurityMonitoringService.UserContext?,
        remediation: String?,
        throwable: Throwable?
    ) {
        // Check if event type is enabled
        if (!config.enabledEventTypes.contains(type)) {
            return
        }
        
        // Check minimum severity level
        if (severity.ordinal < config.minSeverityLevel.ordinal) {
            return
        }
        
        // Rate limiting check
        if (!checkRateLimit(type)) {
            Log.w(TAG, "Rate limit exceeded for event type: $type")
            return
        }
        
        val event = SecurityMonitoringService.SecurityEvent(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            type = type,
            severity = severity,
            message = message,
            details = sanitizeDetails(details),
            userContext = sanitizeUserContext(userContext),
            systemContext = systemContext,
            remediation = remediation,
            stackTrace = throwable?.let { getStackTrace(it) }
        )
        
        // Process event asynchronously
        scope.launch {
            processSecurityEvent(event)
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
            validationError.contains("script", ignoreCase = true) -> 
                SecurityMonitoringService.SecuritySeverity.HIGH
            else -> SecurityMonitoringService.SecuritySeverity.MEDIUM
        }
        
        val eventType = when {
            validationError.contains("path traversal", ignoreCase = true) -> 
                SecurityMonitoringService.SecurityEventType.PATH_TRAVERSAL_ATTEMPT
            validationError.contains("sql", ignoreCase = true) -> 
                SecurityMonitoringService.SecurityEventType.SQL_INJECTION_ATTEMPT
            validationError.contains("script", ignoreCase = true) -> 
                SecurityMonitoringService.SecurityEventType.XSS_ATTEMPT
            else -> SecurityMonitoringService.SecurityEventType.INPUT_VALIDATION_FAILURE
        }
        
        reportSecurityEvent(
            type = eventType,
            severity = severity,
            message = "Validation failure: $validationType",
            details = mapOf(
                "validation_type" to validationType,
                "input_preview" to sanitizeInput(input),
                "validation_error" to validationError
            ),
            userContext = userContext,
            remediation = "Input was rejected and user was notified of the error"
        )
    }

    override suspend fun reportFileSystemViolation(
        operation: String,
        filePath: String,
        violation: String,
        userContext: SecurityMonitoringService.UserContext?
    ) {
        val severity = when {
            violation.contains("traversal", ignoreCase = true) -> 
                SecurityMonitoringService.SecuritySeverity.HIGH
            violation.contains("system", ignoreCase = true) -> 
                SecurityMonitoringService.SecuritySeverity.HIGH
            else -> SecurityMonitoringService.SecuritySeverity.MEDIUM
        }
        
        reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.FILE_SYSTEM_VIOLATION,
            severity = severity,
            message = "File system security violation: $operation",
            details = mapOf(
                "operation" to operation,
                "file_path" to sanitizeFilePath(filePath),
                "violation" to violation
            ),
            userContext = userContext,
            remediation = "File operation was blocked"
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
            message = "Suspicious activity detected: $activityType",
            details = mapOf(
                "activity_type" to activityType,
                "description" to description,
                "confidence" to confidence.toString()
            ),
            userContext = userContext,
            remediation = "Activity is being monitored"
        )
    }

    override fun getSecurityEvents(): Flow<List<SecurityMonitoringService.SecurityEvent>> {
        return eventsFlow.asStateFlow()
    }

    override suspend fun getSecurityMetrics(): SecurityMonitoringService.SecurityMetrics {
        return withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val last24Hours = now - (24 * 60 * 60 * 1000)
            
            val recentEventCount = mutex.withLock {
                recentEvents.count { it.timestamp >= last24Hours }
            }
            
            val eventsByType = eventTypeCounters.mapValues { it.value.get() }
            val eventsBySeverity = severityCounters.mapValues { it.value.get() }
            
            val topThreats = eventsByType
                .filter { it.value > 0 }
                .toList()
                .sortedByDescending { it.second }
                .take(3)
                .map { "${it.first.name}: ${it.second}" }
            
            val totalEvents = totalEventsCount.get()
            val averagePerHour = if (totalEvents > 0) {
                totalEvents.toDouble() / 24.0 // Simplified calculation
            } else 0.0
            
            val systemHealth = when {
                eventsBySeverity[SecurityMonitoringService.SecuritySeverity.CRITICAL]?.let { it > 0 } == true -> "CRITICAL"
                eventsBySeverity[SecurityMonitoringService.SecuritySeverity.HIGH]?.let { it > 5 } == true -> "WARNING"
                else -> "HEALTHY"
            }
            
            SecurityMonitoringService.SecurityMetrics(
                totalEvents = totalEvents,
                eventsByType = eventsByType,
                eventsBySeverity = eventsBySeverity,
                recentEventCount = recentEventCount.toLong(),
                averageEventsPerHour = averagePerHour,
                topThreats = topThreats,
                systemHealth = systemHealth
            )
        }
    }

    override suspend fun cleanupOldEvents() {
        withContext(Dispatchers.IO) {
            val cutoffTime = System.currentTimeMillis() - (config.retentionPeriodDays * 24 * 60 * 60 * 1000L)
            
            mutex.withLock {
                recentEvents.removeAll { it.timestamp < cutoffTime }
                eventsFlow.value = recentEvents.toList()
            }
            
            // Also cleanup log files if they're too large
            cleanupLogFiles()
        }
    }

    override suspend fun updateConfiguration(config: SecurityMonitoringService.SecurityConfig) {
        this.config = config
        Log.i(TAG, "Security monitoring configuration updated")
    }

    override suspend fun getConfiguration(): SecurityMonitoringService.SecurityConfig {
        return config
    }

    /**
     * Processes a security event by logging, storing, and optionally reporting it.
     */
    private suspend fun processSecurityEvent(event: SecurityMonitoringService.SecurityEvent) {
        try {
            // Update metrics
            totalEventsCount.incrementAndGet()
            eventTypeCounters[event.type]?.incrementAndGet()
            severityCounters[event.severity]?.incrementAndGet()
            
            // Log to Android Log based on severity
            logToAndroidLog(event)
            
            // Write to file if detailed logging is enabled
            if (config.enableDetailedLogging) {
                writeToLogFile(event)
            }
            
            // Store in memory for recent access
            mutex.withLock {
                recentEvents.add(event)
                if (recentEvents.size > MAX_EVENTS_IN_MEMORY) {
                    recentEvents.removeFirst()
                }
                eventsFlow.value = recentEvents.toList()
            }
            
            // Handle critical events
            if (event.severity == SecurityMonitoringService.SecuritySeverity.CRITICAL) {
                handleCriticalEvent(event)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing security event", e)
        }
    }

    private fun logToAndroidLog(event: SecurityMonitoringService.SecurityEvent) {
        val logMessage = formatLogMessage(event)
        
        when (event.severity) {
            SecurityMonitoringService.SecuritySeverity.CRITICAL -> Log.e(TAG, logMessage)
            SecurityMonitoringService.SecuritySeverity.HIGH -> Log.w(TAG, logMessage)
            SecurityMonitoringService.SecuritySeverity.MEDIUM -> Log.i(TAG, logMessage)
            SecurityMonitoringService.SecuritySeverity.LOW -> Log.d(TAG, logMessage)
        }
    }

    private suspend fun writeToLogFile(event: SecurityMonitoringService.SecurityEvent) {
        withContext(Dispatchers.IO) {
            try {
                val logFile = File(context.filesDir, LOG_FILE_NAME)
                val logEntry = "${dateFormat.format(Date(event.timestamp))} | ${formatLogMessage(event)}\n"
                
                logFile.appendText(logEntry)
                
                // Check file size and rotate if necessary
                if (logFile.length() > MAX_LOG_FILE_SIZE) {
                    rotateLogFile(logFile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error writing to log file", e)
            }
        }
    }

    private fun formatLogMessage(event: SecurityMonitoringService.SecurityEvent): String {
        val sb = StringBuilder()
        sb.append("[${event.severity}] ${event.type}: ${event.message}")
        
        if (event.details.isNotEmpty()) {
            sb.append(" | Details: ${event.details}")
        }
        
        if (event.remediation != null) {
            sb.append(" | Remediation: ${event.remediation}")
        }
        
        return sb.toString()
    }

    private fun rotateLogFile(logFile: File) {
        try {
            val backupFile = File(context.filesDir, "${LOG_FILE_NAME}.old")
            if (backupFile.exists()) {
                backupFile.delete()
            }
            logFile.renameTo(backupFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error rotating log file", e)
        }
    }

    private fun cleanupLogFiles() {
        try {
            val logFile = File(context.filesDir, LOG_FILE_NAME)
            val backupFile = File(context.filesDir, "${LOG_FILE_NAME}.old")
            
            if (logFile.exists() && logFile.length() > MAX_LOG_FILE_SIZE * 2) {
                logFile.delete()
            }
            
            if (backupFile.exists() && backupFile.lastModified() < System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)) {
                backupFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up log files", e)
        }
    }

    private fun handleCriticalEvent(event: SecurityMonitoringService.SecurityEvent) {
        Log.e(TAG, "CRITICAL SECURITY EVENT: ${event.message}")
        // In a real implementation, this might:
        // - Send immediate notifications
        // - Trigger security protocols
        // - Alert security team
        // - Lock down resources temporarily
    }

    private fun checkRateLimit(type: SecurityMonitoringService.SecurityEventType): Boolean {
        val now = System.currentTimeMillis()
        
        // Reset counters if window has passed
        if (now - lastRateLimitReset.get() > RATE_LIMIT_WINDOW_MS) {
            eventCounts.clear()
            lastRateLimitReset.set(now)
        }
        
        val counter = eventCounts.computeIfAbsent(type) { AtomicLong(0) }
        return counter.incrementAndGet() <= config.maxEventsPerHour
    }

    private fun sanitizeDetails(details: Map<String, String>): Map<String, String> {
        if (!config.gdprCompliant) return details
        
        return details.mapValues { (key, value) ->
            when {
                key.contains("password", ignoreCase = true) -> "[REDACTED]"
                key.contains("token", ignoreCase = true) -> "[REDACTED]"
                key.contains("secret", ignoreCase = true) -> "[REDACTED]"
                key.contains("key", ignoreCase = true) -> "[REDACTED]"
                value.length > 100 -> value.take(100) + "..."
                else -> value
            }
        }
    }

    private fun sanitizeUserContext(userContext: SecurityMonitoringService.UserContext?): SecurityMonitoringService.UserContext? {
        if (!config.gdprCompliant || userContext == null) return userContext
        
        return userContext.copy(
            ipAddress = userContext.ipAddress?.let { ip ->
                // Only keep last octet for privacy
                val parts = ip.split(".")
                if (parts.size == 4) {
                    "xxx.xxx.xxx.${parts.last()}"
                } else {
                    "xxx.xxx.xxx.xxx"
                }
            }
        )
    }

    private fun sanitizeInput(input: String): String {
        return if (input.length > 50) {
            input.take(50) + "... [TRUNCATED]"
        } else {
            input
        }
    }

    private fun sanitizeFilePath(filePath: String): String {
        // Remove sensitive path components but keep structure for analysis
        return filePath
            .replace(Regex("/storage/emulated/\\d+"), "/storage/emulated/[USER]")
            .replace(Regex("/data/data/[^/]+"), "/data/data/[APP]")
            .let { path ->
                if (path.length > 100) {
                    "..." + path.takeLast(100)
                } else {
                    path
                }
            }
    }

    private fun getStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }

    private fun getAvailableMemory(): Long {
        return try {
            Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()
        } catch (e: Exception) {
            -1L
        }
    }

    private fun getAvailableDiskSpace(): Long {
        return try {
            context.filesDir.freeSpace
        } catch (e: Exception) {
            -1L
        }
    }
}