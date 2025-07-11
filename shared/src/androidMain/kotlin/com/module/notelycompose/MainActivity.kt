package com.module.notelycompose

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.koin.android.ext.android.inject
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.platform.Theme
import android.net.Uri

class MainActivity : AppCompatActivity() {
    private val permissionLauncherHolder by inject<PermissionLauncherHolder>()
    private val fileSaverLauncherHolder by inject<FileSaverLauncherHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setupPermissionLauncher()
        setupFileSaverLauncher()
        enableEdgeToEdge()
        setContent {
            val systemUiController = rememberSystemUiController()
            val preferenceRepository by inject<PreferencesRepository>()
            val uiMode by preferenceRepository.getTheme().collectAsState(Theme.SYSTEM.name)
            val darkTheme = when (uiMode) {
                Theme.DARK.name -> true
                Theme.LIGHT.name -> false
                else -> isSystemInDarkTheme()
            }
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = !darkTheme
            )
            App()
        }
    }

    private fun setupPermissionLauncher() {
        permissionLauncherHolder.permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    }

    private fun setupFileSaverLauncher() {
        fileSaverLauncherHolder.fileSaverLauncher =
            registerForActivityResult(ActivityResultContracts.CreateDocument("audio/wav")) { uri: Uri? ->
                uri?.let {
                    fileSaverLauncherHolder.onFileSaved?.invoke(uri.toString())
                }
            }
    }
}