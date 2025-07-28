package com.module.notelycompose.notes.presentation.settings

data class LanguageSelectionState(
    val availableLanguages: Map<String, String> = emptyMap(),
    val filteredLanguages: Map<String, String> = emptyMap(),
    val selectedLanguageCode: String = "",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)