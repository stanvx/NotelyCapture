package com.module.notelycompose.domain.usecases

import com.module.notelycompose.domain.model.Note
import com.module.notelycompose.domain.repository.NoteRepository

/**
 * Use case for deleting a note from the repository.
 * Encapsulates the business logic for note deletion.
 */
class DeleteNoteUseCase(
    private val repository: NoteRepository
) {
    /**
     * Execute the use case to delete a note
     * @param note The note to delete
     */
    suspend operator fun invoke(note: Note) {
        repository.deleteNote(note)
        
        // Clean up associated audio file if it exists
        if (note.hasAudio && note.audioFilePath != null) {
            // In a real implementation, this would delete the audio file
            // For now, we just mark it as handled in the business logic
        }
    }
}