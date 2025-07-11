package com.module.notelycompose

import androidx.activity.result.ActivityResultLauncher

class PermissionLauncherHolder {
    var permissionLauncher: ActivityResultLauncher<String>? = null
}

class FileSaverLauncherHolder {
    var fileSaverLauncher: ActivityResultLauncher<String>? = null
    var onFileSaved: ((String) -> Unit)? = null
}

class PermissionHandler(
    private val launcherHolder: PermissionLauncherHolder
) {
    fun requestPermission(): ActivityResultLauncher<String>?{
        return launcherHolder.permissionLauncher
    }
}

class FileSaverHandler(
    private val fileSaverLauncherHolder: FileSaverLauncherHolder
) {
    fun saveFile(defaultFileName: String, onFileSaved: (String) -> Unit) {
        fileSaverLauncherHolder.onFileSaved = onFileSaved
        fileSaverLauncherHolder.fileSaverLauncher?.launch(defaultFileName)
    }
}
