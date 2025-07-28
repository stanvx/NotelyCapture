package com.module.notelycompose.openai.domain.usecase

import com.module.notelycompose.core.security.SecurityHelper
import com.module.notelycompose.openai.domain.model.SummarizationRequest
import com.module.notelycompose.openai.domain.model.SummarizationResult
import com.module.notelycompose.openai.domain.model.SummarizationStyle
import com.module.notelycompose.openai.domain.repository.OpenAIRepository
import com.module.notelycompose.summary.TFIDFSummarizer
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for text summarization using hybrid online/offline approach.
 * Prioritizes OpenAI API when available, falls back to local TF-IDF summarization.
 */
class SummarizeTextUseCase(
    private val openAIRepository: OpenAIRepository,
    private val localSummarizer: TFIDFSummarizer,
    private val securityHelper: SecurityHelper
) {

    companion object {
        private const val MIN_TEXT_LENGTH = 100 // Minimum text length to justify summarization
        private const val MAX_TEXT_LENGTH = 50000 // Maximum text length for safety
        private const val DEFAULT_SUMMARY_RATIO = 0.3 // Default compression ratio for local summarization
    }

    /**
     * Summarizes text using the best available method.
     * 
     * @param text The text to summarize
     * @param style The summarization style (defaults to CONCISE)
     * @param maxLength Maximum length of the summary in characters (optional)
     * @param preferOnline Whether to prefer online summarization when available
     * @param fallbackToLocal Whether to fallback to local summarization on failure
     * @return SummarizationResult containing the summary or error information
     */
    suspend fun execute(
        text: String,
        style: SummarizationStyle = SummarizationStyle.CONCISE,
        maxLength: Int? = null,
        preferOnline: Boolean = true,
        fallbackToLocal: Boolean = true
    ): SummarizationResult {
        return withContext(Dispatchers.Default) {
            try {
                // Input validation
                val validationResult = validateInput(text)
                if (!validationResult.isValid) {
                    Napier.w("Text validation failed: ${validationResult.errorMessage}")
                    return@withContext SummarizationResult(
                        summary = validationResult.fallbackSummary ?: "",
                        originalLength = text.length,
                        summaryLength = 0,
                        compressionRatio = 1.0
                    )
                }

                // Security validation
                if (!securityHelper.validateNoteContent(text)) {
                    Napier.e("Security validation failed for text content")
                    return@withContext SummarizationResult(
                        summary = "Content failed security validation",
                        originalLength = text.length,
                        summaryLength = 0,
                        compressionRatio = 1.0
                    )
                }

                // Attempt online summarization first if preferred and available
                if (preferOnline && openAIRepository.isNetworkAvailable() && openAIRepository.isConfigured()) {
                    val onlineResult = attemptOnlineSummarization(text, style, maxLength)
                    if (onlineResult != null) {
                        Napier.d("Online summarization completed successfully")
                        return@withContext onlineResult
                    }
                    
                    Napier.w("Online summarization failed, attempting fallback")
                }

                // Fallback to local summarization
                if (fallbackToLocal) {
                    val localResult = attemptLocalSummarization(text, maxLength)
                    if (localResult != null) {
                        Napier.d("Local summarization completed successfully")
                        return@withContext localResult
                    }
                }

                // Both methods failed - return truncated version as last resort
                Napier.w("All summarization methods failed, returning truncated text")
                val truncatedText = if (maxLength != null && text.length > maxLength) {
                    securityHelper.truncateText(text, maxLength)
                } else {
                    text
                }

                SummarizationResult(
                    summary = truncatedText,
                    originalLength = text.length,
                    summaryLength = truncatedText.length,
                    compressionRatio = text.length.toDouble() / truncatedText.length.toDouble()
                )

            } catch (e: Exception) {
                Napier.e("Unexpected error during summarization", e)
                SummarizationResult(
                    summary = "Error occurred during summarization",
                    originalLength = text.length,
                    summaryLength = 0,
                    compressionRatio = 1.0
                )
            }
        }
    }

    /**
     * Estimates the cost of summarization for the given text.
     * 
     * @param text The text to summarize
     * @param useOnlineService Whether to estimate cost for online service
     * @return Estimated cost in cents, or null if unable to calculate
     */
    suspend fun estimateCost(text: String, useOnlineService: Boolean = true): Int? {
        return try {
            if (useOnlineService && openAIRepository.isConfigured()) {
                openAIRepository.estimateSummarizationCost(text)
            } else {
                // Local summarization is free
                0
            }
        } catch (e: Exception) {
            Napier.e("Error estimating summarization cost", e)
            null
        }
    }

    /**
     * Checks if summarization is available with current configuration.
     * 
     * @return SummarizationAvailability indicating what methods are available
     */
    suspend fun checkAvailability(): SummarizationAvailability {
        return try {
            val onlineAvailable = openAIRepository.isNetworkAvailable() && 
                                 openAIRepository.isConfigured()
            val localAvailable = true // TF-IDF is always available
            
            SummarizationAvailability(
                onlineAvailable = onlineAvailable,
                localAvailable = localAvailable,
                networkType = if (onlineAvailable) openAIRepository.getUsageStats()["network_type"] as? String else null
            )
        } catch (e: Exception) {
            Napier.e("Error checking summarization availability", e)
            SummarizationAvailability(
                onlineAvailable = false,
                localAvailable = true
            )
        }
    }

    /**
     * Analyzes text to determine if summarization would be beneficial.
     * 
     * @param text The text to analyze
     * @return TextAnalysis containing metrics about the text
     */
    suspend fun analyzeText(text: String): TextAnalysis {
        return withContext(Dispatchers.Default) {
            try {
                val wordCount = text.split("\\s+".toRegex()).size
                val sentenceCount = text.split("[.!?]+".toRegex()).size
                val paragraphCount = text.split("\n\n").size
                val avgWordsPerSentence = if (sentenceCount > 0) wordCount.toDouble() / sentenceCount else 0.0
                
                val complexity = when {
                    avgWordsPerSentence > 25 -> TextComplexity.HIGH
                    avgWordsPerSentence > 15 -> TextComplexity.MEDIUM
                    else -> TextComplexity.LOW
                }

                val recommendSummarization = text.length > MIN_TEXT_LENGTH
                val estimatedReduction = if (recommendSummarization) {
                    (text.length * (1.0 - DEFAULT_SUMMARY_RATIO)).toInt()
                } else {
                    0
                }

                TextAnalysis(
                    length = text.length,
                    wordCount = wordCount,
                    sentenceCount = sentenceCount,
                    paragraphCount = paragraphCount,
                    complexity = complexity,
                    recommendSummarization = recommendSummarization,
                    estimatedReduction = estimatedReduction
                )
            } catch (e: Exception) {
                Napier.e("Error analyzing text", e)
                TextAnalysis(
                    length = text.length,
                    wordCount = 0,
                    sentenceCount = 0,
                    paragraphCount = 0,
                    complexity = TextComplexity.UNKNOWN,
                    recommendSummarization = false,
                    estimatedReduction = 0
                )
            }
        }
    }

    private suspend fun attemptOnlineSummarization(
        text: String,
        style: SummarizationStyle,
        maxLength: Int?
    ): SummarizationResult? {
        return try {
            val request = SummarizationRequest(
                text = text,
                style = style,
                maxLength = maxLength
            )
            
            val response = openAIRepository.summarizeText(request)
            
            if (response.isSuccess && response.data != null) {
                response.data
            } else {
                Napier.w("Online summarization failed: ${response.error?.message}")
                null
            }
        } catch (e: Exception) {
            Napier.e("Error during online summarization", e)
            null
        }
    }

    private suspend fun attemptLocalSummarization(
        text: String,
        maxLength: Int?
    ): SummarizationResult? {
        return try {
            val summaryRatio = if (maxLength != null) {
                (maxLength.toDouble() / text.length).coerceIn(0.1, 0.8)
            } else {
                DEFAULT_SUMMARY_RATIO
            }

            val summary = localSummarizer.summarize(text, summaryRatio)
            
            if (summary.isNotBlank()) {
                SummarizationResult(
                    summary = summary,
                    originalLength = text.length,
                    summaryLength = summary.length,
                    compressionRatio = text.length.toDouble() / summary.length.toDouble()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Napier.e("Error during local summarization", e)
            null
        }
    }

    private suspend fun validateInput(text: String): ValidationResult {
        return try {
            when {
                text.isBlank() -> ValidationResult(
                    isValid = false,
                    errorMessage = "Text is empty or blank"
                )
                text.length < MIN_TEXT_LENGTH -> ValidationResult(
                    isValid = false,
                    errorMessage = "Text too short for meaningful summarization",
                    fallbackSummary = text
                )
                text.length > MAX_TEXT_LENGTH -> ValidationResult(
                    isValid = false,
                    errorMessage = "Text too long for processing",
                    fallbackSummary = securityHelper.truncateText(text, MAX_TEXT_LENGTH)
                )
                else -> ValidationResult(isValid = true)
            }
        } catch (e: Exception) {
            Napier.e("Error validating input text", e)
            ValidationResult(
                isValid = false,
                errorMessage = "Error validating input: ${e.message}"
            )
        }
    }

    /**
     * Data class representing validation result
     */
    private data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val fallbackSummary: String? = null
    )

    /**
     * Data class representing summarization availability status
     */
    data class SummarizationAvailability(
        val onlineAvailable: Boolean,
        val localAvailable: Boolean,
        val networkType: String? = null
    ) {
        val anyAvailable: Boolean = onlineAvailable || localAvailable
    }

    /**
     * Data class representing text analysis results
     */
    data class TextAnalysis(
        val length: Int,
        val wordCount: Int,
        val sentenceCount: Int,
        val paragraphCount: Int,
        val complexity: TextComplexity,
        val recommendSummarization: Boolean,
        val estimatedReduction: Int
    )

    /**
     * Enumeration of text complexity levels
     */
    enum class TextComplexity {
        LOW,
        MEDIUM,
        HIGH,
        UNKNOWN
    }
}