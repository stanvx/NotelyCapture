package com.module.notelycompose.openai.data.repository

import com.aallam.openai.api.audio.TranscriptionRequest as OpenAITranscriptionRequest
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import io.ktor.client.plugins.HttpTimeout
import com.module.notelycompose.core.security.SecurityHelper
import com.module.notelycompose.openai.domain.NetworkConnectivityManager
import com.module.notelycompose.openai.domain.model.OpenAIError
import com.module.notelycompose.openai.domain.model.OpenAIResponse
import com.module.notelycompose.openai.domain.model.SummarizationRequest
import com.module.notelycompose.openai.domain.model.SummarizationResult
import com.module.notelycompose.openai.domain.model.SummarizationStyle
import com.module.notelycompose.openai.domain.model.TranscriptionRequest
import com.module.notelycompose.openai.domain.model.TranscriptionResult
import com.module.notelycompose.openai.domain.repository.OpenAIRepository
import io.github.aakira.napier.Napier
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okio.Source
import okio.buffer
import okio.source
import okio.Buffer
import okio.ByteString.Companion.toByteString
import kotlinx.io.files.Path
import kotlinx.io.files.FileSystem
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import java.io.File
import kotlin.math.ceil
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Implementation of OpenAIRepository using openai-kotlin library with Ktor client.
 * Provides hybrid online/offline functionality with fallback mechanisms.
 */
