package com.module.notelycompose.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.module.notelycompose.platform.AudioRecorder
import org.koin.android.ext.android.inject

class AudioRecordingService : Service() {
    private val audioRecorder by inject<AudioRecorder>()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "recording_channel",
            "Recording",
            NotificationManager.IMPORTANCE_LOW,
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun startRecording() {
        audioRecorder.startRecording()
    }

    private fun pauseRecording() {
        audioRecorder.pauseRecording()
    }

    private fun resumeRecording() {
        audioRecorder.resumeRecording()
    }

    private fun stopRecording() {
        audioRecorder.stopRecording()
        stopSelf()
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "recording_channel")
            .setContentTitle("Recording Note")
            .setContentText("Tap to stop recording")
            .setSmallIcon(android.R.drawable.presence_audio_online)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "START_RECORDING"
        const val ACTION_PAUSE = "PAUSE_RECORDING"
        const val ACTION_RESUME = "RESUME_RECORDING"
        const val ACTION_STOP = "STOP_RECORDING"
    }

}
