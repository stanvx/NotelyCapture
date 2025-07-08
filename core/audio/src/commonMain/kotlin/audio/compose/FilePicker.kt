package audio.compose

import androidx.compose.runtime.Composable
import audio.AudioFileResult

@Composable
expect fun FilePicker(
	show: Boolean,
	onResult: (AudioFileResult) -> Unit,
)