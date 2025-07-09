package com.module.notelycompose.service

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.module.notelycompose.android.R
import com.module.notelycompose.extensions.launchMainActivityFromTile
import com.module.notelycompose.extensions.startRecordingService

class RecordingTileService : TileService() {

    override fun onClick() {
        if (qsTile.state == Tile.STATE_INACTIVE) {
            this.launchMainActivityFromTile()
            return
        }
        stopRecording()
    }

    private fun stopRecording() {
        this.startRecordingService(recordingAction = AudioRecordingService.ACTION_STOP_FROM_TILE)
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
}
