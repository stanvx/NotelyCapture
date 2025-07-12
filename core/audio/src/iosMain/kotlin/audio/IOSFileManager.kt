package audio

import audio.converter.AudioConverter
import audio.launcher.IOSAudioPickerLauncher
import audio.utils.deleteFile
import audio.utils.savePickedAudioToAppStorage
import platform.Foundation.NSURL

internal class IOSFileManager(
    private val launcher: IOSAudioPickerLauncher,
    private val audioConverter: AudioConverter
) : FileManager {

    private var path: String? = null
    override fun launchAudioPicker(onResult: () -> Unit) {
        path = null
        launcher.launch {
            path = it
            it?.also { onResult() }
        }
    }

    override suspend fun processPickedAudioToWav(): String? {
        val inputPath = copyToAppStorage() ?: return null
        return audioConverter.convertAudioToWav(inputPath).also {
            // TODO: Delete input file
//            deleteFile(inputPath)
        }
    }

    private fun copyToAppStorage(): String? {
        return path?.run { NSURL.fileURLWithPath(this).savePickedAudioToAppStorage()?.path }
            .also { path = null }
    }
}