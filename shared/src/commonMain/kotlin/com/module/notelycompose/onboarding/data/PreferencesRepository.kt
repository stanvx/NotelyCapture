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

class PreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_MODEL_DOWNLOAD_ID= longPreferencesKey("model_download_id")
        private val KEY_PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        
        // Playback speed validation constants
        val VALID_PLAYBACK_SPEEDS = setOf(1.0f, 1.5f, 2.0f)
        const val DEFAULT_PLAYBACK_SPEED = 1.0f
    }

    suspend fun hasCompletedOnboarding(): Boolean {
        return dataStore.data.first()[KEY_ONBOARDING_COMPLETED] ?: false
    }


    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setDefaultTranscriptionLanguage(language: String) {
        dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language
        }
    }

    suspend fun setTheme(theme: String) {
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
}

