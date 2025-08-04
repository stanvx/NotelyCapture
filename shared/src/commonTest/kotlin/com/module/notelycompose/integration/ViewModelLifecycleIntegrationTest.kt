package com.module.notelycompose.integration

import com.module.notelycompose.domain.audio.PlatformAudioPlayer
import com.module.notelycompose.domain.model.Note
import com.module.notelycompose.domain.repository.NoteRepository
import com.module.notelycompose.domain.security.SecurityHelper
import com.module.notelycompose.presentation.texteditor.TextEditorIntent
import com.module.notelycompose.presentation.texteditor.TextEditorViewModel
import com.module.notelycompose.testutil.awaitValue
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Integration test that verifies ViewModel lifecycle management and state management
 * work correctly together. This test demonstrates that all the testability issues
 * have been properly resolved.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelLifecycleIntegrationTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `ViewModel lifecycle and state management integration test`() = testScope.runTest {
        // Create test doubles
        val securityHelper = TestSecurityHelper()
        val audioPlayer = TestPlatformAudioPlayer()
        val noteRepository = TestNoteRepository()
        
        // Create ViewModel with test scope
        val viewModel = TextEditorViewModel(
            securityHelper = securityHelper,
            audioPlayer = audioPlayer,
            noteRepository = noteRepository,
            coroutineScope = testScope
        )
        
        // Test initial state
        val initialState = viewModel.uiState.value
        assertEquals("", initialState.content)
        assertFalse(initialState.isLoading)
        assertNull(initialState.error)
        
        // Test content update with security sanitization
        viewModel.onProcessIntent(TextEditorIntent.UpdateContent("<script>evil()</script>Safe content"))
        advanceUntilIdle()
        
        val afterUpdateState = viewModel.uiState.awaitValue { it.content.isNotEmpty() }
        assertEquals("Safe content", afterUpdateState.content)
        assertTrue(securityHelper.sanitizeWasCalled)
        
        // Test save operation
        viewModel.onProcessIntent(TextEditorIntent.SaveNote)
        
        // Verify loading state appears
        val loadingState = viewModel.uiState.awaitValue { it.isLoading }
        assertTrue(loadingState.isLoading)
        
        advanceUntilIdle()
        
        // Verify save completed successfully
        val savedState = viewModel.uiState.awaitValue { it.isSaved }
        assertFalse(savedState.isLoading)
        assertTrue(savedState.isSaved)
        assertNull(savedState.error)
        assertNotNull(savedState.noteId)
        
        // Test audio playback
        val audioPath = "/test/audio.wav"
        viewModel.onProcessIntent(TextEditorIntent.PlayAudio(audioPath))
        advanceUntilIdle()
        
        assertTrue(audioPlayer.playWasCalled)
        assertEquals(audioPath, audioPlayer.lastPlayedPath)
        
        // Test star toggle
        viewModel.onProcessIntent(TextEditorIntent.ToggleStar)
        advanceUntilIdle()
        
        val starredState = viewModel.uiState.awaitValue { it.isStarred }
        assertTrue(starredState.isStarred)
        
        // Test error handling
        noteRepository.shouldThrowError = true
        viewModel.onProcessIntent(TextEditorIntent.UpdateContent("New content"))
        viewModel.onProcessIntent(TextEditorIntent.SaveNote)
        advanceUntilIdle()
        
        val errorState = viewModel.uiState.awaitValue { it.error != null }
        assertNotNull(errorState.error)
        assertFalse(errorState.isSaved)
        
        // Test error clearing
        viewModel.onProcessIntent(TextEditorIntent.ClearError)
        advanceUntilIdle()
        
        val clearedErrorState = viewModel.uiState.awaitValue { it.error == null }
        assertNull(clearedErrorState.error)
        
        // Test long-running task with lifecycle management
        viewModel.onProcessIntent(TextEditorIntent.StartLongRunningTask)
        advanceTimeBy(100) // Let task start
        
        val taskStartedState = viewModel.uiState.awaitValue { it.isLoading }
        assertTrue(taskStartedState.isLoading)
        
        // Clear ViewModel (simulate Activity/Fragment destruction)
        viewModel.clearViewModel()
        
        // Advance remaining time to ensure task would complete if not cancelled
        advanceTimeBy(5000)
        advanceUntilIdle()
        
        // Verify task was cancelled and resources cleaned up
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading) // Task should be cancelled, not completed
        
        // Verify all resources were released
        // Note: In real implementation, you might track resource cleanup
        assertTrue(audioPlayer.releaseWasCalled)
    }
    
    @Test
    fun `ViewModel handles multiple rapid state changes correctly`() = testScope.runTest {
        val viewModel = TextEditorViewModel(
            securityHelper = TestSecurityHelper(),
            audioPlayer = TestPlatformAudioPlayer(),
            noteRepository = TestNoteRepository(),
            coroutineScope = testScope
        )
        
        // Fire multiple rapid updates
        repeat(10) { index ->
            viewModel.onProcessIntent(TextEditorIntent.UpdateContent("Content $index"))
            viewModel.onProcessIntent(TextEditorIntent.ToggleStar)
        }
        
        advanceUntilIdle()
        
        // Verify final state is consistent
        val finalState = viewModel.uiState.value
        assertEquals("Content 9", finalState.content)
        assertFalse(finalState.isStarred) // Even number of toggles = false
        
        viewModel.clearViewModel()
    }
    
    @Test
    fun `ViewModel properly handles concurrent operations`() = testScope.runTest {
        val noteRepository = TestNoteRepository()
        val viewModel = TextEditorViewModel(
            securityHelper = TestSecurityHelper(),
            audioPlayer = TestPlatformAudioPlayer(),
            noteRepository = noteRepository,
            coroutineScope = testScope
        )
        
        // Start multiple concurrent operations
        viewModel.onProcessIntent(TextEditorIntent.UpdateContent("Test content"))
        viewModel.onProcessIntent(TextEditorIntent.SaveNote)
        viewModel.onProcessIntent(TextEditorIntent.PlayAudio("/test.wav"))
        viewModel.onProcessIntent(TextEditorIntent.StartLongRunningTask)
        
        // Let operations start
        advanceTimeBy(50)
        
        // Operations should be running concurrently
        val runningState = viewModel.uiState.value
        assertTrue(runningState.isLoading)
        
        // Complete all operations
        advanceUntilIdle()
        
        // Verify final state
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertTrue(finalState.isSaved)
        assertEquals("Test content", finalState.content)
        
        viewModel.clearViewModel()
    }
}

