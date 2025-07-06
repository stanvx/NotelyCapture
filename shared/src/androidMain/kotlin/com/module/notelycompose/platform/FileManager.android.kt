package com.module.notelycompose.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.module.notelycompose.core.debugPrintln
import java.io.File

class AndroidFileManager(
    private val context: Context,
    private val launcherHolder: LauncherHolder,
) : FileManager {

    override fun launchAudioPicker(onResult: (AudioFileResult) -> Unit) {
        if (checkStoragePermission()) {
            launcherHolder.audioPickerLauncher?.launch(onResult)
        }
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

actual fun deleteFile(filePath: String): Boolean {
    debugPrintln { "Deleting file: $filePath" }
    return try {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

actual fun fileExists(filePath: String): Boolean {
    return File(filePath).exists()
}