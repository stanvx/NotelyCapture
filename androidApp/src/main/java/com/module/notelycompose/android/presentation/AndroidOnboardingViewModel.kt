package com.module.notelycompose.android.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.onboarding.presentation.OnboardingViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidOnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val viewModel by lazy {
        OnboardingViewModel(
            preferencesRepository = preferencesRepository,
            coroutineScope = viewModelScope
        )
    }
    val state = viewModel.onboardingState

    fun onCompleteOnboarding() {
        viewModel.onCompleteOnboarding()
    }
}
