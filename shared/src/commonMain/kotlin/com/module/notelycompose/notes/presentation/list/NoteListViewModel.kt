package com.module.notelycompose.notes.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.utils.deleteFile
import com.module.notelycompose.notes.domain.DeleteNoteById
import com.module.notelycompose.notes.domain.GetAllNotesUseCase
import com.module.notelycompose.notes.domain.model.NoteDomainModel
import com.module.notelycompose.notes.domain.model.NotesFilterDomainModel
import com.module.notelycompose.notes.presentation.helpers.getFirstNonEmptyLineAfterFirst
import com.module.notelycompose.notes.presentation.helpers.returnFirstLine
import com.module.notelycompose.notes.presentation.helpers.truncateWithEllipsis
import com.module.notelycompose.notes.presentation.list.mapper.NotesFilterMapper
import com.module.notelycompose.notes.presentation.list.model.NotePresentationModel
import com.module.notelycompose.notes.presentation.list.model.QuickRecordState
import com.module.notelycompose.notes.presentation.mapper.NotePresentationMapper
import com.module.notelycompose.notes.ui.list.model.NoteUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val DEFAULT_TITLE = "New Note"
const val DEFAULT_CONTENT = "No additional text"
const val CONTENT_LENGTH = 36
private const val SEARCH_DEBOUNCE = 300L

