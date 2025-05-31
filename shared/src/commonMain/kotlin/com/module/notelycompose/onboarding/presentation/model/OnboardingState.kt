package com.module.notelycompose.onboarding.presentation.model

sealed class OnboardingState {
    object Initial : OnboardingState()
    object NotCompleted : OnboardingState()
    object Completed : OnboardingState()
}
