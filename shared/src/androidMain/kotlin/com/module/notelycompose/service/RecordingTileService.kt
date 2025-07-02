package com.module.notelycompose.service

import android.service.quicksettings.TileService

class RecordingTileService : TileService() {

    override fun onClick() {
        val tile = qsTile
//        if (tile.state == Tile.STATE_INACTIVE) {
//            startService(Intent(this, AudioRecordingService::class.java).apply {
//                Intent. = "START"
//            })
//            tile.state = Tile.STATE_ACTIVE
//        } else {
//            startService(Intent(this, AudioRecordingService::class.java).apply {
//                Intent.setAction = "STOP"
//            })
//            tile.state = Tile.STATE_INACTIVE
//        }
        tile.updateTile()
    }
}
