package com.module.notelycompose.notes.presentation.settings

import com.module.notelycompose.notes.ui.settings.languageCodeMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for LanguageSelection presentation logic and state management
 */
class LanguageSelectionViewModelTest {

    @Test
    fun `initial state should have correct default values`() {
        val state = LanguageSelectionState()

        assertEquals(emptyMap(), state.availableLanguages)
        assertEquals(emptyMap(), state.filteredLanguages)
        assertEquals("", state.selectedLanguageCode)
        assertEquals("", state.searchQuery)
        assertEquals(false, state.isLoading)
        assertEquals(null, state.error)
    }

    @Test
    fun `state with loaded languages should filter correctly`() {
        val state = LanguageSelectionState(
            availableLanguages = languageCodeMap,
            filteredLanguages = languageCodeMap
        )

        // Test filtering logic (simulating what the ViewModel would do)
        val searchQuery = "eng"
        val filteredLanguages = state.availableLanguages.filter { (code, name) ->
            name.contains(searchQuery, ignoreCase = true) ||
                    code.contains(searchQuery, ignoreCase = true)
        }

        assertEquals(1, filteredLanguages.size)
        assertTrue(filteredLanguages.containsKey("en"))
        assertEquals("English", filteredLanguages["en"])
    }

    @Test
    fun `state filtering should be case insensitive`() {
        val state = LanguageSelectionState(
            availableLanguages = languageCodeMap,
            filteredLanguages = languageCodeMap
        )

        // Test case insensitive filtering
        val searchQuery = "SPANISH"
        val filteredLanguages = state.availableLanguages.filter { (code, name) ->
            name.contains(searchQuery, ignoreCase = true) ||
                    code.contains(searchQuery, ignoreCase = true)
        }

        assertEquals(1, filteredLanguages.size)
        assertTrue(filteredLanguages.containsKey("es"))
        assertEquals("Spanish", filteredLanguages["es"])
    }

    @Test
    fun `state filtering should handle partial matches`() {
        val state = LanguageSelectionState(
            availableLanguages = languageCodeMap,
            filteredLanguages = languageCodeMap
        )

        // Test partial matching - "an" should match some languages
        val searchQuery = "an"
        val filteredLanguages = state.availableLanguages.filter { (code, name) ->
            name.contains(searchQuery, ignoreCase = true) ||
                    code.contains(searchQuery, ignoreCase = true)
        }

        // Check that we have some matches (should match German, Japanese, Ukrainian, Spanish)
        assertTrue(filteredLanguages.isNotEmpty())
        assertTrue(filteredLanguages.size > 2) // Should have multiple matches
        
        // Verify at least one specific expected match
        assertTrue(filteredLanguages.any { it.value == "German" })
    }

    @Test
    fun `state filtering with no matches should return empty map`() {
        val state = LanguageSelectionState(
            availableLanguages = languageCodeMap,
            filteredLanguages = languageCodeMap
        )

        val searchQuery = "xyz123"
        val filteredLanguages = state.availableLanguages.filter { (code, name) ->
            name.contains(searchQuery, ignoreCase = true) ||
                    code.contains(searchQuery, ignoreCase = true)
        }

        assertTrue(filteredLanguages.isEmpty())
    }

    @Test
    fun `state filtering with empty query should show all languages`() {
        val state = LanguageSelectionState(
            availableLanguages = languageCodeMap,
            filteredLanguages = languageCodeMap
        )

        val searchQuery = ""
        val filteredLanguages = if (searchQuery.isBlank()) {
            state.availableLanguages
        } else {
            state.availableLanguages.filter { (code, name) ->
                name.contains(searchQuery, ignoreCase = true) ||
                        code.contains(searchQuery, ignoreCase = true)
            }
        }

        assertEquals(languageCodeMap, filteredLanguages)
    }

    @Test
    fun `intent types should be properly defined`() {
        // Test that all intent types can be created
        val searchIntent = LanguageSelectionIntent.OnSearchQueryChanged("test")
        val selectIntent = LanguageSelectionIntent.OnLanguageSelected("en")
        val clearIntent = LanguageSelectionIntent.OnClearSearch
        val retryIntent = LanguageSelectionIntent.OnRetry

        assertTrue(searchIntent is LanguageSelectionIntent.OnSearchQueryChanged)
        assertTrue(selectIntent is LanguageSelectionIntent.OnLanguageSelected)
        assertTrue(clearIntent is LanguageSelectionIntent.OnClearSearch)
        assertTrue(retryIntent is LanguageSelectionIntent.OnRetry)
    }

    @Test
    fun `language constants are properly defined`() {
        // Ensure our language map has expected content
        assertTrue(languageCodeMap.isNotEmpty())
        assertTrue(languageCodeMap.containsKey("en"))
        assertTrue(languageCodeMap.containsKey("es"))
        assertTrue(languageCodeMap.containsKey("fr"))
        assertTrue(languageCodeMap.containsKey("de"))
        assertTrue(languageCodeMap.containsKey("ja"))
        
        assertEquals("English", languageCodeMap["en"])
        assertEquals("Spanish", languageCodeMap["es"])
        assertEquals("French", languageCodeMap["fr"])
        assertEquals("German", languageCodeMap["de"])
        assertEquals("Japanese", languageCodeMap["ja"])
    }

    @Test
    fun `state transitions should maintain immutability`() {
        val initialState = LanguageSelectionState(
            availableLanguages = languageCodeMap,
            filteredLanguages = languageCodeMap,
            searchQuery = "",
            selectedLanguageCode = "en"
        )

        val updatedState = initialState.copy(
            searchQuery = "spanish",
            filteredLanguages = mapOf("es" to "Spanish")
        )

        // Original state should be unchanged
        assertEquals("", initialState.searchQuery)
        assertEquals(languageCodeMap, initialState.filteredLanguages)

        // Updated state should have new values
        assertEquals("spanish", updatedState.searchQuery)
        assertEquals(mapOf("es" to "Spanish"), updatedState.filteredLanguages)
        assertEquals("en", updatedState.selectedLanguageCode) // Should carry over
    }

    @Test
    fun `error and loading states should work correctly`() {
        val loadingState = LanguageSelectionState(isLoading = true)
        assertTrue(loadingState.isLoading)
        assertEquals(null, loadingState.error)

        val errorState = LanguageSelectionState(
            error = "Failed to save language selection",
            isLoading = false
        )
        assertEquals("Failed to save language selection", errorState.error)
        assertEquals(false, errorState.isLoading)

        val successState = loadingState.copy(
            isLoading = false,
            error = null
        )
        assertEquals(false, successState.isLoading)
        assertEquals(null, successState.error)
    }
}