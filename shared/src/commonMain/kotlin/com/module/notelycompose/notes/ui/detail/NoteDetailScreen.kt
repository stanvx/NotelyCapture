package com.module.notelycompose.notes.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.module.notelycompose.audio.presentation.AudioPlayerViewModel
import com.module.notelycompose.audio.ui.player.PlatformAudioPlayerUi
import com.module.notelycompose.audio.ui.player.model.AudioPlayerUiState
import com.module.notelycompose.modelDownloader.DownloaderDialog
import com.module.notelycompose.modelDownloader.DownloaderEffect
import com.module.notelycompose.modelDownloader.ModelDownloaderViewModel
import com.module.notelycompose.audio.presentation.AudioImportViewModel
import com.module.notelycompose.audio.ui.importing.ImportingAudioStateHost
import com.module.notelycompose.notes.presentation.detail.TextEditorViewModel
import com.module.notelycompose.notes.ui.share.ShareDialog
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.platform.presentation.PlatformViewModel
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.confirmation_cancel
import com.module.notelycompose.resources.download_dialog_error
import com.module.notelycompose.resources.ic_transcription
import com.module.notelycompose.resources.note_detail_recorder
import com.module.notelycompose.resources.transcription_icon
import com.module.notelycompose.resources.vectors.IcRecorder
import com.module.notelycompose.resources.vectors.Images
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NoteDetailScreen(
    noteId: String,
    navigateBack: () -> Unit,
    navigateToRecorder: (noteId: String) -> Unit,
    navigateToTranscription: () -> Unit,
    audioPlayerViewModel: AudioPlayerViewModel = koinViewModel(),
    downloaderViewModel: ModelDownloaderViewModel = koinViewModel(),
    platformViewModel: PlatformViewModel = koinViewModel(),
    audioImportViewModel: AudioImportViewModel = koinViewModel(),
    editorViewModel: TextEditorViewModel
) {
    val currentNoteId by editorViewModel.currentNoteId.collectAsStateWithLifecycle()
    val importingState by audioImportViewModel.importingAudioState.collectAsStateWithLifecycle()
    val downloaderUiState by downloaderViewModel.uiState.collectAsStateWithLifecycle()
    val editorState = editorViewModel.editorPresentationState.collectAsStateWithLifecycle().value
        .let { editorViewModel.onGetUiState(it) }

    val audioPlayerUiState = audioPlayerViewModel.uiState.collectAsStateWithLifecycle().value
        .let { audioPlayerViewModel.onGetUiState(it) }

    var showFormatBar by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var showLoadingDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var isTextFieldFocused by remember { mutableStateOf(false) }
    var showDownloadQuestionDialog by remember { mutableStateOf(false) }
    var showExistingRecordConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (noteId.toLong() > 0L) {
            editorViewModel.onGetNoteById(noteId)
        }
        downloaderViewModel.effects.collect {
            when (it) {
                is DownloaderEffect.DownloadEffect -> {
                    showDownloadDialog = true
                    showDownloadQuestionDialog = false
                    showLoadingDialog = false
                }

                is DownloaderEffect.ErrorEffect -> {
                    showDownloadDialog = false
                    showErrorDialog = true
                    showLoadingDialog = false
                }

                is DownloaderEffect.ModelsAreReady -> {
                    showDownloadDialog = false
                    showLoadingDialog = false
                    navigateToTranscription()
                }

                is DownloaderEffect.AskForUserAcceptance -> {
                    showDownloadQuestionDialog = true
                    showLoadingDialog = false
                }

                is DownloaderEffect.CheckingEffect -> {
                    showLoadingDialog = true
                    showDownloadDialog = false
                }
            }
        }
    }
    Scaffold(
        topBar = {
            DetailNoteTopBar(
                onNavigateBack = navigateBack,
                onShare = {
                    showShareDialog = true
                },
                onExportAudio = {
                    platformViewModel.onExportAudio(editorState.recording.recordingPath)
                },
                onImportClick = {
                    audioPlayerViewModel.releasePlayer()
                    audioImportViewModel.importAudio()
                },
                isRecordingExist = editorState.recording.isRecordingExist
            )
        },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (editorState.recording.isRecordingExist) {
                    FloatingActionButton(
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = LocalCustomColors.current.floatActionButtonBorderColor,
                            shape = CircleShape
                        ),
                        containerColor = LocalCustomColors.current.bodyBackgroundColor,
                        onClick = { downloaderViewModel.checkTranscriptionAvailability() }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_transcription),
                            contentDescription = stringResource(Res.string.transcription_icon),
                            tint = LocalCustomColors.current.bodyContentColor
                        )
                    }
                }

                FloatingActionButton(
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = LocalCustomColors.current.floatActionButtonBorderColor,
                        shape = CircleShape
                    ),
                    containerColor = LocalCustomColors.current.bodyBackgroundColor,
                    onClick = {
                        if (!editorState.recording.isRecordingExist) {
                            navigateToRecorder("$currentNoteId")
                        } else {
                            showExistingRecordConfirmDialog = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Images.Icons.IcRecorder,
                        contentDescription = stringResource(Res.string.note_detail_recorder),
                        tint = LocalCustomColors.current.bodyContentColor
                    )
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
                isTextFieldFocused = isTextFieldFocused,
                selectionSize = editorState.selectionSize,
                isStarred = editorState.isStarred,
                showFormatBar = showFormatBar,
                textFieldFocusRequester = focusRequester,
                onShowTextFormatBar = { showFormatBar = it },
                editorViewModel = editorViewModel,
                navigateBack = navigateBack
            )
        }
    ) { paddingValues ->

        NoteContent(
            paddingValues = paddingValues,
            newNoteDateString = editorState.createdAt,
            editorState = editorState,
            showFormatBar = showFormatBar,
            focusRequester = focusRequester,
            audioPlayerUiState = audioPlayerUiState,
            textEditorViewModel = editorViewModel,
            audioPlayerViewModel = audioPlayerViewModel,
            onFocusChange = {
                isTextFieldFocused = it
            },
        )
    }


    if (showDownloadDialog) {
        LocalSoftwareKeyboardController.current?.hide()
        DownloaderDialog(
            modifier = Modifier.height(100.dp),
            downloaderUiState,
            onDismiss = { showDownloadDialog = false }
        )
    }

    if (showErrorDialog) {
        LocalSoftwareKeyboardController.current?.hide()
        AlertDialog(
            modifier = Modifier.height(100.dp),
            title = { Text(stringResource(resource = Res.string.download_dialog_error)) },
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                    },
                ) { Text(stringResource(resource = Res.string.confirmation_cancel)) }
            }
        )
    }
    if (showDownloadQuestionDialog) {
        LocalSoftwareKeyboardController.current?.hide()
        DownloadModelDialog(
            onDownload = {
                downloaderViewModel.startDownload()
                showDownloadQuestionDialog = false
            },
            onCancel = {
                showDownloadQuestionDialog = false
            }
        )
    }


    if (showLoadingDialog) {
        PreparingLoadingDialog()
    }
    ReplaceRecordingConfirmationDialog(
        showDialog = showExistingRecordConfirmDialog,
        onDismiss = {
            showExistingRecordConfirmDialog = false
        },
        onConfirm = {
            navigateToRecorder("$currentNoteId")
        },
        option = RecordingConfirmationUiModel.Record
    )

    if (showShareDialog) {
        ShareDialog(
            onShareAudioRecording = {
                platformViewModel.shareRecording(editorState.recording.recordingPath)
            },
            onShareTexts = {
                platformViewModel.shareText(editorState.content.text)
            },
            onDismiss = { showShareDialog = false }
        )
    }

    ImportingAudioStateHost(
        state = importingState,
        onSuccess = editorViewModel::onUpdateRecordingPath,
        onRelease = audioImportViewModel::releaseState
    )
}


