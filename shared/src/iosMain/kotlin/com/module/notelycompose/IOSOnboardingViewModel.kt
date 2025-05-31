package com.module.notelycompose

import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.onboarding.presentation.OnboardingViewModel

class IOSOnboardingViewModel (
    private val preferencesRepository: PreferencesRepository
) {
    private val viewModel by lazy {
        OnboardingViewModel(
            preferencesRepository = preferencesRepository,
            coroutineScope = null
        )
    }
    val state = viewModel.onboardingState

    fun onCompleteOnboarding() {
        viewModel.onCompleteOnboarding()
    }
}
