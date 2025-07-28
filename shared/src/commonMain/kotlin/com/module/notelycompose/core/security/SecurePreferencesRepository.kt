package com.module.notelycompose.core.security

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for secure storage of sensitive data like API keys.
 * Uses platform-specific encrypted storage implementations.
 */
interface SecurePreferencesRepository {
    
    /**
     * Stores an encrypted API key securely.
     * 
     * @param key The preference key identifier
     * @param apiKey The API key to encrypt and store
     * @throws SecurityException if encryption fails
     */
    suspend fun storeEncryptedApiKey(key: String, apiKey: String)
    
    /**
     * Retrieves and decrypts an API key.
     * 
     * @param key The preference key identifier
     * @return The decrypted API key or null if not found
     * @throws SecurityException if decryption fails
     */
    suspend fun getDecryptedApiKey(key: String): String?
    
    /**
     * Removes an encrypted API key from storage.
     * 
     * @param key The preference key identifier
     */
    suspend fun removeApiKey(key: String)
    
    /**
     * Checks if an API key exists in storage.
     * 
     * @param key The preference key identifier
     * @return true if the key exists, false otherwise
     */
    suspend fun hasApiKey(key: String): Boolean
    
    /**
     * Flow that emits true when an API key is present, false otherwise.
     * This is useful for reactive UI updates.
     * 
     * @param key The preference key identifier
     * @return Flow<Boolean> indicating presence of the API key
     */
    fun observeApiKeyPresence(key: String): Flow<Boolean>
    
    /**
     * Clears all encrypted preferences.
     * This is a destructive operation that cannot be undone.
     */
    suspend fun clearAll()
    
    companion object {
        const val OPENAI_API_KEY = "openai_api_key"
        const val AI_FEATURES_ENABLED = "ai_features_enabled"
    }
}

/**
 * Result class for API key validation operations.
 */
data class ApiKeyValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * Exception thrown when secure storage operations fail.
 */
class SecureStorageException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)