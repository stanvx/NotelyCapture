package com.module.notelycompose.platform

import android.media.PlaybackParams
import android.os.Build

actual class PlatformAudioPlayer {
    private var mediaPlayer: android.media.MediaPlayer? = null

    actual suspend fun prepare(filePath: String): Int {
        android.util.Log.d("PlatformAudioPlayer", "prepare() called with filePath: $filePath")
        
        // Properly release existing MediaPlayer with error handling
        mediaPlayer?.let { existingPlayer ->
            try {
                android.util.Log.d("PlatformAudioPlayer", "Releasing existing MediaPlayer")
                if (existingPlayer.isPlaying) {
                    existingPlayer.stop()
                }
                existingPlayer.release()
                android.util.Log.d("PlatformAudioPlayer", "Existing MediaPlayer released successfully")
            } catch (e: Exception) {
                android.util.Log.w("PlatformAudioPlayer", "Error releasing existing MediaPlayer", e)
            }
        }
        mediaPlayer = null
        
        try {
            android.util.Log.d("PlatformAudioPlayer", "Creating new MediaPlayer")
            val player = android.media.MediaPlayer().apply {
                // Set error listener to handle MediaPlayer errors gracefully
                setOnErrorListener { mp, what, extra ->
                    android.util.Log.e("PlatformAudioPlayer", "MediaPlayer error: what=$what, extra=$extra")
                    false // Return false to trigger onCompletion
                }
                
                android.util.Log.d("PlatformAudioPlayer", "Setting data source: $filePath")
                setDataSource(filePath)
                android.util.Log.d("PlatformAudioPlayer", "Calling prepare()")
                prepare()
                android.util.Log.d("PlatformAudioPlayer", "MediaPlayer prepared successfully")
            }
            mediaPlayer = player
            val duration = player.duration
            android.util.Log.d("PlatformAudioPlayer", "Audio duration: ${duration}ms")
            return duration
        } catch (e: Exception) {
            android.util.Log.e("PlatformAudioPlayer", "Failed to prepare MediaPlayer", e)
            mediaPlayer = null // Ensure null on failure
            return 0
        }
    }

    actual fun play() {
        android.util.Log.d("PlatformAudioPlayer", "play() called")
        mediaPlayer?.let {
            try {
                android.util.Log.d("PlatformAudioPlayer", "Starting MediaPlayer")
                it.start()
                android.util.Log.d("PlatformAudioPlayer", "MediaPlayer started successfully")
            } catch (e: Exception) {
                android.util.Log.e("PlatformAudioPlayer", "Failed to start MediaPlayer", e)
            }
        } ?: android.util.Log.w("PlatformAudioPlayer", "Cannot play - MediaPlayer is null")
    }

    actual fun pause() {
        android.util.Log.d("PlatformAudioPlayer", "pause() called")
        mediaPlayer?.let {
            try {
                android.util.Log.d("PlatformAudioPlayer", "Pausing MediaPlayer")
                it.pause()
                android.util.Log.d("PlatformAudioPlayer", "MediaPlayer paused successfully")
            } catch (e: Exception) {
                android.util.Log.e("PlatformAudioPlayer", "Failed to pause MediaPlayer", e)
            }
        } ?: android.util.Log.w("PlatformAudioPlayer", "Cannot pause - MediaPlayer is null")
    }

    actual fun stop() {
        mediaPlayer?.stop()
    }

    actual fun release() {
        android.util.Log.d("PlatformAudioPlayer", "release() called")
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    android.util.Log.d("PlatformAudioPlayer", "Stopping MediaPlayer before release")
                    player.stop()
                }
                android.util.Log.d("PlatformAudioPlayer", "Releasing MediaPlayer")
                player.release()
                android.util.Log.d("PlatformAudioPlayer", "MediaPlayer released successfully")
            } catch (e: Exception) {
                android.util.Log.e("PlatformAudioPlayer", "Error during MediaPlayer release", e)
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