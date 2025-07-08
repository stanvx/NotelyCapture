package audio.utils

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import audio.launcher.AndroidAudioPickerLauncher
import audio.launcher.AudioPickerLauncher

class LauncherHolder {
    var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    var audioPickerLauncher: AudioPickerLauncher? = null

    fun init(activity: ComponentActivity) {
        permissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
        audioPickerLauncher = AndroidAudioPickerLauncher(activity)
    }
}