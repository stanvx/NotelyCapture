package audio.compose

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import audio.AudioFileResult
import audio.utils.getFileName
import audio.utils.savePickedAudioToAppStorage

@Composable
actual fun FilePicker(
	show: Boolean,
	onResult: (AudioFileResult) -> Unit
) {
	val context: Context = LocalContext.current
	val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
		val data: Intent? = result.data
		val uri: Uri? = data?.data
		if (result.resultCode == Activity.RESULT_OK && uri != null) {
			val name = context.getFileName(uri)
			val path = context.savePickedAudioToAppStorage(uri)?.absolutePath
			onResult.invoke(AudioFileResult(name, path))
		} else {
			onResult.invoke(AudioFileResult(null, null))
		}
	}

	LaunchedEffect(show) {
		if (show) {
			val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
				type = "audio/*"
				addCategory(Intent.CATEGORY_OPENABLE)
			}

			launcher.launch(Intent.createChooser(intent, "Select Audio File"))
		}
	}
}