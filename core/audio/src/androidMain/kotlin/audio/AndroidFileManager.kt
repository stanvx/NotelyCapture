package audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import audio.converter.AudioConverter
import audio.utils.LauncherHolder
import audio.utils.deleteFile
import audio.utils.savePickedAudioToAppStorage

internal class AndroidFileManager(
    private val context: Context,
    private val launcherHolder: LauncherHolder,
    private val audioConverter: AudioConverter
) : FileManager {

    private var uri: Uri? = null

    override fun launchAudioPicker(
        onResult: () -> Unit
    ) {
        uri = null
        if (checkStoragePermission()) {
            launcherHolder.audioPickerLauncher?.launch {
                uri = it
                it?.also { onResult() }
            }
        }
    }

    override suspend fun processPickedAudioToWav(): String? {
        val inputPath = copyToAppStorage() ?: return null
        return audioConverter.convertAudioToWav(inputPath).also {
            deleteFile(inputPath)
        }
    }

    private fun copyToAppStorage(): String? {
        return uri?.run { context.savePickedAudioToAppStorage(this)?.absolutePath }
            .also { uri = null }
    }

    private fun checkStoragePermission(): Boolean {
        val permissions = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allGranted) {
            launcherHolder.permissionLauncher?.launch(permissions.toTypedArray())
        }

        return allGranted
    }
}