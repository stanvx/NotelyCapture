package com.module.notelycompose.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.module.notelycompose.notes.domain.InsertNoteUseCase
import com.module.notelycompose.notes.presentation.detail.model.EditorPresentationState
import com.module.notelycompose.notes.presentation.mapper.TextAlignPresentationMapper
import com.module.notelycompose.notes.presentation.mapper.TextFormatPresentationMapper
import com.module.notelycompose.platform.AudioRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AudioRecordingService : Service() {
    private val audioRecorder by inject<AudioRecorder>()
    private val insertNoteUseCase by inject<InsertNoteUseCase>()
    private val textFormatPresentationMapper by inject<TextFormatPresentationMapper>()
    private val textAlignPresentationMapper by inject<TextAlignPresentationMapper>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, buildNotification())
        isRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
            ACTION_STOP -> stopRecording()
            ACTION_STOP_INSERT -> stopRecorderAndInsertNote()
        }
        stoppedFromQuicSettings = false
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
        if (audioRecorder.hasRecordingPermission()) {
            audioRecorder.startRecording()
        }
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

    private fun stopRecorderAndInsertNote() {
        audioRecorder.stopRecording()
        val noteState = EditorPresentationState()
        coroutineScope.launch {
            insertNoteUseCase.execute(
                title = noteState.content.text,
                content = noteState.content.text,
                starred = noteState.starred,
                formatting = noteState.formats.map {
                    textFormatPresentationMapper.mapToDomainModel(
                        it
                    )
                },
                textAlign = textAlignPresentationMapper.mapToDomainModel(noteState.textAlign),
                recordingPath = audioRecorder.getRecordingFilePath(),
            )
            stoppedFromQuicSettings = true
            stopSelf()
        }
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
        var isRunning = false
        private var stoppedFromQuicSettings = false

        fun stopedByTile(): Boolean {
            return stoppedFromQuicSettings.also {
                stoppedFromQuicSettings = false
            }
        }

        const val ACTION_START = "START_RECORDING"
        const val ACTION_PAUSE = "PAUSE_RECORDING"
        const val ACTION_RESUME = "RESUME_RECORDING"
        const val ACTION_STOP = "STOP_RECORDING"
        const val ACTION_STOP_INSERT = "STOP_RECORDING_INSERT"
    }

}
