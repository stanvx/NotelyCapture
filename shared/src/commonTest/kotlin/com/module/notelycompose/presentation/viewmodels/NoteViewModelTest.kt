package com.module.notelycompose.presentation.viewmodels

import com.module.notelycompose.domain.model.Note
import com.module.notelycompose.domain.usecases.AddNoteUseCase
import com.module.notelycompose.domain.usecases.DeleteNoteUseCase
import com.module.notelycompose.domain.usecases.GetAllNotesUseCase
import com.module.notelycompose.domain.usecases.UpdateNoteUseCase
import com.module.notelycompose.domain.repository.NoteRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModelTest {

    private class MockNoteRepository : NoteRepository {
        private val notes = mutableListOf<Note>()
        
        override suspend fun insertNote(note: Note) {
            notes.add(note)
        }
        
        override suspend fun deleteNote(note: Note) {
            notes.removeAll { it.id == note.id }
        }
        
        override suspend fun getNoteById(id: Long): Note? {
            return notes.find { it.id == id }
        }
        
        override suspend fun getAllNotes() = notes.sortedByDescending { it.timestamp }
        
        override suspend fun updateNote(note: Note) {
            val index = notes.indexOfFirst { it.id == note.id }
            if (index != -1) {
                notes[index] = note
            }
        }
        
        fun addTestNote(note: Note) {
            notes.add(note)
        }
        
        fun clear() {
            notes.clear()
        }
    }

    private lateinit var repository: MockNoteRepository
    private lateinit var viewModel: NoteViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = MockNoteRepository()
        
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

    @Test
    fun `initial state should have empty notes list and not loading`() = runTest {
        // When
        val state = viewModel.state.first()

        // Then
        assertTrue(state.notes.isEmpty())
        assertFalse(state.isLoading)
        assertEquals("", state.searchQuery)
    }

    @Test
    fun `loadNotes should update state with notes from repository`() = runTest {
        // Given
        val testNote = Note(
            id = 1L,
            title = "Test Note",
            content = "Test Content",
            timestamp = System.currentTimeMillis(),
            isStarred = false
        )
        repository.addTestNote(testNote)

        // When
        viewModel.loadNotes()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals(1, state.notes.size)
        assertEquals("Test Note", state.notes[0].title)
        assertFalse(state.isLoading)
    }

    @Test
    fun `addNote should add note and refresh list`() = runTest {
        // Given
        val newNote = Note(
            id = 1L,
            title = "New Note",
            content = "New Content",
            timestamp = System.currentTimeMillis(),
            isStarred = true
        )

        // When
        viewModel.addNote(newNote)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals(1, state.notes.size)
        assertEquals("New Note", state.notes[0].title)
        assertTrue(state.notes[0].isStarred)
    }

    @Test
    fun `deleteNote should remove note and refresh list`() = runTest {
        // Given
        val testNote = Note(
            id = 1L,
            title = "To Delete",
            content = "Content",
            timestamp = System.currentTimeMillis(),
            isStarred = false
        )
        repository.addTestNote(testNote)
        viewModel.loadNotes()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.deleteNote(testNote)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertTrue(state.notes.isEmpty())
    }

    @Test
    fun `updateSearchQuery should filter notes by title and content`() = runTest {
        // Given
        val note1 = Note(
            id = 1L,
            title = "Meeting Notes",
            content = "Important discussion",
            timestamp = 1000L,
            isStarred = false
        )
        val note2 = Note(
            id = 2L,
            title = "Shopping List",
            content = "Buy groceries",
            timestamp = 2000L,
            isStarred = false
        )
        val note3 = Note(
            id = 3L,
            title = "Project Ideas",
            content = "Meeting with team tomorrow",
            timestamp = 3000L,
            isStarred = true
        )
        
        repository.addTestNote(note1)
        repository.addTestNote(note2)
        repository.addTestNote(note3)
        viewModel.loadNotes()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.updateSearchQuery("meeting")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals("meeting", state.searchQuery)
        assertEquals(2, state.notes.size) // Should find "Meeting Notes" and "Project Ideas" (contains "meeting")
        assertTrue(state.notes.any { it.title == "Meeting Notes" })
        assertTrue(state.notes.any { it.title == "Project Ideas" })
    }

    @Test
    fun `toggleStarred should update note starred status`() = runTest {
        // Given
        val testNote = Note(
            id = 1L,
            title = "Test Note",
            content = "Content",
            timestamp = System.currentTimeMillis(),
            isStarred = false
        )
        repository.addTestNote(testNote)
        viewModel.loadNotes()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleStarred(testNote)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals(1, state.notes.size)
        assertTrue(state.notes[0].isStarred)
    }

    @Test
    fun `clearSearch should reset search query and show all notes`() = runTest {
        // Given
        val note1 = Note(id = 1L, title = "Note 1", content = "Content 1", timestamp = 1000L, isStarred = false)
        val note2 = Note(id = 2L, title = "Note 2", content = "Content 2", timestamp = 2000L, isStarred = false)
        
        repository.addTestNote(note1)
        repository.addTestNote(note2)
        viewModel.loadNotes()
        viewModel.updateSearchQuery("Note 1")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals("", state.searchQuery)
        assertEquals(2, state.notes.size) // Should show all notes again
    }
}