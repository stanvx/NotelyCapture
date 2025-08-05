package com.module.notelycompose.domain.usecases

import com.module.notelycompose.domain.model.Note
import com.module.notelycompose.domain.repository.NoteRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AddNoteUseCaseTest {

    private class MockNoteRepository : NoteRepository {
        private val notes = mutableListOf<Note>()
        
        override suspend fun insertNote(note: Note) {
            notes.add(note)
        }
        
        override suspend fun deleteNote(note: Note) {
            notes.remove(note)
        }
        
        override suspend fun getNoteById(id: Long): Note? {
            return notes.find { it.id == id }
        }
        
        override suspend fun getAllNotes() = notes.toList()
        
        override suspend fun updateNote(note: Note) {
            val index = notes.indexOfFirst { it.id == note.id }
            if (index != -1) {
                notes[index] = note
            }
        }
    }

    @Test
    fun `addNote should successfully add note to repository`() = runTest {
        // Given
        val repository = MockNoteRepository()
        val addNoteUseCase = AddNoteUseCase(repository)
        val note = Note(
            id = 1L,
            title = "Test Note",
            content = "Test Content",
            timestamp = System.currentTimeMillis(),
            isStarred = false
        )

        // When
        addNoteUseCase(note)

        // Then
        val savedNote = repository.getNoteById(1L)
        assertNotNull(savedNote)
        assertEquals("Test Note", savedNote.title)
        assertEquals("Test Content", savedNote.content)
    }

    @Test
    fun `addNote should handle empty title`() = runTest {
        // Given
        val repository = MockNoteRepository()
        val addNoteUseCase = AddNoteUseCase(repository)
        val note = Note(
            id = 1L,
            title = "",
            content = "Test Content",
            timestamp = System.currentTimeMillis(),
            isStarred = false
        )

        // When
        addNoteUseCase(note)

        // Then
        val savedNote = repository.getNoteById(1L)
        assertNotNull(savedNote)
        assertEquals("", savedNote.title)
        assertEquals("Test Content", savedNote.content)
    }

    @Test
    fun `addNote should preserve timestamp`() = runTest {
        // Given
        val repository = MockNoteRepository()
        val addNoteUseCase = AddNoteUseCase(repository)
        val timestamp = System.currentTimeMillis()
        val note = Note(
            id = 1L,
            title = "Test Note",
            content = "Test Content",
            timestamp = timestamp,
            isStarred = true
        )

        // When
        addNoteUseCase(note)

        // Then
        val savedNote = repository.getNoteById(1L)
        assertNotNull(savedNote)
        assertEquals(timestamp, savedNote.timestamp)
        assertTrue(savedNote.isStarred)
    }
}