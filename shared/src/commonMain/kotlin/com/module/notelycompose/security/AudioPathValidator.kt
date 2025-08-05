package com.module.notelycompose.security

import kotlin.jvm.JvmInline

/**
 * Security utility for validating audio file paths to prevent path traversal attacks
 * and unauthorized file access.
 * 
 * This class implements comprehensive security validation for audio file paths
 * to address critical security vulnerabilities identified in Apple QA review.
 * 
 * Security Features:
 * - Path traversal attack prevention (../, ..\, etc.)
 * - Protocol injection protection (file://, http://, etc.)
 * - File extension validation for audio files only
 * - Path sanitization and normalization
 * - Boundary validation for app audio directory
 * - Comprehensive logging for security monitoring
 */
object AudioPathValidator {
    
    /**
     * Allowed audio file extensions for the application
     */
    private val ALLOWED_AUDIO_EXTENSIONS = setOf(
        "mp3", "wav", "m4a", "aac", "ogg", "flac", "wma", "amr", "3gp"
    )
    
    /**
     * Blocked path patterns that indicate potential attacks
     */
    private val BLOCKED_PATTERNS = setOf(
        // Path traversal patterns
        "..", "..\\", "../", "..\\\\",
        // Protocol injections
        "file://", "http://", "https://", "ftp://", "content://",
        // System directory patterns (but allow app-specific storage)
        "/system/", "/data/system/", "/data/misc/", 
        // Dangerous storage locations (but allow app-specific paths)
        "/storage/emulated/0/Android/obb/", "/storage/emulated/0/DCIM/", 
        "/storage/emulated/0/Download/", "/storage/emulated/0/Pictures/",
        "/storage/emulated/0/Music/!", // Block access to public Music folder (explicit pattern to prevent confusion)
        "/sdcard/DCIM/", "/sdcard/Download/", "/sdcard/Pictures/",
        // Windows system patterns
        "C:\\", "D:\\", "\\\\", "\\Windows\\", "\\System32\\",
        // Unix system patterns
        "/etc/", "/usr/", "/bin/", "/sbin/", "/var/", "/tmp/",
        // Dangerous characters
        "<", ">", "|", "&", ";", "`", "$", "\"", "'",
        // Null bytes and control characters
        "\u0000", "\u0001", "\u0002", "\u0003", "\u0004", "\u0005"
    )
    
    /**
     * Allowed base directory patterns for Android app storage
     */
    private val ALLOWED_APP_PATTERNS = setOf(
        "/storage/emulated/0/Android/data/",
        "/data/data/",
        "/android_asset/",
        "/android_res/"
    )
    
    /**
     * Maximum allowed path length to prevent buffer overflow attacks
     */
    private const val MAX_PATH_LENGTH = 260
    
    /**
     * Result of audio path validation
     */
    sealed class ValidationResult {
        data object Valid : ValidationResult()
        
        sealed class Invalid : ValidationResult() {
            abstract val reason: String
            abstract val securityThreat: SecurityThreat
            
            data class EmptyPath(
                override val reason: String = "Audio file path is empty or null",
                override val securityThreat: SecurityThreat = SecurityThreat.LOW
            ) : Invalid()
            
            data class PathTooLong(
                override val reason: String = "Audio file path exceeds maximum allowed length",
                override val securityThreat: SecurityThreat = SecurityThreat.MEDIUM
            ) : Invalid()
            
            data class PathTraversal(
                override val reason: String = "Audio file path contains path traversal patterns",
                override val securityThreat: SecurityThreat = SecurityThreat.CRITICAL
            ) : Invalid()
            
            data class ProtocolInjection(
                override val reason: String = "Audio file path contains protocol injection",
                override val securityThreat: SecurityThreat = SecurityThreat.CRITICAL
            ) : Invalid()
            
            data class InvalidExtension(
                override val reason: String = "Audio file path has invalid or missing extension",
                override val securityThreat: SecurityThreat = SecurityThreat.HIGH
            ) : Invalid()
            
            data class MaliciousCharacters(
                override val reason: String = "Audio file path contains malicious characters",
                override val securityThreat: SecurityThreat = SecurityThreat.HIGH
            ) : Invalid()
            
