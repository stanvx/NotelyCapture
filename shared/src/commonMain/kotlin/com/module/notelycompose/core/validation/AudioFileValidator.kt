package com.module.notelycompose.core.validation

import com.module.notelycompose.core.constants.AppConstants
import com.module.notelycompose.transcription.error.TranscriptionError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.jvm.JvmStatic

/**
 * Validator for audio files used in transcription with security validation and format checking.
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
     */
    @JvmStatic
    suspend fun validateAudioFile(filePath: String, appDirectory: String? = null): Result<Unit> {
        return try {
            if (filePath.isBlank()) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Audio file path cannot be empty",
                        filePath = filePath
                    )
                )
            }
            
            val extension = getFileExtension(filePath).lowercase()
            if (extension.isEmpty()) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Audio file must have a valid extension",
                        filePath = filePath
                    )
                )
            }
            
            if (!supportedExtensions.contains(extension)) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Unsupported audio format: .$extension. Supported formats: ${supportedExtensions.joinToString(", ") { ".$it" }}",
                        filePath = filePath
                    )
                )
            }
            
            // Check for multiple extensions in filename only (not full path)
            val fileName = filePath.substringAfterLast('/').substringAfterLast('\\')
            if (fileName.count { it == '.' } > 1) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Invalid filename: multiple extensions detected",
                        filePath = filePath
                    )
                )
            }
            
            appDirectory?.let { appDir ->
                val securityValidation = validatePathSecurity(filePath, appDir)
                if (securityValidation.isFailure) {
                    return securityValidation
                }
            }
            
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
     */
    private fun validatePathSecurity(filePath: String, appDirectory: String): Result<Unit> {
        try {
            val dangerousPatterns = listOf(
                "..", "./", ".\\", 
                "%2e%2e", "%2E%2E",
                "%2f", "%2F", "%5c", "%5C",
                "\\\\", "//",
                "\u0000",
                "%00",
                "~",
                "$",
                "`",
                "|",
                "&",
                ";",
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
            
            if (filePath.any { it.isISOControl() && it != '\t' && it != '\n' && it != '\r' }) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Invalid file path: contains control characters",
                        filePath = filePath
                    )
                )
            }
            
            val securityValidation = validateCanonicalPath(filePath, appDirectory)
            if (securityValidation.isFailure) {
                return securityValidation
            }
            
            val normalizedFilePath = filePath.replace("\\", "/").replace("//", "/")
            val normalizedAppDir = appDirectory.replace("\\", "/").replace("//", "/")
            
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
     */
    private fun validateFileAccess(filePath: String): Result<Unit> {
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

/**
 * Platform-specific audio duration retrieval in milliseconds.
 * Returns null if duration cannot be determined.
 */
expect fun getAudioDurationMs(filePath: String): Long?