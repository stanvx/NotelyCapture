package com.module.notelycompose.platform

enum class Theme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}

expect class PlatformUtils {
    fun shareText(text: String)
    fun shareRecording(path: String)
    fun exportRecordingWithFilePicker(
        sourcePath: String,
        fileName: String,
        onResult: (Boolean, String?) -> Unit
    )
    fun requestStoragePermission(): Boolean
}
