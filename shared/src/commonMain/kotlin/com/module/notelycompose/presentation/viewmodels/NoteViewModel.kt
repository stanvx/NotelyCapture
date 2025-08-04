package com.module.notelycompose.presentation.viewmodels

import com.module.notelycompose.domain.model.Note
import com.module.notelycompose.domain.usecases.AddNoteUseCase
import com.module.notelycompose.domain.usecases.DeleteNoteUseCase
import com.module.notelycompose.domain.usecases.GetAllNotesUseCase
import com.module.notelycompose.domain.usecases.UpdateNoteUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing note-related UI state and operations.
 * Handles the presentation logic for the notes screen.
 */
class NoteViewModel(
    private val addNoteUseCase: AddNoteUseCase,
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _state = MutableStateFlow(NoteState())
    val state: StateFlow<NoteState> = _state.asStateFlow()
    
    data class NoteState(
        val notes: List<Note> = emptyList(),
        val isLoading: Boolean = false,
        val searchQuery: String = "",
        val error: String? = null
    )
    
    init {
        loadNotes()
    }
    
    /**
     * Load all notes from the repository
     */
    fun loadNotes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val allNotes = getAllNotesUseCase()
                val filteredNotes = if (_state.value.searchQuery.isBlank()) {
                    allNotes
                } else {
                    allNotes.filter { it.containsQuery(_state.value.searchQuery) }
                }
                _state.value = _state.value.copy(
                    notes = filteredNotes,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    /**
     * Add a new note
     */
    fun addNote(note: Note) {
        viewModelScope.launch {
            try {
                addNoteUseCase(note)
                loadNotes() // Refresh the list
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to add note"
                )
            }
        }
    }
    
    /**
     * Delete a note
     */
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                deleteNoteUseCase(note)
                loadNotes() // Refresh the list
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to delete note"
                )
            }
        }
    }
    
    /**
     * Update search query and filter notes
     */
    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(searchQuery = query)
            loadNotes() // This will apply the filter
        }
    }
    
    /**
     * Clear search query and show all notes
     */
    fun clearSearch() {
        updateSearchQuery("")
    }
    
    /**
     * Toggle starred status of a note
     */
    fun toggleStarred(note: Note) {
        viewModelScope.launch {
            try {
                val updatedNote = note.copy(isStarred = !note.isStarred)
                updateNoteUseCase(updatedNote)
                loadNotes() // Refresh the list
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to update note"
                )
            }
        }
    }
    
    /**
     * Clear any error state
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}