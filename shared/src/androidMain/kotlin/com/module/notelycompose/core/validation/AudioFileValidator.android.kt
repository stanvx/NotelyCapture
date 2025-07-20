package com.module.notelycompose.core.validation

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