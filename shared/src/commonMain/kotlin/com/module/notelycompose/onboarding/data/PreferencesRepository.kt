package com.module.notelycompose.onboarding.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.module.notelycompose.notes.ui.settings.languageCodeMap
import com.module.notelycompose.platform.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.module.notelycompose.core.validation.InputValidator

class PreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_MODEL_SETUP_COMPLETED = booleanPreferencesKey("model_setup_completed")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_ACCENT_COLOR = stringPreferencesKey("accent_color")
        private val KEY_MODEL_DOWNLOAD_ID= longPreferencesKey("model_download_id")
        private val KEY_PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        
        // Auto-complete preferences
        private val KEY_AUTOCOMPLETE_ENABLED = booleanPreferencesKey("autocomplete_enabled")
        private val KEY_SEARCH_SUGGESTIONS_ENABLED = booleanPreferencesKey("search_suggestions_enabled")
        private val KEY_TEXT_PREDICTION_ENABLED = booleanPreferencesKey("text_prediction_enabled")
        private val KEY_LANGUAGE_AWARE_AUTOCOMPLETE = booleanPreferencesKey("language_aware_autocomplete")
        private val KEY_FORMATTING_SUGGESTIONS_ENABLED = booleanPreferencesKey("formatting_suggestions_enabled")
        private val KEY_AUTOCOMPLETE_DELAY = longPreferencesKey("autocomplete_delay_ms")
        
        // Playback speed validation constants
        val VALID_PLAYBACK_SPEEDS = setOf(1.0f, 1.5f, 2.0f)
        const val DEFAULT_PLAYBACK_SPEED = 1.0f
        
        // Auto-complete defaults
        const val DEFAULT_AUTOCOMPLETE_ENABLED = true
        const val DEFAULT_SEARCH_SUGGESTIONS_ENABLED = true
        const val DEFAULT_TEXT_PREDICTION_ENABLED = true
        const val DEFAULT_LANGUAGE_AWARE_AUTOCOMPLETE = true
        const val DEFAULT_FORMATTING_SUGGESTIONS_ENABLED = true
        const val DEFAULT_AUTOCOMPLETE_DELAY_MS = 300L
        
        // Accent color validation constants
        val VALID_ACCENT_COLORS = setOf(
            "Material Red",
            "Material Green", 
            "Material Blue",
            "Material Purple",
            "Material Orange",
            "Material Teal"
        )
        const val DEFAULT_ACCENT_COLOR = "Material Blue"
    }

    suspend fun hasCompletedOnboarding(): Boolean {
        return dataStore.data.first()[KEY_ONBOARDING_COMPLETED] ?: false
    }


    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun hasCompletedModelSetup(): Boolean {
        return dataStore.data.first()[KEY_MODEL_SETUP_COMPLETED] ?: false
    }

    suspend fun setModelSetupCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_MODEL_SETUP_COMPLETED] = completed
        }
    }

    suspend fun setDefaultTranscriptionLanguage(language: String) {
        // Validate language code
        val validation = InputValidator.validateLanguage(language)
        if (!validation.isValid) {
            throw IllegalArgumentException("Invalid language: ${validation.errorMessage}")
        }
        
        dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language
        }
    }

    suspend fun setTheme(theme: String) {
        // Validate theme value
        val validation = InputValidator.validateTheme(theme)
        if (!validation.isValid) {
            throw IllegalArgumentException("Invalid theme: ${validation.errorMessage}")
        }
        
        dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme
        }
    }

     fun getDefaultTranscriptionLanguage(): Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: languageCodeMap.entries.first().key
    }


    fun getTheme(): Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_THEME]?:Theme.SYSTEM.name
    }

    fun getModelDownloadId(): Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_MODEL_DOWNLOAD_ID]?:-1
    }
    suspend fun setModelDownloadId(downloadId: Long) {
        dataStore.edit { prefs ->
            prefs[KEY_MODEL_DOWNLOAD_ID] = downloadId
        }
    }

    fun getPlaybackSpeed(): Flow<Float> = dataStore.data.map { prefs ->
        prefs[KEY_PLAYBACK_SPEED] ?: DEFAULT_PLAYBACK_SPEED
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        // Validate speed value
        when {
            !speed.isFinite() -> throw IllegalArgumentException(
                "Invalid playback speed: $speed. Speed must be a finite number."
            )
            speed !in VALID_PLAYBACK_SPEEDS -> throw IllegalArgumentException(
                "Invalid playback speed: $speed. Valid speeds: $VALID_PLAYBACK_SPEEDS"
            )
        }
        
        dataStore.edit { prefs ->
            prefs[KEY_PLAYBACK_SPEED] = speed
        }
    }

    fun getAccentColor(): Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_ACCENT_COLOR] ?: DEFAULT_ACCENT_COLOR
    }

    suspend fun setAccentColor(accentColor: String) {
        // Validate accent color
        if (accentColor !in VALID_ACCENT_COLORS) {
            throw IllegalArgumentException(
                "Invalid accent color: $accentColor. Valid colors: $VALID_ACCENT_COLORS"
            )
        }
        
        dataStore.edit { prefs ->
            prefs[KEY_ACCENT_COLOR] = accentColor
        }
    }
    
    // Auto-complete preference methods
    
    fun isAutoCompleteEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_AUTOCOMPLETE_ENABLED] ?: DEFAULT_AUTOCOMPLETE_ENABLED
    }
    
    suspend fun setAutoCompleteEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_AUTOCOMPLETE_ENABLED] = enabled
        }
    }
    
    fun isSearchSuggestionsEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_SEARCH_SUGGESTIONS_ENABLED] ?: DEFAULT_SEARCH_SUGGESTIONS_ENABLED
    }
    
    suspend fun setSearchSuggestionsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_SEARCH_SUGGESTIONS_ENABLED] = enabled
        }
    }
    
    fun isTextPredictionEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_TEXT_PREDICTION_ENABLED] ?: DEFAULT_TEXT_PREDICTION_ENABLED
    }
    
    suspend fun setTextPredictionEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_TEXT_PREDICTION_ENABLED] = enabled
        }
    }
    
    fun isLanguageAwareAutoCompleteEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE_AWARE_AUTOCOMPLETE] ?: DEFAULT_LANGUAGE_AWARE_AUTOCOMPLETE
    }
    
    suspend fun setLanguageAwareAutoCompleteEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE_AWARE_AUTOCOMPLETE] = enabled
        }
    }
    
    fun isFormattingSuggestionsEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_FORMATTING_SUGGESTIONS_ENABLED] ?: DEFAULT_FORMATTING_SUGGESTIONS_ENABLED
    }
    
    suspend fun setFormattingSuggestionsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_FORMATTING_SUGGESTIONS_ENABLED] = enabled
        }
    }
    
    fun getAutoCompleteDelay(): Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_AUTOCOMPLETE_DELAY] ?: DEFAULT_AUTOCOMPLETE_DELAY_MS
    }
    
    suspend fun setAutoCompleteDelay(delayMs: Long) {
        // Validate delay value
        when {
            delayMs < 0 -> throw IllegalArgumentException(
                "Invalid autocomplete delay: $delayMs. Delay must be non-negative."
            )
            delayMs > 2000 -> throw IllegalArgumentException(
                "Invalid autocomplete delay: $delayMs. Delay must be less than 2000ms."
            )
        }
        
        dataStore.edit { prefs ->
            prefs[KEY_AUTOCOMPLETE_DELAY] = delayMs
        }
    }
    
    /**
     * Gets comprehensive auto-complete settings as a single object.
     */
    suspend fun getAutoCompleteSettings(): AutoCompleteSettings {
        val prefs = dataStore.data.first()
        return AutoCompleteSettings(
            enabled = prefs[KEY_AUTOCOMPLETE_ENABLED] ?: DEFAULT_AUTOCOMPLETE_ENABLED,
            searchSuggestionsEnabled = prefs[KEY_SEARCH_SUGGESTIONS_ENABLED] ?: DEFAULT_SEARCH_SUGGESTIONS_ENABLED,
            textPredictionEnabled = prefs[KEY_TEXT_PREDICTION_ENABLED] ?: DEFAULT_TEXT_PREDICTION_ENABLED,
            languageAwareEnabled = prefs[KEY_LANGUAGE_AWARE_AUTOCOMPLETE] ?: DEFAULT_LANGUAGE_AWARE_AUTOCOMPLETE,
            formattingSuggestionsEnabled = prefs[KEY_FORMATTING_SUGGESTIONS_ENABLED] ?: DEFAULT_FORMATTING_SUGGESTIONS_ENABLED,
            delayMs = prefs[KEY_AUTOCOMPLETE_DELAY] ?: DEFAULT_AUTOCOMPLETE_DELAY_MS
        )
    }
}

/**
 * Data class representing auto-complete settings.
 */
data class AutoCompleteSettings(
    val enabled: Boolean,
    val searchSuggestionsEnabled: Boolean,
    val textPredictionEnabled: Boolean,
    val languageAwareEnabled: Boolean,
    val formattingSuggestionsEnabled: Boolean,
    val delayMs: Long
)