            data class SystemPathAccess(
                override val reason: String = "Audio file path attempts to access system directories",
                override val securityThreat: SecurityThreat = SecurityThreat.CRITICAL
            ) : Invalid()
        }
    }
    
    /**
     * Security threat level enumeration
     */
    enum class SecurityThreat {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    /**
     * Validated audio path that has passed all security checks
     */
    @JvmInline
    value class ValidatedAudioPath(val path: String)
    
    /**
     * Comprehensive validation of audio file path for security threats
     * 
     * @param path The audio file path to validate
     * @param allowedBaseDirectory Optional base directory to restrict paths (platform-specific)
     * @return ValidationResult indicating if path is safe to use
     */
    fun validateAudioPath(
        path: String?,
        allowedBaseDirectory: String? = null
    ): ValidationResult {
        // Log validation attempt for security monitoring
        logSecurityEvent("AudioPathValidation", "Validating path: ${path?.take(50)}...")
        
        // Check for null or empty path
        if (path.isNullOrBlank()) {
            logSecurityThreat("EmptyPath", "Received null or empty audio path")
            return ValidationResult.Invalid.EmptyPath()
        }
        
        // Check path length to prevent buffer overflow
        if (path.length > MAX_PATH_LENGTH) {
            logSecurityThreat("PathTooLong", "Audio path length ${path.length} exceeds maximum $MAX_PATH_LENGTH")
            return ValidationResult.Invalid.PathTooLong()
        }
        
        // Normalize path for consistent validation
        val normalizedPath = normalizePath(path)
        
        // Check if path is in an allowed app directory first
        val isInAllowedAppDirectory = ALLOWED_APP_PATTERNS.any { allowedPattern ->
            normalizedPath.startsWith(allowedPattern, ignoreCase = true)
        }
        
        // Check for path traversal attacks (but allow legitimate app paths)
        BLOCKED_PATTERNS.forEach { pattern ->
            if (normalizedPath.contains(pattern, ignoreCase = true)) {
                // Skip validation if this is a legitimate app directory path
                if (isInAllowedAppDirectory && !isActualSecurityThreat(pattern)) {
                    return@forEach
                }
                
                val threat = when {
                    pattern.contains("..") -> SecurityThreat.CRITICAL
                    pattern.contains("://") -> SecurityThreat.CRITICAL
                    pattern.contains("/system/") || pattern.contains("\\System32\\") -> SecurityThreat.CRITICAL
                    else -> SecurityThreat.HIGH
                }
                
                val validationResult = when {
                    pattern.contains("..") -> ValidationResult.Invalid.PathTraversal("Path contains traversal pattern: $pattern")
                    pattern.contains("://") -> ValidationResult.Invalid.ProtocolInjection("Path contains protocol injection: $pattern")
                    pattern.contains("/system/") || pattern.contains("\\System32\\") -> ValidationResult.Invalid.SystemPathAccess("Path attempts system access: $pattern")
                    else -> ValidationResult.Invalid.MaliciousCharacters("Path contains malicious pattern: $pattern")
                }
                
                logSecurityThreat("BlockedPattern", "Detected blocked pattern '$pattern' in path: ${normalizedPath.take(100)}")
                return validationResult
            }
        }
        
        // Validate file extension
        val extension = getFileExtension(normalizedPath)
        if (extension == null || !ALLOWED_AUDIO_EXTENSIONS.contains(extension.lowercase())) {
            logSecurityThreat("InvalidExtension", "Invalid audio extension '$extension' for path: ${normalizedPath.take(100)}")
            return ValidationResult.Invalid.InvalidExtension("Invalid audio file extension: $extension")
        }
        
        // Validate against base directory if provided
        allowedBaseDirectory?.let { baseDir ->
            if (!isWithinAllowedDirectory(normalizedPath, baseDir)) {
                logSecurityThreat("DirectoryBoundary", "Path outside allowed directory: ${normalizedPath.take(100)}")
                return ValidationResult.Invalid.SystemPathAccess("Path is outside allowed audio directory")
            }
        }
        
        // Additional checks for known attack patterns
        if (containsSuspiciousPatterns(normalizedPath)) {
            logSecurityThreat("SuspiciousPattern", "Suspicious patterns detected in path: ${normalizedPath.take(100)}")
            return ValidationResult.Invalid.MaliciousCharacters("Path contains suspicious patterns")
        }
        
        logSecurityEvent("AudioPathValidation", "Path validation successful")
        return ValidationResult.Valid
    }
    
