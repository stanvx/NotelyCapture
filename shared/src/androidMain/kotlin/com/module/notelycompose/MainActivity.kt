package com.module.notelycompose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.module.notelycompose.platform.AudioPickerLauncherImpl
import com.module.notelycompose.platform.LauncherHolder
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        injectLauncher()
        enableEdgeToEdge()
        setContent {
            App()
        }
    }

    private fun injectLauncher() {
        val launcherHolder by inject<LauncherHolder>()
        val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
        launcherHolder.permissionLauncher = permissionLauncher
        launcherHolder.audioPickerLauncher = AudioPickerLauncherImpl(this)
    }
}
