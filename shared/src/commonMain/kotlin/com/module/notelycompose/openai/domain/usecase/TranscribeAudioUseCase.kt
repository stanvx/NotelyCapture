package com.module.notelycompose.openai.domain.usecase

import com.module.notelycompose.core.security.SecurityHelper
import com.module.notelycompose.openai.domain.model.OpenAIResponse
import com.module.notelycompose.openai.domain.model.TranscriptionRequest
import com.module.notelycompose.openai.domain.model.TranscriptionResult
import com.module.notelycompose.openai.domain.repository.OpenAIRepository
import com.module.notelycompose.transcription.domain.repository.TranscriptionRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for transcribing audio files using hybrid online/offline approach.
 * Prioritizes OpenAI API when available, falls back to local Whisper model.
 */
class TranscribeAudioUseCase(
    private val openAIRepository: OpenAIRepository,
    private val localTranscriptionRepository: TranscriptionRepository,
    private val securityHelper: SecurityHelper
) {

    /**
     * Transcribes audio file using the best available method.
     * 
     * @param audioFilePath Path to the audio file to transcribe
     * @param language Optional language code for transcription
     * @param preferOnline Whether to prefer online transcription when available
     * @param fallbackToLocal Whether to fallback to local transcription on failure
     * @return TranscriptionResult containing the transcribed text or error
     */
    suspend fun execute(
        audioFilePath: String,
        language: String? = null,
        preferOnline: Boolean = true,
        fallbackToLocal: Boolean = true
    ): TranscriptionResult {
        return withContext(Dispatchers.Default) {
            try {
                // Security validation
                if (!securityHelper.isPathSafe(audioFilePath)) {
                    Napier.e("Security validation failed for audio file path: $audioFilePath")
                    return@withContext TranscriptionResult(
                        text = "",
                        language = language,
                        duration = null
                    )
                }

                // Attempt online transcription first if preferred and available
                if (preferOnline && openAIRepository.isNetworkAvailable() && openAIRepository.isConfigured()) {
                    val onlineResult = attemptOnlineTranscription(audioFilePath, language)
                    if (onlineResult != null) {
                        Napier.d("Online transcription completed successfully")
                        return@withContext onlineResult
                    }
                    
                    Napier.w("Online transcription failed, attempting fallback")
                }

                // Fallback to local transcription
                if (fallbackToLocal && localTranscriptionRepository.isValidModel()) {
                    val localResult = attemptLocalTranscription(audioFilePath, language)
                    if (localResult != null) {
                        Napier.d("Local transcription completed successfully")
                        return@withContext localResult
                    }
                }

                // Both methods failed
                Napier.e("All transcription methods failed for file: $audioFilePath")
                TranscriptionResult(
                    text = "",
                    language = language,
                    duration = null
                )

            } catch (e: Exception) {
                Napier.e("Unexpected error during transcription", e)
                TranscriptionResult(
                    text = "",
                    language = language,
                    duration = null
                )
            }
        }
    }

    /**
     * Estimates the cost of transcription for the given audio file.
     * 
     * @param audioFilePath Path to the audio file
     * @param useOnlineService Whether to estimate cost for online service
     * @return Estimated cost in cents, or null if unable to calculate
     */
    suspend fun estimateCost(audioFilePath: String, useOnlineService: Boolean = true): Int? {
        return try {
            if (useOnlineService && openAIRepository.isConfigured()) {
                openAIRepository.estimateTranscriptionCost(audioFilePath)
            } else {
                // Local transcription is free
                0
            }
        } catch (e: Exception) {
            Napier.e("Error estimating transcription cost", e)
            null
        }
    }

    /**
     * Checks if transcription is available with current configuration.
     * 
     * @return TranscriptionAvailability indicating what methods are available
     */
    suspend fun checkAvailability(): TranscriptionAvailability {
        return try {
            val onlineAvailable = openAIRepository.isNetworkAvailable() && 
                                 openAIRepository.isConfigured()
            val localAvailable = localTranscriptionRepository.isValidModel()
            
            TranscriptionAvailability(
                onlineAvailable = onlineAvailable,
                localAvailable = localAvailable,
                networkType = if (onlineAvailable) openAIRepository.getUsageStats()["network_type"] as? String else null
            )
        } catch (e: Exception) {
            Napier.e("Error checking transcription availability", e)
            TranscriptionAvailability(
                onlineAvailable = false,
                localAvailable = false
            )
        }
    }

    private suspend fun attemptOnlineTranscription(
        audioFilePath: String,
        language: String?
    ): TranscriptionResult? {
        return try {
            val request = TranscriptionRequest(
                audioFilePath = audioFilePath,
                language = language,
                temperature = 0.0 // More deterministic results
            )
            
            val response = openAIRepository.transcribeAudio(request)
            
            if (response.isSuccess && response.data != null) {
                response.data
            } else {
                Napier.w("Online transcription failed: ${response.error?.message}")
                null
            }
        } catch (e: Exception) {
            Napier.e("Error during online transcription", e)
            null
        }
    }

    private suspend fun attemptLocalTranscription(
        audioFilePath: String,
        language: String?
    ): TranscriptionResult? {
        return try {
            if (!localTranscriptionRepository.doesModelExists()) {
                Napier.w("Local transcription model not available")
                return null
            }

            var transcriptionText = ""
            var isCompleted = false

            localTranscriptionRepository.start(
                filePath = audioFilePath,
                language = language ?: "en",
                onProgress = { progress ->
                    Napier.d("Local transcription progress: $progress%")
                },
                onNewSegment = { start, end, text ->
                    transcriptionText += text + " "
                },
                onComplete = {
                    isCompleted = true
                }
            )

            // Wait for completion (this is a simplified approach)
            // In production, you might want to use a more sophisticated waiting mechanism
            while (!isCompleted) {
                kotlinx.coroutines.delay(100)
            }

            TranscriptionResult(
                text = transcriptionText.trim(),
                language = language,
                duration = null
            )

        } catch (e: Exception) {
            Napier.e("Error during local transcription", e)
            null
        } finally {
            try {
                localTranscriptionRepository.finish()
            } catch (e: Exception) {
                Napier.w("Error finishing local transcription", e)
            }
        }
    }

    /**
     * Data class representing transcription availability status
     */
    data class TranscriptionAvailability(
        val onlineAvailable: Boolean,
        val localAvailable: Boolean,
        val networkType: String? = null
    ) {
        val anyAvailable: Boolean = onlineAvailable || localAvailable
    }
}