class OpenAIRepositoryImpl(
    private val networkConnectivityManager: NetworkConnectivityManager,
    private val securityHelper: SecurityHelper,
    private val openAIApiKey: String? = null
) : OpenAIRepository {

    private var openAIClient: OpenAI? = null
    private var currentConfig = Config()
    
    /**
     * Internal configuration data class
     */
    private data class Config(
        val apiKey: String? = null,
        val timeoutMs: Long = 30000,
        val maxRetries: Int = 3,
        val model: String = "gpt-3.5-turbo"
    )

    private fun initializeClient(apiKey: String) {
        try {
            val config = OpenAIConfig(
                token = apiKey
            )
            openAIClient = OpenAI(config)
            Napier.d("OpenAI client initialized successfully")
        } catch (e: Exception) {
            Napier.e("Failed to initialize OpenAI client", e)
            throw e
        }
    }

    override suspend fun isConfigured(): Boolean {
        return try {
            val apiKey = openAIApiKey ?: currentConfig.apiKey
            apiKey?.isNotBlank() == true && openAIClient != null
        } catch (e: Exception) {
            Napier.e("Error checking OpenAI configuration", e)
            false
        }
    }

    override suspend fun testConnection(): Boolean {
        return withContext(Dispatchers.Default) {
            try {
                if (!isNetworkAvailable()) {
                    Napier.w("No network available for OpenAI connection test")
                    return@withContext false
                }

                val client = openAIClient ?: run {
                    val apiKey = openAIApiKey ?: currentConfig.apiKey
                    if (apiKey.isNullOrBlank()) {
                        Napier.e("No API key available for connection test")
                        return@withContext false
                    }
                    initializeClient(apiKey)
                    openAIClient!!
                }

                // Test with a minimal request to verify API key and connectivity
                val testRequest = ChatCompletionRequest(
                    model = ModelId("gpt-3.5-turbo"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.User,
                            content = "test"
                        )
                    ),
                    maxTokens = 1
                )
                
                client.chatCompletion(testRequest)
                Napier.d("OpenAI connection test successful")
                true
            } catch (e: Exception) {
                Napier.e("OpenAI connection test failed", e)
                false
            }
        }
    }

    override suspend fun transcribeAudio(request: TranscriptionRequest): OpenAIResponse<TranscriptionResult> {
        return withContext(Dispatchers.Default) {
            try {
                // Security validation
                if (!securityHelper.isPathSafe(request.audioFilePath)) {
                    return@withContext OpenAIResponse(
                        error = OpenAIError(
                            code = "SECURITY_ERROR",
                            message = "Invalid audio file path detected"
                        )
                    )
                }

                // Network availability check
                if (!isNetworkAvailable()) {
                    return@withContext OpenAIResponse(
                        error = OpenAIError(
                            code = "NETWORK_ERROR",
                            message = "No network connection available for transcription"
                        )
                    )
                }

                // Validate audio file
                if (!validateAudioFile(request.audioFilePath)) {
                    return@withContext OpenAIResponse(
                        error = OpenAIError(
                            code = "INVALID_AUDIO",
                            message = "Audio file format not supported or file not accessible"
                        )
                    )
                }

                val client = getOrInitializeClient() ?: return@withContext OpenAIResponse(
                    error = OpenAIError(
                        code = "CLIENT_ERROR",
                        message = "OpenAI client not properly configured"
                    )
                )

                // Create file source for the audio file
                val audioFile = File(request.audioFilePath)
                val fileSource = FileSource(
                    path = Path(request.audioFilePath)
                )

                val openAIRequest = OpenAITranscriptionRequest(
                    audio = fileSource,
                    model = ModelId("whisper-1"),
                    language = request.language,
                    prompt = request.prompt,
                    temperature = request.temperature
                )

                val response = client.transcription(openAIRequest)
                
                val result = TranscriptionResult(
                    text = response.text,
                    language = request.language,
                    duration = null // OpenAI doesn't provide duration in response
                )

                Napier.d("Audio transcription completed successfully")
                OpenAIResponse(data = result)

            } catch (e: ClientRequestException) {
                Napier.e("OpenAI client request error during transcription", e)
                OpenAIResponse(
                    error = OpenAIError(
                        code = "CLIENT_ERROR",
                        message = "Invalid request: ${e.message}",
                        type = "client_error"
                    )
                )
            } catch (e: ServerResponseException) {
                Napier.e("OpenAI server error during transcription", e)
                OpenAIResponse(
                    error = OpenAIError(
                        code = "SERVER_ERROR",
                        message = "Server error: ${e.message}",
                        type = "server_error"
                    )
                )
            } catch (e: HttpRequestTimeoutException) {
                Napier.e("OpenAI request timeout during transcription", e)
                OpenAIResponse(
                    error = OpenAIError(
                        code = "TIMEOUT_ERROR",
                        message = "Request timed out after ${currentConfig.timeoutMs}ms"
                    )
                )
            } catch (e: Exception) {
                Napier.e("Unexpected error during audio transcription", e)
                OpenAIResponse(
                    error = OpenAIError(
                        code = "UNKNOWN_ERROR",
                        message = "Unexpected error: ${e.message}"
                    )
                )
            }
        }
    }

    override suspend fun summarizeText(request: SummarizationRequest): OpenAIResponse<SummarizationResult> {
        return withContext(Dispatchers.Default) {
            try {
                // Input validation
                if (request.text.isBlank()) {
                    return@withContext OpenAIResponse(
                        error = OpenAIError(
                            code = "INVALID_INPUT",
                            message = "Text cannot be empty for summarization"
                        )
                    )
                }

                // Network availability check
                if (!isNetworkAvailable()) {
                    return@withContext OpenAIResponse(
                        error = OpenAIError(
                            code = "NETWORK_ERROR",
                            message = "No network connection available for summarization"
                        )
                    )
                }

                val client = getOrInitializeClient() ?: return@withContext OpenAIResponse(
                    error = OpenAIError(
                        code = "CLIENT_ERROR",
                        message = "OpenAI client not properly configured"
                    )
                )

                val prompt = buildSummarizationPrompt(request)
                val originalLength = request.text.length

                val chatRequest = ChatCompletionRequest(
                    model = ModelId(currentConfig.model),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = prompt
                        ),
                        ChatMessage(
                            role = ChatRole.User,
                            content = request.text
                        )
                    ),
                    maxTokens = request.maxLength ?: (originalLength / 4), // Default to 1/4 of original
                    temperature = 0.3 // Lower temperature for more consistent summaries
                )

                val response = client.chatCompletion(chatRequest)
                val summaryText = response.choices.firstOrNull()?.message?.content ?: ""
                
                if (summaryText.isBlank()) {
                    return@withContext OpenAIResponse(
                        error = OpenAIError(
                            code = "EMPTY_RESPONSE",
                            message = "OpenAI returned empty summary"
                        )
                    )
                }

                val summaryLength = summaryText.length
                val compressionRatio = if (originalLength > 0) {
                    originalLength.toDouble() / summaryLength.toDouble()
                } else {
                    1.0
                }

                val result = SummarizationResult(
                    summary = summaryText,
                    originalLength = originalLength,
                    summaryLength = summaryLength,
                    compressionRatio = compressionRatio
                )

                Napier.d("Text summarization completed successfully")
                OpenAIResponse(data = result)

            } catch (e: ClientRequestException) {
                Napier.e("OpenAI client request error during summarization", e)
                OpenAIResponse(
                    error = OpenAIError(
                        code = "CLIENT_ERROR",
                        message = "Invalid request: ${e.message}",
                        type = "client_error"
                    )
                )
            } catch (e: ServerResponseException) {
                Napier.e("OpenAI server error during summarization", e)
                OpenAIResponse(
                    error = OpenAIError(
                        code = "SERVER_ERROR",
                        message = "Server error: ${e.message}",
                        type = "server_error"
                    )
                )
            } catch (e: HttpRequestTimeoutException) {
                Napier.e("OpenAI request timeout during summarization", e)
                OpenAIResponse(
                    error = OpenAIError(
                        code = "TIMEOUT_ERROR",
                        message = "Request timed out after ${currentConfig.timeoutMs}ms"
                    )
                )
            } catch (e: Exception) {
                Napier.e("Unexpected error during text summarization", e)
                OpenAIResponse(
                    error = OpenAIError(
                        code = "UNKNOWN_ERROR",
                        message = "Unexpected error: ${e.message}"
                    )
                )
            }
        }
    }

    override suspend fun isNetworkAvailable(): Boolean {
        return networkConnectivityManager.isNetworkAvailable()
    }

    override suspend fun getUsageStats(): Map<String, Any> {
        // This would require additional API calls to track usage
        // For now, return empty map as OpenAI doesn't provide usage stats in responses
        return emptyMap()
    }

    override suspend fun updateConfiguration(apiKey: String, timeoutMs: Long, maxRetries: Int) {
        currentConfig = currentConfig.copy(
            apiKey = apiKey,
            timeoutMs = timeoutMs,
            maxRetries = maxRetries
        )
        
        // Reinitialize client with new configuration
        if (apiKey.isNotBlank()) {
            initializeClient(apiKey)
        }
    }

    override suspend fun validateAudioFile(audioFilePath: String): Boolean {
        return try {
            val file = File(audioFilePath)
            when {
                !file.exists() -> {
                    Napier.w("Audio file does not exist: $audioFilePath")
                    false
                }
                !file.canRead() -> {
                    Napier.w("Audio file is not readable: $audioFilePath")
                    false
                }
                file.length() == 0L -> {
                    Napier.w("Audio file is empty: $audioFilePath")
                    false
                }
                file.length() > 25 * 1024 * 1024 -> { // 25MB OpenAI limit
                    Napier.w("Audio file too large (>25MB): $audioFilePath")
                    false
                }
                else -> {
                    val extension = file.extension.lowercase()
                    val supportedFormats = listOf("mp3", "mp4", "mpeg", "mpga", "m4a", "wav", "webm")
                    if (extension !in supportedFormats) {
                        Napier.w("Unsupported audio format: $extension")
                        false
                    } else {
                        true
                    }
                }
            }
        } catch (e: Exception) {
            Napier.e("Error validating audio file", e)
            false
        }
    }

    override suspend fun estimateTranscriptionCost(audioFilePath: String): Int? {
        return try {
            val file = File(audioFilePath)
            if (!file.exists()) return null
            
            // OpenAI Whisper pricing: $0.006 per minute
            // Estimate duration based on file size (rough approximation)
            val fileSizeKB = file.length() / 1024
            val estimatedMinutes = fileSizeKB / 1000 // Very rough estimation
            val costCents = ceil(estimatedMinutes * 0.6).toInt() // $0.006 = 0.6 cents
            
            maxOf(1, costCents) // Minimum 1 cent
        } catch (e: Exception) {
            Napier.e("Error estimating transcription cost", e)
            null
        }
    }

    override suspend fun estimateSummarizationCost(text: String): Int? {
        return try {
            // GPT-3.5-turbo pricing: ~$0.002 per 1K tokens
            // Rough estimation: 1 token ≈ 4 characters
            val estimatedTokens = text.length / 4
            val costPer1K = 0.2 // 0.2 cents per 1K tokens
            val costCents = ceil((estimatedTokens / 1000.0) * costPer1K).toInt()
            
            maxOf(1, costCents) // Minimum 1 cent
        } catch (e: Exception) {
            Napier.e("Error estimating summarization cost", e)
            null
        }
    }

    private fun getOrInitializeClient(): OpenAI? {
        return openAIClient ?: run {
            val apiKey = openAIApiKey ?: currentConfig.apiKey
            if (apiKey.isNullOrBlank()) {
                Napier.e("No API key available for OpenAI client initialization")
                return null
            }
            try {
                initializeClient(apiKey)
                openAIClient
            } catch (e: Exception) {
                Napier.e("Failed to initialize OpenAI client", e)
                null
            }
        }
    }

    private fun buildSummarizationPrompt(request: SummarizationRequest): String {
        val basePrompt = when (request.style) {
            SummarizationStyle.CONCISE -> "Provide a concise summary of the following text. Focus on the main points and key information."
            SummarizationStyle.DETAILED -> "Provide a detailed summary of the following text. Include important details while maintaining clarity."
            SummarizationStyle.BULLET_POINTS -> "Summarize the following text as a list of bullet points highlighting the key information."
            SummarizationStyle.KEY_INSIGHTS -> "Extract and summarize the key insights and main takeaways from the following text."
        }

        val lengthGuidance = request.maxLength?.let { 
            " Keep the summary under $it characters." 
        } ?: ""

        return "$basePrompt$lengthGuidance"
    }
}