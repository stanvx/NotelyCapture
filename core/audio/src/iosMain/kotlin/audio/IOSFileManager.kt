package audio

import audio.launcher.AudioPickerLauncher

class IOSFileManager(
    private val audioPickerLauncher: AudioPickerLauncher
) : FileManager {

    override fun launchAudioPicker(onResult: (AudioFileResult) -> Unit) {
        audioPickerLauncher.launch(onResult)
    }
}