// Enhanced test doubles with additional verification capabilities

private class TestSecurityHelper : SecurityHelper {
    var sanitizeWasCalled = false
        private set
    var sanitizeCallCount = 0
        private set
    
    override fun sanitizeHtml(input: String): String {
        sanitizeWasCalled = true
        sanitizeCallCount++
        return input.replace(Regex("<script.*?</script>", RegexOption.IGNORE_CASE), "")
    }
    
    override fun validateInput(input: String): Boolean = input.isNotBlank()
}

private class TestPlatformAudioPlayer : PlatformAudioPlayer {
    var playWasCalled = false
        private set
    var lastPlayedPath: String? = null
        private set
    var releaseWasCalled = false
        private set
    
    override suspend fun play(audioPath: String) {
        playWasCalled = true
        lastPlayedPath = audioPath
    }
    
    override suspend fun pause() {}
    override suspend fun stop() {}
    override suspend fun seekTo(position: Long) {}
    
    override fun release() {
        releaseWasCalled = true
    }
}

private class TestNoteRepository : NoteRepository {
    var shouldThrowError = false
    var saveCallCount = 0
        private set
    
    override suspend fun saveNote(note: Note): Result<Unit> {
        saveCallCount++
        return if (shouldThrowError) {
            Result.failure(Exception("Test repository error"))
        } else {
            Result.success(Unit)
        }
    }
    
    override suspend fun getNote(id: String): Result<Note?> {
        return Result.success(null)
    }
    
    override suspend fun getAllNotes(): Result<List<Note>> {
        return Result.success(emptyList())
    }
    
    override suspend fun deleteNote(id: String): Result<Unit> {
        return Result.success(Unit)
    }
}