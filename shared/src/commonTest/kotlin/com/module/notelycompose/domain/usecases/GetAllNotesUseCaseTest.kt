package com.module.notelycompose.domain.usecases

import com.module.notelycompose.domain.model.Note
import com.module.notelycompose.domain.repository.NoteRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetAllNotesUseCaseTest {

    private class MockNoteRepository : NoteRepository {
        private val notes = mutableListOf<Note>()
        
        fun addTestNote(note: Note) {
            notes.add(note)
        }
        
        override suspend fun insertNote(note: Note) {
            notes.add(note)
        }
        
        override suspend fun deleteNote(note: Note) {
            notes.remove(note)
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
    }

    @Test
    fun `getAllNotes should return empty list when no notes exist`() = runTest {
        // Given
        val repository = MockNoteRepository()
        val getAllNotesUseCase = GetAllNotesUseCase(repository)

        // When
        val result = getAllNotesUseCase()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllNotes should return all notes sorted by timestamp descending`() = runTest {
        // Given
        val repository = MockNoteRepository()
        val getAllNotesUseCase = GetAllNotesUseCase(repository)
        
        val note1 = Note(
            id = 1L,
            title = "First Note",
            content = "Content 1",
            timestamp = 1000L,
            isStarred = false
        )
        val note2 = Note(
            id = 2L,
            title = "Second Note",
            content = "Content 2",
            timestamp = 2000L,
            isStarred = true
        )
        val note3 = Note(
            id = 3L,
            title = "Third Note",
            content = "Content 3",
            timestamp = 1500L,
            isStarred = false
        )
        
        repository.addTestNote(note1)
        repository.addTestNote(note2)
        repository.addTestNote(note3)

        // When
        val result = getAllNotesUseCase()

        // Then
        assertEquals(3, result.size)
        assertEquals("Second Note", result[0].title) // Most recent
        assertEquals("Third Note", result[1].title)  // Middle
        assertEquals("First Note", result[2].title)  // Oldest
    }

    @Test
    fun `getAllNotes should handle single note`() = runTest {
        // Given
        val repository = MockNoteRepository()
        val getAllNotesUseCase = GetAllNotesUseCase(repository)
        
        val note = Note(
            id = 1L,
            title = "Single Note",
            content = "Single Content",
            timestamp = System.currentTimeMillis(),
            isStarred = true
        )
        
        repository.addTestNote(note)

        // When
        val result = getAllNotesUseCase()

        // Then
        assertEquals(1, result.size)
        assertEquals("Single Note", result[0].title)
        assertTrue(result[0].isStarred)
    }

    @Test
    fun `getAllNotes should preserve note properties`() = runTest {
        // Given
        val repository = MockNoteRepository()
        val getAllNotesUseCase = GetAllNotesUseCase(repository)
        
        val note = Note(
            id = 42L,
            title = "Test Note",
            content = "Test Content with special chars: àáâãäå",
            timestamp = 12345L,
            isStarred = true
        )
        
        repository.addTestNote(note)

        // When
        val result = getAllNotesUseCase()

        // Then
        assertEquals(1, result.size)
        val retrievedNote = result[0]
        assertEquals(42L, retrievedNote.id)
        assertEquals("Test Note", retrievedNote.title)
        assertEquals("Test Content with special chars: àáâãäå", retrievedNote.content)
        assertEquals(12345L, retrievedNote.timestamp)
        assertTrue(retrievedNote.isStarred)
    }
}