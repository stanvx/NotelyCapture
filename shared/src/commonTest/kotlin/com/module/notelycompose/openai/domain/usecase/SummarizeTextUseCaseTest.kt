package com.module.notelycompose.openai.domain.usecase

import com.module.notelycompose.openai.domain.repository.OpenAIRepository
import com.module.notelycompose.openai.domain.model.OpenAIRequest
import com.module.notelycompose.openai.domain.model.OpenAIResponse
import com.module.notelycompose.summary.TFIDFSummarizer
import com.module.notelycompose.core.security.SecurityHelper
import com.module.notelycompose.core.security.SecurityMonitoringService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Comprehensive tests for SummarizeTextUseCase focusing on hybrid AI processing,
 * error handling, fallback mechanisms, and performance considerations.
 */
class SummarizeTextUseCaseTest {

    private lateinit var fakeOpenAIRepository: FakeOpenAIRepository
    private lateinit var fakeTFIDFSummarizer: FakeTFIDFSummarizer
    private lateinit var fakeSecurityHelper: FakeSecurityHelper
    private lateinit var useCase: SummarizeTextUseCase

    private fun setUp() {
        fakeOpenAIRepository = FakeOpenAIRepository()
        fakeTFIDFSummarizer = FakeTFIDFSummarizer()
        fakeSecurityHelper = FakeSecurityHelper()
        
        useCase = SummarizeTextUseCase(
            openAIRepository = fakeOpenAIRepository,
            tfidfSummarizer = fakeTFIDFSummarizer,
            securityHelper = fakeSecurityHelper
        )
    }

    @Test
    fun `execute with valid text uses OpenAI when available`() = runTest {
        setUp()
        val inputText = "This is a long text that needs to be summarized. " +
                "It contains multiple sentences and important information. " +
                "The summary should capture the key points effectively."
        
        fakeOpenAIRepository.shouldSucceed = true
        fakeOpenAIRepository.summaryToReturn = "Key points captured effectively."
        
        val result = useCase.execute(inputText)
        
        assertTrue(result.isSuccess)
        assertEquals("Key points captured effectively.", result.getOrNull())
        assertEquals(1, fakeOpenAIRepository.callCount)
        assertEquals(0, fakeTFIDFSummarizer.callCount) // Fallback not used
    }

    @Test
    fun `execute falls back to TFIDF when OpenAI fails`() = runTest {
        setUp()
        val inputText = "This is a test document for summarization. " +
                "It should work with the TFIDF fallback mechanism. " +
                "The fallback ensures reliability when AI services are unavailable."
        
        fakeOpenAIRepository.shouldSucceed = false
        fakeTFIDFSummarizer.summaryToReturn = "TFIDF summary of key points."
        
        val result = useCase.execute(inputText)
        
        assertTrue(result.isSuccess)
        assertEquals("TFIDF summary of key points.", result.getOrNull())
        assertEquals(1, fakeOpenAIRepository.callCount) // OpenAI attempted first
        assertEquals(1, fakeTFIDFSummarizer.callCount) // Fallback used
    }

