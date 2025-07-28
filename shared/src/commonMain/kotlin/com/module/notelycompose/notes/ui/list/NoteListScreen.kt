package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.module.notelycompose.audio.presentation.AudioPlayerViewModel
import com.module.notelycompose.notes.presentation.list.NoteListIntent
import com.module.notelycompose.notes.presentation.list.NoteListPresentationState
import com.module.notelycompose.notes.presentation.list.NoteListViewModel
import com.module.notelycompose.notes.ui.components.SpeedDialFAB
import com.module.notelycompose.notes.ui.components.UnifiedNoteCard
import com.module.notelycompose.notes.ui.components.NoteCardLayoutMode
import com.module.notelycompose.notes.ui.list.model.NoteUiModel
import com.module.notelycompose.notes.ui.share.ShareDialog
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.notes.utils.ShareUtils
import com.module.notelycompose.platform.presentation.PlatformUiState
import com.module.notelycompose.platform.presentation.PlatformViewModel
import kotlinx.coroutines.launch
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.note_list_add_note
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    navigateToSettings: () -> Unit,
    navigateToMenu: () -> Unit,
    navigateToNoteDetails: (String) -> Unit,
    navigateToQuickRecord: () -> Unit,
    viewModel: NoteListViewModel = koinViewModel(),
    platformUiState: PlatformUiState,
    onScrollStateChanged: (LazyStaggeredGridState) -> Unit = {}
) {
    val notesListState by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val lazyStaggeredGridState = rememberLazyStaggeredGridState()
    
    val sharedAudioPlayerViewModel: AudioPlayerViewModel = koinViewModel()
    val sharedAudioPlayerUiState by sharedAudioPlayerViewModel.uiState.collectAsState()
    
    val platformViewModel: PlatformViewModel = koinViewModel()
    
    var showShareDialog by remember { mutableStateOf(false) }
    var selectedNoteForSharing by remember { mutableStateOf<NoteUiModel?>(null) }
    
    DisposableEffect(sharedAudioPlayerViewModel) {
        onDispose {
            sharedAudioPlayerViewModel.onClear()
        }
    }
    
    onScrollStateChanged(lazyStaggeredGridState)
    
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        TopBar(
            onMenuClicked = {
               navigateToMenu()
            },
            onSettingsClicked = {
              navigateToSettings()
            },
            scrollBehavior = scrollBehavior
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {
            if(notesListState.showEmptyContent) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NoteListHeader(
                        noteCount = notesListState.filteredNotes.size,
                        isTablet = platformUiState.isTablet
                    )
                    
                    if (notesListState.isSearchActive) {
                        SearchBar(
                            onSearchByKeyword = { keyword ->
                                viewModel.onProcessIntent(NoteListIntent.OnSearchNote(keyword))
                            },
                            onActiveChange = { isActive ->
                                viewModel.onProcessIntent(NoteListIntent.OnToggleSearch(isActive))
                            },
                            externalActivation = true
                        )
                    }
                    
                    FilterTabBar(
                        selectedTabTitle = notesListState.selectedTabTitle,
                        onFilterTabItemClicked = { title ->
                            viewModel.onProcessIntent(NoteListIntent.OnFilterNote(title))
                        }
                    )
                    
                    EmptyNoteUi(platformUiState.isTablet)
                }
            } else {
                NoteListWithHeader(
                    noteList = viewModel.onGetUiState(notesListState),
                    notesListState = notesListState,
                    platformUiState = platformUiState,
                    viewModel = viewModel,
                    lazyStaggeredGridState = lazyStaggeredGridState,
                    navigateToNoteDetails = navigateToNoteDetails,
                    sharedAudioPlayerViewModel = sharedAudioPlayerViewModel,
                    sharedAudioPlayerUiState = sharedAudioPlayerUiState,
                    onShareClick = { note ->
                        selectedNoteForSharing = note
                        showShareDialog = true
                    }
                )
            }
        }
    }
    
    if (showShareDialog && selectedNoteForSharing != null) {
        ShareDialog(
            onShareAudioRecording = {
                selectedNoteForSharing?.let { note ->
                    if (ShareUtils.canShareRecording(note.recordingPath)) {
                        platformViewModel.shareRecording(note.recordingPath!!)
                    }
                }
                showShareDialog = false
                selectedNoteForSharing = null
            },
            onShareTexts = {
                selectedNoteForSharing?.let { note ->
                    val shareText = ShareUtils.buildShareText(note)
                    platformViewModel.shareText(shareText)
                }
                showShareDialog = false
                selectedNoteForSharing = null
            },
            onDismiss = { 
                showShareDialog = false
                selectedNoteForSharing = null
            }
        )
    }

}

@Composable
private fun NoteListWithHeader(
    noteList: List<NoteUiModel>,
    notesListState: NoteListPresentationState,
    platformUiState: PlatformUiState,
    viewModel: NoteListViewModel,
    lazyStaggeredGridState: LazyStaggeredGridState,
    navigateToNoteDetails: (String) -> Unit,
    sharedAudioPlayerViewModel: AudioPlayerViewModel,
    sharedAudioPlayerUiState: com.module.notelycompose.audio.presentation.AudioPlayerPresentationState,
    onShareClick: (NoteUiModel) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 280.dp),
        state = lazyStaggeredGridState,
        modifier = Modifier.fillMaxSize(),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(
            start = 8.dp,
            end = 8.dp,
            bottom = 88.dp
        )
    ) {
        item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
            Column {
                NoteListHeader(
                    noteCount = notesListState.filteredNotes.size,
                    isTablet = platformUiState.isTablet
                )
                
                if (notesListState.isSearchActive) {
                    SearchBar(
                        onSearchByKeyword = { keyword ->
                            viewModel.onProcessIntent(NoteListIntent.OnSearchNote(keyword))
                        },
                        onActiveChange = { isActive ->
                            viewModel.onProcessIntent(NoteListIntent.OnToggleSearch(isActive))
                        },
                        externalActivation = true
                    )
                }
                
                FilterTabBar(
                    selectedTabTitle = notesListState.selectedTabTitle,
                    onFilterTabItemClicked = { title ->
                        viewModel.onProcessIntent(NoteListIntent.OnFilterNote(title))
                    }
                )
            }
        }
        
        itemsIndexed(
            items = noteList,
            key = { _, note -> note.id }
        ) { index, note ->
            UnifiedNoteCard(
                note = note,
                layoutMode = NoteCardLayoutMode.LIST,
                onClick = {
                    navigateToNoteDetails("${note.id}")
                },
                onShareClick = { noteId ->
                    onShareClick(note)
                },
                onEditClick = { noteId ->
                    navigateToNoteDetails("$noteId")
                },
                onDeleteClick = { noteId ->
                    viewModel.onProcessIntent(NoteListIntent.OnNoteDeleted(note))
                },
                audioPlayerViewModel = sharedAudioPlayerViewModel,
                audioPlayerUiState = sharedAudioPlayerViewModel.onGetUiState(sharedAudioPlayerUiState),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
