package com.module.notelycompose.notes.domain.interfaces

/**
 * Interface for deleting a note by its ID.
 * This contract defines the business logic for note deletion operations.
 */
interface DeleteNoteByIdUseCase {
    /**
     * Execute the use case to delete a note by ID
     * @param id The ID of the note to delete
     */
    suspend fun execute(id: Long)
}