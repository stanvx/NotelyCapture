package audio

interface FileManager {
    fun launchAudioPicker(onResult: (AudioFileResult) -> Unit)
}