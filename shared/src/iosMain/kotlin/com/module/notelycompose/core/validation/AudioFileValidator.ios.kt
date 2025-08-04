package com.module.notelycompose.core.validation

import com.module.notelycompose.transcription.error.TranscriptionError
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.CMTimeGetSeconds
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import kotlin.math.roundToLong

/**
 * iOS-specific implementation of file validation functions
 */
actual fun validateFileExists(filePath: String): Boolean {
    return try {
        NSFileManager.defaultManager.fileExistsAtPath(filePath)
    } catch (exception: Exception) {
        false
    }
}

actual fun getFileSize(filePath: String): Long? {
    return try {
        val fileManager = NSFileManager.defaultManager
        if (fileManager.fileExistsAtPath(filePath)) {
            val attributes = fileManager.attributesOfItemAtPath(filePath, null)
            attributes?.get("NSFileSize") as? Long
        } else {
            null
        }
    } catch (exception: Exception) {
        null
    }
}

actual fun canReadFile(filePath: String): Boolean {
    return try {
        NSFileManager.defaultManager.isReadableFileAtPath(filePath)
    } catch (exception: Exception) {
        false
    }
}

actual fun validateCanonicalPath(filePath: String, appDirectory: String): Result<Unit> {
    return try {
        val fileManager = NSFileManager.defaultManager
        
        // Check if app directory exists
        if (!fileManager.fileExistsAtPath(appDirectory)) {
            return Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "App directory does not exist for validation",
                    filePath = filePath
                )
            )
        }
        
        // For iOS, use basic path validation since canonical path resolution
        // is more complex and not as critical in the iOS sandbox environment
        if (!filePath.startsWith(appDirectory)) {
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
        Result.failure(
            TranscriptionError.AudioFileValidationError(
                message = "Path validation failed: ${e.message}",
                filePath = filePath
            )
        )
    }
}

actual fun getAudioDurationMs(filePath: String): Long? {
    return try {
        val url = NSURL.fileURLWithPath(filePath)
        val asset = AVURLAsset.URLAssetWithURL(url, null)
        val durationSeconds = CMTimeGetSeconds(asset.duration)
        
        // Convert seconds to milliseconds, handle invalid durations
        if (durationSeconds.isFinite() && durationSeconds > 0) {
            (durationSeconds * 1000).roundToLong()
        } else {
            null
        }
    } catch (e: Exception) {
        // Log error but don't throw - return null to indicate failure
        println("AudioFileValidator: Failed to get audio duration for $filePath: ${e.message}")
        null
    }
}