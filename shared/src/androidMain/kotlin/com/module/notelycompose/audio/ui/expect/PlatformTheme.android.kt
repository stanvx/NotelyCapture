package com.module.notelycompose.audio.ui.expect

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.core.content.edit
import java.io.File


actual class PlatformUtils(
    private val context:Context,
    private val prefs: SharedPreferences
) {


    actual fun applyTheme(theme: Theme) {
        prefs.edit { putString("theme", theme.name) }
        when (theme) {
            Theme.LIGHT -> AppCompatDelegate
                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            Theme.DARK -> AppCompatDelegate
                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            Theme.SYSTEM -> AppCompatDelegate
                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    actual fun setDefaultTranscriptionLanguage(languageCode: String) {
        prefs.edit { putString("language", languageCode) }
    }

    actual fun getSelectedTheme(): Theme {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> Theme.DARK
            AppCompatDelegate.MODE_NIGHT_NO -> Theme.LIGHT
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> Theme.SYSTEM
            else -> Theme.SYSTEM
        }

    }

    actual fun getDefaultTranscriptionLanguage(): String {
        return prefs.getString("language", "auto") ?: "auto"
    }

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

}