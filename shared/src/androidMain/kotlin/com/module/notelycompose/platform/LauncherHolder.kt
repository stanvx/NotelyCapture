package com.module.notelycompose.platform

import androidx.activity.result.ActivityResultLauncher

class LauncherHolder {
    var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    var audioPickerLauncher: AudioPickerLauncher? = null
}