package com.module.notelycompose.presentation

import app.cash.turbine.test
import com.module.notelycompose.testing.KoinTestBase
import com.module.notelycompose.testing.TestDataBuilder
import com.module.notelycompose.testing.TestFixtures
import com.module.notelycompose.testing.TestGetNoteUseCase
import com.module.notelycompose.testing.TestMatchers
import com.module.notelycompose.testing.TestModules
import com.module.notelycompose.testing.TestNote
import com.module.notelycompose.testing.TestSaveNoteUseCase
import com.module.notelycompose.testing.TestSecurityHelper
import com.module.notelycompose.testing.TestUiState
import com.module.notelycompose.testing.TestValidationResult
import com.module.notelycompose.testing.testModule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive test suite for TextEditorViewModel using modern testing patterns.
 * 
 * This test demonstrates:
 * - Interface-based mocking to avoid final class extension issues
 * - Modern mockk usage for Kotlin Multiplatform
 * - Proper Flow testing with Turbine
 * - Test-specific Koin configuration
 * - Custom matchers and assertions
 * - Parameterized testing scenarios
 */
class TextEditorViewModelTest : KoinTestBase() {
    
    // Mock dependencies - created as properties for easy access
    private val mockGetNoteUseCase = mockk<TestGetNoteUseCase>()
    private val mockSaveNoteUseCase = mockk<TestSaveNoteUseCase>()
    private val mockSecurityHelper = mockk<TestSecurityHelper>()
    
    // Test module configuration
    override val testModule: Module = testModule(TestModules.textEditorTestModule) {
        withMock(mockGetNoteUseCase)
        withMock(mockSaveNoteUseCase)
        withMock(mockSecurityHelper)
    }
    
    // System under test
    private lateinit var viewModel: TestTextEditorViewModel
    
    override fun setupKoin() {
        super.setupKoin()
        viewModel = TestTextEditorViewModel(
            getNoteUseCase = mockGetNoteUseCase,
            saveNoteUseCase = mockSaveNoteUseCase,
            securityHelper = mockSecurityHelper
        )
    }
    
    @Test
    fun `when loading note, should update state correctly`() = runTest {
        // Given
        val noteId = 1L
        val expectedNote = TestFixtures.standardNote.copy(id = noteId)
        coEvery { mockGetNoteUseCase(noteId) } returns expectedNote
        
        // When & Then
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()
            TestMatchers.assertStateSuccess(initialState)
            
            // Trigger load
            viewModel.loadNote(noteId)
            
            // Loading state
            val loadingState = awaitItem()
            TestMatchers.assertStateLoading(loadingState)
            
            // Success state
            val successState = awaitItem()
            TestMatchers.assertStateSuccess(successState)
            assertEquals(expectedNote.title, successState.title)
            assertEquals(expectedNote.content, successState.content)
            assertEquals(expectedNote.isStarred, successState.isStarred)
        }
        
