package com.module.notelycompose.domain.repository

import com.module.notelycompose.domain.model.Note

/**
 * Repository interface for note operations.
 * This defines the contract for data access without specifying implementation details.
 */
interface NoteRepository {
    
    /**
     * Insert a new note into the repository
     * @param note The note to insert
     */
    suspend fun insertNote(note: Note)
    
    /**
     * Delete a note from the repository
     * @param note The note to delete
     */
    suspend fun deleteNote(note: Note)
    
    /**
     * Get a note by its ID
     * @param id The ID of the note to retrieve
     * @return The note if found, null otherwise
     */
    suspend fun getNoteById(id: Long): Note?
    
    /**
     * Get all notes from the repository
     * @return List of all notes, sorted by timestamp (most recent first)
     */
    suspend fun getAllNotes(): List<Note>
    
    /**
     * Update an existing note
     * @param note The note with updated information
     */
    suspend fun updateNote(note: Note)
    
    /**
     * Get all starred notes
     * @return List of starred notes, sorted by timestamp (most recent first)
     */
    suspend fun getStarredNotes(): List<Note> {
        return getAllNotes().filter { it.isStarred }
    }
    
    /**
     * Get all voice notes (notes with audio)
     * @return List of voice notes, sorted by timestamp (most recent first)
     */
    suspend fun getVoiceNotes(): List<Note> {
        return getAllNotes().filter { it.isVoiceNote() }
    }
    
    /**
     * Search notes by query
     * @param query The search query
     * @return List of notes matching the query
     */
    suspend fun searchNotes(query: String): List<Note> {
        return getAllNotes().filter { it.containsQuery(query) }
    }
}