package com.module.notelycompose

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.module.notelycompose.extensions.restartMainActivity
import com.module.notelycompose.extensions.startRecordingService
import com.module.notelycompose.service.AudioRecordingService
import com.module.notelycompose.service.AudioRecordingService.Companion.ACTION_START
import com.module.notelycompose.service.AudioRecordingService.Companion.ACTION_STOP_INSERT
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {

    companion object {
        var paused = false
    }
    private val permissionLauncherHolder by inject<PermissionLauncherHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncherHolder.permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
        installSplashScreen()
        enableEdgeToEdge()
        handleIntentAction(intent.action)
        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntentAction(intent.action)
    }

    override fun onResume() {
        super.onResume()
        paused = false
        if (AudioRecordingService.stopedByTile()) {
            this.restartMainActivity()
        }
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }
}

private fun Activity.handleIntentAction(intentAction: String?) {
    when (val action = intentAction) {
        ACTION_START, ACTION_STOP_INSERT -> {
            this.startRecordingService(recordingAction = action)
            intent.setAction(null)
        }
    }
}
