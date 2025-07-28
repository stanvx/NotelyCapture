package com.module.notelycompose.notes.presentation.settings

sealed class LanguageSelectionIntent {
    data class OnSearchQueryChanged(val query: String) : LanguageSelectionIntent()
    data class OnLanguageSelected(val languageCode: String) : LanguageSelectionIntent()
    data object OnClearSearch : LanguageSelectionIntent()
    data object OnRetry : LanguageSelectionIntent()
}