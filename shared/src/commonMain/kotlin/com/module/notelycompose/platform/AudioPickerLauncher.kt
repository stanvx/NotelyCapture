package com.module.notelycompose.platform

interface AudioPickerLauncher {
    fun launch(onResult: (AudioFileResult) -> Unit)
}

data class AudioFileResult(
    val name: String?,
    val path: String?,
    val mimeType: String?
)