    /**
     * Safe wrapper that returns validated path or null if invalid
     */
    fun getValidatedPath(path: String?, allowedBaseDirectory: String? = null): ValidatedAudioPath? {
        return when (val result = validateAudioPath(path, allowedBaseDirectory)) {
            is ValidationResult.Valid -> path?.let { ValidatedAudioPath(it) }
            is ValidationResult.Invalid -> {
                // Log security incident for monitoring
                logSecurityIncident(result.securityThreat, result.reason, path)
                null
            }
        }
    }
    
    /**
     * Normalize path to prevent encoding attacks and ensure consistent validation
     */
    private fun normalizePath(path: String): String {
        return path
            .replace("\\", "/") // Normalize directory separators
            .replace("//", "/") // Remove double slashes
            .trim()
    }
    
    /**
     * Extract file extension from path
     */
    private fun getFileExtension(path: String): String? {
        val lastDotIndex = path.lastIndexOf('.')
        val lastSlashIndex = maxOf(path.lastIndexOf('/'), path.lastIndexOf('\\'))
        
        return if (lastDotIndex > lastSlashIndex && lastDotIndex < path.length - 1) {
            path.substring(lastDotIndex + 1)
        } else {
            null
        }
    }
    
    /**
     * Check if path is within allowed base directory
     */
    private fun isWithinAllowedDirectory(path: String, baseDirectory: String): Boolean {
        val normalizedBase = normalizePath(baseDirectory)
        val normalizedPath = normalizePath(path)
        
        // Simple prefix check - in production, you'd want more sophisticated validation
        return normalizedPath.startsWith(normalizedBase) && !normalizedPath.contains("..")
    }
    
    /**
     * Check for additional suspicious patterns
     */
    private fun containsSuspiciousPatterns(path: String): Boolean {
        // Check for suspicious character sequences
        val suspiciousPatterns = listOf(
            "\\x", "\\u", "%2e%2e", "%2f", "%5c", // URL encoded attacks
            "\${", "#{", "{{", // Template injection patterns
            "javascript:", "data:", "vbscript:", // Script injections
        )
        
        return suspiciousPatterns.any { pattern ->
            path.contains(pattern, ignoreCase = true)
        }
    }
    
    /**
     * Determines if a blocked pattern represents an actual security threat
     * vs. a legitimate path component that happens to match a pattern
     */
    private fun isActualSecurityThreat(pattern: String): Boolean {
        return when {
            // Always treat these as security threats
            pattern.contains("..") -> true
            pattern.contains("://") -> true
            pattern.contains("/system/") -> true
            pattern.contains("\\System32\\") -> true
            pattern.contains("/etc/") -> true
            pattern.contains("/usr/") -> true
            pattern.contains("/bin/") -> true
            pattern.contains("/sbin/") -> true
            pattern.contains("/var/") -> true
            pattern.contains("/tmp/") -> true
            // Control characters and dangerous symbols are always threats
            pattern.any { char -> char.isISOControl() || char in "<>|&;`$\"'" } -> true
            // Storage-related patterns might be legitimate for app directories
            pattern.contains("/storage/") -> false
            pattern.contains("/data/data/") -> false
            pattern.contains("/sdcard/") -> false
            // Everything else is a potential threat
            else -> true
        }
    }
    
    /**
     * Log security events for monitoring
     */
    private fun logSecurityEvent(eventType: String, message: String) {
        // In production, this would integrate with your security logging system
        println("[SECURITY-INFO] $eventType: $message")
    }
    
    /**
     * Log security threats for immediate attention
     */
    private fun logSecurityThreat(threatType: String, message: String) {
        // In production, this would trigger security alerts
        println("[SECURITY-THREAT] $threatType: $message")
    }
    
    /**
     * Log security incidents with full context
     */
    private fun logSecurityIncident(
        threatLevel: SecurityThreat, 
        reason: String, 
        attemptedPath: String?
    ) {
        // In production, this would create security incident tickets
        println("[SECURITY-INCIDENT] Level: $threatLevel, Reason: $reason, Path: ${attemptedPath?.take(100)}")
    }
}