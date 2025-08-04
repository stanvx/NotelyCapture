package com.module.notelycompose.notes.ui.cache

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Memory-efficient LRU Cache for note preview components to prevent memory accumulation
 * in large note collections. This addresses the memory management issue identified in
 * Apple QA review for complex caching keys.
 * 
 * Features:
 * - Thread-safe operations with coroutine Mutex
 * - Configurable cache size limits (default: 150 entries)
 * - Memory pressure monitoring
 * - Automatic eviction of least recently used entries
 * - Lightweight cache keys to prevent memory bloat
 * - Fallback strategies for memory pressure scenarios
 */
class NotePreviewLRUCache<K, V>(
    private val maxSize: Int = DEFAULT_CACHE_SIZE,
    private val onMemoryPressure: (() -> Unit)? = null
) {
    companion object {
        const val DEFAULT_CACHE_SIZE = 150
        const val MEMORY_PRESSURE_THRESHOLD = 0.85 // 85% of max size
        const val EMERGENCY_CLEAR_THRESHOLD = 0.95 // 95% of max size
    }

    private val mutex = Mutex()
    // Set initial capacity to (maxSize / loadFactor) + 1 to minimize rehashing
    private val cache = LinkedHashMap<K, CacheEntry<V>>((maxSize / 0.75f).toInt() + 1, 0.75f, true)
    private val _memoryUsage = mutableStateOf(MemoryUsageInfo())
    val memoryUsage: State<MemoryUsageInfo> = _memoryUsage

    private data class CacheEntry<V>(
        val value: V,
        val accessTime: Long = System.currentTimeMillis(),
        val creationTime: Long = System.currentTimeMillis()
    )

    data class MemoryUsageInfo(
        val currentSize: Int = 0,
        val maxSize: Int = DEFAULT_CACHE_SIZE,
        val usagePercentage: Float = 0f,
        val isMemoryPressure: Boolean = false,
        val totalEvictions: Long = 0
    )

    private var totalEvictions = 0L

    /**
     * Get value from cache or compute if not present
     * Thread-safe operation with memory pressure monitoring
     */
    suspend fun getOrPut(key: K, factory: suspend () -> V): V = mutex.withLock {
        // Check for emergency memory situation
        if (cache.size >= maxSize * EMERGENCY_CLEAR_THRESHOLD) {
            performEmergencyClear()
        }

        val existing = cache[key]
        if (existing != null) {
            // Move to end (most recently used)
            cache.remove(key)
            cache[key] = existing.copy(accessTime = System.currentTimeMillis())
            updateMemoryUsage()
            return existing.value
        }

        // Evict LRU entries if needed
        while (cache.size >= maxSize) {
            evictLRU()
        }

        // Compute new value and cache it
        val newValue = factory()
        cache[key] = CacheEntry(newValue)
        
        updateMemoryUsage()
        checkMemoryPressure()
        
        return newValue
    }

    /**
     * Get cached value without factory fallback
     */
    suspend fun get(key: K): V? = mutex.withLock {
        val entry = cache[key]
        if (entry != null) {
            // Update access time and move to end
            cache.remove(key)
            cache[key] = entry.copy(accessTime = System.currentTimeMillis())
            updateMemoryUsage()
        }
        return entry?.value
    }

    /**
     * Put value in cache
     */
    suspend fun put(key: K, value: V) = mutex.withLock {
        // Evict if necessary
        while (cache.size >= maxSize) {
            evictLRU()
        }
        
        cache[key] = CacheEntry(value)
        updateMemoryUsage()
        checkMemoryPressure()
    }

    /**
     * Remove specific entry
     */
    suspend fun remove(key: K): V? = mutex.withLock {
        val removed = cache.remove(key)
        updateMemoryUsage()
        return removed?.value
    }

    /**
     * Clear all entries
     */
    suspend fun clear() = mutex.withLock {
        cache.clear()
        updateMemoryUsage()
    }

    /**
     * Evict entries older than specified age
     */
    suspend fun evictStale(maxAgeMs: Long = 5 * 60 * 1000L) = mutex.withLock { // 5 minutes default
        val currentTime = System.currentTimeMillis()
        val iterator = cache.iterator()
        var evictionCount = 0
        
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (currentTime - entry.value.creationTime > maxAgeMs) {
                iterator.remove()
                evictionCount++
            }
        }
        
        if (evictionCount > 0) {
            totalEvictions += evictionCount
            updateMemoryUsage()
        }
    }

    /**
     * Get cache statistics for monitoring
     */
    suspend fun getStats(): CacheStats = mutex.withLock {
        val entries = cache.values.toList()
        val currentTime = System.currentTimeMillis()
        
        CacheStats(
            size = cache.size,
            maxSize = maxSize,
            usagePercentage = (cache.size.toFloat() / maxSize) * 100,
            totalEvictions = totalEvictions,
            averageAge = if (entries.isNotEmpty()) {
                entries.map { currentTime - it.creationTime }.average().toLong()
            } else 0L,
            oldestEntry = entries.maxOfOrNull { currentTime - it.creationTime } ?: 0L
        )
    }

    private fun evictLRU() {
        val lruEntry = cache.entries.firstOrNull()
        if (lruEntry != null) {
            cache.remove(lruEntry.key)
            totalEvictions++
        }
    }

    private fun performEmergencyClear() {
        // Clear 50% of cache in emergency situations
        val itemsToRemove = cache.size / 2
        repeat(itemsToRemove) {
            evictLRU()
        }
        
        // Trigger memory pressure callback
        onMemoryPressure?.invoke()
    }

    private fun checkMemoryPressure() {
        val usageRatio = cache.size.toFloat() / maxSize
        if (usageRatio >= MEMORY_PRESSURE_THRESHOLD) {
            onMemoryPressure?.invoke()
        }
    }

    private fun updateMemoryUsage() {
        val usagePercentage = (cache.size.toFloat() / maxSize) * 100
        _memoryUsage.value = MemoryUsageInfo(
            currentSize = cache.size,
            maxSize = maxSize,
            usagePercentage = usagePercentage,
            isMemoryPressure = usagePercentage >= MEMORY_PRESSURE_THRESHOLD * 100,
            totalEvictions = totalEvictions
        )
    }

    data class CacheStats(
        val size: Int,
        val maxSize: Int,
        val usagePercentage: Float,
        val totalEvictions: Long,
        val averageAge: Long,
        val oldestEntry: Long
    )
}

