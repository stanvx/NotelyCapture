package com.module.notelycompose.domain.usecases

import com.module.notelycompose.domain.model.Note
import com.module.notelycompose.domain.repository.NoteRepository

/**
 * Use case for updating an existing note in the repository.
 * Encapsulates the business logic for note updates.
 */
class UpdateNoteUseCase(
    private val repository: NoteRepository
) {
    /**
     * Execute the use case to update a note
     * @param note The note with updated information
     */
    suspend operator fun invoke(note: Note) {
        // Validate note before updating
        require(note.isValid()) { "Note must have title, content, or audio" }
        require(note.id > 0L) { "Note must have a valid ID for updates" }
        
        repository.updateNote(note)
    }
}