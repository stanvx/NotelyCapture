package com.module.notelycompose.domain.audio

/**
 * Platform-agnostic audio player interface for note playback.
 * This interface allows for different implementations on Android and iOS
 * while providing a consistent API and enabling easy testing with mock implementations.
 */
interface PlatformAudioPlayer {
    /**
     * Plays audio from the specified file path.
     *
     * @param audioPath Absolute path to the audio file
     * @throws AudioPlaybackException if playback fails
     */
    suspend fun play(audioPath: String)
    
    /**
     * Pauses the currently playing audio.
     * No-op if no audio is currently playing.
     */
    suspend fun pause()
    
    /**
     * Stops audio playback and resets position to beginning.
     */
    suspend fun stop()
    
    /**
     * Seeks to a specific position in the audio file.
     *
     * @param position Position in milliseconds
     */
    suspend fun seekTo(position: Long)
    
    /**
     * Releases audio player resources.
     * Should be called when the player is no longer needed.
     */
    fun release()
}

/**
 * Exception thrown when audio playback operations fail.
 */
class AudioPlaybackException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)