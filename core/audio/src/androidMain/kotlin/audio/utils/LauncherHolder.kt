package audio.utils

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import audio.launcher.AndroidAudioPickerLauncher

class LauncherHolder {
    var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    var audioPickerLauncher: AndroidAudioPickerLauncher? = null

    fun init(activity: ComponentActivity) {
        permissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
        audioPickerLauncher = AndroidAudioPickerLauncher(activity)
    }
}