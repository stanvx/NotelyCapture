package com.module.notelycompose.openai.domain.repository

import com.module.notelycompose.openai.domain.model.OpenAIResponse
import com.module.notelycompose.openai.domain.model.SummarizationRequest
import com.module.notelycompose.openai.domain.model.SummarizationResult
import com.module.notelycompose.openai.domain.model.TranscriptionRequest
import com.module.notelycompose.openai.domain.model.TranscriptionResult

/**
 * Repository interface for OpenAI API operations.
 * Provides abstractions for transcription and text processing capabilities.
 */
interface OpenAIRepository {
    
    /**
     * Checks if the repository is properly configured with valid API credentials.
     * @return true if configured and ready to use, false otherwise
     */
    suspend fun isConfigured(): Boolean
    
    /**
     * Tests the connection to OpenAI API to verify credentials and connectivity.
     * @return true if connection is successful, false otherwise
     */
    suspend fun testConnection(): Boolean
    
    /**
     * Transcribes audio file using OpenAI Whisper API.
     * 
     * @param request The transcription request parameters
     * @return OpenAI response containing transcription result or error
     */
    suspend fun transcribeAudio(request: TranscriptionRequest): OpenAIResponse<TranscriptionResult>
    
    /**
     * Summarizes text using OpenAI GPT models.
     * 
     * @param request The summarization request parameters
     * @return OpenAI response containing summarization result or error
     */
    suspend fun summarizeText(request: SummarizationRequest): OpenAIResponse<SummarizationResult>
    
    /**
     * Checks if the device currently has network connectivity.
     * @return true if network is available, false otherwise
     */
    suspend fun isNetworkAvailable(): Boolean
    
    /**
     * Gets the current API usage statistics if available.
     * @return Map containing usage metrics or empty map if unavailable
     */
    suspend fun getUsageStats(): Map<String, Any>
    
    /**
     * Updates the API configuration (e.g., API key, timeout settings).
     * 
     * @param apiKey The new API key
     * @param timeoutMs Request timeout in milliseconds
     * @param maxRetries Maximum number of retry attempts
     */
    suspend fun updateConfiguration(
        apiKey: String,
        timeoutMs: Long = 30000,
        maxRetries: Int = 3
    )
    
    /**
     * Validates the format and content of an audio file for transcription.
     * 
     * @param audioFilePath Path to the audio file
     * @return true if file is valid for transcription, false otherwise
     */
    suspend fun validateAudioFile(audioFilePath: String): Boolean
    
    /**
     * Estimates the cost of a transcription request based on audio duration.
     * 
     * @param audioFilePath Path to the audio file
     * @return Estimated cost in USD cents, or null if unable to calculate
     */
    suspend fun estimateTranscriptionCost(audioFilePath: String): Int?
    
    /**
     * Estimates the cost of a text summarization request based on text length.
     * 
     * @param text The text to be summarized
     * @return Estimated cost in USD cents, or null if unable to calculate
     */
    suspend fun estimateSummarizationCost(text: String): Int?
}