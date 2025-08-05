package com.module.notelycompose.domain.usecases

import com.module.notelycompose.domain.model.Note
import com.module.notelycompose.domain.repository.NoteRepository

/**
 * Use case for retrieving all notes from the repository.
 * Encapsulates the business logic for fetching and ordering notes.
 */
class GetAllNotesUseCase(
    private val repository: NoteRepository
) {
    /**
     * Execute the use case to get all notes
     * @return List of all notes, sorted by timestamp (most recent first)
     */
    suspend operator fun invoke(): List<Note> {
        return repository.getAllNotes()
    }
}