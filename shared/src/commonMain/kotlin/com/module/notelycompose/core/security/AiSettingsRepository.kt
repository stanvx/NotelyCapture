package com.module.notelycompose.core.security

import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing AI-related settings including secure API key storage.
 * Combines regular preferences with secure encrypted storage.
 */
class AiSettingsRepository(
    private val securePreferencesRepository: SecurePreferencesRepository,
    private val securityHelper: SecurityHelper
) {
    
    /**
     * Stores the OpenAI API key securely after validation.
     * 
     * @param apiKey The API key to store
     * @param userContext Optional user context for security monitoring
     * @throws SecureStorageException if storage fails
     * @throws IllegalArgumentException if API key is invalid
     */
    suspend fun storeOpenAiApiKey(
        apiKey: String,
        userContext: SecurityMonitoringService.UserContext? = null
    ) {
        // Validate API key format
        val validation = securityHelper.validateOpenAiApiKey(apiKey, userContext)
        if (!validation.isValid) {
            throw IllegalArgumentException(validation.errorMessage ?: "Invalid API key")
        }
        
        // Store securely
        securePreferencesRepository.storeEncryptedApiKey(
            SecurePreferencesRepository.OPENAI_API_KEY,
            apiKey
        )
        
        securityHelper.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.ENCRYPTION_SUCCESS,
            severity = SecurityMonitoringService.SecuritySeverity.LOW,
            message = "OpenAI API key stored successfully",
            details = mapOf(
                "operation" to "store_api_key",
                "key_preview" to securityHelper.sanitizeApiKeyForLogging(apiKey)
            ),
            userContext = userContext
        )
    }
    
    /**
     * Retrieves the OpenAI API key if present.
     * 
     * @param userContext Optional user context for security monitoring
     * @return The API key or null if not stored
     * @throws SecureStorageException if decryption fails
     */
    suspend fun getOpenAiApiKey(
        userContext: SecurityMonitoringService.UserContext? = null
    ): String? {
        return try {
            val apiKey = securePreferencesRepository.getDecryptedApiKey(
                SecurePreferencesRepository.OPENAI_API_KEY
            )
            
            if (apiKey != null) {
                securityHelper.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.DECRYPTION_SUCCESS,
                    severity = SecurityMonitoringService.SecuritySeverity.LOW,
                    message = "OpenAI API key retrieved successfully",
                    details = mapOf(
                        "operation" to "retrieve_api_key",
                        "key_preview" to securityHelper.sanitizeApiKeyForLogging(apiKey)
                    ),
                    userContext = userContext
                )
            }
            
            apiKey
        } catch (e: SecureStorageException) {
            securityHelper.reportSecurityEvent(
                type = SecurityMonitoringService.SecurityEventType.DECRYPTION_FAILURE,
                severity = SecurityMonitoringService.SecuritySeverity.HIGH,
                message = "Failed to retrieve OpenAI API key",
                details = mapOf(
                    "operation" to "retrieve_api_key",
                    "error" to (e.message ?: "Unknown error")
                ),
                userContext = userContext,
                throwable = e
            )
            throw e
        }
    }
    
    /**
     * Removes the stored OpenAI API key.
     * 
     * @param userContext Optional user context for security monitoring
     */
    suspend fun removeOpenAiApiKey(
        userContext: SecurityMonitoringService.UserContext? = null
    ) {
        securePreferencesRepository.removeApiKey(SecurePreferencesRepository.OPENAI_API_KEY)
        
        securityHelper.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.DATA_DELETION,
            severity = SecurityMonitoringService.SecuritySeverity.LOW,
            message = "OpenAI API key removed",
            details = mapOf(
                "operation" to "remove_api_key"
            ),
            userContext = userContext
        )
    }
    
    /**
     * Checks if an OpenAI API key is stored.
     * 
     * @return true if API key exists, false otherwise
     */
    suspend fun hasOpenAiApiKey(): Boolean {
        return securePreferencesRepository.hasApiKey(SecurePreferencesRepository.OPENAI_API_KEY)
    }
    
    /**
     * Observes the presence of an OpenAI API key.
     * Useful for reactive UI updates.
     * 
     * @return Flow<Boolean> indicating API key presence
     */
    fun observeOpenAiApiKeyPresence(): Flow<Boolean> {
        return securePreferencesRepository.observeApiKeyPresence(
            SecurePreferencesRepository.OPENAI_API_KEY
        )
    }
    
    /**
     * Validates an API key without storing it.
     * Useful for real-time validation in UI.
     * 
     * @param apiKey The API key to validate
     * @param userContext Optional user context for security monitoring
     * @return ApiKeyValidationResult with validation status
     */
    suspend fun validateApiKey(
        apiKey: String?,
        userContext: SecurityMonitoringService.UserContext? = null
    ): ApiKeyValidationResult {
        return securityHelper.validateOpenAiApiKey(apiKey, userContext)
    }
    
    /**
     * Clears all AI-related secure data.
     * This is a destructive operation.
     * 
     * @param userContext Optional user context for security monitoring
     */
    suspend fun clearAllAiData(
        userContext: SecurityMonitoringService.UserContext? = null
    ) {
        securePreferencesRepository.clearAll()
        
        securityHelper.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.DATA_DELETION,
            severity = SecurityMonitoringService.SecuritySeverity.MEDIUM,
            message = "All AI data cleared",
            details = mapOf(
                "operation" to "clear_all_ai_data"
            ),
            userContext = userContext
        )
    }
}

/**
 * Data class representing complete AI settings state.
 */
data class AiSettingsState(
    val hasApiKey: Boolean = false,
    val apiKeyValid: Boolean = false,
    val lastValidated: Long? = null
)

/**
 * UI state for AI settings screen.
 */
data class AiSettingsUiState(
    val hasApiKey: Boolean = false,
    val isValidating: Boolean = false,
    val validationError: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val showApiKey: Boolean = false
)