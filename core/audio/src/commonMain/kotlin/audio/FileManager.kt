package audio

interface FileManager {
    fun launchAudioPicker(onResult: () -> Unit)

    suspend fun processPickedAudioToWav(onProgress: (Float) -> Unit): String?
}