package audio.launcher

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class AndroidAudioPickerLauncher(
    activity: ComponentActivity,
) {
    private var launcherCallback: ((Uri?) -> Unit)? = null

    private val launcher: ActivityResultLauncher<Intent>? =
        activity.activityResultRegistry.register(
            "pick_audio",
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data: Intent? = result.data
            val uri: Uri? = data?.data
            if (result.resultCode == Activity.RESULT_OK && uri != null) {
                launcherCallback?.invoke(uri)
            } else {
                launcherCallback?.invoke(null)
            }
        }

    fun launch(onResult: (Uri?) -> Unit) {
        this.launcherCallback = onResult

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        launcher?.launch(Intent.createChooser(intent, "Select Audio File"))
    }
}