package audio.converter

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

internal class IOSAudioConverter : AudioConverter {
    override suspend fun convertAudioToWav(path: String): String? {
        //TODO: Implement this
        delay(1.seconds)
        return path
    }
}