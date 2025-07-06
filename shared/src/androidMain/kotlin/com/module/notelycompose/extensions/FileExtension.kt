package com.module.notelycompose.extensions

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

private const val RECORDING_PREFIX = "recording_"
private const val RECORDING_EXTENSION = ".wav"

internal fun Context.generateNewAudioFile(): File {
    val fileName = "$RECORDING_PREFIX${System.currentTimeMillis()}$RECORDING_EXTENSION"
    val outputFile = File(this.getExternalFilesDir(Environment.DIRECTORY_MUSIC), fileName)
    return outputFile
}

internal fun Context.savePickedAudioToAppStorage(uri: Uri): String? {
    val file = generateNewAudioFile()
    return try {
        this.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}