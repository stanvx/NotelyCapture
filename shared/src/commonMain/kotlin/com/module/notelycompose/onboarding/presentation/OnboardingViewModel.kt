package com.module.notelycompose.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.modelDownloader.ModelAvailabilityService
import com.module.notelycompose.modelDownloader.ModelStatus
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.onboarding.presentation.model.OnboardingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val modelAvailabilityService: ModelAvailabilityService,
) : ViewModel(){
    private val _onboardingState = MutableStateFlow<OnboardingState>(OnboardingState.Initial)
    val onboardingState: StateFlow<OnboardingState> = _onboardingState.asStateFlow()

    init {
        checkOnboardingStatus()
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            try {
                val hasCompletedOnboarding = preferencesRepository.hasCompletedOnboarding()
                val hasCompletedModelSetup = preferencesRepository.hasCompletedModelSetup()
                
                _onboardingState.value = when {
                    !hasCompletedOnboarding -> OnboardingState.NotCompleted
                    hasCompletedOnboarding && !hasCompletedModelSetup -> {
                        // Check if model is actually available
                        val modelStatus = modelAvailabilityService.checkModelAvailability()
                        when (modelStatus) {
                            ModelStatus.Ready, ModelStatus.Available -> {
                                // Model exists, just mark setup as complete
                                modelAvailabilityService.markModelSetupCompleted()
                                OnboardingState.Completed
                            }
                            else -> OnboardingState.SettingUpModel
                        }
                    }
                    else -> OnboardingState.Completed
                }
            } catch (e: Exception) {
                _onboardingState.value = OnboardingState.NotCompleted
            }
        }
    }

    fun onCompleteOnboarding() {
        viewModelScope.launch {
            try {
                preferencesRepository.setOnboardingCompleted(true)
                
                // Check if model setup is needed
                val modelStatus = modelAvailabilityService.checkModelAvailability()
                _onboardingState.value = when (modelStatus) {
                    ModelStatus.Ready -> OnboardingState.Completed
                    ModelStatus.Available -> {
                        // Model exists but setup not marked complete
                        modelAvailabilityService.markModelSetupCompleted()
                        OnboardingState.Completed
                    }
                    else -> OnboardingState.SettingUpModel
                }
            } catch (e: Exception) {
                // If there's an error, still try to proceed to model setup
                _onboardingState.value = OnboardingState.SettingUpModel
            }
        }
    }

    fun onModelSetupCompleted() {
        viewModelScope.launch {
            try {
                modelAvailabilityService.markModelSetupCompleted()
                _onboardingState.value = OnboardingState.Completed
            } catch (e: Exception) {
                // Handle error if needed - could retry or show error state
            }
        }
    }

    fun onModelSetupError(errorMessage: String) {
        viewModelScope.launch {
            // For now, we'll stay in the SettingUpModel state to allow retry
            // In the future, we could add an error state or show a dialog
            _onboardingState.value = OnboardingState.SettingUpModel
        }
    }

    fun retryModelSetup() {
        viewModelScope.launch {
            // Reset state to trigger model setup again
            _onboardingState.value = OnboardingState.SettingUpModel
        }
    }
}