/**
 * Lightweight cache key for note previews to prevent memory bloat from complex objects
 */
data class NotePreviewCacheKey(
    val noteId: Long,
    val contentHash: Int,
    val isVoice: Boolean,
    val isStarred: Boolean
) {
    companion object {
        /**
         * Create cache key from note data with efficient hashing
         */
        fun fromNoteData(
            id: Long,
            title: String,
            content: String,
            isVoice: Boolean,
            isStarred: Boolean
        ): NotePreviewCacheKey {
            // Use efficient hash calculation instead of storing full content
            val contentHash = (title + content).hashCode()
            return NotePreviewCacheKey(id, contentHash, isVoice, isStarred)
        }
    }
}

/**
 * Cached note color scheme to prevent repeated color calculations
 */
data class CachedNoteColorScheme(
    val container: Color,
    val onContainer: Color,
    val accent: Color,
    val outline: Color,
    val key: NotePreviewCacheKey
)

/**
 * Cached formatted text to prevent repeated string operations
 */
data class CachedFormattedText(
    val formattedText: String,
    val key: NotePreviewCacheKey
)

/**
 * Global cache instances for note preview components
 */
object NotePreviewCaches {
    /**
     * Cache for note color schemes - most memory intensive
     */
    val colorSchemeCache = NotePreviewLRUCache<NotePreviewCacheKey, CachedNoteColorScheme>(
        maxSize = 100, // Smaller cache for color schemes
        onMemoryPressure = {
            // Could trigger UI warning or reduce cache sizes
            println("Memory pressure detected in color scheme cache")
        }
    )

    /**
     * Cache for formatted date strings
     */
    val dateFormatCache = NotePreviewLRUCache<NotePreviewCacheKey, CachedFormattedText>(
        maxSize = 200, // Larger cache for lighter string data
        onMemoryPressure = {
            println("Memory pressure detected in date format cache")
        }
    )

    /**
     * Cache for computed content previews
     */
    val contentPreviewCache = NotePreviewLRUCache<NotePreviewCacheKey, CachedFormattedText>(
        maxSize = 150,
        onMemoryPressure = {
            println("Memory pressure detected in content preview cache")
        }
    )

    /**
     * Perform maintenance on all caches - should be called periodically
     */
    suspend fun performMaintenance() {
        val maxAge = 10 * 60 * 1000L // 10 minutes
        colorSchemeCache.evictStale(maxAge)
        dateFormatCache.evictStale(maxAge)
        contentPreviewCache.evictStale(maxAge)
    }

    /**
     * Clear all caches in low memory situations
     */
    suspend fun clearAllCaches() {
        colorSchemeCache.clear()
        dateFormatCache.clear()
        contentPreviewCache.clear()
    }

    /**
     * Get aggregated memory usage across all caches
     */
    suspend fun getAggregatedMemoryUsage(): AggregatedMemoryUsage {
        val colorStats = colorSchemeCache.getStats()
        val dateStats = dateFormatCache.getStats()
        val contentStats = contentPreviewCache.getStats()

        return AggregatedMemoryUsage(
            totalEntries = colorStats.size + dateStats.size + contentStats.size,
            totalEvictions = colorStats.totalEvictions + dateStats.totalEvictions + contentStats.totalEvictions,
            colorCacheUsage = colorStats.usagePercentage,
            dateCacheUsage = dateStats.usagePercentage,
            contentCacheUsage = contentStats.usagePercentage
        )
    }

    data class AggregatedMemoryUsage(
        val totalEntries: Int,
        val totalEvictions: Long,
        val colorCacheUsage: Float,
        val dateCacheUsage: Float,
        val contentCacheUsage: Float
    ) {
        val averageUsage: Float = (colorCacheUsage + dateCacheUsage + contentCacheUsage) / 3
        val isMemoryPressure: Boolean = averageUsage > 85f
    }
}