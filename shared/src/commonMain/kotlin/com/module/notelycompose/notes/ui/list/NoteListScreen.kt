package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.presentation.list.NoteListIntent
import com.module.notelycompose.notes.presentation.list.NoteListViewModel
import com.module.notelycompose.notes.ui.components.SpeedDialFAB
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.platform.presentation.PlatformUiState
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
    
    // Pass scroll state to parent
    onScrollStateChanged(lazyStaggeredGridState)
    
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        // TopBar
        TopBar(
            onMenuClicked = {
               navigateToMenu()
            },
            onSettingsClicked = {
              navigateToSettings()
            },
            scrollBehavior = scrollBehavior
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {
                // Filter Tab Bar (first item)
                FilterTabBar(
                    selectedTabTitle = notesListState.selectedTabTitle,
                    onFilterTabItemClicked = { title ->
                        viewModel.onProcessIntent(NoteListIntent.OnFilterNote(title))
                    }
                )
                
                // Note List Content (takes remaining space)
                if(notesListState.showEmptyContent) {
                    EmptyNoteUi(platformUiState.isTablet)
                } else {
                    NoteList(
                        noteList = viewModel.onGetUiState(notesListState),
                        onNoteClicked = { id ->
                            navigateToNoteDetails("$id")
                        },
                        onNoteDeleteClicked = {
                            viewModel.onProcessIntent(NoteListIntent.OnNoteDeleted(it))
                        },
                        lazyStaggeredGridState = lazyStaggeredGridState
                    )
                }
            }
        }

}
