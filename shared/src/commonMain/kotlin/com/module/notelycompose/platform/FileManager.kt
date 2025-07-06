package com.module.notelycompose.platform

expect fun deleteFile(filePath: String): Boolean
expect fun fileExists(filePath: String): Boolean

interface FileManager {
    fun launchAudioPicker(onResult: (AudioFileResult) -> Unit)
}