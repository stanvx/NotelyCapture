package com.module.notelycompose.platform

import com.module.notelycompose.core.debugPrintln
import java.io.File

actual fun deleteFile(filePath: String): Boolean {
    debugPrintln { "Deleting file: $filePath" }
    return try {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

actual fun fileExists(filePath: String): Boolean {
    return File(filePath).exists()
}