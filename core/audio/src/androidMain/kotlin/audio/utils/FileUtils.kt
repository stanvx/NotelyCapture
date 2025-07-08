package audio.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

private const val RECORDING_PREFIX = "recording_"
private const val RECORDING_EXTENSION = ".wav"

fun Context.generateNewAudioFile(): File {
    val fileName = "$RECORDING_PREFIX${System.currentTimeMillis()}$RECORDING_EXTENSION"
    val outputFile = File(this.getExternalFilesDir(Environment.DIRECTORY_MUSIC), fileName)
    return outputFile
}

fun Context.savePickedAudioToAppStorage(uri: Uri): File? {
    val file = generateNewAudioFile()
    return try {
        this.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun Context.getFileName(uri: Uri): String? {
    val cursor = this.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            return cursor.getString(nameIndex)
        }
    }
    return null
}