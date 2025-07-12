package audio.converter

interface AudioConverter {

    suspend fun convertAudioToWav(path: String, onProgress: (Float) -> Unit): String?
}