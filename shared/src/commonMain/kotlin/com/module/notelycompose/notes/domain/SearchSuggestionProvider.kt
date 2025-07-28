package com.module.notelycompose.notes.domain

import com.module.notelycompose.core.validation.InputValidator
import com.module.notelycompose.notes.domain.model.NoteDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * Provides intelligent search suggestions based on note content, search history, and user patterns.
 * 
 * Features:
 * - Recent search history
 * - Content-based suggestions from note titles and content
 * - Contextual predictions based on user behavior
 * - Security validation for all suggestions
 */
class SearchSuggestionProvider(
    private val noteDataSource: NoteDataSource,
    private val searchHistoryManager: SearchHistoryManager
) {
    
    companion object {
        private const val MAX_SUGGESTIONS = 8
        private const val MIN_QUERY_LENGTH = 1
        private const val MAX_CONTENT_PREVIEW_LENGTH = 50
    }
    
    /**
     * Provides comprehensive search suggestions combining multiple sources.
     * 
     * @param query Current search query
     * @param limit Maximum number of suggestions to return
     * @return Flow of search suggestions ordered by relevance
     */
    fun getSuggestions(
        query: String,
        limit: Int = MAX_SUGGESTIONS
    ): Flow<List<SearchSuggestion>> {
        // Validate query input for security
        val validation = InputValidator.validateSearchQuery(query)
        if (!validation.isValid || query.length < MIN_QUERY_LENGTH) {
            return flowOf(emptyList())
        }
        
        val sanitizedQuery = InputValidator.sanitizeSearchQuery(query)
        
        return combine(
            getRecentSearchSuggestions(sanitizedQuery),
            getContentBasedSuggestions(sanitizedQuery),
            getCommonPhraseSuggestions(sanitizedQuery)
        ) { recentSuggestions, contentSuggestions, phraseSuggestions ->
            
            val allSuggestions = mutableListOf<SearchSuggestion>()
            
            // Add recent searches with high priority
            allSuggestions.addAll(recentSuggestions.take(limit / 3))
            
            // Add content-based suggestions
            allSuggestions.addAll(contentSuggestions.take(limit / 2))
            
            // Fill remaining slots with phrase suggestions
            val remainingSlots = limit - allSuggestions.size
            if (remainingSlots > 0) {
                allSuggestions.addAll(phraseSuggestions.take(remainingSlots))
            }
            
            // Remove duplicates and sort by relevance
            allSuggestions
                .distinctBy { it.text.lowercase() }
                .sortedByDescending { it.relevanceScore }
                .take(limit)
        }
    }
    
    /**
     * Gets suggestions from recent search history.
     */
    private fun getRecentSearchSuggestions(query: String): Flow<List<SearchSuggestion>> {
        return flow {
            searchHistoryManager.getRecentSearches().collect { recentSearches ->
                val suggestions = recentSearches
                    .filter { search ->
                        search.lowercase().contains(query.lowercase()) && 
                        search.lowercase() != query.lowercase()
                    }
                    .map { search ->
                        SearchSuggestion(
                            text = search,
                            type = SearchSuggestionType.RECENT_SEARCH,
                            relevanceScore = calculateRecentSearchScore(search, query),
                            preview = null
                        )
                    }
                emit(suggestions)
            }
        }
    }
    
    /**
     * Gets suggestions based on note content analysis.
     */
    private fun getContentBasedSuggestions(query: String): Flow<List<SearchSuggestion>> {
        return flow {
            val notes = noteDataSource.getNotes()
            notes.collect { notesList ->
                val suggestions = mutableListOf<SearchSuggestion>()
                
                for (note in notesList) {
                    // Check note title for matches
                    if (note.title.contains(query, ignoreCase = true)) {
                        suggestions.add(
                            SearchSuggestion(
                                text = note.title,
                                type = SearchSuggestionType.NOTE_TITLE,
                                relevanceScore = calculateTitleScore(note.title, query),
                                preview = note.content.take(MAX_CONTENT_PREVIEW_LENGTH)
                            )
                        )
                    }
                    
                    // Extract relevant phrases from content
                    val contentSuggestions = extractRelevantPhrases(note.content, query)
                    suggestions.addAll(contentSuggestions)
                }
                
                emit(suggestions
                    .distinctBy { it.text.lowercase() }
                    .sortedByDescending { it.relevanceScore })
            }
        }
    }
    
    /**
     * Gets common phrase suggestions based on query patterns.
     */
    private fun getCommonPhraseSuggestions(query: String): Flow<List<SearchSuggestion>> = flow {
        val commonPhrases = getCommonSearchPhrases()
        
        val suggestions = commonPhrases
            .filter { phrase ->
                phrase.startsWith(query, ignoreCase = true) && 
                phrase.length > query.length
            }
            .map { phrase ->
                SearchSuggestion(
                    text = phrase,
                    type = SearchSuggestionType.COMMON_PHRASE,
                    relevanceScore = calculatePhraseScore(phrase, query),
                    preview = null
                )
            }
        
        emit(suggestions)
    }
    
    /**
     * Records a search query for future suggestions.
     */
    suspend fun recordSearch(query: String) {
        val validation = InputValidator.validateSearchQuery(query)
        if (validation.isValid) {
            val sanitizedQuery = InputValidator.sanitizeSearchQuery(query)
            searchHistoryManager.addSearch(sanitizedQuery)
        }
    }
    
    /**
     * Clears search history.
     */
    suspend fun clearSearchHistory() {
        searchHistoryManager.clearHistory()
    }
    
    // Private helper methods
    
    private fun calculateRecentSearchScore(search: String, query: String): Float {
        val lengthFactor = (query.length.toFloat() / search.length).coerceIn(0.1f, 1.0f)
        val startsWithBonus = if (search.startsWith(query, ignoreCase = true)) 0.3f else 0.0f
        return 0.8f + lengthFactor * 0.2f + startsWithBonus
    }
    
    private fun calculateTitleScore(title: String, query: String): Float {
        val exactMatch = if (title.equals(query, ignoreCase = true)) 1.0f else 0.0f
        val startsWithBonus = if (title.startsWith(query, ignoreCase = true)) 0.4f else 0.0f
        val containsScore = if (title.contains(query, ignoreCase = true)) 0.6f else 0.0f
        val lengthPenalty = (title.length - query.length).toFloat() / 100f
        
        return exactMatch + startsWithBonus + containsScore - lengthPenalty.coerceIn(0f, 0.3f)
    }
    
    private fun calculatePhraseScore(phrase: String, query: String): Float {
        val startsWithBonus = if (phrase.startsWith(query, ignoreCase = true)) 0.7f else 0.2f
        val lengthFactor = (query.length.toFloat() / phrase.length).coerceIn(0.1f, 0.8f)
        return startsWithBonus + lengthFactor * 0.3f
    }
    
    private fun extractRelevantPhrases(content: String, query: String): List<SearchSuggestion> {
        val words = content.split("\\s+".toRegex())
        val suggestions = mutableListOf<SearchSuggestion>()
        
        // Look for phrases that start with or contain the query
        for (i in words.indices) {
            val phrase = words.drop(i).take(3).joinToString(" ")
            if (phrase.contains(query, ignoreCase = true) && phrase.length <= 50) {
                suggestions.add(
                    SearchSuggestion(
                        text = phrase.trim(),
                        type = SearchSuggestionType.CONTENT_PHRASE,
                        relevanceScore = calculateContentPhraseScore(phrase, query),
                        preview = null
                    )
                )
            }
        }
        
        return suggestions.take(3) // Limit content phrases per note
    }
    
    private fun calculateContentPhraseScore(phrase: String, query: String): Float {
        val containsScore = if (phrase.contains(query, ignoreCase = true)) 0.5f else 0.0f
        val startsWithBonus = if (phrase.startsWith(query, ignoreCase = true)) 0.2f else 0.0f
        val lengthPenalty = phrase.length / 100f
        return containsScore + startsWithBonus - lengthPenalty.coerceIn(0f, 0.2f)
    }
    
    private fun getCommonSearchPhrases(): List<String> {
        return listOf(
            "meeting notes",
            "today's tasks",
            "important",
            "urgent",
            "follow up",
            "action items",
            "project",
            "deadline",
            "reminder",
            "call summary",
            "ideas",
            "brainstorm",
            "todo",
            "grocery list",
            "shopping",
            "travel",
            "recipes",
            "contacts",
            "research"
        )
    }
}

/**
 * Represents a search suggestion with metadata.
 */
data class SearchSuggestion(
    val text: String,
    val type: SearchSuggestionType,
    val relevanceScore: Float,
    val preview: String? = null
)

/**
 * Types of search suggestions for different UI treatments.
 */
enum class SearchSuggestionType {
    RECENT_SEARCH,
    NOTE_TITLE,
    CONTENT_PHRASE,
    COMMON_PHRASE
}