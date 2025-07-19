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
                    // Validate speed range for Android
                    if (speed < 0.125f || speed > 8.0f) {
                        android.util.Log.w("PlatformAudioPlayer", 
                            "Warning: Android playback speed $speed is outside supported range (0.125-8.0)")
                    }
                    
                    val params = PlaybackParams().setSpeed(speed)
                    player.playbackParams = params
                    android.util.Log.d("PlatformAudioPlayer", 
                        "Successfully set Android playback speed to $speed")
                } catch (e: IllegalStateException) {
                    android.util.Log.e("PlatformAudioPlayer", 
                        "IllegalStateException setting playback speed to $speed: MediaPlayer in invalid state", e)
                    // MediaPlayer is in invalid state - gracefully ignore
                } catch (e: IllegalArgumentException) {
                    android.util.Log.e("PlatformAudioPlayer", 
                        "IllegalArgumentException setting playback speed to $speed: Invalid speed value", e)
                    // Invalid speed value - gracefully ignore
                } catch (e: Exception) {
                    android.util.Log.e("PlatformAudioPlayer", 
                        "Unexpected error setting playback speed to $speed", e)
                    // Fallback: ignore speed change if not supported
                }
            } else {
                android.util.Log.w("PlatformAudioPlayer", 
                    "Playback speed control not supported on API level ${Build.VERSION.SDK_INT} (requires API 23+)")
            }
        } ?: run {
            android.util.Log.w("PlatformAudioPlayer", 
                "Cannot set playback speed - MediaPlayer is null")
        }
    }
}