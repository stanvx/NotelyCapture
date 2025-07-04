package com.module.notelycompose.service

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.module.notelycompose.MainActivity
import com.module.notelycompose.R
import com.module.notelycompose.extensions.startRecordingService

class RecordingTileService : TileService() {

    override fun onClick() {
        if (qsTile.state != Tile.STATE_ACTIVE) return
        stopRecording()
    }

    private fun stopRecording() {
        if (MainActivity.paused) {
            this.startRecordingService(recordingAction = AudioRecordingService.ACTION_STOP_INSERT)
        } else {
            startMainActivity(recordingAction = AudioRecordingService.ACTION_STOP_INSERT)
        }
        qsTile.state = Tile.STATE_INACTIVE
        qsTile.icon = Icon.createWithResource(this, R.drawable.ic_outline_edit_note)
        qsTile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        if (AudioRecordingService.isRunning) {
            qsTile.state = Tile.STATE_ACTIVE
            qsTile.icon = Icon.createWithResource(this, android.R.drawable.ic_media_pause)
        } else {
            qsTile.state = Tile.STATE_INACTIVE
            qsTile.icon = Icon.createWithResource(this, R.drawable.ic_outline_edit_note)
        }
        qsTile.updateTile()
    }


    private fun startMainActivity(recordingAction: String?) {
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
}
