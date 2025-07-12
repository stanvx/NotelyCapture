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

    private var pickedAudioPath: String? = null

    override fun launchAudioPicker(onResult: () -> Unit) {
        pickedAudioPath = null
        launcher.launch { selectedPath ->
            pickedAudioPath = selectedPath
            selectedPath?.let { onResult() }
        }
    }

    override suspend fun processPickedAudioToWav(onProgress: (Float) -> Unit): String? {
        val inputPath = copyToAppStorage() ?: return null
        val outputPath = audioConverter.convertAudioToWav(inputPath, onProgress)
        deleteFile(inputPath)
        return outputPath
    }

    private fun copyToAppStorage(): String? {
        return pickedAudioPath?.let { path ->
            NSURL.fileURLWithPath(path).savePickedAudioToAppStorage()?.path
        }.also {
            pickedAudioPath = null
        }
    }
}
