package com.module.notelycompose.notes.data

import com.module.notelycompose.notes.domain.SearchHistoryDataSource
import com.module.notelycompose.notes.domain.SearchHistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Implementation of SearchHistoryDataSource using DataStore for persistence.
 * 
 * Features:
 * - JSON serialization for complex data structures
 * - Atomic operations using DataStore
 * - Background thread operations
 * - Error handling and recovery
 */
class SearchHistoryDataSourceImpl(
    private val dataStore: DataStore<Preferences>
) : SearchHistoryDataSource {
    
    companion object {
        private val SEARCH_HISTORY_KEY = stringPreferencesKey("search_history")
        private const val MAX_STORED_ITEMS = 100
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    override suspend fun getAllSearchHistory(): List<SearchHistoryItem> = withContext(Dispatchers.IO) {
        try {
            val serializedData = dataStore.data
                .map { preferences -> preferences[SEARCH_HISTORY_KEY] }
                .first()
            
            if (serializedData.isNullOrEmpty()) {
                return@withContext emptyList()
            }
            
            val storedItems = json.decodeFromString<List<SearchHistoryItemData>>(serializedData)
            storedItems.map { it.toDomainModel() }
        } catch (e: Exception) {
            println("Error loading search history: ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun insertSearchHistory(item: SearchHistoryItem) = withContext(Dispatchers.IO) {
        try {
            val currentItems = getAllSearchHistory().toMutableList()
            currentItems.add(item)
            
            // Maintain size limit
            if (currentItems.size > MAX_STORED_ITEMS) {
                val sortedItems = currentItems.sortedByDescending { it.lastUsed }
                currentItems.clear()
                currentItems.addAll(sortedItems.take(MAX_STORED_ITEMS))
            }
            
            saveItems(currentItems)
        } catch (e: Exception) {
            println("Error inserting search history: ${e.message}")
        }
    }
    
    override suspend fun updateSearchHistory(item: SearchHistoryItem) = withContext(Dispatchers.IO) {
        try {
            val currentItems = getAllSearchHistory().toMutableList()
            val existingIndex = currentItems.indexOfFirst { 
                it.query.equals(item.query, ignoreCase = true) 
            }
            
            if (existingIndex >= 0) {
                currentItems[existingIndex] = item
                saveItems(currentItems)
            } else {
                // Item doesn't exist, insert it
                insertSearchHistory(item)
            }
        } catch (e: Exception) {
            println("Error updating search history: ${e.message}")
        }
    }
    
    override suspend fun deleteSearchHistory(query: String) = withContext(Dispatchers.IO) {
        try {
            val currentItems = getAllSearchHistory().toMutableList()
            val updatedItems = currentItems.filter { 
                !it.query.equals(query, ignoreCase = true) 
            }
            
            if (updatedItems.size != currentItems.size) {
                saveItems(updatedItems)
            }
        } catch (e: Exception) {
            println("Error deleting search history: ${e.message}")
        }
    }
    
    override suspend fun deleteOldSearchHistory(cutoffTime: Instant) = withContext(Dispatchers.IO) {
        try {
            val currentItems = getAllSearchHistory().toMutableList()
            val filteredItems = currentItems.filter { it.lastUsed >= cutoffTime }
            
            if (filteredItems.size != currentItems.size) {
                saveItems(filteredItems)
            }
        } catch (e: Exception) {
            println("Error cleaning up old search history: ${e.message}")
        }
    }
    
    override suspend fun clearAllSearchHistory(): Unit = withContext(Dispatchers.IO) {
        try {
            dataStore.edit { preferences ->
                preferences.remove(SEARCH_HISTORY_KEY)
            }
        } catch (e: Exception) {
            println("Error clearing search history: ${e.message}")
        }
    }
    
    // Private helper methods
    
    private suspend fun saveItems(items: List<SearchHistoryItem>) {
        try {
            val dataItems = items.map { SearchHistoryItemData.fromDomainModel(it) }
            val serializedData = json.encodeToString(dataItems)
            
            dataStore.edit { preferences ->
                preferences[SEARCH_HISTORY_KEY] = serializedData
            }
        } catch (e: Exception) {
            println("Error saving search history: ${e.message}")
        }
    }
}

/**
 * Serializable data model for search history persistence.
 */
@Serializable
private data class SearchHistoryItemData(
    val query: String,
    val firstUsedEpochSeconds: Long,
    val lastUsedEpochSeconds: Long,
    val frequency: Int
) {
    fun toDomainModel(): SearchHistoryItem {
        return SearchHistoryItem(
            query = query,
            firstUsed = Instant.fromEpochSeconds(firstUsedEpochSeconds),
            lastUsed = Instant.fromEpochSeconds(lastUsedEpochSeconds),
            frequency = frequency
        )
    }
    
    companion object {
        fun fromDomainModel(item: SearchHistoryItem): SearchHistoryItemData {
            return SearchHistoryItemData(
                query = item.query,
                firstUsedEpochSeconds = item.firstUsed.epochSeconds,
                lastUsedEpochSeconds = item.lastUsed.epochSeconds,
                frequency = item.frequency
            )
        }
    }
}