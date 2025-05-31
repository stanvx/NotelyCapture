package com.module.notelycompose.onboarding.data

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.SuspendSettings
import com.russhwolf.settings.coroutines.toSuspendSettings

class PreferencesRepository(settings: Settings) {
    @OptIn(ExperimentalSettingsApi::class)
    private val suspendSettings: SuspendSettings = settings.toSuspendSettings()

    @OptIn(ExperimentalSettingsApi::class)
    suspend fun hasCompletedOnboarding(): Boolean {
        return suspendSettings.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    @OptIn(ExperimentalSettingsApi::class)
    suspend fun setOnboardingCompleted(completed: Boolean) {
        suspendSettings.putBoolean(KEY_ONBOARDING_COMPLETED, completed)
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
