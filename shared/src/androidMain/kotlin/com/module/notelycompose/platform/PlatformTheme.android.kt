package com.module.notelycompose.platform

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.net.toUri
import com.module.notelycompose.FileSaverHandler

actual class PlatformUtils(
    private val context: Context,
    private val fileSaverHandler: FileSaverHandler
) {

    actual fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, "Share via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    actual fun shareRecording(path: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(path)
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/wav"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share WAV file")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    actual fun exportRecordingWithFilePicker(
        sourcePath: String,
        fileName: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        try {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) {
                onResult(false, "Source file not found")
                return
            }

            fileSaverHandler.saveFile(fileName) { targetUri ->
                try {
                    val success = exportRecordingWithStorageAccessFramework(
                        sourcePath = sourcePath,
                        targetUri = targetUri
                    )
                    onResult(success, if (success) "File exported successfully" else "Failed to export file")
                } catch (e: Exception) {
                    onResult(false, "Export failed: ${e.message}")
                }
            }
        } catch (e: Exception) {
            onResult(false, "Export failed: ${e.message}")
        }
    }

    actual fun requestStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true // No permission needed for Android 10+
        }

        return context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun exportRecordingWithStorageAccessFramework(
        sourcePath: String,
        targetUri: String
    ): Boolean {
        return try {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) {
                return false
            }

            val uri = targetUri.toUri()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                sourceFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}