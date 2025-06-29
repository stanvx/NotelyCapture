package com.module.notelycompose.platform

expect class PlatformAudioPlayer() {
    suspend fun prepare(filePath: String): Int
    fun play()
    fun pause()
    fun stop()
    fun release()
    fun seekTo(position: Int)
    fun getCurrentPosition(): Int
    fun isPlaying(): Boolean
}