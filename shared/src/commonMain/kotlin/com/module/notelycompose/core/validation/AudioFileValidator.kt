package com.module.notelycompose.core.validation

import com.module.notelycompose.transcription.error.TranscriptionError
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
    fun validateAudioFile(filePath: String, appDirectory: String? = null): Result<Unit> {
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
            
            // Platform-specific file validation
            val platformValidation = validateFileAccess(filePath)
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
     * 
     * @param filePath The file path to validate
     * @param appDirectory The app's data directory
     * @return Result indicating if path is secure
     */
    private fun validatePathSecurity(filePath: String, appDirectory: String): Result<Unit> {
        // Check for directory traversal patterns
        if (filePath.contains("..") || filePath.contains("./")) {
            return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Invalid file path: directory traversal detected",
                    filePath = filePath
                )
            )
        }
        
        // Normalize paths and check if file is within app directory
        val normalizedFilePath = filePath.replace("\\", "/")
        val normalizedAppDir = appDirectory.replace("\\", "/")
        
        if (!normalizedFilePath.startsWith(normalizedAppDir)) {
            return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Invalid file path: file must be within app data directory",
                    filePath = filePath
                )
            )
        }
        
        return Result.success(Unit)
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
            if (fileSize > 100 * 1024 * 1024) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Audio file is too large (max 100MB): ${fileSize / (1024 * 1024)}MB",
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