@Composable
private fun NoteContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    newNoteDateString: String,
    editorState: EditorUiState,
    showFormatBar: Boolean,
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
    audioPlayerUiState: AudioPlayerUiState,
    textEditorViewModel: TextEditorViewModel,
    audioPlayerViewModel: AudioPlayerViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var showDeleteRecordingDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(editorState.content) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(scrollState)
            .background(LocalCustomColors.current.bodyBackgroundColor)
            .imePadding()
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            DateHeader(newNoteDateString)

            if (editorState.recording.isRecordingExist) {

                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                    LaunchedEffect(Unit) {
                        showDeleteRecordingDialog = true
                    }
                }

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        // Background that appears when swiping
                        Box(
                            modifier = Modifier
                                .width(800.dp)
                                .height(36.dp)
                                .padding(horizontal = 16.dp, vertical = 0.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Red),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    },
                    content = {
                        PlatformAudioPlayerUi(
                            filePath = editorState.recording.recordingPath,
                            uiState = audioPlayerUiState,
                            onLoadAudio = audioPlayerViewModel::onLoadAudio,
                            onClear = audioPlayerViewModel::onClear,
                            onSeekTo = audioPlayerViewModel::onSeekTo,
                            onTogglePlayPause = audioPlayerViewModel::onTogglePlayPause,
                            onTogglePlaybackSpeed = audioPlayerViewModel::onTogglePlaybackSpeed
                        )
                    }
                )
            }

            NoteEditor(
                modifier = Modifier.fillMaxWidth().weight(1f),
                editorState = editorState,
                showFormatBar = showFormatBar,
                focusRequester = focusRequester,
                onFocusChange = onFocusChange,
                textEditorViewModel = textEditorViewModel
            )
        }
    }
    DeleteRecordingConfirmationDialog(
        showDialog = showDeleteRecordingDialog,
        onDismiss = {
            showDeleteRecordingDialog = false
            coroutineScope.launch {
                dismissState.reset()
            }
        },
        onConfirm = {
            textEditorViewModel.onDeleteRecord()
        }
    )
}


