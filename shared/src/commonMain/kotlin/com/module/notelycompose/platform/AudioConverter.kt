package com.module.notelycompose.platform

interface AudioConverter {
    /**
     * Convert .mp3 to .wav
     * @param mp3Path full path to the .mp3 file
     * @return wav path if conversion succeeds
     */
    suspend fun convertMp3ToWav(mp3Path: String): String?
}