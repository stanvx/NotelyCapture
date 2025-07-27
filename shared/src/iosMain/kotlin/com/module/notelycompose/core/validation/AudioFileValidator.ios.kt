package com.module.notelycompose.core.validation

import com.module.notelycompose.transcription.error.TranscriptionError
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.stringByStandardizingPath

/**
 * iOS-specific implementation of file validation functions
 */
@OptIn(ExperimentalForeignApi::class)
actual fun validateFileExists(filePath: String): Boolean {
    return try {
        val fileManager = NSFileManager.defaultManager
        val standardizedPath = (filePath as NSString).stringByStandardizingPath
        fileManager.fileExistsAtPath(standardizedPath)
    } catch (exception: Exception) {
        false
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun getFileSize(filePath: String): Long? {
    return try {
        val fileManager = NSFileManager.defaultManager
        val standardizedPath = (filePath as NSString).stringByStandardizingPath
        
        if (!fileManager.fileExistsAtPath(standardizedPath)) {
            return null
        }
        
        val url = NSURL.fileURLWithPath(standardizedPath)
        val attributes = fileManager.attributesOfItemAtPath(standardizedPath, error = null)
        
        attributes?.get("NSFileSize")?.let { size ->
            (size as? Number)?.toLong()
        }
    } catch (exception: Exception) {
        null
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun canReadFile(filePath: String): Boolean {
    return try {
        val fileManager = NSFileManager.defaultManager
        val standardizedPath = (filePath as NSString).stringByStandardizingPath
        
        // Check if file exists and is readable
        fileManager.fileExistsAtPath(standardizedPath) && 
        fileManager.isReadableFileAtPath(standardizedPath)
    } catch (exception: Exception) {
        false
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun validateCanonicalPath(filePath: String, appDirectory: String): Result<Unit> {
    return try {
        val fileManager = NSFileManager.defaultManager
        
        // Standardize paths to resolve symbolic links and normalize paths (iOS equivalent of canonical paths)
        val standardizedFilePath = (filePath as NSString).stringByStandardizingPath
        val standardizedAppDir = (appDirectory as NSString).stringByStandardizingPath
        
        // Ensure the file is within the app directory using standardized paths
        if (!standardizedFilePath.startsWith(standardizedAppDir)) {
            Result.failure(
                TranscriptionError.AudioFileValidationError(
                    message = "Invalid file path: standardized path is outside app directory",
                    filePath = filePath
                )
            )
        } else {
            Result.success(Unit)
        }
    } catch (e: Exception) {
        // If path standardization fails, return error for security
        Result.failure(
            TranscriptionError.AudioFileValidationError(
                message = "Path validation failed: unable to standardize path - ${e.message}",
                filePath = filePath
            )
        )
    }
}