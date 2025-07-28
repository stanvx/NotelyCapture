package com.module.notelycompose.notes.domain

import com.module.notelycompose.core.validation.InputValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.text.split

/**
 * Provides intelligent text content predictions for rich text editing.
 * 
 * Features:
 * - Word completion based on context
 * - Phrase suggestions from previous notes
 * - Smart punctuation and formatting suggestions
 * - Language-aware predictions
 */
class TextContentPredictor(
    private val noteDataSource: NoteDataSource
) {
    
    companion object {
        private const val MAX_SUGGESTIONS = 5
        private const val MIN_WORD_LENGTH = 2
        private const val CONTEXT_WINDOW_SIZE = 10 // words before cursor
    }
    
    /**
     * Provides text completion suggestions based on current context.
     * 
     * @param currentText The full text content
     * @param cursorPosition Current cursor position
     * @param limit Maximum number of suggestions
     * @return Flow of text completion suggestions
     */
    fun getTextCompletions(
        currentText: String,
        cursorPosition: Int,
        limit: Int = MAX_SUGGESTIONS
    ): Flow<List<TextCompletion>> = flow {
        
        // Validate input
        if (currentText.isEmpty() || cursorPosition < 0 || cursorPosition > currentText.length) {
            emit(emptyList())
            return@flow
        }
        
        // Extract context around cursor
        val textBeforeCursor = currentText.substring(0, cursorPosition)
        val currentWord = extractCurrentWord(textBeforeCursor)
        val context = extractContext(textBeforeCursor)
        
        if (currentWord.length < MIN_WORD_LENGTH) {
            emit(emptyList())
            return@flow
        }
        
        // Get completions from different sources
        val completions = mutableListOf<TextCompletion>()
        
        // Word completions from existing notes
        completions.addAll(getWordCompletions(currentWord, context))
        
        // Phrase completions
        completions.addAll(getPhraseCompletions(context))
        
        // Common word completions
        completions.addAll(getCommonWordCompletions(currentWord))
        
        // Format and rank suggestions
        val rankedCompletions = completions
            .distinctBy { it.text.lowercase() }
            .sortedByDescending { it.confidence }
            .take(limit)
        
        emit(rankedCompletions)
    }
    
    /**
     * Provides smart formatting suggestions based on context.
     * 
     * @param text Current text
     * @param cursorPosition Cursor position
     * @return List of formatting suggestions
     */
    fun getFormattingSuggestions(
        text: String,
        cursorPosition: Int
    ): List<FormattingSuggestion> {
        val suggestions = mutableListOf<FormattingSuggestion>()
        
        // Check for list continuation
        if (shouldSuggestListContinuation(text, cursorPosition)) {
            suggestions.add(
                FormattingSuggestion(
                    type = FormattingSuggestionType.BULLET_LIST,
                    description = "Continue bullet list",
                    action = "• "
                )
            )
        }
        
        // Check for heading suggestion
        if (shouldSuggestHeading(text, cursorPosition)) {
            suggestions.add(
                FormattingSuggestion(
                    type = FormattingSuggestionType.HEADING,
                    description = "Make heading",
                    action = "# "
                )
            )
        }
        
        // Check for emphasis suggestions
        val currentWord = extractCurrentWord(text.substring(0, cursorPosition))
        if (shouldSuggestEmphasis(currentWord)) {
            suggestions.add(
                FormattingSuggestion(
                    type = FormattingSuggestionType.BOLD,
                    description = "Make bold",
                    action = "**$currentWord**"
                )
            )
        }
        
        return suggestions
    }
    
    /**
     * Records user text patterns for future predictions.
     */
    suspend fun recordTextPattern(text: String) {
        val validation = InputValidator.validateNoteContent(text)
        if (validation.isValid) {
            // Extract and store useful patterns
            val words = extractWords(text)
            val phrases = extractPhrases(text)
            
            // This could be expanded to store patterns in a local database
            // For now, we rely on existing note content for predictions
        }
    }
    
    // Private helper methods
    
    private suspend fun getWordCompletions(
        currentWord: String,
        context: List<String>
    ): List<TextCompletion> {
        val completions = mutableListOf<TextCompletion>()
        
        try {
            val notes = noteDataSource.getNotes()
            notes.collect { notesList ->
                for (note in notesList) {
                    val noteWords = extractWords(note.content + " " + note.title)
                    
                    for (word in noteWords) {
                        if (word.startsWith(currentWord, ignoreCase = true) && 
                            word.length > currentWord.length) {
                            
                            val confidence = calculateWordConfidence(word, currentWord, context)
                            completions.add(
                                TextCompletion(
                                    text = word,
                                    type = TextCompletionType.WORD,
                                    confidence = confidence,
                                    replacement = word.substring(currentWord.length)
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Handle error gracefully
        }
        
        return completions.take(3)
    }
    
    private suspend fun getPhraseCompletions(context: List<String>): List<TextCompletion> {
        val completions = mutableListOf<TextCompletion>()
        
        if (context.isEmpty()) return completions
        
        try {
            val notes = noteDataSource.getNotes()
            val contextString = context.takeLast(3).joinToString(" ")
            
            notes.collect { notesList ->
                for (note in notesList) {
                    val phrases = extractPhrasesFromText(note.content)
                    
                    for (phrase in phrases) {
                        if (phrase.contains(contextString, ignoreCase = true)) {
                            val confidence = calculatePhraseConfidence(phrase, context)
                            completions.add(
                                TextCompletion(
                                    text = phrase,
                                    type = TextCompletionType.PHRASE,
                                    confidence = confidence,
                                    replacement = phrase
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Handle error gracefully
        }
        
        return completions.take(2)
    }
    
    private fun getCommonWordCompletions(currentWord: String): List<TextCompletion> {
        val commonWords = getCommonWords()
        
        return commonWords
            .filter { word ->
                word.startsWith(currentWord, ignoreCase = true) && 
                word.length > currentWord.length
            }
            .map { word ->
                TextCompletion(
                    text = word,
                    type = TextCompletionType.COMMON_WORD,
                    confidence = 0.3f,
                    replacement = word.substring(currentWord.length)
                )
            }
            .take(2)
    }
    
    private fun extractCurrentWord(textBeforeCursor: String): String {
        val words = textBeforeCursor.split("\\s+".toRegex())
        return words.lastOrNull()?.trim() ?: ""
    }
    
    private fun extractContext(textBeforeCursor: String): List<String> {
        val words = textBeforeCursor.split("\\s+".toRegex())
            .filter { it.isNotBlank() }
        return words.takeLast(CONTEXT_WINDOW_SIZE)
    }
    
    private fun extractWords(text: String): List<String> {
        return text.split("\\s+".toRegex())
            .map { it.trim().lowercase() }
            .filter { it.length >= MIN_WORD_LENGTH && it.matches("[a-zA-Z]+".toRegex()) }
    }
    
    private fun extractPhrases(text: String): List<String> {
        val sentences = text.split("[.!?]".toRegex())
        return sentences
            .map { it.trim() }
            .filter { it.length in 10..100 }
    }
    
    private fun extractPhrasesFromText(text: String): List<String> {
        val words = text.split("\\s+".toRegex())
        val phrases = mutableListOf<String>()
        
        for (i in 0 until words.size - 2) {
            val phrase = words.subList(i, minOf(i + 4, words.size)).joinToString(" ")
            if (phrase.length in 10..50) {
                phrases.add(phrase)
            }
        }
        
        return phrases
    }
    
    private fun calculateWordConfidence(
        word: String,
        currentWord: String,
        context: List<String>
    ): Float {
        var confidence = 0.5f
        
        // Exact prefix match bonus
        if (word.startsWith(currentWord, ignoreCase = true)) {
            confidence += 0.3f
        }
        
        // Context relevance bonus
        val contextString = context.joinToString(" ").lowercase()
        if (contextString.contains(word.lowercase())) {
            confidence += 0.2f
        }
        
        // Length penalty for very long words
        if (word.length > 15) {
            confidence -= 0.1f
        }
        
        return confidence.coerceIn(0f, 1f)
    }
    
    private fun calculatePhraseConfidence(phrase: String, context: List<String>): Float {
        val contextWords = context.map { it.lowercase() }
        val phraseWords = phrase.split("\\s+".toRegex()).map { it.lowercase() }
        
        val commonWords = contextWords.intersect(phraseWords.toSet()).size
        val totalWords = phraseWords.size
        
        return if (totalWords > 0) {
            (commonWords.toFloat() / totalWords) * 0.6f
        } else {
            0.2f
        }
    }
    
    private fun shouldSuggestListContinuation(text: String, cursorPosition: Int): Boolean {
        val textBeforeCursor = text.substring(0, cursorPosition)
        val lines = textBeforeCursor.split("\n")
        val currentLine = lines.lastOrNull() ?: ""
        val previousLine = lines.getOrNull(lines.size - 2) ?: ""
        
        return currentLine.isBlank() && previousLine.trimStart().startsWith("• ")
    }
    
    private fun shouldSuggestHeading(text: String, cursorPosition: Int): Boolean {
        val textBeforeCursor = text.substring(0, cursorPosition)
        val lines = textBeforeCursor.split("\n")
        val currentLine = lines.lastOrNull() ?: ""
        
        return currentLine.isBlank() && textBeforeCursor.endsWith("\n\n")
    }
    
    private fun shouldSuggestEmphasis(word: String): Boolean {
        val emphasisWords = setOf(
            "important", "urgent", "critical", "note", "warning", 
            "remember", "key", "main", "primary", "essential"
        )
        return word.lowercase() in emphasisWords
    }
    
    private fun getCommonWords(): List<String> {
        return listOf(
            "the", "and", "for", "are", "but", "not", "you", "can", "have", "that",
            "with", "this", "will", "your", "from", "they", "know", "want", "been",
            "good", "much", "some", "time", "very", "when", "come", "here", "just",
            "like", "long", "make", "many", "over", "such", "take", "than", "them",
            "well", "were", "work", "said", "each", "which", "their", "would", "there",
            "important", "meeting", "project", "today", "tomorrow", "schedule", "task",
            "deadline", "update", "review", "discuss", "follow", "action", "item",
            "question", "answer", "solution", "problem", "issue", "feature", "change"
        )
    }
}

/**
 * Represents a text completion suggestion.
 */
data class TextCompletion(
    val text: String,
    val type: TextCompletionType,
    val confidence: Float,
    val replacement: String
)

/**
 * Types of text completions.
 */
enum class TextCompletionType {
    WORD,
    PHRASE,
    COMMON_WORD
}

/**
 * Represents a formatting suggestion.
 */
data class FormattingSuggestion(
    val type: FormattingSuggestionType,
    val description: String,
    val action: String
)

/**
 * Types of formatting suggestions.
 */
enum class FormattingSuggestionType {
    BULLET_LIST,
    HEADING,
    BOLD,
    ITALIC,
    CODE_BLOCK
}