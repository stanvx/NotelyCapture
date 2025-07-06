package com.module.notelycompose.platform

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.module.notelycompose.extensions.savePickedAudioToAppStorage


internal class AudioPickerLauncherImpl(
    private val activity: Activity,
) : AudioPickerLauncher {
    private var launcherCallback: ((AudioFileResult) -> Unit)? = null

    private val launcher: ActivityResultLauncher<Intent>? =
        (activity as? androidx.activity.ComponentActivity)
            ?.activityResultRegistry
            ?.register("pick_audio", ActivityResultContracts.StartActivityForResult()) { result ->
                val data: Intent? = result.data
                val uri: Uri? = data?.data
                if (result.resultCode == Activity.RESULT_OK && uri != null) {
                    val name = getFileName(uri)
                    val mimeType = activity.contentResolver.getType(uri)
                    val path = activity.savePickedAudioToAppStorage(uri)
                    launcherCallback?.invoke(AudioFileResult(name, path, mimeType))
                } else {
                    launcherCallback?.invoke(AudioFileResult(null, null, null))
                }
            }


    override fun launch(onResult: (AudioFileResult) -> Unit) {
        this.launcherCallback = onResult

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        launcher?.launch(Intent.createChooser(intent, "Select Audio File"))
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = activity.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                return cursor.getString(nameIndex)
            }
        }
        return null
    }
}