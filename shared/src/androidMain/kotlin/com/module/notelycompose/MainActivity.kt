package com.module.notelycompose

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.module.notelycompose.di.init
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication


class MainActivity : AppCompatActivity() {
    private lateinit var  permissionLauncher: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            KoinApplication(application = {
                androidContext(context.applicationContext)
                init()
            }) {
                initializePermissionLauncher()

                App()
            }
        }
    }

    private fun initializePermissionLauncher() {
        val permissionLauncherHolder by inject<PermissionLauncherHolder>()
        permissionLauncherHolder.permissionLauncher = this.permissionLauncher
    }

}

