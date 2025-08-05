package com.module.notelycompose.presentation.texteditor

import com.module.notelycompose.platform.PlatformAudioPlayer
import com.module.notelycompose.domain.model.Note
import com.module.notelycompose.domain.repository.NoteRepository
import com.module.notelycompose.domain.security.SecurityHelper
import com.module.notelycompose.testutil.assertEmits
import com.module.notelycompose.testutil.awaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.*

/**
 * Test class for TextEditorViewModel that addresses common KMP ViewModel testing issues:
 * 1. Cannot access protected onCleared() method - Use lifecycle testing patterns
 * 2. Need proper ViewModel lifecycle testing - Use TestViewModelScope and manual cleanup
 * 3. SecurityHelper mocking issues - Use interface-based mocking with test doubles
 * 4. PlatformAudioPlayer constructor and final method issues - Use dependency injection with test implementations
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TextEditorViewModelTest : KoinTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    
    // Test doubles for dependencies
    private val mockSecurityHelper: SecurityHelper by inject()
    private val mockPlatformAudioPlayer: PlatformAudioPlayer by inject()
    private val mockNoteRepository: NoteRepository by inject()
    
    private lateinit var viewModel: TextEditorViewModel
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Setup Koin with test modules
        startKoin {
            modules(testModule)
        }
        
        // Initialize ViewModel with test scope
        viewModel = TextEditorViewModel(
            securityHelper = mockSecurityHelper,
            audioPlayer = mockPlatformAudioPlayer,
            noteRepository = mockNoteRepository,
            coroutineScope = testScope
        )
    }
    
    @AfterTest
    fun tearDown() {
        // Proper ViewModel lifecycle management
        viewModel.clearViewModel() // Custom method instead of protected onCleared()
        stopKoin()
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be empty`() = testScope.runTest {
        val initialState = viewModel.uiState.first()
        
        assertEquals("", initialState.content)
        assertFalse(initialState.isLoading)
        assertNull(initialState.error)
    }
    
    @Test
    fun `updateContent should sanitize input through SecurityHelper`() = testScope.runTest {
        val unsafeContent = "<script>alert('xss')</script>Hello World"
        val expectedSafeContent = "Hello World"
        
        viewModel.onProcessIntent(TextEditorIntent.UpdateContent(unsafeContent))
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals(expectedSafeContent, state.content)
        
        // Verify SecurityHelper was called
        assertTrue((mockSecurityHelper as TestSecurityHelper).sanitizeWasCalled)
    }
    
    @Test
    fun `saveNote should handle success state properly`() = testScope.runTest {
        val content = "Test content"
        viewModel.onProcessIntent(TextEditorIntent.UpdateContent(content))
        viewModel.onProcessIntent(TextEditorIntent.SaveNote)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertTrue(state.isSaved)
        assertNull(state.error)
    }
    
    @Test
    fun `saveNote should handle error state properly`() = testScope.runTest {
        // Configure mock to throw error
        (mockNoteRepository as TestNoteRepository).shouldThrowError = true
        
        viewModel.onProcessIntent(TextEditorIntent.UpdateContent("Test"))
        viewModel.onProcessIntent(TextEditorIntent.SaveNote)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertFalse(state.isSaved)
        assertNotNull(state.error)
    }
    
    @Test
    fun `playAudio should delegate to PlatformAudioPlayer`() = testScope.runTest {
        val audioPath = "/test/audio.wav"
        
        viewModel.onProcessIntent(TextEditorIntent.PlayAudio(audioPath))
        
        advanceUntilIdle()
        
        val testPlayer = mockPlatformAudioPlayer as TestPlatformAudioPlayer
        assertTrue(testPlayer.playWasCalled)
        assertEquals(audioPath, testPlayer.lastPlayedPath)
    }
    
    @Test
    fun `ViewModel should properly manage coroutines lifecycle`() = testScope.runTest {
        // Start a long-running operation
        viewModel.onProcessIntent(TextEditorIntent.StartLongRunningTask)
        
        // Advance time slightly
        advanceTimeBy(100)
        
        // Clear ViewModel (simulating lifecycle destruction)
        viewModel.clearViewModel()
        
        // Advance remaining time
        advanceUntilIdle()
        
        // Verify no crashes and operations are cancelled
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
    }
    
    @Test
    fun `state flow emissions work correctly with test utilities`() = testScope.runTest {
        // Test using custom utility functions
        viewModel.onProcessIntent(TextEditorIntent.UpdateContent("Test content"))
        
        // Use custom assertion helper
        viewModel.uiState.assertEmits(
            expected = viewModel.uiState.value.copy(content = "Test content"),
            timeoutMs = 1000L
        )
        
        // Test awaitValue utility
        val state = viewModel.uiState.awaitValue { it.content == "Test content" }
        assertEquals("Test content", state.content)
    }
    
    @Test
    fun `loadNote should handle existing note properly`() = testScope.runTest {
        // Configure mock to return a note
        val testNote = createTestNote()
        (mockNoteRepository as TestNoteRepository).noteToReturn = testNote
        
        viewModel.onProcessIntent(TextEditorIntent.LoadNote(testNote.id))
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals(testNote.content, state.content)
        assertEquals(testNote.id, state.noteId)
        assertTrue(state.isSaved)
        assertFalse(state.isLoading)
    }
    
    @Test
    fun `toggleStar should update star state`() = testScope.runTest {
        // Initially not starred
        assertFalse(viewModel.uiState.first().isStarred)
        
        viewModel.onProcessIntent(TextEditorIntent.ToggleStar)
        
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.first().isStarred)
        
        // Toggle again
        viewModel.onProcessIntent(TextEditorIntent.ToggleStar)
        
        advanceUntilIdle()
        
        assertFalse(viewModel.uiState.first().isStarred)
    }
    
    @Test
    fun `clearError should remove error from state`() = testScope.runTest {
        // First create an error state
        (mockNoteRepository as TestNoteRepository).shouldThrowError = true
        viewModel.onProcessIntent(TextEditorIntent.UpdateContent("Test"))
        viewModel.onProcessIntent(TextEditorIntent.SaveNote)
        
        advanceUntilIdle()
        
        // Verify error exists
        assertNotNull(viewModel.uiState.first().error)
        
        // Clear error
        viewModel.onProcessIntent(TextEditorIntent.ClearError)
        
        advanceUntilIdle()
        
        assertNull(viewModel.uiState.first().error)
    }
    
    private fun createTestNote(): Note {
        return Note(
            id = "test-note-1",
            title = "Test Note",
            content = "This is test content",
            createdAt = kotlinx.datetime.Clock.System.now(),
            updatedAt = kotlinx.datetime.Clock.System.now(),
            isStarred = false,
            hasAudio = false
        )
    }
    
    companion object {
        val testModule = module {
            single<SecurityHelper> { TestSecurityHelper() }
            single<PlatformAudioPlayer> { TestPlatformAudioPlayer() }
            single<NoteRepository> { TestNoteRepository() }
        }
    }
}

// Test double implementations

class TestSecurityHelper : SecurityHelper {
    var sanitizeWasCalled = false
        private set
    
    override fun sanitizeHtml(input: String): String {
        sanitizeWasCalled = true
        // Simple test implementation - remove script tags
        return input.replace(Regex("<script.*?</script>", RegexOption.IGNORE_CASE), "")
    }
    
    override fun validateInput(input: String): Boolean = input.isNotBlank()
}

class TestPlatformAudioPlayer : PlatformAudioPlayer {
    var playWasCalled = false
        private set
    var lastPlayedPath: String? = null
        private set
    
    override suspend fun play(audioPath: String) {
        playWasCalled = true
        lastPlayedPath = audioPath
    }
    
    override suspend fun pause() {}
    override suspend fun stop() {}
    override suspend fun seekTo(position: Long) {}
    override fun release() {}
}

class TestNoteRepository : NoteRepository {
    var shouldThrowError = false
    var noteToReturn: Note? = null
    
    override suspend fun saveNote(note: Note): Result<Unit> {
        return if (shouldThrowError) {
            Result.failure(Exception("Test error"))
        } else {
            Result.success(Unit)
        }
    }
    
    override suspend fun getNote(id: String): Result<Note?> {
        return if (shouldThrowError) {
            Result.failure(Exception("Test error"))
        } else {
            Result.success(noteToReturn)
        }
    }
    
    override suspend fun getAllNotes(): Result<List<Note>> {
        return if (shouldThrowError) {
            Result.failure(Exception("Test error"))
        } else {
            Result.success(noteToReturn?.let { listOf(it) } ?: emptyList())
        }
    }
    
    override suspend fun deleteNote(id: String): Result<Unit> {
        return if (shouldThrowError) {
            Result.failure(Exception("Test error"))
        } else {
            Result.success(Unit)
        }
    }
}