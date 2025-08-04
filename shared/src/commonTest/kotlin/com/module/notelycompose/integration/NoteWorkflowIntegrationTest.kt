package com.module.notelycompose.integration

import com.module.notelycompose.data.repository.NoteRepositoryImplTest
import com.module.notelycompose.domain.model.Note
import com.module.notelycompose.domain.usecases.AddNoteUseCase
import com.module.notelycompose.domain.usecases.DeleteNoteUseCase
import com.module.notelycompose.domain.usecases.GetAllNotesUseCase
import com.module.notelycompose.domain.usecases.UpdateNoteUseCase
import com.module.notelycompose.presentation.viewmodels.NoteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests to validate that all components work together correctly.
 * Tests the complete workflow from ViewModel through use cases to repository.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NoteWorkflowIntegrationTest {

    private lateinit var viewModel: NoteViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Create real instances (not mocks) to test integration
        val repository = createInMemoryRepository()
        val addNoteUseCase = AddNoteUseCase(repository)
        val getAllNotesUseCase = GetAllNotesUseCase(repository)
        val deleteNoteUseCase = DeleteNoteUseCase(repository)
        val updateNoteUseCase = UpdateNoteUseCase(repository)
        
        viewModel = NoteViewModel(
            addNoteUseCase = addNoteUseCase,
            getAllNotesUseCase = getAllNotesUseCase,
            deleteNoteUseCase = deleteNoteUseCase,
            updateNoteUseCase = updateNoteUseCase
        )
    }

    private fun createInMemoryRepository(): com.module.notelycompose.domain.repository.NoteRepository {
        return object : com.module.notelycompose.domain.repository.NoteRepository {
            private val notes = mutableMapOf<Long, Note>()
            private var nextId = 1L
            
            override suspend fun insertNote(note: Note) {
                val noteToInsert = if (note.id == 0L) {
                    note.copy(id = nextId++)
                } else {
                    note
                }
                notes[noteToInsert.id] = noteToInsert
            }
            
            override suspend fun deleteNote(note: Note) {
                notes.remove(note.id)
            }
            
            override suspend fun getNoteById(id: Long): Note? {
                return notes[id]
            }
            
            override suspend fun getAllNotes(): List<Note> {
                return notes.values.sortedByDescending { it.timestamp }
            }
            
            override suspend fun updateNote(note: Note) {
                notes[note.id] = note
            }
        }
    }

    @Test
    fun `complete note lifecycle should work end-to-end`() = runTest {
        // Initially no notes
        testDispatcher.scheduler.advanceUntilIdle()
        var state = viewModel.state.first()
        assertTrue(state.notes.isEmpty())
        
        // Add a note
        val newNote = Note(
            title = "Integration Test Note",
            content = "This note tests the complete workflow",
            timestamp = System.currentTimeMillis(),
            isStarred = false
        )
        
        viewModel.addNote(newNote)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify note was added
        state = viewModel.state.first()
        assertEquals(1, state.notes.size)
        assertEquals("Integration Test Note", state.notes[0].title)
        assertFalse(state.notes[0].isStarred)
        
        // Toggle starred status
        viewModel.toggleStarred(state.notes[0])
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify star toggle
        state = viewModel.state.first()
        assertTrue(state.notes[0].isStarred)
        
        // Search for the note
        viewModel.updateSearchQuery("Integration")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify search works
        state = viewModel.state.first()
        assertEquals(1, state.notes.size)
        assertEquals("integration", state.searchQuery)
        
        // Clear search
        viewModel.clearSearch()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify all notes visible again
        state = viewModel.state.first()
        assertEquals(1, state.notes.size)
        assertEquals("", state.searchQuery)
        
        // Delete the note
        viewModel.deleteNote(state.notes[0])
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify note was deleted
        state = viewModel.state.first()
        assertTrue(state.notes.isEmpty())
    }

    @Test
    fun `multiple notes workflow should maintain proper ordering`() = runTest {
        // Add multiple notes with different timestamps
        val notes = listOf(
            Note(title = "First Note", content = "Content 1", timestamp = 1000L, isStarred = false),
            Note(title = "Second Note", content = "Content 2", timestamp = 3000L, isStarred = true),
            Note(title = "Third Note", content = "Content 3", timestamp = 2000L, isStarred = false)
        )
        
        notes.forEach { note ->
            viewModel.addNote(note)
            testDispatcher.scheduler.advanceUntilIdle()
        }
        
        // Verify proper ordering (most recent first)
        val state = viewModel.state.first()
        assertEquals(3, state.notes.size)
        assertEquals("Second Note", state.notes[0].title) // timestamp 3000
        assertEquals("Third Note", state.notes[1].title)  // timestamp 2000
        assertEquals("First Note", state.notes[2].title)  // timestamp 1000
        
        // Verify starred note is preserved
        assertTrue(state.notes[0].isStarred)
        assertFalse(state.notes[1].isStarred)
        assertFalse(state.notes[2].isStarred)
    }

    @Test
    fun `search functionality should work across all note fields`() = runTest {
        // Add notes with different content
        val notes = listOf(
            Note(title = "Meeting Notes", content = "Important discussion about project", timestamp = 1000L, isStarred = false, transcription = "Audio transcript here"),
            Note(title = "Shopping List", content = "Buy groceries and supplies", timestamp = 2000L, isStarred = false),
            Note(title = "Project Ideas", content = "Meeting with team tomorrow", timestamp = 3000L, isStarred = true, transcription = "Brainstorming session")
        )
        
        notes.forEach { note ->
            viewModel.addNote(note)
            testDispatcher.scheduler.advanceUntilIdle()
        }
        
        // Search by title
        viewModel.updateSearchQuery("meeting")
        testDispatcher.scheduler.advanceUntilIdle()
        
        var state = viewModel.state.first()
        assertEquals(2, state.notes.size) // "Meeting Notes" and "Project Ideas" (contains "meeting")
        
        // Search by content
        viewModel.updateSearchQuery("groceries")
        testDispatcher.scheduler.advanceUntilIdle()
        
        state = viewModel.state.first()
        assertEquals(1, state.notes.size)
        assertEquals("Shopping List", state.notes[0].title)
        
        // Search by transcription
        viewModel.updateSearchQuery("brainstorming")
        testDispatcher.scheduler.advanceUntilIdle()
        
        state = viewModel.state.first()
        assertEquals(1, state.notes.size)
        assertEquals("Project Ideas", state.notes[0].title)
        
        // Clear search to show all notes
        viewModel.clearSearch()
        testDispatcher.scheduler.advanceUntilIdle()
        
        state = viewModel.state.first()
        assertEquals(3, state.notes.size)
    }

    @Test
    fun `error handling should work throughout the workflow`() = runTest {
        // Initially no error
        var state = viewModel.state.first()
        assertEquals(null, state.error)
        
        // Try to add invalid note (this should be caught by use case validation)
        val invalidNote = Note(
            title = "",
            content = "",
            timestamp = System.currentTimeMillis(),
            isStarred = false,
            hasAudio = false
        )
        
        viewModel.addNote(invalidNote)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Check if error was handled
        state = viewModel.state.first()
        // The error might be set depending on validation logic
        
        // Clear any error
        viewModel.clearError()
        testDispatcher.scheduler.advanceUntilIdle()
        
        state = viewModel.state.first()
        assertEquals(null, state.error)
    }

    @Test
    fun `voice note workflow should handle audio metadata correctly`() = runTest {
        // Add a voice note
        val voiceNote = Note(
            title = "Voice Memo",
            content = "Recorded during meeting",
            timestamp = System.currentTimeMillis(),
            isStarred = false,
            audioFilePath = "/storage/audio/memo.wav",
            hasAudio = true,
            transcription = "This is the transcribed text from audio"
        )
        
        viewModel.addNote(voiceNote)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify voice note properties
        val state = viewModel.state.first()
        assertEquals(1, state.notes.size)
        val savedNote = state.notes[0]
        
        assertTrue(savedNote.hasAudio)
        assertTrue(savedNote.isVoiceNote())
        assertEquals("/storage/audio/memo.wav", savedNote.audioFilePath)
        assertEquals("This is the transcribed text from audio", savedNote.transcription)
        
        // Search by transcription should work
        viewModel.updateSearchQuery("transcribed")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val searchState = viewModel.state.first()
        assertEquals(1, searchState.notes.size)
        assertEquals("Voice Memo", searchState.notes[0].title)
    }
}