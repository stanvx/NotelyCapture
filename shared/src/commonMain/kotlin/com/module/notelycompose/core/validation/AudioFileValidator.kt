package com.module.notelycompose.core.validation

import com.module.notelycompose.transcription.error.TranscriptionError

/**
 * Utility class for validating audio file paths and ensuring security compliance.
 * Provides comprehensive validation for audio files before processing.
 */
object AudioFileValidator {
    
    /**
     * Supported audio file extensions for transcription
     */
    private val SUPPORTED_EXTENSIONS = setOf(
        "wav", "mp3", "m4a", "aac", "flac", "ogg", "mp4", "wma"
    )
    
    /**
     * Maximum file size in bytes (50MB)
     */
    private const val MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024L
    
    /**
     * Minimum file size in bytes (1KB)
     */
    private const val MIN_FILE_SIZE_BYTES = 1024L
    
    /**
     * Validate an audio file path for security and format compliance
     * 
     * @param filePath The absolute path to the audio file
     * @param appDataDir The app's data directory for sandbox validation (optional)
     * @return Result indicating success or specific validation error
     */
    fun validateAudioFile(
        filePath: String,
        appDataDir: String? = null
    ): Result<Unit> {
        return try {
            // Check if path is not empty
            if (filePath.isBlank()) {
                return Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Audio file path cannot be empty",
                        filePath = filePath
                    )
                )
            }
            
            // Validate path format and security
            if (appDataDir != null) {
                validatePathSecurity(filePath, appDataDir)
                    .onFailure { return Result.failure(it) }
            }
            
            // Validate file existence and accessibility
            validateFileAccess(filePath)
                .onFailure { return Result.failure(it) }
            
            // Validate file format
            validateFileFormat(filePath)
                .onFailure { return Result.failure(it) }
            
            // Validate file size
            validateFileSize(filePath)
                .onFailure { return Result.failure(it) }
            
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Unexpected error during file validation: ${exception.message}",
                    filePath = filePath
                )
            )
        }
    }
    
    /**
     * Validate that the file path is within the app's sandbox and secure
     */
    private fun validatePathSecurity(filePath: String, appDataDir: String): Result<Unit> {
        // Normalize paths to prevent directory traversal attacks
        val normalizedFilePath = normalizePath(filePath)
        val normalizedAppDir = normalizePath(appDataDir)
        
        // Check for directory traversal attempts
        if (normalizedFilePath.contains("..") || normalizedFilePath.contains("./")) {
            return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Invalid file path: directory traversal detected",
                    filePath = filePath
                )
            )
        }
        
        // Ensure file is within app's data directory
        if (!normalizedFilePath.startsWith(normalizedAppDir)) {
            return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "File path must be within app data directory",
                    filePath = filePath
                )
            )
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Platform-specific file access validation
     */
    private fun validateFileAccess(filePath: String): Result<Unit> {
        if (!validateFileExists(filePath)) {
            return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Audio file does not exist",
                    filePath = filePath
                )
            )
        }
        
        if (!canReadFile(filePath)) {
            return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Cannot read audio file - check permissions",
                    filePath = filePath
                )
            )
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Validate audio file format based on extension
     */
    private fun validateFileFormat(filePath: String): Result<Unit> {
        val extension = filePath.substringAfterLast('.', "").lowercase()
        
        if (extension.isEmpty()) {
            return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "File must have a valid audio extension",
                    filePath = filePath
                )
            )
        }
        
        if (extension !in SUPPORTED_EXTENSIONS) {
            return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Unsupported audio format: .$extension. Supported formats: ${SUPPORTED_EXTENSIONS.joinToString(", ") { ".$it" }}",
                    filePath = filePath
                )
            )
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Platform-specific file size validation
     */
    private fun validateFileSize(filePath: String): Result<Unit> {
        val fileSize = getFileSize(filePath)
            ?: return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Cannot determine file size",
                    filePath = filePath
                )
            )
        
        if (fileSize < MIN_FILE_SIZE_BYTES) {
            return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Audio file is too small (minimum ${MIN_FILE_SIZE_BYTES / 1024}KB)",
                    filePath = filePath
                )
            )
        }
        
        if (fileSize > MAX_FILE_SIZE_BYTES) {
            return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Audio file is too large (maximum ${MAX_FILE_SIZE_BYTES / (1024 * 1024)}MB)",
                    filePath = filePath
                )
            )
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Normalize file path to prevent security issues
     */
    private fun normalizePath(path: String): String {
        return path
            .replace("\\", "/") // Normalize path separators
            .replace(Regex("/+"), "/") // Remove duplicate slashes
            .trimEnd('/') // Remove trailing slash
    }
    
    /**
     * Extract just the filename from a path for logging purposes
     */
    fun getSecureFileName(filePath: String): String {
        val fileName = filePath.substringAfterLast('/')
        // Don't log full paths for security
        return if (fileName.length > 50) {
            "${fileName.take(20)}...${fileName.takeLast(20)}"
        } else {
            fileName
        }
    }
}

/**
 * Platform-specific file validation functions
 * These should be implemented in androidMain and iosMain source sets
 */
expect fun validateFileExists(filePath: String): Boolean
expect fun getFileSize(filePath: String): Long?
expect fun canReadFile(filePath: String): Boolean