package com.module.notelycompose.core.validation

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