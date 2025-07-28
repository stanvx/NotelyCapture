package com.module.notelycompose.notes.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.notes.ui.settings.languageCodeMap
import com.module.notelycompose.onboarding.data.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LanguageSelectionViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LanguageSelectionState())
    val state: StateFlow<LanguageSelectionState> = _state.asStateFlow()

    init {
        initializeLanguageData()
        observeSelectedLanguage()
    }

    fun onProcessIntent(intent: LanguageSelectionIntent) {
        when (intent) {
            is LanguageSelectionIntent.OnSearchQueryChanged -> handleSearchQueryChanged(intent.query)
            is LanguageSelectionIntent.OnLanguageSelected -> handleLanguageSelected(intent.languageCode)
            is LanguageSelectionIntent.OnClearSearch -> handleClearSearch()
            is LanguageSelectionIntent.OnRetry -> handleRetry()
        }
    }

    private fun initializeLanguageData() {
        _state.update { currentState ->
            currentState.copy(
                availableLanguages = languageCodeMap,
                filteredLanguages = languageCodeMap,
                isLoading = false
            )
        }
    }

    private fun observeSelectedLanguage() {
        preferencesRepository.getDefaultTranscriptionLanguage()
            .onEach { selectedLanguage ->
                _state.update { currentState ->
                    currentState.copy(selectedLanguageCode = selectedLanguage)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleSearchQueryChanged(query: String) {
        _state.update { currentState ->
            val filteredLanguages = if (query.isBlank()) {
                currentState.availableLanguages
            } else {
                currentState.availableLanguages.filter { (code, name) ->
                    name.contains(query, ignoreCase = true) ||
                            code.contains(query, ignoreCase = true)
                }
            }

            currentState.copy(
                searchQuery = query,
                filteredLanguages = filteredLanguages
            )
        }
    }

    private fun handleLanguageSelected(languageCode: String) {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(isLoading = true, error = null)
            }

            try {
                preferencesRepository.setDefaultTranscriptionLanguage(languageCode)
                // The state will be updated automatically via observeSelectedLanguage()
                _state.update { currentState ->
                    currentState.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = "Failed to save language selection: ${e.message}"
                    )
                }
            }
        }
    }

    private fun handleClearSearch() {
        _state.update { currentState ->
            currentState.copy(
                searchQuery = "",
                filteredLanguages = currentState.availableLanguages
            )
        }
    }

    private fun handleRetry() {
        _state.update { currentState ->
            currentState.copy(error = null)
        }
        initializeLanguageData()
    }
}