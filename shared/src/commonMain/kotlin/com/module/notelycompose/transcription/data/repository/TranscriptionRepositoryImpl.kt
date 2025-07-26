package com.module.notelycompose.transcription.data.repository

import com.module.notelycompose.platform.Transcriber
import com.module.notelycompose.transcription.domain.WhisperLoadResult
import com.module.notelycompose.transcription.domain.WhisperModelManager
import com.module.notelycompose.transcription.domain.repository.TranscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class TranscriptionRepositoryImpl(
    private val transcriber: Transcriber,
    private val whisperModelManager: WhisperModelManager
) : TranscriptionRepository {

    override fun doesModelExists(): Boolean {
        return transcriber.doesModelExists()
    }

    override suspend fun initialize() {
        // Use singleton model manager instead of direct transcriber initialization
        val result = whisperModelManager.ensureModelLoaded()
        
        // Handle initialization failures
        when (result) {
            is WhisperLoadResult.Success -> {
                // Model loaded successfully
            }
            is WhisperLoadResult.Failure.InsufficientMemory -> {
                throw RuntimeException("Insufficient memory to load Whisper model", result.exception)
            }
            is WhisperLoadResult.Failure.ModelNotFound -> {
                throw RuntimeException("Whisper model file not found", result.exception)
            }
            is WhisperLoadResult.Failure.LoadError -> {
                throw RuntimeException("Failed to load Whisper model", result.exception)
            }
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
        // End transcription session when manually stopped
        whisperModelManager.endTranscriptionSession()
    }

    override suspend fun start(
        filePath: String,
        language: String,
        onProgress: (Int) -> Unit,
        onNewSegment: (Long, Long, String) -> Unit,
        onComplete: () -> Unit
    ) {
        // Mark transcription session start
        whisperModelManager.startTranscriptionSession()
        
        withContext(Dispatchers.IO) {
            try {
                transcriber.start(
                    filePath = filePath,
                    language = language,
                    onProgress = onProgress,
                    onNewSegment = onNewSegment,
                    onComplete = {
                        // Mark transcription session end when complete
                        whisperModelManager.endTranscriptionSession()
                        onComplete()
                    }
                )
            } catch (e: Exception) {
                // Ensure session is ended even on error
                whisperModelManager.endTranscriptionSession()
                throw e
            }
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
