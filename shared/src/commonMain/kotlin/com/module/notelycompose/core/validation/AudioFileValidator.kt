package com.module.notelycompose.core.validation

import com.module.notelycompose.core.constants.AppConstants
import com.module.notelycompose.transcription.error.TranscriptionError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.jvm.JvmStatic

/**
 * Validator for audio files used in transcription.
 * Provides security validation, format checking, and file access verification.
 * 
 * Features:
 * - Audio format validation
 * - Path security validation (directory traversal protection)
 * - File existence and access verification
 * - Secure filename generation for logging
 */
object AudioFileValidator {
    
    /**
     * Supported audio file extensions (case insensitive)
     */
    private val supportedExtensions = setOf(
        "wav", "mp3", "m4a", "aac", "flac", "ogg", "mp4", "wma"
    )
    
    /**
     * Validates an audio file for transcription processing.
     * 
     * @param filePath Path to the audio file
     * @param appDirectory Optional app directory to validate against (for security)
     * @return Result indicating success or failure with appropriate error
     */
    @JvmStatic
    suspend fun validateAudioFile(filePath: String, appDirectory: String? = null): Result<Unit> {
        return try {
            // Check for empty/blank path
            if (filePath.isBlank()) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Audio file path cannot be empty",
                        filePath = filePath
                    )
                )
            }
            
            // Validate file format
            val extension = getFileExtension(filePath).lowercase()
            if (!supportedExtensions.contains(extension)) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Unsupported audio format: .$extension. Supported formats: ${supportedExtensions.joinToString(", ") { ".$it" }}",
                        filePath = filePath
                    )
                )
            }
            
            // Security validation if app directory is provided
            appDirectory?.let { appDir ->
                val securityValidation = validatePathSecurity(filePath, appDir)
                if (securityValidation.isFailure) {
                    return securityValidation
                }
            }
            
            // Platform-specific file validation (run on default dispatcher)
            val platformValidation = withContext(Dispatchers.Default) {
                validateFileAccess(filePath)
            }
            if (platformValidation.isFailure) {
                return platformValidation
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Validation failed: ${e.message}",
                    filePath = filePath
                )
            )
        }
    }
    
    /**
     * Validates path security to prevent directory traversal attacks.
     * Enhanced with canonical path resolution and comprehensive security checks.
     * 
     * @param filePath The file path to validate
     * @param appDirectory The app's data directory
     * @return Result indicating if path is secure
     */
    private fun validatePathSecurity(filePath: String, appDirectory: String): Result<Unit> {
        try {
            // Check for directory traversal patterns (comprehensive list)
            val dangerousPatterns = listOf(
                "..", "./", ".\\", 
                "%2e%2e", "%2E%2E", // URL encoded ..
                "%2f", "%2F", "%5c", "%5C", // URL encoded / and \
                "\\\\", "//", // Double separators
                "\u0000", // Null byte injection
            )
            
            val lowerPath = filePath.lowercase()
            for (pattern in dangerousPatterns) {
                if (lowerPath.contains(pattern.lowercase())) {
                    return Result.failure(
                        TranscriptionError.AudioFileValidationError(
                            message = "Invalid file path: potentially dangerous pattern detected",
                            filePath = filePath
                        )
                    )
                }
            }
            
            // Check for suspicious characters and control characters
            if (filePath.any { it.isISOControl() && it != '\t' && it != '\n' && it != '\r' }) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Invalid file path: contains control characters",
                        filePath = filePath
                    )
                )
            }
            
            // Platform-specific path validation and normalization
            val securityValidation = validateCanonicalPath(filePath, appDirectory)
            if (securityValidation.isFailure) {
                return securityValidation
            }
            
            // Basic path normalization fallback for platforms without canonical path support
            val normalizedFilePath = filePath.replace("\\", "/").replace("//", "/")
            val normalizedAppDir = appDirectory.replace("\\", "/").replace("//", "/")
            
            // Ensure file path is within app directory bounds
            if (!normalizedFilePath.startsWith(normalizedAppDir)) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Invalid file path: file must be within app data directory",
                        filePath = filePath
                    )
                )
            }
            
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Path security validation failed: ${e.message}",
                    filePath = filePath
                )
            )
        }
    }
    
    /**
     * Platform-specific file access validation.
     * Actual implementation provided by platform-specific expect/actual.
     */
    private fun validateFileAccess(filePath: String): Result<Unit> {
        // For now, just check basic file operations that should work on all platforms
        try {
            if (!validateFileExists(filePath)) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Audio file does not exist or is not accessible",
                        filePath = filePath
                    )
                )
            }
            
            if (!canReadFile(filePath)) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Cannot read audio file: insufficient permissions",
                        filePath = filePath
                    )
                )
            }
            
            val fileSize = getFileSize(filePath)
            if (fileSize == null || fileSize == 0L) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Audio file is empty or size cannot be determined",
                        filePath = filePath
                    )
                )
            }
            
            // Check if file is too large (100MB limit)
            if (fileSize > AppConstants.Audio.MAX_FILE_SIZE_BYTES) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Audio file is too large (max 100MB): ${fileSize / AppConstants.Audio.BYTES_PER_MB}MB",
                        filePath = filePath
                    )
                )
            }
            
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "File access validation failed: ${e.message}",
                    filePath = filePath
                )
            )
        }
    }
    
    /**
     * Gets file extension from path.
     */
    private fun getFileExtension(filePath: String): String {
        val lastDot = filePath.lastIndexOf('.')
        return if (lastDot > 0 && lastDot < filePath.length - 1) {
            filePath.substring(lastDot + 1)
        } else {
            ""
        }
    }
    
    /**
     * Generates a secure filename for logging purposes.
     * Truncates long filenames and removes directory information.
     * 
     * @param filePath The full file path
     * @return Secure filename suitable for logging
     */
    @JvmStatic
    fun getSecureFileName(filePath: String): String {
        val fileName = filePath.substringAfterLast('/')
            .substringAfterLast('\\')
        
        return if (fileName.length > 50) {
            val extension = getFileExtension(fileName)
            val nameWithoutExt = fileName.substringBeforeLast('.')
            val truncated = nameWithoutExt.take(40)
            "$truncated...$extension"
        } else {
            fileName
        }
    }
}

/**
 * Platform-specific file existence check.
 */
expect fun validateFileExists(filePath: String): Boolean

/**
 * Platform-specific file size retrieval.
 */
expect fun getFileSize(filePath: String): Long?

/**
 * Platform-specific file read permission check.
 */
expect fun canReadFile(filePath: String): Boolean

/**
 * Platform-specific canonical path validation for enhanced security.
 * Resolves symbolic links and validates against canonical app directory path.
 */
expect fun validateCanonicalPath(filePath: String, appDirectory: String): Result<Unit>