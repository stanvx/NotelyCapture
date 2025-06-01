package com.module.notelycompose.audio.ui.expect

enum class Theme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}


expect class PlatformUtils {
    fun shareText(text: String)
    fun applyTheme(theme: Theme)
    fun setDefaultTranscriptionLanguage(languageCode: String)
    fun getDefaultTranscriptionLanguage(): String
    fun getSelectedTheme(): Theme
    fun shareRecording(path: String)
}