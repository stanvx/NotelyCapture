package com.module.notelycompose.onboarding.presentation

import com.module.notelycompose.modelDownloader.ModelAvailabilityService
import com.module.notelycompose.modelDownloader.ModelStatus
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.onboarding.presentation.model.OnboardingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for OnboardingViewModel state management and business logic
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private lateinit var viewModel: OnboardingViewModel
    private lateinit var mockPreferencesRepository: MockPreferencesRepository
    private lateinit var mockModelAvailabilityService: MockModelAvailabilityService
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockPreferencesRepository = MockPreferencesRepository()
        mockModelAvailabilityService = MockModelAvailabilityService()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): OnboardingViewModel {
        return OnboardingViewModel(
            preferencesRepository = mockPreferencesRepository,
            modelAvailabilityService = mockModelAvailabilityService
        )
    }

    @Test
    fun `initial state when onboarding not completed`() = runTest {
        mockPreferencesRepository.hasCompletedOnboardingResult = false
        mockPreferencesRepository.hasCompletedModelSetupResult = false

        viewModel = createViewModel()

        assertEquals(OnboardingState.NotCompleted, viewModel.onboardingState.first())
    }

    @Test
    fun `initial state when onboarding completed but model setup not completed and model not ready`() = runTest {
        mockPreferencesRepository.hasCompletedOnboardingResult = true
        mockPreferencesRepository.hasCompletedModelSetupResult = false
        mockModelAvailabilityService.modelStatus = ModelStatus.NotAvailable

        viewModel = createViewModel()

        assertEquals(OnboardingState.SettingUpModel, viewModel.onboardingState.first())
    }

    @Test
    fun `initial state when onboarding completed but model setup not completed and model ready`() = runTest {
        mockPreferencesRepository.hasCompletedOnboardingResult = true
        mockPreferencesRepository.hasCompletedModelSetupResult = false
        mockModelAvailabilityService.modelStatus = ModelStatus.Ready

        viewModel = createViewModel()

        assertEquals(OnboardingState.Completed, viewModel.onboardingState.first())
        assertEquals(true, mockModelAvailabilityService.markModelSetupCompletedCalled)
    }

    @Test
    fun `initial state when both onboarding and model setup completed`() = runTest {
        mockPreferencesRepository.hasCompletedOnboardingResult = true
        mockPreferencesRepository.hasCompletedModelSetupResult = true

        viewModel = createViewModel()

        assertEquals(OnboardingState.Completed, viewModel.onboardingState.first())
    }

    @Test
    fun `onCompleteOnboarding with model ready transitions to completed`() = runTest {
        mockPreferencesRepository.hasCompletedOnboardingResult = false
        mockModelAvailabilityService.modelStatus = ModelStatus.Ready

        viewModel = createViewModel()
        viewModel.onCompleteOnboarding()

        assertEquals(OnboardingState.Completed, viewModel.onboardingState.first())
        assertEquals(true, mockPreferencesRepository.setOnboardingCompletedCalled)
    }

    @Test
    fun `onCompleteOnboarding with model available but not setup completed`() = runTest {
        mockPreferencesRepository.hasCompletedOnboardingResult = false
        mockModelAvailabilityService.modelStatus = ModelStatus.Available

        viewModel = createViewModel()
        viewModel.onCompleteOnboarding()

        assertEquals(OnboardingState.Completed, viewModel.onboardingState.first())
        assertEquals(true, mockModelAvailabilityService.markModelSetupCompletedCalled)
    }

    @Test
    fun `onCompleteOnboarding with model not available transitions to setting up model`() = runTest {
        mockPreferencesRepository.hasCompletedOnboardingResult = false
        mockModelAvailabilityService.modelStatus = ModelStatus.NotAvailable

        viewModel = createViewModel()
        viewModel.onCompleteOnboarding()

        assertEquals(OnboardingState.SettingUpModel, viewModel.onboardingState.first())
    }

    @Test
    fun `onModelSetupCompleted transitions to completed state`() = runTest {
        mockPreferencesRepository.hasCompletedOnboardingResult = true
        mockPreferencesRepository.hasCompletedModelSetupResult = false

        viewModel = createViewModel()
        viewModel.onModelSetupCompleted()

        assertEquals(OnboardingState.Completed, viewModel.onboardingState.first())
        assertEquals(true, mockModelAvailabilityService.markModelSetupCompletedCalled)
    }

    @Test
    fun `onModelSetupError keeps state as setting up model`() = runTest {
        mockPreferencesRepository.hasCompletedOnboardingResult = true
        mockPreferencesRepository.hasCompletedModelSetupResult = false

        viewModel = createViewModel()
        viewModel.onModelSetupError("Network error")

        assertEquals(OnboardingState.SettingUpModel, viewModel.onboardingState.first())
    }

    @Test
    fun `retryModelSetup resets state to setting up model`() = runTest {
        mockPreferencesRepository.hasCompletedOnboardingResult = true
        mockPreferencesRepository.hasCompletedModelSetupResult = true

        viewModel = createViewModel()
        viewModel.retryModelSetup()

        assertEquals(OnboardingState.SettingUpModel, viewModel.onboardingState.first())
    }

    @Test
    fun `error in checkOnboardingStatus falls back to not completed`() = runTest {
        mockPreferencesRepository.shouldThrowError = true

        viewModel = createViewModel()

        assertEquals(OnboardingState.NotCompleted, viewModel.onboardingState.first())
    }

    @Test
    fun `error in onCompleteOnboarding falls back to setting up model`() = runTest {
        mockPreferencesRepository.shouldThrowError = true

        viewModel = createViewModel()
        viewModel.onCompleteOnboarding()

        assertEquals(OnboardingState.SettingUpModel, viewModel.onboardingState.first())
    }
}

// Mock implementations
private class MockPreferencesRepository : PreferencesRepository {
    var hasCompletedOnboardingResult = false
    var hasCompletedModelSetupResult = false
    var setOnboardingCompletedCalled = false
    var shouldThrowError = false

    override suspend fun hasCompletedOnboarding(): Boolean {
        if (shouldThrowError) throw Exception("Test error")
        return hasCompletedOnboardingResult
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        if (shouldThrowError) throw Exception("Test error")
        setOnboardingCompletedCalled = true
    }

    override suspend fun hasCompletedModelSetup(): Boolean {
        if (shouldThrowError) throw Exception("Test error")
        return hasCompletedModelSetupResult
    }

    override suspend fun setModelSetupCompleted(completed: Boolean) {
        // Not used in the tests but required by interface
    }

    override suspend fun getPlaybackSpeed(): Float = 1.0f
    override suspend fun setPlaybackSpeed(speed: Float) {}
}

private class MockModelAvailabilityService : ModelAvailabilityService {
    var modelStatus: ModelStatus = ModelStatus.NotAvailable
    var markModelSetupCompletedCalled = false

    override suspend fun checkModelAvailability(): ModelStatus {
        return modelStatus
    }

    override suspend fun markModelSetupCompleted() {
        markModelSetupCompletedCalled = true
    }
}