package audio

import audio.launcher.IOSAudioPickerLauncher
import audio.utils.savePickedAudioToAppStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import kotlin.time.Duration.Companion.seconds

internal class IOSFileManager(
) : FileManager {

    private var path: String? = null
    override fun launchAudioPicker(onResult: () -> Unit) {
        val launcher = IOSAudioPickerLauncher()
        path = null
        launcher.launch {
            path = it
            it?.also { onResult() }
        }
    }

    override suspend fun processPickedAudioToWav(): String? = withContext(Dispatchers.IO) {
        // TODO: Remove delay used for showing the Importing screen
        delay(2.seconds)
        path?.run { NSURL.fileURLWithPath(this).savePickedAudioToAppStorage()?.path }
    }
}