package com.module.notelycompose.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import io.github.aakira.napier.Napier

/**
 * Android implementation of SecurePreferencesRepository using EncryptedSharedPreferences.
 * Uses AES256 encryption with keys stored in Android Keystore.
 */
class SecurePreferencesRepositoryImpl(
    private val context: Context,
    private val securityMonitoringService: SecurityMonitoringService
) : SecurePreferencesRepository {
    
    private val mutex = Mutex()
    private val keyPresenceStates = mutableMapOf<String, MutableStateFlow<Boolean>>()
    
    companion object {
        private const val PREFERENCES_NAME = "notely_secure_prefs"
        private const val PRESENCE_SUFFIX = "_present"
    }
    
    private val encryptedPreferences: SharedPreferences by lazy {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            
            EncryptedSharedPreferences.create(
                PREFERENCES_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            securityMonitoringService.reportSecurityEvent(
                type = SecurityMonitoringService.SecurityEventType.ENCRYPTION_FAILURE,
                severity = SecurityMonitoringService.SecuritySeverity.CRITICAL,
                message = "Failed to initialize encrypted preferences",
                details = mapOf(
                    "error" to (e.message ?: "Unknown error"),
                    "operation" to "initialization"
                ),
                throwable = e
            )
            throw SecureStorageException("Failed to initialize secure storage", e)
        }
    }
    
    override suspend fun storeEncryptedApiKey(key: String, apiKey: String) {
        mutex.withLock {
            try {
                validateApiKey(key, apiKey)
                
                encryptedPreferences.edit()
                    .putString(key, apiKey)
                    .putBoolean(key + PRESENCE_SUFFIX, true)
                    .apply()
                
                // Update presence state
                getOrCreatePresenceFlow(key).value = true
                
                securityMonitoringService.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.ENCRYPTION_SUCCESS,
                    severity = SecurityMonitoringService.SecuritySeverity.LOW,
                    message = "API key stored successfully",
                    details = mapOf(
                        "key" to key,
                        "operation" to "store"
                    )
                )
                
                Napier.d("API key stored securely for key: $key")
                
            } catch (e: Exception) {
                securityMonitoringService.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.ENCRYPTION_FAILURE,
                    severity = SecurityMonitoringService.SecuritySeverity.HIGH,
                    message = "Failed to store API key",
                    details = mapOf(
                        "key" to key,
                        "error" to (e.message ?: "Unknown error"),
                        "operation" to "store"
                    ),
                    throwable = e
                )
                
                if (e is SecureStorageException) {
                    throw e
                } else {
                    throw SecureStorageException("Failed to store API key for $key", e)
                }
            }
        }
    }
    
    override suspend fun getDecryptedApiKey(key: String): String? {
        return mutex.withLock {
            try {
                val apiKey = encryptedPreferences.getString(key, null)
                
                if (apiKey != null) {
                    securityMonitoringService.reportSecurityEvent(
                        type = SecurityMonitoringService.SecurityEventType.DECRYPTION_SUCCESS,
                        severity = SecurityMonitoringService.SecuritySeverity.LOW,
                        message = "API key retrieved successfully",
                        details = mapOf(
                            "key" to key,
                            "operation" to "retrieve"
                        )
                    )
                }
                
                apiKey
                
            } catch (e: Exception) {
                securityMonitoringService.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.DECRYPTION_FAILURE,
                    severity = SecurityMonitoringService.SecuritySeverity.HIGH,
                    message = "Failed to retrieve API key",
                    details = mapOf(
                        "key" to key,
                        "error" to (e.message ?: "Unknown error"),
                        "operation" to "retrieve"
                    ),
                    throwable = e
                )
                
                throw SecureStorageException("Failed to retrieve API key for $key", e)
            }
        }
    }
    
    override suspend fun removeApiKey(key: String) {
        mutex.withLock {
            try {
                encryptedPreferences.edit()
                    .remove(key)
                    .remove(key + PRESENCE_SUFFIX)
                    .apply()
                
                // Update presence state
                getOrCreatePresenceFlow(key).value = false
                
                securityMonitoringService.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.DATA_DELETION,
                    severity = SecurityMonitoringService.SecuritySeverity.LOW,
                    message = "API key removed successfully",
                    details = mapOf(
                        "key" to key,
                        "operation" to "remove"
                    )
                )
                
                Napier.d("API key removed for key: $key")
                
            } catch (e: Exception) {
                securityMonitoringService.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.DATA_DELETION,
                    severity = SecurityMonitoringService.SecuritySeverity.MEDIUM,
                    message = "Failed to remove API key",
                    details = mapOf(
                        "key" to key,
                        "error" to (e.message ?: "Unknown error"),
                        "operation" to "remove"
                    ),
                    throwable = e
                )
                
                throw SecureStorageException("Failed to remove API key for $key", e)
            }
        }
    }
    
    override suspend fun hasApiKey(key: String): Boolean {
        return mutex.withLock {
            try {
                encryptedPreferences.getBoolean(key + PRESENCE_SUFFIX, false)
            } catch (e: Exception) {
                securityMonitoringService.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.DECRYPTION_FAILURE,
                    severity = SecurityMonitoringService.SecuritySeverity.LOW,
                    message = "Failed to check API key presence",
                    details = mapOf(
                        "key" to key,
                        "error" to (e.message ?: "Unknown error"),
                        "operation" to "check_presence"
                    ),
                    throwable = e
                )
                false
            }
        }
    }
    
    override fun observeApiKeyPresence(key: String): Flow<Boolean> {
        return getOrCreatePresenceFlow(key).asStateFlow()
    }
    
    override suspend fun clearAll() {
        mutex.withLock {
            try {
                encryptedPreferences.edit().clear().apply()
                
                // Reset all presence states
                keyPresenceStates.values.forEach { flow ->
                    flow.value = false
                }
                
                securityMonitoringService.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.DATA_DELETION,
                    severity = SecurityMonitoringService.SecuritySeverity.MEDIUM,
                    message = "All secure preferences cleared",
                    details = mapOf(
                        "operation" to "clear_all"
                    )
                )
                
                Napier.w("All secure preferences cleared")
                
            } catch (e: Exception) {
                securityMonitoringService.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.DATA_DELETION,
                    severity = SecurityMonitoringService.SecuritySeverity.HIGH,
                    message = "Failed to clear secure preferences",
                    details = mapOf(
                        "error" to (e.message ?: "Unknown error"),
                        "operation" to "clear_all"
                    ),
                    throwable = e
                )
                
                throw SecureStorageException("Failed to clear secure preferences", e)
            }
        }
    }
    
    private fun getOrCreatePresenceFlow(key: String): MutableStateFlow<Boolean> {
        return keyPresenceStates.getOrPut(key) {
            val initialValue = encryptedPreferences.getBoolean(key + PRESENCE_SUFFIX, false)
            MutableStateFlow(initialValue)
        }
    }
    
    private fun validateApiKey(key: String, apiKey: String) {
        when {
            key.isBlank() -> throw SecureStorageException("API key identifier cannot be blank")
            apiKey.isBlank() -> throw SecureStorageException("API key cannot be blank")
            apiKey.length < 10 -> throw SecureStorageException("API key too short")
            apiKey.length > 200 -> throw SecureStorageException("API key too long")
            !apiKey.matches(Regex("^[a-zA-Z0-9\\-_.]+$")) -> {
                throw SecureStorageException("API key contains invalid characters")
            }
        }
        
        // Additional validation for OpenAI API keys
        if (key == SecurePreferencesRepository.OPENAI_API_KEY) {
            if (!apiKey.startsWith("sk-")) {
                throw SecureStorageException("Invalid OpenAI API key format")
            }
        }
    }
}