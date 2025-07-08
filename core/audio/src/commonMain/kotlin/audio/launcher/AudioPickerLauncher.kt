package audio.launcher

import audio.AudioFileResult

interface AudioPickerLauncher {
    fun launch(onResult: (AudioFileResult) -> Unit)
}