package com.module.notelycompose.core.validation

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
        
        // Get canonical paths to resolve symbolic links and normalize paths
        val canonicalFilePath = file.canonicalPath
        val canonicalAppDir = appDir.canonicalPath
        
        // Ensure the file is within the app directory using canonical paths
        if (!canonicalFilePath.startsWith(canonicalAppDir)) {
            Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Invalid file path: canonical path is outside app directory",
                    filePath = filePath
                )
            )
        } else {
            Result.success(Unit)
        }
    } catch (e: Exception) {
        // If canonical path resolution fails, return error for security
        Result.failure(
            TranscriptionError.AudioFileValidationError(
                message = "Path validation failed: unable to resolve canonical path - ${e.message}",
                filePath = filePath
            )
        )
    }
}