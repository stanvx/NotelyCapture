package com.module.notelycompose.domain.usecases

import com.module.notelycompose.domain.model.Note
import com.module.notelycompose.domain.repository.NoteRepository

/**
 * Use case for adding a new note to the repository.
 * Encapsulates the business logic for note creation.
 */
class AddNoteUseCase(
    private val repository: NoteRepository
) {
    /**
     * Execute the use case to add a note
     * @param note The note to add
     */
    suspend operator fun invoke(note: Note) {
        // Validate note before adding
        require(note.isValid()) { "Note must have title, content, or audio" }
        
        // Ensure timestamp is set
        val noteToAdd = if (note.timestamp == 0L) {
            note.copy(timestamp = System.currentTimeMillis())
        } else {
            note
        }
        
        repository.insertNote(noteToAdd)
    }
}