package audio.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import audio.AudioFileResult
import audio.launcher.IOSAudioPickerLauncher

@Composable
actual fun FilePicker(
	show: Boolean,
    onResult: (AudioFileResult) -> Unit,
) {
    val launcher = remember { IOSAudioPickerLauncher() }

    LaunchedEffect(show) {
        if (show) {
            launcher.launch(onResult = onResult)
        }
    }
}

