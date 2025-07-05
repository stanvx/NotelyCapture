package com.module.notelycompose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {
    private val permissionLauncherHolder by inject<PermissionLauncherHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        permissionLauncherHolder.permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}