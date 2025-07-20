package com.module.notelycompose.transcription.data.repository

import com.module.notelycompose.platform.Transcriber
import com.module.notelycompose.transcription.domain.repository.TranscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class TranscriptionRepositoryImpl(
    private val transcriber: Transcriber
) : TranscriptionRepository {

    override fun doesModelExists(): Boolean {
        return transcriber.doesModelExists()
    }

    override suspend fun initialize() {
        withContext(Dispatchers.IO) {
            transcriber.initialize()
        }
    }

    override suspend fun finish() {
        withContext(Dispatchers.IO) {
            transcriber.finish()
        }
    }

    override suspend fun stop() {
        withContext(Dispatchers.IO) {
            transcriber.stop()
        }
    }

    override suspend fun start(
        filePath: String,
        language: String,
        onProgress: (Int) -> Unit,
        onNewSegment: (Long, Long, String) -> Unit,
        onComplete: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            transcriber.start(
                filePath = filePath,
                language = language,
                onProgress = onProgress,
                onNewSegment = onNewSegment,
                onComplete = onComplete
            )
        }
    }

    override fun hasRecordingPermission(): Boolean {
        return transcriber.hasRecordingPermission()
    }

    override suspend fun requestRecordingPermission(): Boolean {
        return transcriber.requestRecordingPermission()
    }

    override fun isValidModel(): Boolean {
        return transcriber.isValidModel()
    }
}
