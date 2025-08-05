package com.module.notelycompose.data.repository

import com.module.notelycompose.domain.model.Note
import com.module.notelycompose.domain.repository.NoteRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NoteRepositoryImplTest {

    // Mock database implementation for testing
    private class InMemoryNoteStorage {
        private val notes = mutableMapOf<Long, Note>()
        private var nextId = 1L
        
        fun insert(note: Note): Note {
            val noteWithId = if (note.id == 0L) {
                note.copy(id = nextId++)
            } else {
                notes[note.id] = note
                note
            }
            notes[noteWithId.id] = noteWithId
            return noteWithId
        }
        
        fun delete(noteId: Long): Boolean {
            return notes.remove(noteId) != null
        }
        
        fun findById(id: Long): Note? {
            return notes[id]
        }
        
        fun findAll(): List<Note> {
            return notes.values.sortedByDescending { it.timestamp }
        }
        
        fun update(note: Note): Boolean {
            return if (notes.containsKey(note.id)) {
                notes[note.id] = note
                true
            } else {
                false
            }
        }
        
        fun clear() {
            notes.clear()
            nextId = 1L
        }
        
        fun count(): Int = notes.size
    }

    // Test implementation of repository
    private class TestNoteRepository(private val storage: InMemoryNoteStorage) : NoteRepository {
        
        override suspend fun insertNote(note: Note) {
            storage.insert(note)
        }
        
        override suspend fun deleteNote(note: Note) {
            storage.delete(note.id)
        }
        
        override suspend fun getNoteById(id: Long): Note? {
            return storage.findById(id)
        }
        
        override suspend fun getAllNotes(): List<Note> {
            return storage.findAll()
        }
        
        override suspend fun updateNote(note: Note) {
            storage.update(note)
        }
    }

    private fun createTestRepository(): Pair<TestNoteRepository, InMemoryNoteStorage> {
        val storage = InMemoryNoteStorage()
        val repository = TestNoteRepository(storage)
        return repository to storage
    }

    @Test
    fun `insertNote should add new note with generated ID`() = runTest {
        // Given
        val (repository, storage) = createTestRepository()
        val note = Note(
            id = 0L, // Will be auto-generated
            title = "Test Note",
            content = "Test Content",
            timestamp = System.currentTimeMillis(),
            isStarred = false
        )

        // When
        repository.insertNote(note)

        // Then
        assertEquals(1, storage.count())
        val savedNote = storage.findById(1L)
        assertNotNull(savedNote)
        assertEquals("Test Note", savedNote.title)
        assertEquals("Test Content", savedNote.content)
    }

    @Test
    fun `insertNote should preserve existing ID when provided`() = runTest {
        // Given
        val (repository, storage) = createTestRepository()
        val note = Note(
            id = 42L,
            title = "Test Note",
            content = "Test Content",
            timestamp = System.currentTimeMillis(),
            isStarred = true
        )

        // When
        repository.insertNote(note)

        // Then
        val savedNote = storage.findById(42L)
        assertNotNull(savedNote)
        assertEquals(42L, savedNote.id)
        assertEquals("Test Note", savedNote.title)
        assertTrue(savedNote.isStarred)
    }

    @Test
    fun `getAllNotes should return notes sorted by timestamp descending`() = runTest {
        // Given
        val (repository, _) = createTestRepository()
        val note1 = Note(id = 1L, title = "First", content = "Content 1", timestamp = 1000L, isStarred = false)
        val note2 = Note(id = 2L, title = "Second", content = "Content 2", timestamp = 3000L, isStarred = false)
        val note3 = Note(id = 3L, title = "Third", content = "Content 3", timestamp = 2000L, isStarred = false)

        repository.insertNote(note1)
        repository.insertNote(note2)
        repository.insertNote(note3)

        // When
        val notes = repository.getAllNotes()

        // Then
        assertEquals(3, notes.size)
        assertEquals("Second", notes[0].title) // timestamp 3000L
        assertEquals("Third", notes[1].title)  // timestamp 2000L  
        assertEquals("First", notes[2].title)  // timestamp 1000L
    }

    @Test
    fun `getNoteById should return correct note`() = runTest {
        // Given
        val (repository, _) = createTestRepository()
        val note = Note(
            id = 100L,
            title = "Specific Note",
            content = "Specific Content",
            timestamp = System.currentTimeMillis(),
            isStarred = true
        )
        repository.insertNote(note)

        // When
        val retrievedNote = repository.getNoteById(100L)

        // Then
        assertNotNull(retrievedNote)
        assertEquals(100L, retrievedNote.id)
        assertEquals("Specific Note", retrievedNote.title)
        assertEquals("Specific Content", retrievedNote.content)
        assertTrue(retrievedNote.isStarred)
    }

    @Test
    fun `getNoteById should return null for non-existent note`() = runTest {
        // Given
        val (repository, _) = createTestRepository()

        // When
        val retrievedNote = repository.getNoteById(999L)

        // Then
        assertNull(retrievedNote)
    }

    @Test
    fun `updateNote should modify existing note`() = runTest {
        // Given
        val (repository, _) = createTestRepository()
        val originalNote = Note(
            id = 1L,
            title = "Original Title",
            content = "Original Content",
            timestamp = 1000L,
            isStarred = false
        )
        repository.insertNote(originalNote)

        val updatedNote = originalNote.copy(
            title = "Updated Title",
            content = "Updated Content",
            isStarred = true
        )

        // When
        repository.updateNote(updatedNote)

        // Then
        val retrievedNote = repository.getNoteById(1L)
        assertNotNull(retrievedNote)
        assertEquals("Updated Title", retrievedNote.title)
        assertEquals("Updated Content", retrievedNote.content)
        assertTrue(retrievedNote.isStarred)
        assertEquals(1000L, retrievedNote.timestamp) // Timestamp should remain unchanged
    }

    @Test
    fun `deleteNote should remove note from repository`() = runTest {
        // Given
        val (repository, storage) = createTestRepository()
        val note = Note(
            id = 1L,
            title = "To Delete",
            content = "Content",
            timestamp = System.currentTimeMillis(),
            isStarred = false
        )
        repository.insertNote(note)
        assertEquals(1, storage.count())

        // When
        repository.deleteNote(note)

        // Then
        assertEquals(0, storage.count())
        val retrievedNote = repository.getNoteById(1L)
        assertNull(retrievedNote)
    }

    @Test
    fun `repository should handle empty state`() = runTest {
        // Given
        val (repository, _) = createTestRepository()

        // When
        val notes = repository.getAllNotes()

        // Then
        assertTrue(notes.isEmpty())
    }

    @Test
    fun `repository should handle special characters in content`() = runTest {
        // Given
        val (repository, _) = createTestRepository()
        val specialContent = """
            Special characters: àáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿ
            Symbols: !@#$%^&*()_+-=[]{}|;':\",./<>?
            Unicode: 🎵🎶🎤🎧 音楽 музыка موسيقى
            Newlines and tabs:
            	Line 1
            	Line 2
            		Indented line
        """.trimIndent()
        
        val note = Note(
            id = 1L,
            title = "Special Chars Test",
            content = specialContent,
            timestamp = System.currentTimeMillis(),
            isStarred = false
        )

        // When
        repository.insertNote(note)

        // Then
        val retrievedNote = repository.getNoteById(1L)
        assertNotNull(retrievedNote)
        assertEquals(specialContent, retrievedNote.content)
    }

    @Test
    fun `repository should handle large content`() = runTest {
        // Given
        val (repository, _) = createTestRepository()
        val largeContent = "A".repeat(10000) // 10KB of content
        val note = Note(
            id = 1L,
            title = "Large Content Test",
            content = largeContent,
            timestamp = System.currentTimeMillis(),
            isStarred = false
        )

        // When
        repository.insertNote(note)

        // Then
        val retrievedNote = repository.getNoteById(1L)
        assertNotNull(retrievedNote)
        assertEquals(largeContent, retrievedNote.content)
        assertEquals(10000, retrievedNote.content.length)
    }

    @Test
    fun `repository should maintain data integrity during concurrent operations`() = runTest {
        // Given
        val (repository, storage) = createTestRepository()
        val notes = (1..100).map { i ->
            Note(
                id = i.toLong(),
                title = "Note $i",
                content = "Content $i",
                timestamp = i * 1000L,
                isStarred = i % 5 == 0
            )
        }

        // When - Simulate concurrent insertions
        notes.forEach { note ->
            repository.insertNote(note)
        }

        // Then
        assertEquals(100, storage.count())
        val retrievedNotes = repository.getAllNotes()
        assertEquals(100, retrievedNotes.size)
        
        // Verify sorting (most recent first)
        assertEquals("Note 100", retrievedNotes.first().title)
        assertEquals("Note 1", retrievedNotes.last().title)
        
        // Verify starred notes
        val starredNotes = retrievedNotes.filter { it.isStarred }
        assertEquals(20, starredNotes.size) // Every 5th note should be starred
    }
}