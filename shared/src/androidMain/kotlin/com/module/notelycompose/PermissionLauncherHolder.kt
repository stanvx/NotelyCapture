package com.module.notelycompose

import androidx.activity.result.ActivityResultLauncher

class PermissionLauncherHolder {
    var permissionLauncher: ActivityResultLauncher<String>? = null
}


class PermissionHandler(
    private val launcherHolder: PermissionLauncherHolder
) {
    fun requestPermission(): ActivityResultLauncher<String>?{
        return launcherHolder.permissionLauncher
    }
}