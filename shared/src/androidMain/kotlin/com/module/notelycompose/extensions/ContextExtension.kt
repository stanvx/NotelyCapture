package com.module.notelycompose.extensions

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import com.module.notelycompose.MainActivity
import com.module.notelycompose.service.AudioRecordingService

internal fun Context.startRecordingService(recordingAction: String) {
    val intent = Intent(this, AudioRecordingService::class.java).apply {
        action = recordingAction
    }
    startForegroundService(intent)
}

internal fun Context.restartMainActivity() {
    val intent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    try {
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

internal fun TileService.startMainActivity(recordingAction: String?) {
    val intent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        action = recordingAction
    }
    val pendingIntentFlags =
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    val pendingIntent = PendingIntent.getActivity(
        this,
        0,
        intent,
        pendingIntentFlags
    )

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(pendingIntent)
        } else {
            startActivity(intent)
        }
    } catch (e: PendingIntent.CanceledException) {
        e.printStackTrace()
    }
}