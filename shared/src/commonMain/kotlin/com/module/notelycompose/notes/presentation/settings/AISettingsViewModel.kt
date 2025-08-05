package com.module.notelycompose.notes.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.core.security.AiSettingsRepository
import com.module.notelycompose.core.security.AiSettingsUiState
import com.module.notelycompose.core.security.SecurityHelper
import com.module.notelycompose.core.security.SecurityMonitoringService
import com.module.notelycompose.core.security.SecureStorageException
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel for managing AI settings UI state and user interactions.
 * Handles API key management, validation, and secure storage operations.
 */
class AISettingsViewModel(
    private val aiSettingsRepository: AiSettingsRepository,
    private val securityHelper: SecurityHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiSettingsUiState())
    val uiState: StateFlow<AiSettingsUiState> = _uiState.asStateFlow()

    private val _currentApiKey = MutableStateFlow("")
    val currentApiKey: StateFlow<String> = _currentApiKey.asStateFlow()

    // Security context for monitoring
    private val sessionContext = SecurityMonitoringService.UserContext(
        sessionId = "ai_settings_${System.currentTimeMillis()}",
        userAgent = "NotelyCapture/Android"
    )

    init {
        loadInitialState()
        observeApiKeyChanges()
    }

    /**
     * Handles user intents for AI settings management.
     */
    fun handleIntent(intent: AISettingsIntent) {
        when (intent) {
            is AISettingsIntent.UpdateApiKey -> updateApiKey(intent.apiKey)
            is AISettingsIntent.SaveApiKey -> saveApiKey()
            is AISettingsIntent.RemoveApiKey -> removeApiKey()
            is AISettingsIntent.ToggleApiKeyVisibility -> toggleApiKeyVisibility()
            is AISettingsIntent.ValidateApiKey -> validateCurrentApiKey(showErrors = intent.showErrors)
            is AISettingsIntent.ClearErrors -> clearErrors()
        }
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            try {
                val hasApiKey = aiSettingsRepository.hasOpenAiApiKey()
                val apiKey = if (hasApiKey) {
                    aiSettingsRepository.getOpenAiApiKey() ?: ""
                } else {
                    ""
                }

                _uiState.value = _uiState.value.copy(
                    hasApiKey = hasApiKey
                )
                _currentApiKey.value = apiKey

            } catch (e: Exception) {
                handleError("Failed to load AI settings", e)
            }
        }
    }

    private fun observeApiKeyChanges() {
        viewModelScope.launch {
            combine(
                _currentApiKey,
                _uiState
            ) { apiKey, uiState ->
                if (apiKey.isNotBlank() && !uiState.isValidating) {
                    validateCurrentApiKey(showErrors = false)
                }
            }
        }
    }

    private fun updateApiKey(apiKey: String) {
        _currentApiKey.value = apiKey
        if (apiKey.isBlank()) {
            _uiState.value = _uiState.value.copy(
                validationError = null,
                saveError = null
            )
        }
    }

    private fun saveApiKey() {
        val apiKey = _currentApiKey.value.trim()
        if (apiKey.isBlank()) {
            _uiState.value = _uiState.value.copy(
                saveError = "API key cannot be empty"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isSaving = true,
                    saveError = null,
                    validationError = null
                )

                // Validate the API key first
                val validation = securityHelper.validateOpenAiApiKey(
                    apiKey = apiKey,
                    userContext = sessionContext
                )

                if (!validation.isValid) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        validationError = validation.errorMessage
                    )
                    return@launch
                }

                // Store the API key securely
                aiSettingsRepository.storeOpenAiApiKey(
                    apiKey = apiKey,
                    userContext = sessionContext
                )

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    hasApiKey = true,
                    saveError = null,
                    validationError = null
                )

                securityHelper.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.SUSPICIOUS_ACTIVITY,
                    severity = SecurityMonitoringService.SecuritySeverity.LOW,
                    message = "User saved AI API key",
                    details = mapOf(
                        "action" to "save_api_key",
                        "success" to "true"
                    ),
                    userContext = sessionContext
                )

                Napier.i("AI API key saved successfully")

            } catch (e: IllegalArgumentException) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    validationError = e.message ?: "Invalid API key format"
                )
                Napier.w("Invalid API key provided: ${e.message}")

            } catch (e: SecureStorageException) {
                handleError("Failed to save API key securely", e)

            } catch (e: Exception) {
                handleError("An unexpected error occurred while saving", e)
            }
        }
    }

    private fun removeApiKey() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true)

                aiSettingsRepository.removeOpenAiApiKey(userContext = sessionContext)
                _currentApiKey.value = ""

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    hasApiKey = false,
                    saveError = null,
                    validationError = null,
                    showApiKey = false
                )

                securityHelper.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.SUSPICIOUS_ACTIVITY,
                    severity = SecurityMonitoringService.SecuritySeverity.LOW,
                    message = "User removed AI API key",
                    details = mapOf(
                        "action" to "remove_api_key",
                        "success" to "true"
                    ),
                    userContext = sessionContext
                )

                Napier.i("AI API key removed successfully")

            } catch (e: Exception) {
                handleError("Failed to remove API key", e)
            }
        }
    }

    fun toggleApiKeyVisibility() {
        _uiState.value = _uiState.value.copy(
            showApiKey = !_uiState.value.showApiKey
        )

        viewModelScope.launch {
            securityHelper.reportSecurityEvent(
                type = SecurityMonitoringService.SecurityEventType.SUSPICIOUS_ACTIVITY,
                severity = SecurityMonitoringService.SecuritySeverity.LOW,
                message = "User toggled API key visibility",
                details = mapOf(
                    "action" to "toggle_visibility",
                    "visible" to _uiState.value.showApiKey.toString()
                ),
                userContext = sessionContext
            )
        }
    }

    private fun validateCurrentApiKey(showErrors: Boolean = true) {
        val apiKey = _currentApiKey.value.trim()
        if (apiKey.isEmpty()) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isValidating = true)

                val validation = securityHelper.validateOpenAiApiKey(
                    apiKey = apiKey,
                    userContext = sessionContext
                )

                _uiState.value = _uiState.value.copy(
                    isValidating = false,
                    validationError = if (showErrors && !validation.isValid) {
                        validation.errorMessage
                    } else null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isValidating = false,
                    validationError = if (showErrors) "Validation failed" else null
                )
                Napier.w("API key validation error: ${e.message}")
            }
        }
    }

    private fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            validationError = null,
            saveError = null
        )
    }

    private fun handleError(message: String, exception: Exception) {
        _uiState.value = _uiState.value.copy(
            isSaving = false,
            isValidating = false,
            saveError = message
        )

        viewModelScope.launch {
            securityHelper.reportSecurityEvent(
                type = SecurityMonitoringService.SecurityEventType.RESOURCE_EXHAUSTION,
                severity = SecurityMonitoringService.SecuritySeverity.MEDIUM,
                message = message,
                details = mapOf(
                    "error" to (exception.message ?: "Unknown error"),
                    "error_type" to exception.javaClass.simpleName
                ),
                userContext = sessionContext,
                throwable = exception
            )
        }

        Napier.e("AI Settings error: $message", exception)
    }

    override fun onCleared() {
        super.onCleared()
        Napier.d("AISettingsViewModel cleared")
    }
}

/**
 * Sealed interface representing user intents for AI settings.
 */
sealed interface AISettingsIntent {
    data class UpdateApiKey(val apiKey: String) : AISettingsIntent
    data object SaveApiKey : AISettingsIntent
    data object RemoveApiKey : AISettingsIntent
    data object ToggleApiKeyVisibility : AISettingsIntent
    data class ValidateApiKey(val showErrors: Boolean = true) : AISettingsIntent
    data object ClearErrors : AISettingsIntent
}
