package com.module.notelycompose

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.module.notelycompose.notes.ui.detail.DownloaderActions
import com.module.notelycompose.notes.ui.detail.NoteActions
import com.module.notelycompose.notes.ui.detail.NoteAudioActions
import com.module.notelycompose.notes.ui.detail.NoteDetailScreen
import com.module.notelycompose.notes.ui.detail.NoteFormatActions
import com.module.notelycompose.notes.ui.detail.ShareActions
import com.module.notelycompose.notes.ui.detail.TranscriptionActions
import com.module.notelycompose.notes.ui.theme.MyApplicationTheme

fun NoteDetailController(
    noteId: String? = null,
    onSaveClicked: () -> Unit,
    onNavigateBack: () -> Unit
) = ComposeUIViewController(
    configure = {
        onFocusBehavior = OnFocusBehavior.DoNothing
    }
) {
    MyApplicationTheme {

        val audioPlayerModule = AudioPlayerModule()
        val audioPlayerViewModel = remember {
            IOSAudioPlayerViewModel(
                audioPlayer = audioPlayerModule.platformAudioPlayer,
                mapper = audioPlayerModule.audioPlayerPresentationToUiMapper
            )
        }
        val audioPlayerPresentationState by audioPlayerViewModel.state.collectAsState()
        val audioPlayerState = audioPlayerViewModel.onGetUiState(audioPlayerPresentationState)

        val audioRecorderModule = AudioRecorderModule()
        val audioRecorderViewModel = remember {
            IOSAudioRecorderViewModel(
                audioRecorder = audioRecorderModule.audioRecorder,
                mapper = audioRecorderModule.audioRecorderPresentationToUiMapper
            )
        }


        val transcriptionModule = TranscriptionModule()
        val platformUtilsModule = PlatformModule()
        val transcriptionViewModel = remember {
            IOSTranscriptionViewModel(
                transcriber = transcriptionModule.mTranscriber,
                platformUtils = platformUtilsModule.platformUtils
            )
        }


        val modelDownloaderViewModel = remember {
            IOSModelDownloaderViewModel(
                downloader = transcriptionModule.downloader,
                transcriber = transcriptionModule.mTranscriber
            )
        }

        val platformViewModel = remember {
            IOSPlatformViewModel(
                platformUtils = platformUtilsModule.platformUtils,
                platformInfo = platformUtilsModule.platformInfo
            )
        }


        val transcriptionState by transcriptionViewModel.state.collectAsState()
        val downloadingState by modelDownloaderViewModel.state.collectAsState()
        val audioRecorderPresentationState by audioRecorderViewModel.state.collectAsState()
        val audioRecorderState = audioRecorderViewModel.onGetUiState(audioRecorderPresentationState)

        val appModule = AppModule()
        val editorViewModel = remember {
            IOSTextEditorViewModel(
                getNoteByIdUseCase = appModule.getNoteById,
                deleteNoteUseCase = appModule.deleteNoteById,
                insertNoteUseCase = appModule.insertNote,
                updateNoteUseCase = appModule.updateNote,
                getLastNoteUseCase = appModule.getLastNoteUseCase,
                editorPresentationToUiStateMapper = appModule.editorPresentationToUiStateMapper,
                textFormatPresentationMapper = appModule.textFormatPresentationMapper,
                textAlignPresentationMapper = appModule.textAlignPresentationMapper,
                textEditorHelper = appModule.textEditorHelper
            )
        }
        if (noteId != null) editorViewModel.onGetNoteById(noteId)
        val editorPresentationState by editorViewModel.state.collectAsState()
        val editorState = editorViewModel.onGetUiState(editorPresentationState)

        val downloadActions = DownloaderActions(
            checkModelAvailability = modelDownloaderViewModel::checkModelAvailability,
            startDownload = modelDownloaderViewModel::startDownload
        )
        val formatActions = NoteFormatActions(
            onToggleBold = editorViewModel::onToggleBold,
            onToggleItalic = editorViewModel::onToggleItalic,
            onToggleUnderline = editorViewModel::onToggleUnderline,
            onSetAlignment = editorViewModel::onSetAlignment,
            onToggleBulletList = editorViewModel::onToggleBulletList,
            onSelectTextSizeFormat = editorViewModel::setTextSize
        )

        val audioActions = NoteAudioActions(
            onStartRecord = audioRecorderViewModel::onStartRecording,
            onStopRecord = audioRecorderViewModel::onStopRecording,
            onRequestAudioPermission = audioRecorderViewModel::onRequestAudioPermission,
            onAfterRecord = { editorViewModel.onUpdateRecordingPath(audioRecorderState.recordingPath) },
            onDeleteRecord = { editorViewModel.onDeleteRecord() },
            onLoadAudio = audioPlayerViewModel::onLoadAudio,
            onClear = audioPlayerViewModel::onCleared,
            onSeekTo = audioPlayerViewModel::onSeekTo,
            onTogglePlayPause = audioPlayerViewModel::onTogglePlayPause,
            setupRecorder = audioRecorderViewModel::setupRecorder,
            finishRecorder = audioRecorderViewModel::finishRecorder,
            onPauseRecording = audioRecorderViewModel::onPauseRecording,
            onResumeRecording = audioRecorderViewModel::onResumeRecording
        )

        val transcriptionActions = TranscriptionActions(
            requestAudioPermission = transcriptionViewModel::requestAudioPermission,
            initRecognizer = transcriptionViewModel::initRecognizer,
            finishRecognizer = transcriptionViewModel::finishRecognizer,
            startRecognizer = transcriptionViewModel::startRecognizer,
            stopRecognition = transcriptionViewModel::stopRecognizer,
            summarize =  transcriptionViewModel::summarize
        )

        val noteActions = NoteActions(
            onDeleteNote = {
                editorViewModel.onDeleteNote()
                onNavigateBack()
            },
            onStarNote = editorViewModel::onToggleStar
        )

        val onShareActions = ShareActions(
            shareText = platformViewModel::shareText,
            shareRecording = platformViewModel::shareRecording
        )

        NoteDetailScreen(
            newNoteDateString = editorState.createdAt,
            editorState = editorState,
            audioPlayerUiState = audioPlayerState,
            recordCounterString = audioRecorderState.recordCounterString,
            onNavigateBack = onNavigateBack,
            onUpdateContent = editorViewModel::onUpdateContent,
            onFormatActions = formatActions,
            onAudioActions = audioActions,
            onNoteActions = noteActions,
            onTranscriptionActions = transcriptionActions,
            transcriptionUiState = transcriptionState,
            downloaderUiState = downloadingState,
            downloaderEffect = modelDownloaderViewModel.effect,
            onDownloaderActions = downloadActions,
            isRecordPaused = audioRecorderState.isRecordPaused,
            onShareActions = onShareActions
        )
    }
}
