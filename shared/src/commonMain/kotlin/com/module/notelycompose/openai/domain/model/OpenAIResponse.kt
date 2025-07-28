package com.module.notelycompose.openai.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a generic OpenAI API response
 */
@Serializable
data class OpenAIResponse<T>(
    val data: T? = null,
    val error: OpenAIError? = null,
    val isSuccess: Boolean = error == null
)

/**
 * Domain model representing OpenAI API errors
 */
@Serializable
data class OpenAIError(
    val code: String,
    val message: String,
    val type: String? = null
)

/**
 * Domain model for transcription response
 */
@Serializable
data class TranscriptionResult(
    val text: String,
    val language: String? = null,
    val duration: Double? = null,
    val segments: List<TranscriptionSegment> = emptyList()
)

/**
 * Domain model for transcription segments with timestamps
 */
@Serializable
data class TranscriptionSegment(
    val text: String,
    val start: Double,
    val end: Double,
    val confidence: Double? = null
)

/**
 * Domain model for text summarization response
 */
@Serializable
data class SummarizationResult(
    val summary: String,
    val originalLength: Int,
    val summaryLength: Int,
    val compressionRatio: Double
)

/**
 * Configuration for OpenAI requests
 */
data class OpenAIConfig(
    val apiKey: String,
    val maxRetries: Int = 3,
    val timeoutMs: Long = 30000,
    val model: String = "gpt-3.5-turbo"
)

/**
 * Request parameters for transcription
 */
data class TranscriptionRequest(
    val audioFilePath: String,
    val language: String? = null,
    val prompt: String? = null,
    val temperature: Double = 0.0
)

/**
 * Request parameters for summarization
 */
data class SummarizationRequest(
    val text: String,
    val maxLength: Int? = null,
    val style: SummarizationStyle = SummarizationStyle.CONCISE
)

/**
 * Enumeration of summarization styles
 */
enum class SummarizationStyle {
    CONCISE,
    DETAILED,
    BULLET_POINTS,
    KEY_INSIGHTS
}