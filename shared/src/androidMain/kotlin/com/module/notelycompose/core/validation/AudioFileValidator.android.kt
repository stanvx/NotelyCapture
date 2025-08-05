package com.module.notelycompose.core.validation

import android.media.MediaMetadataRetriever
import com.module.notelycompose.transcription.error.TranscriptionError
import java.io.File

/**
 * Android-specific implementation of file validation functions
 */
actual fun validateFileExists(filePath: String): Boolean {
    return try {
        File(filePath).exists()
    } catch (exception: Exception) {
        false
    }
}

actual fun getFileSize(filePath: String): Long? {
    return try {
        val file = File(filePath)
        if (file.exists() && file.isFile) {
            file.length()
        } else {
            null
        }
    } catch (exception: Exception) {
        null
    }
}

actual fun canReadFile(filePath: String): Boolean {
    return try {
        val file = File(filePath)
        file.exists() && file.isFile && file.canRead()
    } catch (exception: Exception) {
        false
    }
}

actual fun validateCanonicalPath(filePath: String, appDirectory: String): Result<Unit> {
    return try {
        val file = File(filePath)
        val appDir = File(appDirectory)
        
        // First check if the app directory exists - if not, we can't validate
        if (!appDir.exists()) {
            return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "App directory does not exist for validation",
                    filePath = filePath
                )
            )
        }
        
        // For non-existent files, validate the parent directory path instead
        val pathToValidate = if (file.exists()) {
            file
        } else {
            // Use parent directory for validation if file doesn't exist
            file.parentFile ?: return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Cannot determine parent directory for validation",
                    filePath = filePath
                )
            )
        }
        
        // Get canonical paths to resolve symbolic links and normalize paths
        val canonicalFilePath = if (pathToValidate.exists()) {
            pathToValidate.canonicalPath
        } else {
            // Fallback to absolute path if canonical path fails
            pathToValidate.absolutePath
        }
        val canonicalAppDir = appDir.canonicalPath
        
        // Ensure the file/directory is within the app directory using canonical paths
        if (!canonicalFilePath.startsWith(canonicalAppDir)) {
            Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Invalid file path: path is outside app directory",
                    filePath = filePath
                )
            )
        } else {
            Result.success(Unit)
        }
    } catch (e: Exception) {
        // If canonical path resolution fails, fall back to basic path validation
        return try {
            val normalizedFilePath = File(filePath).absolutePath
            val normalizedAppDir = File(appDirectory).absolutePath
            
            if (!normalizedFilePath.startsWith(normalizedAppDir)) {
                Result.failure(
                    TranscriptionError.AudioFileValidationError(
                        message = "Invalid file path: path is outside app directory (fallback validation)",
                        filePath = filePath
                    )
                )
            } else {
                Result.success(Unit)
            }
        } catch (fallbackException: Exception) {
            Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Path validation failed: ${e.message}",
                    filePath = filePath
                )
            )
        }
    }
}

actual fun getAudioDurationMs(filePath: String): Long? {
    return try {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLongOrNull()
        } finally {
            retriever.release()
        }
    } catch (e: Exception) {
        // Log the error but don't throw - return null to indicate failure
        android.util.Log.w("AudioFileValidator", "Failed to get audio duration for $filePath", e)
        null
    }
}