    @Test
    fun `execute with empty text returns appropriate error`() = runTest {
        setUp()
        
        val result = useCase.execute("")
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("empty") == true)
        assertEquals(0, fakeOpenAIRepository.callCount)
        assertEquals(0, fakeTFIDFSummarizer.callCount)
    }

    @Test
    fun `execute with whitespace-only text returns error`() = runTest {
        setUp()
        
        val result = useCase.execute("   \n\t   ")
        
        assertTrue(result.isFailure)
        assertEquals(0, fakeOpenAIRepository.callCount)
        assertEquals(0, fakeTFIDFSummarizer.callCount)
    }

    @Test
    fun `execute with very short text returns original text`() = runTest {
        setUp()
        val shortText = "Short text."
        
        val result = useCase.execute(shortText)
        
        assertTrue(result.isSuccess)
        assertEquals(shortText, result.getOrNull())
        assertEquals(0, fakeOpenAIRepository.callCount) // Too short for AI processing
        assertEquals(0, fakeTFIDFSummarizer.callCount)
    }

    @Test
    fun `execute with text requiring chunking processes multiple chunks`() = runTest {
        setUp()
        // Create text that would exceed typical AI model limits
        val longText = "This is a very long document. ".repeat(1000) +
                "It contains important information that needs summarization. " +
                "The system should handle large texts by chunking them appropriately."
        
        fakeOpenAIRepository.shouldSucceed = true
        fakeOpenAIRepository.summaryToReturn = "Summary of chunked content."
        
        val result = useCase.execute(longText)
        
        assertTrue(result.isSuccess)
        assertTrue(fakeOpenAIRepository.callCount >= 1) // May be called multiple times for chunks
    }

    @Test
    fun `execute handles security validation failure`() = runTest {
        setUp()
        fakeSecurityHelper.shouldValidateText = false
        
        val result = useCase.execute("Potentially unsafe content")
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("security") == true ||
                  result.exceptionOrNull()?.message?.contains("validation") == true)
        assertEquals(0, fakeOpenAIRepository.callCount)
        assertEquals(0, fakeTFIDFSummarizer.callCount)
    }

    @Test
    fun `execute with both OpenAI and TFIDF failing returns error`() = runTest {
        setUp()
        fakeOpenAIRepository.shouldSucceed = false
        fakeTFIDFSummarizer.shouldSucceed = false
        
        val result = useCase.execute("Text that causes both summarizers to fail")
        
        assertTrue(result.isFailure)
        assertEquals(1, fakeOpenAIRepository.callCount) // OpenAI attempted
        assertEquals(1, fakeTFIDFSummarizer.callCount) // TFIDF fallback attempted
    }

    @Test
    fun `execute handles text with special characters`() = runTest {
        setUp()
        val specialText = "Text with émojis 😀, ñ, and other spëcial characters: <>&\"'"
        
        fakeOpenAIRepository.shouldSucceed = true
        fakeOpenAIRepository.summaryToReturn = "Processed special characters correctly."
        
        val result = useCase.execute(specialText)
        
        assertTrue(result.isSuccess)
        assertEquals("Processed special characters correctly.", result.getOrNull())
    }

    @Test
    fun `execute handles malformed or invalid text gracefully`() = runTest {
        setUp()
        val malformedText = "Text with\u0000null\u0001control\u0002characters"
        
        fakeOpenAIRepository.shouldSucceed = false
        fakeTFIDFSummarizer.summaryToReturn = "Cleaned and summarized text."
        
        val result = useCase.execute(malformedText)
        
        assertTrue(result.isSuccess)
        assertEquals("Cleaned and summarized text.", result.getOrNull())
    }

    @Test
    fun `execute with mixed language content`() = runTest {
        setUp()
        val multilingualText = "English text mixed with español and 日本語 content. " +
                "This should be handled appropriately by the summarization system."
        
        fakeOpenAIRepository.shouldSucceed = true
        fakeOpenAIRepository.summaryToReturn = "Multilingual summary."
        
        val result = useCase.execute(multilingualText)
        
        assertTrue(result.isSuccess)
        assertEquals("Multilingual summary.", result.getOrNull())
    }

    @Test
    fun `execute performance with multiple text split operations`() = runTest {
        setUp()
        // Test text that would trigger multiple split operations
        val complexText = buildString {
            repeat(100) {
                append("Sentence $it with multiple words. ")
                if (it % 10 == 0) append("\n\n") // Add paragraph breaks
            }
        }
        
        fakeOpenAIRepository.shouldSucceed = true
        fakeOpenAIRepository.summaryToReturn = "Efficiently processed complex text."
        
        val result = useCase.execute(complexText)
        
        assertTrue(result.isSuccess)
        assertEquals("Efficiently processed complex text.", result.getOrNull())
    }

    @Test
    fun `execute handles network timeout gracefully`() = runTest {
        setUp()
        fakeOpenAIRepository.shouldTimeout = true
        fakeTFIDFSummarizer.summaryToReturn = "Offline summary after timeout."
        
        val result = useCase.execute("Text that causes network timeout")
        
        assertTrue(result.isSuccess)
        assertEquals("Offline summary after timeout.", result.getOrNull())
        assertEquals(1, fakeOpenAIRepository.callCount)
        assertEquals(1, fakeTFIDFSummarizer.callCount) // Fallback used
    }

    @Test
    fun `execute handles API rate limiting`() = runTest {
        setUp()
        fakeOpenAIRepository.shouldRateLimit = true
        fakeTFIDFSummarizer.summaryToReturn = "Local processing due to rate limit."
        
        val result = useCase.execute("Text during rate limiting")
        
        assertTrue(result.isSuccess)
        assertEquals("Local processing due to rate limit.", result.getOrNull())
        assertEquals(1, fakeTFIDFSummarizer.callCount) // Fallback used
    }

    @Test
    fun `execute validates input before processing`() = runTest {
        setUp()
        val suspiciousText = "Text with potential injection attempts or malicious content"
        fakeSecurityHelper.shouldValidateText = true
        fakeOpenAIRepository.summaryToReturn = "Safe summary."
        
        val result = useCase.execute(suspiciousText)
        
        assertTrue(result.isSuccess)
        assertEquals("Safe summary.", result.getOrNull())
        assertEquals(1, fakeSecurityHelper.validateTextCallCount)
    }
}

