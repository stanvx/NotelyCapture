package com.module.notelycompose.core.security

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for AiSettingsRepository functionality.
 */
class AiSettingsRepositoryTest {
    
    private val mockSecurityMonitoringService = object : SecurityMonitoringService {
        override suspend fun reportSecurityEvent(
            type: SecurityMonitoringService.SecurityEventType,
            severity: SecurityMonitoringService.SecuritySeverity,
            message: String,
            details: Map<String, String>,
            userContext: SecurityMonitoringService.UserContext?,
            throwable: Throwable?
        ) {
            // Mock implementation - do nothing
        }
        
        override suspend fun reportValidationFailure(
            validationType: String,
            input: String,
            validationError: String,
            userContext: SecurityMonitoringService.UserContext?
        ) {
            // Mock implementation - do nothing
        }
        
        override suspend fun reportFileSystemViolation(
            operation: String,
            filePath: String,
            violation: String,
            userContext: SecurityMonitoringService.UserContext?
        ) {
            // Mock implementation - do nothing
        }
    }
    
    private class MockSecurePreferencesRepository : SecurePreferencesRepository {
        private val storage = mutableMapOf<String, String>()
        private val presenceFlows = mutableMapOf<String, MutableStateFlow<Boolean>>()
        
        override suspend fun storeEncryptedApiKey(key: String, apiKey: String) {
            storage[key] = apiKey
            getOrCreatePresenceFlow(key).value = true
        }
        
        override suspend fun getDecryptedApiKey(key: String): String? {
            return storage[key]
        }
        
        override suspend fun removeApiKey(key: String) {
            storage.remove(key)
            getOrCreatePresenceFlow(key).value = false
        }
        
        override suspend fun hasApiKey(key: String): Boolean {
            return storage.containsKey(key)
        }
        
        override fun observeApiKeyPresence(key: String): Flow<Boolean> {
            return getOrCreatePresenceFlow(key)
        }
        
        override suspend fun clearAll() {
            storage.clear()
            presenceFlows.values.forEach { it.value = false }
        }
        
        private fun getOrCreatePresenceFlow(key: String): MutableStateFlow<Boolean> {
            return presenceFlows.getOrPut(key) {
                MutableStateFlow(storage.containsKey(key))
            }
        }
    }
    
    private val mockSecurePrefs = MockSecurePreferencesRepository()
    private val securityHelper = SecurityHelper(mockSecurityMonitoringService)
    private val aiSettingsRepository = AiSettingsRepository(mockSecurePrefs, securityHelper)
    
    @Test
    fun `storeOpenAiApiKey should store valid API key successfully`() = runTest {
        val validApiKey = "sk-1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        
        aiSettingsRepository.storeOpenAiApiKey(validApiKey)
        
        assertTrue(aiSettingsRepository.hasOpenAiApiKey())
        assertEquals(validApiKey, aiSettingsRepository.getOpenAiApiKey())
    }
    
    @Test
    fun `storeOpenAiApiKey should reject invalid API key`() = runTest {
        val invalidApiKey = "invalid-key"
        
        assertFailsWith<IllegalArgumentException> {
            aiSettingsRepository.storeOpenAiApiKey(invalidApiKey)
        }
        
        assertFalse(aiSettingsRepository.hasOpenAiApiKey())
    }
    
    @Test
    fun `getOpenAiApiKey should return null when no key stored`() = runTest {
        val result = aiSettingsRepository.getOpenAiApiKey()
        
        assertNull(result)
        assertFalse(aiSettingsRepository.hasOpenAiApiKey())
    }
    
    @Test
    fun `removeOpenAiApiKey should remove stored key`() = runTest {
        val validApiKey = "sk-1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        
        // Store key first
        aiSettingsRepository.storeOpenAiApiKey(validApiKey)
        assertTrue(aiSettingsRepository.hasOpenAiApiKey())
        
        // Remove key
        aiSettingsRepository.removeOpenAiApiKey()
        
        assertFalse(aiSettingsRepository.hasOpenAiApiKey())
        assertNull(aiSettingsRepository.getOpenAiApiKey())
    }
    
    @Test
    fun `observeOpenAiApiKeyPresence should emit correct states`() = runTest {
        val validApiKey = "sk-1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        val presenceFlow = aiSettingsRepository.observeOpenAiApiKeyPresence()
        
        // Initially no key
        assertFalse(presenceFlow.first())
        
        // Store key
        aiSettingsRepository.storeOpenAiApiKey(validApiKey)
        assertTrue(presenceFlow.first())
        
        // Remove key
        aiSettingsRepository.removeOpenAiApiKey()
        assertFalse(presenceFlow.first())
    }
    
    @Test
    fun `validateApiKey should validate without storing`() = runTest {
        val validApiKey = "sk-1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        val invalidApiKey = "invalid-key"
        
        val validResult = aiSettingsRepository.validateApiKey(validApiKey)
        assertTrue(validResult.isValid)
        assertNull(validResult.errorMessage)
        
        val invalidResult = aiSettingsRepository.validateApiKey(invalidApiKey)
        assertFalse(invalidResult.isValid)
        assertEquals("OpenAI API keys must start with 'sk-'", invalidResult.errorMessage)
        
        // Ensure no keys were stored during validation
        assertFalse(aiSettingsRepository.hasOpenAiApiKey())
    }
    
    @Test
    fun `clearAllAiData should remove all stored data`() = runTest {
        val validApiKey = "sk-1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        
        // Store key first
        aiSettingsRepository.storeOpenAiApiKey(validApiKey)
        assertTrue(aiSettingsRepository.hasOpenAiApiKey())
        
        // Clear all data
        aiSettingsRepository.clearAllAiData()
        
        assertFalse(aiSettingsRepository.hasOpenAiApiKey())
        assertNull(aiSettingsRepository.getOpenAiApiKey())
    }
    
    @Test
    fun `multiple operations should work correctly in sequence`() = runTest {
        val firstKey = "sk-1111111111111111111111111111111111111111111111111111111111111111"
        val secondKey = "sk-2222222222222222222222222222222222222222222222222222222222222222"
        
        // Store first key
        aiSettingsRepository.storeOpenAiApiKey(firstKey)
        assertEquals(firstKey, aiSettingsRepository.getOpenAiApiKey())
        
        // Update to second key
        aiSettingsRepository.storeOpenAiApiKey(secondKey)
        assertEquals(secondKey, aiSettingsRepository.getOpenAiApiKey())
        assertTrue(aiSettingsRepository.hasOpenAiApiKey())
        
        // Remove key
        aiSettingsRepository.removeOpenAiApiKey()
        assertFalse(aiSettingsRepository.hasOpenAiApiKey())
        assertNull(aiSettingsRepository.getOpenAiApiKey())
    }
    
    @Test
    fun `storeOpenAiApiKey should handle empty string`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            aiSettingsRepository.storeOpenAiApiKey("")
        }
    }
    
    @Test
    fun `storeOpenAiApiKey should handle whitespace-only string`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            aiSettingsRepository.storeOpenAiApiKey("   ")
        }
    }
    
    @Test
    fun `validateApiKey should handle null input`() = runTest {
        val result = aiSettingsRepository.validateApiKey(null)
        
        assertFalse(result.isValid)
        assertEquals("API key cannot be empty", result.errorMessage)
    }
    
    @Test
    fun `validateApiKey should handle empty input`() = runTest {
        val result = aiSettingsRepository.validateApiKey("")
        
        assertFalse(result.isValid)
        assertEquals("API key cannot be empty", result.errorMessage)
    }
}