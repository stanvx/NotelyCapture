package com.module.notelycompose.platform

enum class Theme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}


expect class PlatformUtils {
    fun shareText(text: String)
    fun shareRecording(path: String)
    fun exportRecording(sourcePath: String, fileName: String): Boolean
    fun requestStoragePermission(): Boolean
}
