package com.module.notelycompose.transcription.domain

import com.module.notelycompose.core.debugPrintln
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * Application-level singleton that manages Whisper model lifecycle.
 * Prevents duplicate model loading and ensures thread-safe initialization.
 * Implements intelligent lifecycle management with idle timeout and memory pressure handling.
 */
class WhisperModelManager(
    private val modelLoader: WhisperModelLoader
) {
    private var isModelLoaded: Boolean = false
    private var lastUsageTimestamp: Long = 0L
    private var activeTranscriptionCount: Int = 0
    private var wasUnloadedDueToPressure: Boolean = false
    private val initMutex = Mutex()
    
    // 5 minute idle timeout before considering model for unload
    private val idleTimeout = 5.minutes
    
    /**
     * Ensures the Whisper model is loaded exactly once.
     * Thread-safe and idempotent - safe to call multiple times.
     */
    suspend fun ensureModelLoaded(): WhisperLoadResult {
        updateLastUsageTime()
        
        if (isModelLoaded) {
            debugPrintln { "WhisperModelManager: Model already loaded, skipping initialization" }
            return WhisperLoadResult.Success
        }
        
        return initMutex.withLock {
            if (isModelLoaded) {
                debugPrintln { "WhisperModelManager: Model loaded by another thread, skipping" }
                return@withLock WhisperLoadResult.Success
            }
            
            debugPrintln { "WhisperModelManager: Initializing model..." }
            try {
                withContext(Dispatchers.Default) {
                    modelLoader.loadModel()
                }
                isModelLoaded = true
                wasUnloadedDueToPressure = false
                debugPrintln { "WhisperModelManager: Model initialization complete" }
                WhisperLoadResult.Success
                
            } catch (e: Exception) {
                debugPrintln { "WhisperModelManager: Model initialization failed: ${e.message}" }
                when {
                    e.message?.contains("OutOfMemoryError") == true -> 
                        WhisperLoadResult.Failure.InsufficientMemory(e)
                    e.message?.contains("FileNotFoundException") == true -> 
                        WhisperLoadResult.Failure.ModelNotFound(e)
                    else -> 
                        WhisperLoadResult.Failure.LoadError(e)
                }
            }
        }
    }
    
    /**
     * Marks the start of a transcription session.
     * Prevents idle timeout while transcription is active.
     */
    fun startTranscriptionSession() {
        activeTranscriptionCount++
        updateLastUsageTime()
        debugPrintln { "WhisperModelManager: Transcription session started, active count: $activeTranscriptionCount" }
    }
    
    /**
     * Marks the end of a transcription session.
     */
    fun endTranscriptionSession() {
        if (activeTranscriptionCount > 0) {
            activeTranscriptionCount--
        }
        updateLastUsageTime()
        debugPrintln { "WhisperModelManager: Transcription session ended, active count: $activeTranscriptionCount" }
    }
    
    /**
     * Called by platform-specific memory pressure handlers.
     * Releases model immediately if not actively transcribing.
     */
    suspend fun handleMemoryPressure() {
        initMutex.withLock {
            if (isModelLoaded && activeTranscriptionCount == 0) {
                debugPrintln { "WhisperModelManager: Releasing model due to memory pressure" }
                withContext(Dispatchers.Default) {
                    modelLoader.releaseModel()
                }
                isModelLoaded = false
                wasUnloadedDueToPressure = true
            } else if (activeTranscriptionCount > 0) {
                debugPrintln { "WhisperModelManager: Memory pressure detected but transcription active, keeping model" }
            }
        }
    }
    
    /**
     * Called when app goes to background. Starts idle timeout.
     */
    fun onAppBackground() {
        debugPrintln { "WhisperModelManager: App went to background" }
        // Background idle handling can be implemented here if needed
        // For now, we rely on memory pressure callbacks
    }
    
    /**
     * Called when app returns to foreground.
     */
    fun onAppForeground() {
        debugPrintln { "WhisperModelManager: App returned to foreground" }
        updateLastUsageTime()
    }
    
    /**
     * Checks if model is idle and can be released.
     */
    fun isIdle(): Boolean {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val idleTime = currentTime - lastUsageTimestamp
        return isModelLoaded && 
               activeTranscriptionCount == 0 && 
               idleTime > idleTimeout.inWholeMilliseconds
    }
    
    /**
     * Releases model if it's been idle.
     * Called periodically by platform-specific lifecycle handlers.
     */
    suspend fun releaseIfIdle() {
        if (isIdle()) {
            initMutex.withLock {
                if (isIdle()) { // Double-check after acquiring lock
                    debugPrintln { "WhisperModelManager: Releasing idle model" }
                    withContext(Dispatchers.Default) {
                        modelLoader.releaseModel()
                    }
                    isModelLoaded = false
                }
            }
        }
    }
    
    /**
     * Force release the model (for app termination or explicit cleanup).
     */
    suspend fun forceRelease() {
        initMutex.withLock {
            if (isModelLoaded) {
                debugPrintln { "WhisperModelManager: Force releasing model resources" }
                withContext(Dispatchers.Default) {
                    modelLoader.releaseModel()
                }
                isModelLoaded = false
                activeTranscriptionCount = 0
            }
        }
    }
    
    /**
     * Checks if the model is currently loaded.
     */
    fun isLoaded(): Boolean = isModelLoaded
    
    /**
     * Gets statistics about model usage.
     */
    fun getStats(): WhisperModelStats {
        return WhisperModelStats(
            isLoaded = isModelLoaded,
            activeTranscriptions = activeTranscriptionCount,
            lastUsedAgo = Clock.System.now().toEpochMilliseconds() - lastUsageTimestamp,
            wasUnloadedDueToPressure = wasUnloadedDueToPressure
        )
    }
    
    private fun updateLastUsageTime() {
        lastUsageTimestamp = Clock.System.now().toEpochMilliseconds()
    }
    
    /**
     * Legacy method for backward compatibility.
     */
    suspend fun resetModel() = forceRelease()
    
    /**
     * Legacy method for backward compatibility.
     */
    suspend fun close() = forceRelease()
}

/**
 * Result of Whisper model loading operation.
 */
sealed class WhisperLoadResult {
    object Success : WhisperLoadResult()
    
    sealed class Failure : WhisperLoadResult() {
        abstract val exception: Exception
        
        data class InsufficientMemory(override val exception: Exception) : Failure()
        data class ModelNotFound(override val exception: Exception) : Failure()
        data class LoadError(override val exception: Exception) : Failure()
    }
}

/**
 * Statistics about Whisper model usage.
 */
data class WhisperModelStats(
    val isLoaded: Boolean,
    val activeTranscriptions: Int,
    val lastUsedAgo: Long,
    val wasUnloadedDueToPressure: Boolean
)

/**
 * Platform-specific model loader interface.
 * Implementations should handle actual Whisper model loading/releasing.
 */
expect class WhisperModelLoader {
    suspend fun loadModel()
    suspend fun releaseModel()
}