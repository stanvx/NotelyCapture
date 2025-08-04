package com.module.notelycompose.data.audio

import android.content.Context
import android.media.MediaPlayer
import com.module.notelycompose.domain.audio.AudioPlaybackException
import com.module.notelycompose.domain.audio.PlatformAudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android implementation of PlatformAudioPlayer using MediaPlayer.
 * This implementation is designed to be non-final to allow for testing with inheritance
 * or can be easily mocked through the interface.
 */
open class AndroidAudioPlayer(private val context: Context) : PlatformAudioPlayer {
    
    private var mediaPlayer: MediaPlayer? = null
    private var isInitialized = false
    
    override suspend fun play(audioPath: String) = withContext(Dispatchers.IO) {
        try {
            release() // Clean up any existing player
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
                prepareAsync()
                setOnPreparedListener { player ->
                    isInitialized = true
                    player.start()
                }
                setOnErrorListener { _, what, extra ->
                    throw AudioPlaybackException("MediaPlayer error: what=$what, extra=$extra")
                }
                setOnCompletionListener {
                    isInitialized = false
                }
            }
        } catch (e: Exception) {
            throw AudioPlaybackException("Failed to play audio: ${e.message}", e)
        }
    }
    
    override suspend fun pause() = withContext(Dispatchers.Main) {
        try {
            if (isInitialized && mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            throw AudioPlaybackException("Failed to pause audio: ${e.message}", e)
        }
    }
    
    override suspend fun stop() = withContext(Dispatchers.Main) {
        try {
            if (isInitialized) {
                mediaPlayer?.stop()
                isInitialized = false
            }
        } catch (e: Exception) {
            throw AudioPlaybackException("Failed to stop audio: ${e.message}", e)
        }
    }
    
    override suspend fun seekTo(position: Long) = withContext(Dispatchers.Main) {
        try {
            if (isInitialized) {
                mediaPlayer?.seekTo(position.toInt())
            }
        } catch (e: Exception) {
            throw AudioPlaybackException("Failed to seek audio: ${e.message}", e)
        }
    }
    
    override fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            isInitialized = false
        } catch (e: Exception) {
            // Log error but don't throw since this is cleanup
        }
    }
    
    /**
     * Gets current playback position in milliseconds.
     * Returns 0 if player is not initialized.
     */
    open fun getCurrentPosition(): Long {
        return try {
            if (isInitialized) {
                mediaPlayer?.currentPosition?.toLong() ?: 0L
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Gets total duration in milliseconds.
     * Returns 0 if player is not initialized.
     */
    open fun getDuration(): Long {
        return try {
            if (isInitialized) {
                mediaPlayer?.duration?.toLong() ?: 0L
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Checks if audio is currently playing.
     */
    open fun isPlaying(): Boolean {
        return try {
            isInitialized && mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }
}