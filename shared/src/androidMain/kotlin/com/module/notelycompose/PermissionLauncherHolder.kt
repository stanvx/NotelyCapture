package com.module.notelycompose

import androidx.activity.result.ActivityResultLauncher

class FileSaverLauncherHolder {
    var fileSaverLauncher: ActivityResultLauncher<String>? = null
    var onFileSaved: ((String) -> Unit)? = null
}

class FileSaverHandler(
    private val fileSaverLauncherHolder: FileSaverLauncherHolder
) {
    fun saveFile(defaultFileName: String, onFileSaved: (String) -> Unit) {
        fileSaverLauncherHolder.onFileSaved = onFileSaved
        fileSaverLauncherHolder.fileSaverLauncher?.launch(defaultFileName)
    }
}
