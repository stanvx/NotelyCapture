package com.module.notelycompose.notes.domain.interfaces

import com.module.notelycompose.notes.domain.model.NoteDomainModel

/**
 * Interface for retrieving a note by its ID.
 * This contract defines the business logic for fetching a specific note.
 */
interface GetNoteByIdUseCaseContract {
    /**
     * Execute the use case to get a note by ID
     * @param id The ID of the note to retrieve
     * @return The note if found, null otherwise
     */
    fun execute(id: Long): NoteDomainModel?
}