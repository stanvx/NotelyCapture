package com.module.notelycompose.transcription.domain.repository

interface TranscriptionRepository {
    fun doesModelExists(): Boolean
    suspend fun initialize()
    suspend fun finish()
    suspend fun stop()
    suspend fun start(
        filePath: String,
        language: String,
        onProgress: (Int) -> Unit,
        onNewSegment: (Long, Long, String) -> Unit,
        onComplete: () -> Unit
    )
    fun hasRecordingPermission(): Boolean
    suspend fun requestRecordingPermission(): Boolean
    fun isValidModel(): Boolean
}