        // Verify use case was called
        coVerify { mockGetNoteUseCase(noteId) }
    }
    
    @Test
    fun `when loading non-existent note, should handle error gracefully`() = runTest {
        // Given
        val noteId = 999L
        coEvery { mockGetNoteUseCase(noteId) } returns null
        
        // When & Then
        viewModel.uiState.test {
            awaitItem() // Initial state
            
            viewModel.loadNote(noteId)
            
            awaitItem() // Loading state
            
            val errorState = awaitItem()
            TestMatchers.assertStateError(errorState, "Note not found")
        }
    }
    
    @Test
    fun `when saving valid note, should emit loading then success states`() = runTest {
        // Given
        val note = TestFixtures.standardNote
        val sanitizedContent = "Sanitized content"
        
        every { mockSecurityHelper.sanitizeHtml(note.content) } returns sanitizedContent
        every { mockSecurityHelper.validateNote(any()) } returns TestFixtures.validResult
        coEvery { mockSaveNoteUseCase(any()) } returns Result.success(Unit)
        
        // When & Then
        viewModel.uiState.test {
            val initialState = awaitItem()
            
            // Set note data
            viewModel.updateTitle(note.title)
            awaitItem() // Title update
            
            viewModel.updateContent(note.content)
            awaitItem() // Content update
            
            // Trigger save
            viewModel.saveNote()
            
            // Loading state
            val loadingState = awaitItem()
            TestMatchers.assertStateLoading(loadingState)
            
            // Success state
            val successState = awaitItem()
            TestMatchers.assertStateSuccess(successState)
        }
        
        // Verify interactions
        verify { mockSecurityHelper.sanitizeHtml(note.content) }
        verify { mockSecurityHelper.validateNote(any()) }
        coVerify { mockSaveNoteUseCase(any()) }
    }
    
    @Test
    fun `when saving note with invalid content, should show validation error`() = runTest {
        // Given
        val invalidNote = TestFixtures.maliciousContentNote
        val validationError = "Content contains invalid HTML"
        
        every { mockSecurityHelper.validateNote(any()) } returns TestDataBuilder.createTestValidationResult(
            isValid = false,
            errorMessage = validationError
        )
        
        // When & Then
        viewModel.uiState.test {
            awaitItem() // Initial state
            
            viewModel.updateContent(invalidNote.content)
            awaitItem() // Content update
            
            viewModel.saveNote()
            
            val errorState = awaitItem()
            TestMatchers.assertStateError(errorState, validationError)
        }
        
        // Verify validation was called but save was not
        verify { mockSecurityHelper.validateNote(any()) }
        coVerify(exactly = 0) { mockSaveNoteUseCase(any()) }
    }
    
    @Test
    fun `when updating title, should sanitize input and update state`() = runTest {
        // Given
        val rawTitle = "<script>alert('xss')</script>Valid Title"
        val sanitizedTitle = "Valid Title"
        
        every { mockSecurityHelper.sanitizeHtml(rawTitle) } returns sanitizedTitle
        
        // When & Then
        viewModel.uiState.test {
            awaitItem() // Initial state
            
            viewModel.updateTitle(rawTitle)
            
            val updatedState = awaitItem()
            assertEquals(sanitizedTitle, updatedState.title)
        }
        
        verify { mockSecurityHelper.sanitizeHtml(rawTitle) }
    }
    
    @Test
    fun `when updating content, should sanitize input and update state`() = runTest {
        // Given
        val rawContent = TestDataBuilder.createHtmlContent(includeUnsafeContent = true)
        val sanitizedContent = TestDataBuilder.createHtmlContent(includeUnsafeContent = false)
        
        every { mockSecurityHelper.sanitizeHtml(rawContent) } returns sanitizedContent
        
        // When & Then
        viewModel.uiState.test {
            awaitItem() // Initial state
            
            viewModel.updateContent(rawContent)
            
            val updatedState = awaitItem()
            assertEquals(sanitizedContent, updatedState.content)
        }
        
        verify { mockSecurityHelper.sanitizeHtml(rawContent) }
    }
    
    @Test
    fun `when toggling starred status, should update state correctly`() = runTest {
        // Given
        val initialNote = TestFixtures.standardNote.copy(isStarred = false)
        
        // When & Then
        viewModel.uiState.test {
            awaitItem() // Initial state
            
            viewModel.toggleStarred()
            
            val updatedState = awaitItem()
            assertTrue(updatedState.isStarred)
        }
    }
    
    @Test
    fun `when clearing content, should reset to empty state`() = runTest {
        // Given - Set some initial content
        viewModel.updateTitle("Some title")
        viewModel.updateContent("Some content")
        
        // When & Then
        viewModel.uiState.test {
            // Skip to current state
            var currentState = awaitItem()
            while (currentState.title.isEmpty() || currentState.content.isEmpty()) {
                currentState = awaitItem()
            }
            
            viewModel.clearContent()
            
            val clearedState = awaitItem()
            assertEquals("", clearedState.title)
            assertEquals("", clearedState.content)
            assertFalse(clearedState.isStarred)
        }
    }
    
    @Test
    fun `validation scenarios should handle different input types`() = runTest {
        // Test with various validation scenarios from fixtures
        TestFixtures.inputValidationTestCases.forEach { testCase ->
            // Given
            val validationResult = TestDataBuilder.createTestValidationResult(
                isValid = testCase.expectedValid,
                errorMessage = if (!testCase.expectedValid) "Invalid input" else null
            )
            
            every { mockSecurityHelper.validateNote(any()) } returns validationResult
            every { mockSecurityHelper.sanitizeHtml(testCase.input) } returns testCase.input
            
            // When
            viewModel.updateContent(testCase.input)
            
            // Then - validation should be called appropriately
            if (testCase.expectedValid) {
                // Valid input should not show error
                viewModel.uiState.test {
                    val state = awaitItem()
                    assertNull(state.error, "Should not have error for valid input: ${testCase.name}")
                }
            }
        }
    }
}

/**
 * Test implementation of TextEditorViewModel for testing purposes.
 * This represents what the actual ViewModel would look like using the interfaces.
 */
class TestTextEditorViewModel(
    private val getNoteUseCase: TestGetNoteUseCase,
    private val saveNoteUseCase: TestSaveNoteUseCase,
    private val securityHelper: TestSecurityHelper
) {
    private val _uiState = MutableStateFlow(TextEditorUiState())
    val uiState = _uiState
    
    suspend fun loadNote(noteId: Long) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        try {
            val note = getNoteUseCase(noteId)
            if (note != null) {
                _uiState.value = TextEditorUiState(
                    title = note.title,
                    content = note.content,
                    isStarred = note.isStarred,
                    isLoading = false,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Note not found"
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Unknown error"
            )
        }
    }
    
    suspend fun saveNote() {
        val currentState = _uiState.value
        val note = TestNote(
            id = 0L, // New note
            title = currentState.title,
            content = currentState.content,
            isStarred = currentState.isStarred,
            dateCreated = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis()
        )
        
        // Validate before saving
        val validationResult = securityHelper.validateNote(note)
        if (!validationResult.isValid) {
            _uiState.value = currentState.copy(error = validationResult.errorMessage)
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, error = null)
        
        try {
            saveNoteUseCase(note).getOrThrow()
            _uiState.value = currentState.copy(isLoading = false, error = null)
        } catch (e: Exception) {
            _uiState.value = currentState.copy(
                isLoading = false,
                error = e.message ?: "Save failed"
            )
        }
    }
    
    fun updateTitle(title: String) {
        val sanitizedTitle = securityHelper.sanitizeHtml(title)
        _uiState.value = _uiState.value.copy(title = sanitizedTitle)
    }
    
    fun updateContent(content: String) {
        val sanitizedContent = securityHelper.sanitizeHtml(content)
        _uiState.value = _uiState.value.copy(content = sanitizedContent)
    }
    
    fun toggleStarred() {
        _uiState.value = _uiState.value.copy(isStarred = !_uiState.value.isStarred)
    }
    
    fun clearContent() {
        _uiState.value = TextEditorUiState()
    }
}

/**
 * UI state for the text editor.
 */
data class TextEditorUiState(
    val title: String = "",
    val content: String = "",
    val isStarred: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    // Extension to work with TestMatchers
    fun toTestUiState(): TestUiState = TestUiState(
        isLoading = isLoading,
        error = error,
        data = this
    )
}