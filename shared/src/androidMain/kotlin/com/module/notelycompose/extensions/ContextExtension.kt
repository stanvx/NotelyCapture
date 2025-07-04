package com.module.notelycompose.extensions

import android.content.Context
import android.content.Intent
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