package com.module.notelycompose.platform

import android.media.PlaybackParams
import android.os.Build

actual class PlatformAudioPlayer {
    private var mediaPlayer: android.media.MediaPlayer? = null

    actual suspend fun prepare(filePath: String): Int {
        // Release existing MediaPlayer if any
        mediaPlayer?.let { existingPlayer ->
            try {
                if (existingPlayer.isPlaying) {
                    existingPlayer.stop()
                }
                existingPlayer.release()
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
        mediaPlayer = null
        
        return try {
            val player = android.media.MediaPlayer().apply {
                setOnErrorListener { _, what, extra ->
                    android.util.Log.e("PlatformAudioPlayer", "MediaPlayer error: what=$what, extra=$extra")
                    false
                }
                setDataSource(filePath)
                prepare()
            }
            mediaPlayer = player
            player.duration
        } catch (e: Exception) {
            android.util.Log.e("PlatformAudioPlayer", "Failed to prepare audio", e)
            mediaPlayer = null
            0
        }
    }

    actual fun play() {
        mediaPlayer?.let {
            try {
                it.start()
            } catch (e: Exception) {
                android.util.Log.e("PlatformAudioPlayer", "Failed to start playback", e)
            }
        }
    }

    actual fun pause() {
        mediaPlayer?.let {
            try {
                it.pause()
            } catch (e: Exception) {
                android.util.Log.e("PlatformAudioPlayer", "Failed to pause playback", e)
            }
        }
    }

    actual fun stop() {
        mediaPlayer?.stop()
    }

    actual fun release() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
        mediaPlayer = null
    }

    actual fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    actual fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    actual fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    actual fun setPlaybackSpeed(speed: Float) {
        mediaPlayer?.let { player ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val params = PlaybackParams().setSpeed(speed)
                    player.playbackParams = params
                } catch (e: Exception) {
                    // Ignore speed change errors
                }
            }
        }
    }
}