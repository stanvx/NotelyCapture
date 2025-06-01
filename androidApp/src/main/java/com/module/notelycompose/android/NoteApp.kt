package com.module.notelycompose.android

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.module.notelycompose.audio.ui.expect.Theme
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NoteApp : Application() {

    @Inject
    lateinit var prefs: SharedPreferences


    override fun onCreate() {
        super.onCreate()
        val theme = prefs.getString("theme", Theme.SYSTEM.name)

        when (theme) {
            Theme.LIGHT.name -> AppCompatDelegate
                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            Theme.DARK.name -> AppCompatDelegate
                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            Theme.SYSTEM.name -> AppCompatDelegate
                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

    }
}