package com.module.notelycompose.platform

import android.media.PlaybackParams
import android.os.Build

actual class PlatformAudioPlayer {
    private var mediaPlayer: android.media.MediaPlayer? = null

    actual suspend fun prepare(filePath: String): Int {
        mediaPlayer?.release()
        try {
            val player = android.media.MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
            }
            mediaPlayer = player
            return player.duration
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }

    actual fun play() {
        mediaPlayer?.start()
    }

    actual fun pause() {
        mediaPlayer?.pause()
    }

    actual fun stop() {
        mediaPlayer?.stop()
    }

    actual fun release() {
        mediaPlayer?.release()
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
                    android.util.Log.e("PlatformAudioPlayer", "Failed to set playback speed", e)
                    // Fallback: ignore speed change if not supported
                }
            }
            // For API levels below 23, speed control is not supported
        }
    }
}