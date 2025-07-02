package com.module.notelycompose.android.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.content.Intent
import com.module.notelycompose.service.AudioRecordingService

class RecordingTileService : TileService() {

    override fun onClick() {
        val tile = qsTile
        if (tile.state == Tile.STATE_INACTIVE) {
            startService(Intent(this, AudioRecordingService::class.java).apply {
                action = "START"
            })
            tile.state = Tile.STATE_ACTIVE
        } else {
            startService(Intent(this, AudioRecordingService::class.java).apply {
                action = "STOP"
            })
            tile.state = Tile.STATE_INACTIVE
        }
        tile.updateTile()
    }
}
