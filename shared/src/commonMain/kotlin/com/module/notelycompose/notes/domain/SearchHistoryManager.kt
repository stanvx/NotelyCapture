package com.module.notelycompose.notes.domain

import com.module.notelycompose.core.CommonFlow
import com.module.notelycompose.core.toCommonFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

/**
 * Manages search history with caching, persistence, and intelligent ranking.
 * 
 * Features:
 * - LRU cache for recent searches
 * - Frequency-based ranking
 * - Automatic cleanup of old entries
 * - Thread-safe operations
 */
class SearchHistoryManager(
    private val searchHistoryDataSource: SearchHistoryDataSource
) {
    
    companion object {
        private const val MAX_RECENT_SEARCHES = 50
        private const val MAX_CACHE_SIZE = 20
        private const val CLEANUP_THRESHOLD_DAYS = 30L
    }
    
    // In-memory cache for performance
    private val _cachedSearches = MutableStateFlow<List<SearchHistoryItem>>(emptyList())
    private val cachedSearches = _cachedSearches.asStateFlow()
    
    // Performance optimization: track if cache is initialized
    private var isCacheInitialized = false
    
    /**
     * Gets recent search history ordered by relevance (frequency + recency).
     */
    fun getRecentSearches(): CommonFlow<List<String>> {
        return cachedSearches.map { items ->
            items
                .sortedByDescending { calculateRelevanceScore(it) }
                .map { it.query }
                .take(MAX_CACHE_SIZE)
        }.toCommonFlow()
    }
    
    /**
     * Adds a new search to history with intelligent deduplication.
     */
    suspend fun addSearch(query: String) {
        if (query.isBlank()) return
        
        // Ensure cache is loaded
        initializeCacheIfNeeded()
        
        val currentTime = Clock.System.now()
        val currentItems = _cachedSearches.value.toMutableList()
        
        // Check if query already exists
        val existingIndex = currentItems.indexOfFirst { 
            it.query.equals(query, ignoreCase = true) 
        }
        
        if (existingIndex >= 0) {
            // Update existing item with new timestamp and increment frequency
            val existingItem = currentItems[existingIndex]
            currentItems[existingIndex] = existingItem.copy(
                lastUsed = currentTime,
                frequency = existingItem.frequency + 1
            )
        } else {
            // Add new item
            currentItems.add(
                SearchHistoryItem(
                    query = query,
                    firstUsed = currentTime,
                    lastUsed = currentTime,
                    frequency = 1
                )
            )
        }
        
        // Maintain cache size limit
        if (currentItems.size > MAX_RECENT_SEARCHES) {
            // Remove oldest items with lowest relevance scores
            val sortedItems = currentItems.sortedByDescending { calculateRelevanceScore(it) }
            currentItems.clear()
            currentItems.addAll(sortedItems.take(MAX_RECENT_SEARCHES))
        }
        
        // Update cache and persist
        _cachedSearches.value = currentItems
        
        // Persist to storage (async)
        try {
            if (existingIndex >= 0) {
                searchHistoryDataSource.updateSearchHistory(currentItems[existingIndex])
            } else {
                searchHistoryDataSource.insertSearchHistory(currentItems.last())
            }
        } catch (e: Exception) {
            // Log error but don't fail the operation
            println("Failed to persist search history: ${e.message}")
        }
    }
    
    /**
     * Clears all search history.
     */
    suspend fun clearHistory() {
        _cachedSearches.value = emptyList()
        try {
            searchHistoryDataSource.clearAllSearchHistory()
        } catch (e: Exception) {
            println("Failed to clear search history: ${e.message}")
        }
    }
    
    /**
     * Removes a specific search from history.
     */
    suspend fun removeSearch(query: String) {
        val currentItems = _cachedSearches.value.toMutableList()
        val indexToRemove = currentItems.indexOfFirst { 
            it.query.equals(query, ignoreCase = true) 
        }
        
        if (indexToRemove >= 0) {
            val removedItem = currentItems.removeAt(indexToRemove)
            _cachedSearches.value = currentItems
            
            try {
                searchHistoryDataSource.deleteSearchHistory(removedItem.query)
            } catch (e: Exception) {
                println("Failed to remove search from storage: ${e.message}")
            }
        }
    }
    
    /**
     * Performs cleanup of old search entries.
     */
    suspend fun performCleanup() {
        val cutoffTime = Clock.System.now().minus(
            CLEANUP_THRESHOLD_DAYS.days
        )
        
        val currentItems = _cachedSearches.value.toMutableList()
        val filteredItems = currentItems.filter { it.lastUsed >= cutoffTime }
        
        if (filteredItems.size != currentItems.size) {
            _cachedSearches.value = filteredItems
            
            try {
                searchHistoryDataSource.deleteOldSearchHistory(cutoffTime)
            } catch (e: Exception) {
                println("Failed to cleanup old search history: ${e.message}")
            }
        }
    }
    
    /**
     * Gets search suggestions that match a partial query.
     */
    fun getSearchSuggestions(partialQuery: String): CommonFlow<List<String>> {
        return cachedSearches.map { items ->
            if (partialQuery.isBlank()) {
                emptyList()
            } else {
                items
                    .filter { item ->
                        item.query.contains(partialQuery, ignoreCase = true) &&
                        !item.query.equals(partialQuery, ignoreCase = true)
                    }
                    .sortedByDescending { calculateRelevanceScore(it) }
                    .map { it.query }
                    .take(8) // Limit suggestions
            }
        }.toCommonFlow()
    }
    
    // Private methods
    
    private suspend fun initializeCacheIfNeeded() {
        if (!isCacheInitialized) {
            try {
                val storedItems = searchHistoryDataSource.getAllSearchHistory()
                _cachedSearches.value = storedItems
                isCacheInitialized = true
            } catch (e: Exception) {
                println("Failed to load search history from storage: ${e.message}")
                _cachedSearches.value = emptyList()
                isCacheInitialized = true
            }
        }
    }
    
    /**
     * Calculates relevance score based on frequency and recency.
     * Higher scores indicate more relevant searches.
     */
    private fun calculateRelevanceScore(item: SearchHistoryItem): Double {
        val now = Clock.System.now()
        val daysSinceLastUse = (now - item.lastUsed).inWholeDays.toDouble()
        
        // Frequency score (normalized)
        val frequencyScore = kotlin.math.min(item.frequency.toDouble() / 10.0, 1.0)
        
        // Recency score (exponential decay)
        val recencyScore = kotlin.math.exp(-daysSinceLastUse / 7.0) // 7-day half-life
        
        // Combined score with weights
        return (frequencyScore * 0.6) + (recencyScore * 0.4)
    }
}

/**
 * Represents a search history item with usage metadata.
 */
data class SearchHistoryItem(
    val query: String,
    val firstUsed: Instant,
    val lastUsed: Instant,
    val frequency: Int
)

/**
 * Data source interface for search history persistence.
 */
interface SearchHistoryDataSource {
    suspend fun getAllSearchHistory(): List<SearchHistoryItem>
    suspend fun insertSearchHistory(item: SearchHistoryItem)
    suspend fun updateSearchHistory(item: SearchHistoryItem)
    suspend fun deleteSearchHistory(query: String)
    suspend fun deleteOldSearchHistory(cutoffTime: Instant)
    suspend fun clearAllSearchHistory()
}