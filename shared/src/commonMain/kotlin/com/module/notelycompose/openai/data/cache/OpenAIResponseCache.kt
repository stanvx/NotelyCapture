package com.module.notelycompose.openai.data.cache

import com.module.notelycompose.notes.ui.cache.NotePreviewLRUCache
import com.module.notelycompose.openai.domain.model.OpenAIResponse
import com.module.notelycompose.openai.domain.model.SummarizationRequest
import com.module.notelycompose.openai.domain.model.SummarizationResult
import com.module.notelycompose.openai.domain.model.TranscriptionRequest
import com.module.notelycompose.openai.domain.model.TranscriptionResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * Cache for OpenAI API responses to enable offline access and reduce API costs.
 * 
 * Features:
 * - Separate caches for transcription and summarization
 * - TTL-based expiration
 * - Memory-efficient storage using LRU eviction
 * - Thread-safe operations
 */
class OpenAIResponseCache {
    companion object {
        private const val DEFAULT_TRANSCRIPTION_CACHE_SIZE = 50
        private const val DEFAULT_SUMMARIZATION_CACHE_SIZE = 100
        private val DEFAULT_TTL: Duration = 24.hours
    }
    
    private val transcriptionCache = NotePreviewLRUCache<TranscriptionCacheKey, CachedTranscriptionResponse>(
        maxSize = DEFAULT_TRANSCRIPTION_CACHE_SIZE
    )
    
    private val summarizationCache = NotePreviewLRUCache<SummarizationCacheKey, CachedSummarizationResponse>(
        maxSize = DEFAULT_SUMMARIZATION_CACHE_SIZE
    )
    
    private val mutex = Mutex()
    
    /**
     * Attempts to get a cached transcription result.
     * 
     * @param request The transcription request
     * @return Cached result if available and not expired, null otherwise
     */
    suspend fun getCachedTranscription(request: TranscriptionRequest): OpenAIResponse<TranscriptionResult>? {
        return mutex.withLock {
            val key = TranscriptionCacheKey.from(request)
            val cached = transcriptionCache.get(key)
            
            if (cached != null && !cached.isExpired()) {
                cached.response
            } else {
                if (cached != null) {
                    // Remove expired entry
                    transcriptionCache.remove(key)
                }
                null
            }
        }
    }
    
    /**
     * Caches a transcription result.
     * 
     * @param request The original request
     * @param response The API response to cache
     */
    suspend fun cacheTranscription(request: TranscriptionRequest, response: OpenAIResponse<TranscriptionResult>) {
        mutex.withLock {
            // Only cache successful responses
            if (response.data != null) {
                val key = TranscriptionCacheKey.from(request)
                val cached = CachedTranscriptionResponse(
                    response = response,
                    timestamp = System.currentTimeMillis(),
                    ttl = DEFAULT_TTL
                )
                transcriptionCache.put(key, cached)
            }
        }
    }
    
    /**
     * Attempts to get a cached summarization result.
     * 
     * @param request The summarization request
     * @return Cached result if available and not expired, null otherwise
     */
    suspend fun getCachedSummarization(request: SummarizationRequest): OpenAIResponse<SummarizationResult>? {
        return mutex.withLock {
            val key = SummarizationCacheKey.from(request)
            val cached = summarizationCache.get(key)
            
            if (cached != null && !cached.isExpired()) {
                cached.response
            } else {
                if (cached != null) {
                    // Remove expired entry
                    summarizationCache.remove(key)
                }
                null
            }
        }
    }
    
    /**
     * Caches a summarization result.
     * 
     * @param request The original request
     * @param response The API response to cache
     */
    suspend fun cacheSummarization(request: SummarizationRequest, response: OpenAIResponse<SummarizationResult>) {
        mutex.withLock {
            // Only cache successful responses
            if (response.data != null) {
                val key = SummarizationCacheKey.from(request)
                val cached = CachedSummarizationResponse(
                    response = response,
                    timestamp = System.currentTimeMillis(),
                    ttl = DEFAULT_TTL
                )
                summarizationCache.put(key, cached)
            }
        }
    }
    
    /**
     * Clears all cached responses.
     */
    suspend fun clearAll() {
        mutex.withLock {
            transcriptionCache.clear()
            summarizationCache.clear()
        }
    }
    
    /**
     * Gets cache statistics for monitoring.
     */
    suspend fun getCacheStats(): CacheStats {
        return mutex.withLock {
            CacheStats(
                transcriptionCacheSize = transcriptionCache.size(),
                transcriptionCacheMaxSize = DEFAULT_TRANSCRIPTION_CACHE_SIZE,
                summarizationCacheSize = summarizationCache.size(),
                summarizationCacheMaxSize = DEFAULT_SUMMARIZATION_CACHE_SIZE
            )
        }
    }
}

/**
 * Cache key for transcription requests based on file content hash and parameters.
 */
data class TranscriptionCacheKey(
    val fileHash: String,
    val language: String?,
    val prompt: String?,
    val temperature: Double?
) {
    companion object {
        fun from(request: TranscriptionRequest): TranscriptionCacheKey {
            // Create hash from file path (simplified - in production you'd hash file content)
            val fileHash = request.audioFilePath.hashCode().toString()
            return TranscriptionCacheKey(
                fileHash = fileHash,
                language = request.language,
                prompt = request.prompt,
                temperature = request.temperature
            )
        }
    }
}

/**
 * Cache key for summarization requests based on text hash and parameters.
 */
data class SummarizationCacheKey(
    val textHash: String,
    val style: String,
    val maxLength: Int?
) {
    companion object {
        fun from(request: SummarizationRequest): SummarizationCacheKey {
            val textHash = request.text.hashCode().toString()
            return SummarizationCacheKey(
                textHash = textHash,
                style = request.style.name,
                maxLength = request.maxLength
            )
        }
    }
}

/**
 * Cached transcription response with TTL.
 */
data class CachedTranscriptionResponse(
    val response: OpenAIResponse<TranscriptionResult>,
    val timestamp: Long,
    val ttl: Duration
) {
    fun isExpired(): Boolean {
        val ageMs = System.currentTimeMillis() - timestamp
        return ageMs > ttl.inWholeMilliseconds
    }
}

/**
 * Cached summarization response with TTL.
 */
data class CachedSummarizationResponse(
    val response: OpenAIResponse<SummarizationResult>,
    val timestamp: Long,
    val ttl: Duration
) {
    fun isExpired(): Boolean {
        val ageMs = System.currentTimeMillis() - timestamp
        return ageMs > ttl.inWholeMilliseconds
    }
}

/**
 * Cache statistics for monitoring and analytics.
 */
data class CacheStats(
    val transcriptionCacheSize: Int,
    val transcriptionCacheMaxSize: Int,
    val summarizationCacheSize: Int,
    val summarizationCacheMaxSize: Int
) {
    val transcriptionHitRate: Double
        get() = if (transcriptionCacheMaxSize > 0) {
            transcriptionCacheSize.toDouble() / transcriptionCacheMaxSize
        } else 0.0
    
    val summarizationHitRate: Double
        get() = if (summarizationCacheMaxSize > 0) {
            summarizationCacheSize.toDouble() / summarizationCacheMaxSize
        } else 0.0
}