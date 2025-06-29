package com.module.notelycompose.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel(){
    private val _onboardingState = MutableStateFlow<OnboardingState>(OnboardingState.Completed)
    val onboardingState: StateFlow<OnboardingState> = _onboardingState.asStateFlow()

    init {
        checkOnboardingStatus()
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            try {
                val hasCompleted = preferencesRepository.hasCompletedOnboarding()
                _onboardingState.value = if (hasCompleted) {
                    OnboardingState.Completed
                } else {
                    OnboardingState.NotCompleted
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
                _onboardingState.value = OnboardingState.Completed
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }
}
