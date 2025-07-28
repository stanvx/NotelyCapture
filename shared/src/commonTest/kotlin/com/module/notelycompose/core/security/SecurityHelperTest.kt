package com.module.notelycompose.core.security

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for SecurityHelper API key validation functionality.
 */
class SecurityHelperTest {
    
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
    
    private val securityHelper = SecurityHelper(mockSecurityMonitoringService)
    
    @Test
    fun `validateOpenAiApiKey should accept valid API key`() = runTest {
        val validApiKey = "sk-1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        
        val result = securityHelper.validateOpenAiApiKey(validApiKey)
        
        assertTrue(result.isValid)
        assertEquals(null, result.errorMessage)
    }
    
    @Test
    fun `validateOpenAiApiKey should reject empty API key`() = runTest {
        val result = securityHelper.validateOpenAiApiKey("")
        
        assertFalse(result.isValid)
        assertEquals("API key cannot be empty", result.errorMessage)
    }
    
    @Test
    fun `validateOpenAiApiKey should reject null API key`() = runTest {
        val result = securityHelper.validateOpenAiApiKey(null)
        
        assertFalse(result.isValid)
        assertEquals("API key cannot be empty", result.errorMessage)
    }
    
    @Test
    fun `validateOpenAiApiKey should reject API key without sk- prefix`() = runTest {
        val invalidApiKey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        
        val result = securityHelper.validateOpenAiApiKey(invalidApiKey)
        
        assertFalse(result.isValid)
        assertEquals("OpenAI API keys must start with 'sk-'", result.errorMessage)
    }
    
    @Test
    fun `validateOpenAiApiKey should reject too short API key`() = runTest {
        val shortApiKey = "sk-123"
        
        val result = securityHelper.validateOpenAiApiKey(shortApiKey)
        
        assertFalse(result.isValid)
        assertEquals("API key too short", result.errorMessage)
    }
    
    @Test
    fun `validateOpenAiApiKey should reject too long API key`() = runTest {
        val longApiKey = "sk-" + "a".repeat(250) // 252 chars total
        
        val result = securityHelper.validateOpenAiApiKey(longApiKey)
        
        assertFalse(result.isValid)
        assertEquals("API key too long", result.errorMessage)
    }
    
    @Test
    fun `validateOpenAiApiKey should reject API key with invalid characters`() = runTest {
        val invalidApiKey = "sk-1234567890abcdef!@#$%^&*()"
        
        val result = securityHelper.validateOpenAiApiKey(invalidApiKey)
        
        assertFalse(result.isValid)
        assertEquals("API key contains invalid characters", result.errorMessage)
    }
    
    @Test
    fun `sanitizeApiKeyForLogging should mask API key properly`() {
        val apiKey = "sk-1234567890abcdef1234567890abcdef"
        val result = securityHelper.sanitizeApiKeyForLogging(apiKey)
        
        assertEquals("sk-***cdef", result)
    }
    
    @Test
    fun `sanitizeApiKeyForLogging should handle empty API key`() {
        val result = securityHelper.sanitizeApiKeyForLogging("")
        assertEquals("empty", result)
    }
    
    @Test
    fun `sanitizeApiKeyForLogging should handle null API key`() {
        val result = securityHelper.sanitizeApiKeyForLogging(null)
        assertEquals("empty", result)
    }
    
    @Test
    fun `sanitizeApiKeyForLogging should handle short API key`() {
        val shortKey = "sk-123"
        val result = securityHelper.sanitizeApiKeyForLogging(shortKey)
        assertEquals("***", result)
    }
    
    @Test
    fun `validateAiSettings should accept valid settings`() = runTest {
        val settings = mapOf(
            "ai_features_enabled" to true,
            "openai_api_key" to "sk-1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        )
        
        val result = securityHelper.validateAiSettings(settings)
        
        assertTrue(result)
    }
    
    @Test
    fun `validateAiSettings should reject invalid boolean for ai_features_enabled`() = runTest {
        val settings = mapOf(
            "ai_features_enabled" to "true" // String instead of Boolean
        )
        
        val result = securityHelper.validateAiSettings(settings)
        
        assertFalse(result)
    }
    
    @Test
    fun `validateAiSettings should reject invalid API key in settings`() = runTest {
        val settings = mapOf(
            "ai_features_enabled" to true,
            "openai_api_key" to "invalid-key"
        )
        
        val result = securityHelper.validateAiSettings(settings)
        
        assertFalse(result)
    }
    
    @Test
    fun `validateAiSettings should handle empty API key in settings`() = runTest {
        val settings = mapOf(
            "ai_features_enabled" to true,
            "openai_api_key" to ""
        )
        
        val result = securityHelper.validateAiSettings(settings)
        
        assertTrue(result) // Empty API key is allowed in settings
    }
    
    @Test
    fun `validateAiSettings should handle unknown settings gracefully`() = runTest {
        val settings = mapOf(
            "ai_features_enabled" to true,
            "unknown_setting" to "some_value"
        )
        
        val result = securityHelper.validateAiSettings(settings)
        
        assertTrue(result) // Unknown settings should not fail validation
    }
    
    @Test
    fun `createUserContext should create valid context`() {
        val sessionId = "test-session-123"
        val userAgent = "TestAgent/1.0"
        val ipAddress = "192.168.1.1"
        val deviceFingerprint = "android-device-123"
        
        val context = securityHelper.createUserContext(
            sessionId = sessionId,
            userAgent = userAgent,
            ipAddress = ipAddress,
            deviceFingerprint = deviceFingerprint
        )
        
        assertEquals(sessionId, context.sessionId)
        assertEquals(userAgent, context.userAgent)
        assertEquals(ipAddress, context.ipAddress)
        assertEquals(deviceFingerprint, context.deviceFingerprint)
    }
}