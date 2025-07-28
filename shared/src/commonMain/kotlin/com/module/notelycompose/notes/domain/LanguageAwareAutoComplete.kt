package com.module.notelycompose.notes.domain

import com.module.notelycompose.onboarding.data.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Provides language-aware auto-complete functionality based on user's selected transcription language.
 * 
 * Features:
 * - Language-specific word completions
 * - Common phrases in user's language
 * - Smart language detection from context
 * - Fallback to English for unsupported languages
 */
class LanguageAwareAutoComplete(
    private val preferencesRepository: PreferencesRepository
) {
    
    companion object {
        private const val MAX_LANGUAGE_SUGGESTIONS = 3
    }
    
    /**
     * Gets language-specific auto-complete suggestions.
     * 
     * @param query Current input text
     * @param context Surrounding text context
     * @return Flow of language-aware suggestions
     */
    fun getLanguageSpecificSuggestions(
        query: String,
        context: String = ""
    ): Flow<List<LanguageAwareSuggestion>> = flow {
        if (query.length < 2) {
            emit(emptyList())
            return@flow
        }
        
        // Get user's preferred language
        val languageCode = preferencesRepository.getDefaultTranscriptionLanguage().first()
        
        // Get suggestions for the user's language
        val suggestions = mutableListOf<LanguageAwareSuggestion>()
        
        // Add common words in user's language
        suggestions.addAll(getCommonWordsForLanguage(languageCode, query))
        
        // Add common phrases in user's language
        suggestions.addAll(getCommonPhrasesForLanguage(languageCode, query, context))
        
        // Add language-specific formatting suggestions
        suggestions.addAll(getLanguageSpecificFormatting(languageCode, query, context))
        
        // Sort by relevance and limit results
        val sortedSuggestions = suggestions
            .distinctBy { it.text.lowercase() }
            .sortedByDescending { it.confidence }
            .take(MAX_LANGUAGE_SUGGESTIONS)
        
        emit(sortedSuggestions)
    }
    
    /**
     * Gets common words for a specific language that match the query.
     */
    private fun getCommonWordsForLanguage(
        languageCode: String,
        query: String
    ): List<LanguageAwareSuggestion> {
        val commonWords = getCommonWordsByLanguage(languageCode)
        
        return commonWords
            .filter { word ->
                word.startsWith(query, ignoreCase = true) && 
                word.length > query.length
            }
            .map { word ->
                LanguageAwareSuggestion(
                    text = word,
                    type = LanguageSuggestionType.COMMON_WORD,
                    languageCode = languageCode,
                    confidence = calculateWordConfidence(word, query)
                )
            }
            .take(5)
    }
    
    /**
     * Gets common phrases for a specific language.
     */
    private fun getCommonPhrasesForLanguage(
        languageCode: String,
        query: String,
        context: String
    ): List<LanguageAwareSuggestion> {
        val commonPhrases = getCommonPhrasesByLanguage(languageCode)
        
        return commonPhrases
            .filter { phrase ->
                phrase.contains(query, ignoreCase = true) ||
                phrase.startsWith(query, ignoreCase = true)
            }
            .map { phrase ->
                LanguageAwareSuggestion(
                    text = phrase,
                    type = LanguageSuggestionType.COMMON_PHRASE,
                    languageCode = languageCode,
                    confidence = calculatePhraseConfidence(phrase, query, context)
                )
            }
            .take(3)
    }
    
    /**
     * Gets language-specific formatting suggestions.
     */
    private fun getLanguageSpecificFormatting(
        languageCode: String,
        query: String,
        context: String
    ): List<LanguageAwareSuggestion> {
        val suggestions = mutableListOf<LanguageAwareSuggestion>()
        
        // Language-specific punctuation suggestions
        when (languageCode) {
            "es", "ca" -> {
                // Spanish/Catalan inverted punctuation
                if (query.lowercase().startsWith("que") || context.contains("pregunta")) {
                    suggestions.add(
                        LanguageAwareSuggestion(
                            text = "¿$query?",
                            type = LanguageSuggestionType.PUNCTUATION,
                            languageCode = languageCode,
                            confidence = 0.8f
                        )
                    )
                }
            }
            "fr" -> {
                // French spacing rules
                if (query.endsWith(":") || query.endsWith("!") || query.endsWith("?")) {
                    suggestions.add(
                        LanguageAwareSuggestion(
                            text = query.dropLast(1) + " " + query.last(),
                            type = LanguageSuggestionType.PUNCTUATION,
                            languageCode = languageCode,
                            confidence = 0.7f
                        )
                    )
                }
            }
            "de" -> {
                // German capitalization suggestions
                if (isNoun(query, languageCode)) {
                    suggestions.add(
                        LanguageAwareSuggestion(
                            text = query.replaceFirstChar { it.uppercaseChar() },
                            type = LanguageSuggestionType.CAPITALIZATION,
                            languageCode = languageCode,
                            confidence = 0.6f
                        )
                    )
                }
            }
            "zh", "ja" -> {
                // CJK character suggestions (simplified)
                suggestions.addAll(getCJKSuggestions(query, languageCode))
            }
        }
        
        return suggestions
    }
    
    /**
     * Gets common words by language code.
     */
    private fun getCommonWordsByLanguage(languageCode: String): List<String> {
        return when (languageCode) {
            "es" -> listOf(
                "hola", "gracias", "por favor", "perdón", "disculpe", "sí", "no",
                "buenos días", "buenas tardes", "buenas noches", "hasta luego",
                "importante", "reunión", "proyecto", "hoy", "mañana", "trabajo",
                "tarea", "fecha límite", "actualización", "revisar", "discutir"
            )
            "fr" -> listOf(
                "bonjour", "merci", "s'il vous plaît", "pardon", "excusez-moi", "oui", "non",
                "bonsoir", "au revoir", "à bientôt", "important", "réunion",
                "projet", "aujourd'hui", "demain", "travail", "tâche", "échéance"
            )
            "de" -> listOf(
                "hallo", "danke", "bitte", "entschuldigung", "ja", "nein",
                "guten Tag", "auf Wiedersehen", "wichtig", "Besprechung",
                "Projekt", "heute", "morgen", "Arbeit", "Aufgabe", "Termin"
            )
            "it" -> listOf(
                "ciao", "grazie", "prego", "scusi", "sì", "no", "buongiorno",
                "buonasera", "arrivederci", "importante", "riunione", "progetto",
                "oggi", "domani", "lavoro", "compito", "scadenza"
            )
            "pt" -> listOf(
                "olá", "obrigado", "por favor", "desculpe", "sim", "não",
                "bom dia", "boa tarde", "boa noite", "tchau", "importante",
                "reunião", "projeto", "hoje", "amanhã", "trabalho", "tarefa"
            )
            "ru" -> listOf(
                "привет", "спасибо", "пожалуйста", "извините", "да", "нет",
                "добро пожаловать", "до свидания", "важно", "встреча",
                "проект", "сегодня", "завтра", "работа", "задача"
            )
            "zh" -> listOf(
                "你好", "谢谢", "请", "对不起", "是", "不是", "再见",
                "重要", "会议", "项目", "今天", "明天", "工作", "任务"
            )
            "ja" -> listOf(
                "こんにちは", "ありがとう", "お願いします", "すみません", "はい", "いいえ",
                "さようなら", "重要", "会議", "プロジェクト", "今日", "明日", "仕事", "タスク"
            )
            "ko" -> listOf(
                "안녕하세요", "감사합니다", "부탁합니다", "죄송합니다", "네", "아니요",
                "안녕히 가세요", "중요한", "회의", "프로젝트", "오늘", "내일", "일", "업무"
            )
            "ar" -> listOf(
                "مرحبا", "شكرا", "من فضلك", "آسف", "نعم", "لا", "مع السلامة",
                "مهم", "اجتماع", "مشروع", "اليوم", "غدا", "عمل", "مهمة"
            )
            else -> listOf(
                "hello", "thank you", "please", "sorry", "yes", "no", "goodbye",
                "important", "meeting", "project", "today", "tomorrow", "work", "task"
            )
        }
    }
    
    /**
     * Gets common phrases by language code.
     */
    private fun getCommonPhrasesByLanguage(languageCode: String): List<String> {
        return when (languageCode) {
            "es" -> listOf(
                "buenos días", "buenas tardes", "¿cómo estás?", "muy bien",
                "no hay problema", "hasta luego", "nos vemos", "que tengas un buen día",
                "muchas gracias", "de nada", "lo siento mucho"
            )
            "fr" -> listOf(
                "bonjour", "bonsoir", "comment allez-vous?", "très bien",
                "pas de problème", "à bientôt", "à plus tard", "bonne journée",
                "merci beaucoup", "de rien", "je suis désolé"
            )
            "de" -> listOf(
                "guten Tag", "guten Abend", "wie geht es Ihnen?", "sehr gut",
                "kein Problem", "bis bald", "bis später", "schönen Tag noch",
                "vielen Dank", "bitte schön", "es tut mir leid"
            )
            "it" -> listOf(
                "buongiorno", "buonasera", "come sta?", "molto bene",
                "nessun problema", "a presto", "ci vediamo", "buona giornata",
                "molte grazie", "prego", "mi dispiace"
            )
            "pt" -> listOf(
                "bom dia", "boa tarde", "como vai?", "muito bem",
                "sem problema", "até logo", "até mais", "tenha um bom dia",
                "muito obrigado", "de nada", "desculpe"
            )
            else -> listOf(
                "good morning", "good evening", "how are you?", "very well",
                "no problem", "see you later", "take care", "have a good day",
                "thank you very much", "you're welcome", "I'm sorry"
            )
        }
    }
    
    /**
     * Gets CJK-specific suggestions for Chinese, Japanese, Korean.
     */
    private fun getCJKSuggestions(query: String, languageCode: String): List<LanguageAwareSuggestion> {
        val suggestions = mutableListOf<LanguageAwareSuggestion>()
        
        // This is a simplified implementation
        // In a real app, you'd use a proper CJK input method library
        when (languageCode) {
            "zh" -> {
                if (query.lowercase().startsWith("ni")) {
                    suggestions.add(
                        LanguageAwareSuggestion(
                            text = "你好",
                            type = LanguageSuggestionType.CJK_INPUT,
                            languageCode = languageCode,
                            confidence = 0.9f
                        )
                    )
                }
            }
            "ja" -> {
                if (query.lowercase().startsWith("kon")) {
                    suggestions.add(
                        LanguageAwareSuggestion(
                            text = "こんにちは",
                            type = LanguageSuggestionType.CJK_INPUT,
                            languageCode = languageCode,
                            confidence = 0.9f
                        )
                    )
                }
            }
        }
        
        return suggestions
    }
    
    private fun calculateWordConfidence(word: String, query: String): Float {
        val lengthRatio = query.length.toFloat() / word.length
        val startsWithBonus = if (word.startsWith(query, ignoreCase = true)) 0.3f else 0.0f
        return (0.5f + lengthRatio * 0.2f + startsWithBonus).coerceIn(0f, 1f)
    }
    
    private fun calculatePhraseConfidence(phrase: String, query: String, context: String): Float {
        val containsQuery = if (phrase.contains(query, ignoreCase = true)) 0.4f else 0.0f
        val startsWithQuery = if (phrase.startsWith(query, ignoreCase = true)) 0.3f else 0.0f
        val contextRelevance = if (context.isNotEmpty() && phrase.contains(context.take(10), ignoreCase = true)) 0.2f else 0.0f
        return (containsQuery + startsWithQuery + contextRelevance).coerceIn(0f, 1f)
    }
    
    private fun isNoun(word: String, languageCode: String): Boolean {
        // Simplified noun detection for German
        // In a real implementation, you'd use proper NLP libraries
        return when (languageCode) {
            "de" -> {
                val germanNounIndicators = listOf("ung", "heit", "keit", "schaft", "tum")
                germanNounIndicators.any { word.lowercase().endsWith(it) }
            }
            else -> false
        }
    }
}

/**
 * Represents a language-aware auto-complete suggestion.
 */
data class LanguageAwareSuggestion(
    val text: String,
    val type: LanguageSuggestionType,
    val languageCode: String,
    val confidence: Float
)

/**
 * Types of language-aware suggestions.
 */
enum class LanguageSuggestionType {
    COMMON_WORD,
    COMMON_PHRASE,
    PUNCTUATION,
    CAPITALIZATION,
    CJK_INPUT
}