class NoteListViewModel(
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val deleteNoteById: DeleteNoteById,
    private val notePresentationMapper: NotePresentationMapper,
    private val notesFilterMapper: NotesFilterMapper,
) :ViewModel(){
    private val _state = MutableStateFlow(NoteListPresentationState())
    val state: StateFlow<NoteListPresentationState> = _state

    // Search query flow
    private val searchQuery = MutableStateFlow("")

    init {
        setupNoteFlow()
        setupSearch()
    }

    private fun setupSearch() {
        // Combine notes flow with filter and search

        searchQuery.debounce(SEARCH_DEBOUNCE)
            .onEach { query ->
                _state.update { currentState ->
                    val filtered = applyFilters(_state.value.originalNotes, _state.value.selectedTabTitle, query)
                    currentState.copy(
                        filteredNotes = filtered,
                        showEmptyContent = filtered.isEmpty()
                    )
                }
            }.launchIn(viewModelScope)
    }

    fun onProcessIntent(intent: NoteListIntent) {
        when (intent) {
            is NoteListIntent.OnNoteDeleted -> handleNoteDeletion(intent.note)
            is NoteListIntent.OnFilterNote -> setSelectedTab(intent.filter)
            is NoteListIntent.OnSearchNote -> searchQuery.value = intent.keyword
            is NoteListIntent.OnToggleSearch -> handleToggleSearch(intent.isActive)
            
            // Quick Record Intents
            is NoteListIntent.OnQuickRecordStarted -> handleQuickRecordStarted()
            is NoteListIntent.OnQuickRecordCompleted -> handleQuickRecordCompleted()
            is NoteListIntent.OnQuickRecordError -> handleQuickRecordError(intent.error)
            is NoteListIntent.OnQuickRecordReset -> handleQuickRecordReset()
        }
    }

    private fun setupNoteFlow() {
        // Combine notes flow with filter and search
        combine(
            getAllNotesUseCase.execute(),
            _state.map { it.selectedTabTitle }.distinctUntilChanged(),
            ) { notes, filter ->
            Pair(notes, filter)
        }.onEach { (notes, filter) ->
            viewModelScope.launch {
                handleNotesUpdate(notes, filter, "")
            }
        }.launchIn(viewModelScope)
    }

    private suspend fun domainToPresentationModel(note: NoteDomainModel): NotePresentationModel {
        val retrievedNote = notePresentationMapper.mapToPresentationModel(note)
        return retrievedNote.copy(
            title = note.title.trim().takeIf { it.isNotEmpty() }
                ?.returnFirstLine()
                ?.truncateWithEllipsis()
                ?: note.content.trim().returnFirstLine().truncateWithEllipsis().takeIf { it.isNotEmpty() }
                ?: DEFAULT_TITLE,
            content = note.content.trim().takeIf { it.isNotEmpty() }
                ?.getFirstNonEmptyLineAfterFirst()
                ?.truncateWithEllipsis(CONTENT_LENGTH)
                ?: DEFAULT_CONTENT
        )
    }

    private fun applyFilters(
        notes: List<NotePresentationModel>,
        filter: String,
        query: String
    ): List<NotePresentationModel> {
        val domainFilter = notesFilterMapper.mapToDomainModel(
            notesFilterMapper.mapStringToPresentationModel(filter)
        )
        if (query.isBlank() && (domainFilter == NotesFilterDomainModel.ALL || domainFilter == NotesFilterDomainModel.RECENT)) {
            return notes
        }
        return notes.filter { note ->
            matchesFilter(note, domainFilter) && matchesSearch(
                note,
                query
            )
        }
    }

    private suspend fun handleNotesUpdate(
        notes: List<NoteDomainModel>,
        filter: String,
        query: String
    ) {
        // Map notes to presentation models with async duration calculation
        val presentationNotes = notes.map { note ->
            domainToPresentationModel(note)
        }

        _state.update { currentState ->
            currentState.copy(
                originalNotes = presentationNotes,
                filteredNotes = applyFilters(presentationNotes, filter, query),
                showEmptyContent = presentationNotes.isEmpty()
            )
        }
    }

    private fun matchesFilter(note: NotePresentationModel, filter: NotesFilterDomainModel): Boolean {
        return when (filter) {
            NotesFilterDomainModel.VOICES -> isVoiceNote(note)
            NotesFilterDomainModel.STARRED -> isStarred(note)
            NotesFilterDomainModel.ALL, NotesFilterDomainModel.RECENT -> true
        }
    }

    private fun matchesSearch(note: NotePresentationModel, query: String): Boolean {
        if (query.isBlank()) return true
        return note.title.contains(query, ignoreCase = true) ||
                note.content.contains(query, ignoreCase = true)
    }

    private fun handleNoteDeletion(note: NoteUiModel) {
        viewModelScope.launch {
            deleteFile(note.recordingPath)
            deleteNoteById.execute(note.id)
            // No need to manually refresh - flow will handle it
        }
    }

    fun onGetUiState(presentationState: NoteListPresentationState): List<NoteUiModel> {
        return presentationState.filteredNotes.map { notePresentationMapper.mapToUiModel(it) }
    }

    private fun setSelectedTab(tabTitle: String) {
        _state.value = _state.value.copy(selectedTabTitle = tabTitle)
    }

    private fun isVoiceNote(note: NotePresentationModel): Boolean {
        return note.recordingPath.isNotEmpty()
    }

    private fun isStarred(note: NotePresentationModel): Boolean {
        return note.isStarred
    }

    private fun handleToggleSearch(isActive: Boolean) {
        _state.update { currentState ->
            currentState.copy(isSearchActive = isActive)
        }
        // Clear search when closing search
        if (!isActive) {
            searchQuery.value = ""
        }
    }

    // Quick Record Handler Methods
    private fun handleQuickRecordStarted() {
        _state.update { currentState ->
            currentState.copy(
                quickRecordState = QuickRecordState.Recording,
                quickRecordError = null
            )
        }
    }

    private fun handleQuickRecordCompleted() {
        _state.update { currentState ->
            currentState.copy(
                quickRecordState = QuickRecordState.Complete,
                quickRecordError = null
            )
        }
        // Auto-reset state after completion
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000) // Show completion state for 2 seconds
            handleQuickRecordReset()
        }
    }

    private fun handleQuickRecordError(error: String) {
        _state.update { currentState ->
            currentState.copy(
                quickRecordState = QuickRecordState.Error,
                quickRecordError = error
            )
        }
    }

    private fun handleQuickRecordReset() {
        _state.update { currentState ->
            currentState.copy(
                quickRecordState = QuickRecordState.Idle,
                quickRecordError = null
            )
        }
    }

    /**
     * Updates quick record state to Processing during background transcription
     */
    fun updateQuickRecordToProcessing() {
        _state.update { currentState ->
            currentState.copy(
                quickRecordState = QuickRecordState.Processing,
                quickRecordError = null
            )
        }
    }
}