// --- Fake Implementations ---

private class FakeOpenAIRepository : OpenAIRepository {
    var callCount = 0
    var shouldSucceed = true
    var shouldTimeout = false
    var shouldRateLimit = false
    var summaryToReturn = "Default AI summary"
    
    override suspend fun summarizeText(request: OpenAIRequest): Result<OpenAIResponse> {
        callCount++
        
        return when {
            shouldTimeout -> Result.failure(Exception("Network timeout"))
            shouldRateLimit -> Result.failure(Exception("Rate limit exceeded"))
            shouldSucceed -> Result.success(OpenAIResponse(
                id = "test-id",
                choices = listOf(
                    OpenAIResponse.Choice(
                        message = OpenAIResponse.Message(
                            role = "assistant",
                            content = summaryToReturn
                        ),
                        finishReason = "stop"
                    )
                ),
                usage = OpenAIResponse.Usage(
                    promptTokens = 100,
                    completionTokens = 50,
                    totalTokens = 150
                )
            ))
            else -> Result.failure(Exception("OpenAI processing failed"))
        }
    }
    
    override suspend fun transcribeAudio(audioPath: String): Result<String> {
        return Result.failure(Exception("Not implemented in test"))
    }
    
    override suspend fun isAvailable(): Boolean = shouldSucceed
}

private class FakeTFIDFSummarizer : TFIDFSummarizer {
    var callCount = 0
    var shouldSucceed = true
    var summaryToReturn = "Default TFIDF summary"
    
    override suspend fun summarize(text: String, maxSentences: Int): Result<String> {
        callCount++
        
        return if (shouldSucceed) {
            Result.success(summaryToReturn)
        } else {
            Result.failure(Exception("TFIDF summarization failed"))
        }
    }
    
    override suspend fun extractKeywords(text: String, maxKeywords: Int): Result<List<String>> {
        return Result.success(listOf("keyword1", "keyword2"))
    }
}

private class FakeSecurityHelper : SecurityHelper(FakeSecurityMonitoringService()) {
    var shouldValidateText = true
    var validateTextCallCount = 0
    
    override suspend fun validateNoteContent(content: String?): Boolean {
        validateTextCallCount++
        return shouldValidateText
    }
    
    override suspend fun sanitizeTextForProcessing(text: String): String {
        return text.replace(Regex("[\\u0000-\\u001F]"), "") // Remove control characters
    }
}

private class FakeSecurityMonitoringService : SecurityMonitoringService {
    override suspend fun reportSecurityEvent(
        type: SecurityMonitoringService.SecurityEventType,
        severity: SecurityMonitoringService.SecuritySeverity,
        message: String,
        details: Map<String, String>,
        userContext: SecurityMonitoringService.UserContext?,
        remediation: String?,
        throwable: Throwable?
    ) {}
    
    override suspend fun reportValidationFailure(
        validationType: String,
        input: String,
        validationError: String,
        userContext: SecurityMonitoringService.UserContext?
    ) {}
    
    override suspend fun reportFileSystemViolation(
        operation: String,
        filePath: String,
        violation: String,
        userContext: SecurityMonitoringService.UserContext?
    ) {}
    
    override suspend fun reportSuspiciousActivity(
        activityType: String,
        description: String,
        confidence: Double,
        userContext: SecurityMonitoringService.UserContext?
    ) {}
    
    override fun getSecurityEvents(): kotlinx.coroutines.flow.Flow<List<SecurityMonitoringService.SecurityEvent>> =
        kotlinx.coroutines.flow.flowOf(emptyList())
    
    override suspend fun getSecurityMetrics(): SecurityMonitoringService.SecurityMetrics =
        SecurityMonitoringService.SecurityMetrics(0, emptyMap(), emptyMap(), 0, 0.0, emptyList(), "")
    
    override suspend fun cleanupOldEvents() {}
    
    override suspend fun updateConfiguration(config: SecurityMonitoringService.SecurityConfig) {}
    
    override suspend fun getConfiguration(): SecurityMonitoringService.SecurityConfig =
        SecurityMonitoringService.SecurityConfig()
}