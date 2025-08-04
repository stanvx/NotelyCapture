package com.module.notelycompose.data.audio

import com.module.notelycompose.domain.audio.AudioPlaybackException
import com.module.notelycompose.domain.audio.PlatformAudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.AVFoundation.*
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.fileURLWithPath

/**
 * iOS implementation of PlatformAudioPlayer using AVAudioPlayer.
 * This implementation is designed to be open for testing and provides
 * robust audio playback capabilities on iOS.
 */
open class IOSAudioPlayer : PlatformAudioPlayer {
    
    private var audioPlayer: AVAudioPlayer? = null
    private var isInitialized = false
    
    override suspend fun play(audioPath: String) = withContext(Dispatchers.Main) {
        try {
            release() // Clean up any existing player
            
            val fileUrl = NSURL.fileURLWithPath(audioPath)
            var error: NSError? = null
            
            audioPlayer = AVAudioPlayer(contentsOfURL = fileUrl, error = error.ptr).also { player ->
                if (error != null) {
                    throw AudioPlaybackException("Failed to create AVAudioPlayer: ${error?.localizedDescription}")
                }
                
                val prepared = player.prepareToPlay()
                if (!prepared) {
                    throw AudioPlaybackException("Failed to prepare audio player")
                }
                
                isInitialized = true
                val started = player.play()
                if (!started) {
                    throw AudioPlaybackException("Failed to start audio playback")
                }
            }
        } catch (e: Exception) {
            when (e) {
                is AudioPlaybackException -> throw e
                else -> throw AudioPlaybackException("Failed to play audio: ${e.message}", e)
            }
        }
    }
    
    override suspend fun pause() = withContext(Dispatchers.Main) {
        try {
            if (isInitialized && audioPlayer?.playing == true) {
                audioPlayer?.pause()
            }
        } catch (e: Exception) {
            throw AudioPlaybackException("Failed to pause audio: ${e.message}", e)
        }
    }
    
    override suspend fun stop() = withContext(Dispatchers.Main) {
        try {
            if (isInitialized) {
                audioPlayer?.stop()
                audioPlayer?.currentTime = 0.0
                isInitialized = false
            }
        } catch (e: Exception) {
            throw AudioPlaybackException("Failed to stop audio: ${e.message}", e)
        }
    }
    
    override suspend fun seekTo(position: Long) = withContext(Dispatchers.Main) {
        try {
            if (isInitialized) {
                val timeInSeconds = position.toDouble() / 1000.0
                audioPlayer?.currentTime = timeInSeconds
            }
        } catch (e: Exception) {
            throw AudioPlaybackException("Failed to seek audio: ${e.message}", e)
        }
    }
    
    override fun release() {
        try {
            audioPlayer?.stop()
            audioPlayer = null
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
                ((audioPlayer?.currentTime ?: 0.0) * 1000.0).toLong()
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
                ((audioPlayer?.duration ?: 0.0) * 1000.0).toLong()
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
            isInitialized && audioPlayer?.playing == true
        } catch (e: Exception) {
            false
        }
    }
}