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
            EncryptedSharedPreferences.create(
                PREFERENCES_NAME,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Napier.e("Failed to initialize encrypted preferences", e)
            throw SecurityException("Failed to initialize secure storage", e)
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
                    type = SecurityMonitoringService.SecurityEventType.SUSPICIOUS_ACTIVITY,
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
                    type = SecurityMonitoringService.SecurityEventType.CONFIGURATION_TAMPERING,
                    severity = SecurityMonitoringService.SecuritySeverity.HIGH,
                    message = "Failed to store API key",
                    details = mapOf(
                        "key" to key,
                        "error" to (e.message ?: "Unknown error"),
                        "operation" to "store"
                    ),
                    throwable = e
                )
                
                throw SecureStorageException("Failed to store API key for $key", e)
            }
        }
    }
    
    override suspend fun getDecryptedApiKey(key: String): String? {
        return mutex.withLock {
            try {
                val apiKey = encryptedPreferences.getString(key, null)
                
                if (apiKey != null) {
                    securityMonitoringService.reportSecurityEvent(
                        type = SecurityMonitoringService.SecurityEventType.SUSPICIOUS_ACTIVITY,
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
                    type = SecurityMonitoringService.SecurityEventType.CONFIGURATION_TAMPERING,
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
                    type = SecurityMonitoringService.SecurityEventType.PRIVACY_VIOLATION,
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
                    type = SecurityMonitoringService.SecurityEventType.PRIVACY_VIOLATION,
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
                    type = SecurityMonitoringService.SecurityEventType.CONFIGURATION_TAMPERING,
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
                
                // Update all presence states
                keyPresenceStates.values.forEach { flow ->
                    flow.value = false
                }
                
                securityMonitoringService.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.PRIVACY_VIOLATION,
                    severity = SecurityMonitoringService.SecuritySeverity.MEDIUM,
                    message = "All secure preferences cleared",
                    details = mapOf(
                        "operation" to "clear_all"
                    )
                )
                
                Napier.w("All secure preferences cleared")
                
            } catch (e: Exception) {
                securityMonitoringService.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.PRIVACY_VIOLATION,
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
            MutableStateFlow(encryptedPreferences.getBoolean(key + PRESENCE_SUFFIX, false))
        }
    }
    
    private fun validateApiKey(key: String, apiKey: String) {
        if (key.isBlank()) {
            throw IllegalArgumentException("API key identifier cannot be blank")
        }
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("API key cannot be blank")
        }
        if (apiKey.length < 8) {
            throw IllegalArgumentException("API key is too short")
        }
    }
}
