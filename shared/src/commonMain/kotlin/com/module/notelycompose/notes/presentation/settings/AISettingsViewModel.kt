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
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * ViewModel for AI Settings screen handling secure API key management.
 * Follows the established pattern of intent-based user actions and reactive state.
 */
class AISettingsViewModel(
    private val aiSettingsRepository: AiSettingsRepository,
    private val securityHelper: SecurityHelper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AiSettingsUiState())
    val uiState: StateFlow<AiSettingsUiState> = _uiState.asStateFlow()
    
    private val _currentApiKey = MutableStateFlow("")
    val currentApiKey: StateFlow<String> = _currentApiKey.asStateFlow()
    
    private val sessionContext = securityHelper.createUserContext(
        sessionId = UUID.randomUUID().toString(),
        deviceFingerprint = "android_device"
    )
    
    init {
        loadInitialState()
        observeApiKeyPresence()
        Napier.d("AISettingsViewModel initialized")
    }
    
    /**
     * Processes user intents for AI settings management.
     */
    fun onProcessIntent(intent: AISettingsIntent) {
        when (intent) {
            is AISettingsIntent.LoadSettings -> loadInitialState()
            is AISettingsIntent.UpdateApiKey -> updateApiKey(intent.apiKey)
            is AISettingsIntent.SaveApiKey -> saveApiKey()
            is AISettingsIntent.RemoveApiKey -> removeApiKey()
            is AISettingsIntent.ToggleApiKeyVisibility -> toggleApiKeyVisibility()
            is AISettingsIntent.ValidateApiKey -> validateCurrentApiKey()
            is AISettingsIntent.ClearErrors -> clearErrors()
        }
    }
    
    private fun loadInitialState() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true)
                
                val hasApiKey = aiSettingsRepository.hasOpenAiApiKey()
                val currentKey = if (hasApiKey) {
                    aiSettingsRepository.getOpenAiApiKey(sessionContext) ?: ""
                } else {
                    ""
                }
                
                _currentApiKey.value = currentKey
                _uiState.value = _uiState.value.copy(
                    hasApiKey = hasApiKey,
                    isSaving = false,
                    saveError = null,
                    validationError = null
                )
                
                Napier.d("Initial AI settings state loaded - hasApiKey: $hasApiKey")
                
            } catch (e: Exception) {
                handleError("Failed to load AI settings", e)
            }
        }
    }
    
    private fun observeApiKeyPresence() {
        viewModelScope.launch {
            aiSettingsRepository.observeOpenAiApiKeyPresence()
                .combine(_uiState) { hasKey, currentState ->
                    currentState.copy(hasApiKey = hasKey)
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }
    
    private fun updateApiKey(apiKey: String) {
        _currentApiKey.value = apiKey
        
        // Clear previous validation errors when user starts typing
        if (_uiState.value.validationError != null) {
            _uiState.value = _uiState.value.copy(validationError = null)
        }
        
        // Debounced validation for better UX
        viewModelScope.launch {
            delay(500) // Wait for user to stop typing
            if (_currentApiKey.value == apiKey && apiKey.isNotEmpty()) {
                validateApiKeyAsync(apiKey, showErrors = false)
            }
        }
    }
    
    private fun saveApiKey() {
        val apiKey = _currentApiKey.value.trim()
        
        if (apiKey.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                validationError = "API key cannot be empty"
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
                
                // Validate before saving
                val validation = aiSettingsRepository.validateApiKey(apiKey, sessionContext)
                if (!validation.isValid) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        validationError = validation.errorMessage
                    )
                    return@launch
                }
                
                // Save the API key
                aiSettingsRepository.storeOpenAiApiKey(apiKey, sessionContext)
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    hasApiKey = true,
                    saveError = null,
                    validationError = null
                )
                
                securityHelper.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.USER_ACTION,
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
                    validationError = e.message
                )
                Napier.w("API key validation failed: ${e.message}")
                
            } catch (e: SecureStorageException) {
                handleError("Failed to save API key securely", e)
                
            } catch (e: Exception) {
                handleError("Unexpected error while saving API key", e)
            }
        }
    }
    
    private fun removeApiKey() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true)
                
                aiSettingsRepository.removeOpenAiApiKey(sessionContext)
                _currentApiKey.value = ""
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    hasApiKey = false,
                    saveError = null,
                    validationError = null,
                    showApiKey = false
                )
                
                securityHelper.reportSecurityEvent(
                    type = SecurityMonitoringService.SecurityEventType.USER_ACTION,
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
    
    private fun toggleApiKeyVisibility() {
        _uiState.value = _uiState.value.copy(
            showApiKey = !_uiState.value.showApiKey
        )
        
        securityHelper.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.USER_ACTION,
            severity = SecurityMonitoringService.SecuritySeverity.LOW,
            message = "User toggled API key visibility",
            details = mapOf(
                "action" to "toggle_visibility",
                "visible" to _uiState.value.showApiKey.toString()
            ),
            userContext = sessionContext
        )
    }
    
    private fun validateCurrentApiKey() {
        val apiKey = _currentApiKey.value.trim()
        if (apiKey.isEmpty()) return
        
        viewModelScope.launch {
            validateApiKeyAsync(apiKey, showErrors = true)
        }
    }
    
    private suspend fun validateApiKeyAsync(apiKey: String, showErrors: Boolean) {
        try {
            _uiState.value = _uiState.value.copy(isValidating = true)
            
            val validation = aiSettingsRepository.validateApiKey(apiKey, sessionContext)
            
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
        
        securityHelper.reportSecurityEvent(
            type = SecurityMonitoringService.SecurityEventType.APPLICATION_ERROR,
            severity = SecurityMonitoringService.SecuritySeverity.MEDIUM,
            message = message,
            details = mapOf(
                "error" to (exception.message ?: "Unknown error"),
                "error_type" to exception.javaClass.simpleName
            ),
            userContext = sessionContext,
            throwable = exception
        )
        
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
    data object LoadSettings : AISettingsIntent
    data class UpdateApiKey(val apiKey: String) : AISettingsIntent
    data object SaveApiKey : AISettingsIntent
    data object RemoveApiKey : AISettingsIntent
    data object ToggleApiKeyVisibility : AISettingsIntent
    data object ValidateApiKey : AISettingsIntent
    data object ClearErrors : AISettingsIntent
}