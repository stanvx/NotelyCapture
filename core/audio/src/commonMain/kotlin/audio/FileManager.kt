package audio

interface FileManager {
    fun launchAudioPicker(onResult: () -> Unit)

    suspend fun processPickedAudioToWav(): String?
}