@Composable
private fun DateHeader(dateString: String) {
    Text(
        text = dateString,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        fontSize = 12.sp,
        color = LocalCustomColors.current.bodyContentColor
    )
}

@Composable
private fun NoteEditor(
    modifier: Modifier = Modifier,
    editorState: EditorUiState,
    showFormatBar: Boolean,
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
    textEditorViewModel: TextEditorViewModel
) {

    val transformation = VisualTransformation { text ->
        TransformedText(
            buildAnnotatedString {
                append(text)
                editorState.formats.forEach { format ->
                    addStyle(
                        SpanStyle(
                            fontWeight = if (format.isBold) FontWeight.Bold else null,
                            fontStyle = if (format.isItalic) FontStyle.Italic else null,
                            textDecoration = if (format.isUnderline)
                                TextDecoration.Underline else null,
                            fontSize = format.textSize?.sp ?: TextUnit.Unspecified
                        ),
                        format.range.first.coerceIn(0, text.length),
                        format.range.last.coerceIn(0, text.length)
                    )
                }
            },
            OffsetMapping.Identity
        )
    }

    BasicTextField(
        value = editorState.content,
        onValueChange = textEditorViewModel::onUpdateContent,
        modifier =
            modifier
                .focusRequester(focusRequester)
                .padding(horizontal = 16.dp)
                .onFocusChanged {
                    onFocusChange(it.isFocused)
                },
        textStyle = TextStyle(
            color = LocalCustomColors.current.bodyContentColor,
            textAlign = editorState.textAlign
        ),
        cursorBrush = SolidColor(LocalCustomColors.current.bodyContentColor),
        readOnly = showFormatBar,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        ),
        visualTransformation = transformation
    )
}



