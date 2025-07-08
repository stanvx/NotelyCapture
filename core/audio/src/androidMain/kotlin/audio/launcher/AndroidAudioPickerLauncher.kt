package audio.launcher

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import audio.AudioFileResult
import audio.utils.getFileName
import audio.utils.savePickedAudioToAppStorage

class AndroidAudioPickerLauncher(
    private val activity: ComponentActivity,
) : AudioPickerLauncher {
    private var launcherCallback: ((AudioFileResult) -> Unit)? = null

    private val launcher: ActivityResultLauncher<Intent>? =
        activity.activityResultRegistry.register(
            "pick_audio",
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data: Intent? = result.data
            val uri: Uri? = data?.data
            if (result.resultCode == Activity.RESULT_OK && uri != null) {
                val name = activity.getFileName(uri)
                val path = activity.savePickedAudioToAppStorage(uri)?.absolutePath
                launcherCallback?.invoke(AudioFileResult(name, path))
            } else {
                launcherCallback?.invoke(AudioFileResult(null, null))
            }
        }


    override fun launch(onResult: (AudioFileResult) -> Unit) {
        this.launcherCallback = onResult

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        launcher?.launch(Intent.createChooser(intent, "Select Audio File"))
    }
}