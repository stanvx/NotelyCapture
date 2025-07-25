package com.module.notelycompose.modelDownloader

import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.transcription.domain.repository.TranscriptionRepository

/**
 * Service for managing model availability and setup status.
 * Provides centralized logic for checking model status and coordinating setup.
 */
class ModelAvailabilityService(
    private val transcriptionRepository: TranscriptionRepository,
    private val preferencesRepository: PreferencesRepository
) {

    /**
     * Check the current model availability status
     */
    suspend fun checkModelAvailability(): ModelStatus {
        val hasCompletedSetup = preferencesRepository.hasCompletedModelSetup()
        val modelExists = transcriptionRepository.doesModelExists()
        val isValidModel = if (modelExists) transcriptionRepository.isValidModel() else false

        return when {
            hasCompletedSetup && modelExists && isValidModel -> ModelStatus.Ready
            hasCompletedSetup && (!modelExists || !isValidModel) -> ModelStatus.CorruptedOrMissing
            !hasCompletedSetup && modelExists && isValidModel -> ModelStatus.Available
            else -> ModelStatus.NotAvailable
        }
    }

    /**
     * Ensure the model is ready for use. Returns true if model is ready or successfully initialized.
     */
    suspend fun ensureModelReady(): Boolean {
        return when (checkModelAvailability()) {
            ModelStatus.Ready -> true
            ModelStatus.Available -> {
                // Model exists but setup not marked complete - just mark as complete
                preferencesRepository.setModelSetupCompleted(true)
                true
            }
            ModelStatus.CorruptedOrMissing -> {
                // Model was set up before but is now missing/corrupt
                preferencesRepository.setModelSetupCompleted(false)
                false
            }
            ModelStatus.NotAvailable -> false
        }
    }

    /**
     * Mark model setup as completed in preferences
     */
    suspend fun markModelSetupCompleted() {
        preferencesRepository.setModelSetupCompleted(true)
    }

    /**
     * Reset model setup status (useful for testing or recovery scenarios)
     */
    suspend fun resetModelSetupStatus() {
        preferencesRepository.setModelSetupCompleted(false)
    }
}

/**
 * Represents the current status of the model availability
 */
sealed class ModelStatus {
    /**
     * Model is downloaded, valid, and setup is marked complete
     */
    data object Ready : ModelStatus()

    /**
     * Model file exists and is valid, but setup completion is not marked in preferences
     */
    data object Available : ModelStatus()

    /**
     * Setup was completed before, but model is now missing or corrupted
     */
    data object CorruptedOrMissing : ModelStatus()

    /**
     * Model does not exist and setup is not complete
     */
    data object NotAvailable : ModelStatus()
}