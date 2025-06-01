package com.module.notelycompose.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.module.notelycompose.Platform
import com.module.notelycompose.android.di.AudioRecorderSpeechModule
import com.module.notelycompose.android.presentation.AndroidAudioPlayerViewModel
import com.module.notelycompose.android.presentation.AndroidAudioRecorderViewModel
import com.module.notelycompose.android.presentation.AndroidModelDownloaderViewModel
import com.module.notelycompose.android.presentation.AndroidNoteListViewModel
import com.module.notelycompose.android.presentation.AndroidTranscriptionViewModel
import com.module.notelycompose.android.presentation.AndroidOnboardingViewModel
import com.module.notelycompose.android.presentation.AndroidPlatformViewModel
import com.module.notelycompose.android.presentation.AndroidTextEditorViewModel
import com.module.notelycompose.android.presentation.core.Routes
import com.module.notelycompose.android.presentation.ui.NoteListScreen
import com.module.notelycompose.notes.ui.detail.DownloaderActions
import com.module.notelycompose.notes.ui.detail.NoteActions
import com.module.notelycompose.notes.ui.detail.NoteAudioActions
import com.module.notelycompose.notes.ui.detail.NoteDetailScreen
import com.module.notelycompose.notes.ui.detail.NoteFormatActions
import com.module.notelycompose.notes.ui.detail.ShareActions
import com.module.notelycompose.notes.ui.detail.TranscriptionActions
import com.module.notelycompose.notes.ui.theme.MyApplicationTheme
import com.module.notelycompose.onboarding.presentation.model.OnboardingState
import com.module.notelycompose.onboarding.ui.OnboardingWalkthrough
import com.module.notelycompose.platform.presentation.PlatformViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val NOTE_ID_PARAM = "noteId"
private const val DEFAULT_NOTE_ID = "0"
private const val ROUTE_SEPARATOR = "/"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var permissionLauncherHolder: AudioRecorderSpeechModule.PermissionLauncherHolder
    @Inject
    lateinit var platformInfo: Platform

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        initializeAudioRecorder()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NoteAppRoot()
//                    val viewmodel = hiltViewModel<AndroidOnboardingViewModel>()
//                    val onboardingState by viewmodel.state.collectAsState()
//
//                    when (onboardingState) {
//                        is OnboardingState.Initial -> Unit
//                        is OnboardingState.NotCompleted -> {
//                            OnboardingWalkthrough(
//                                onFinish = {
//                                    viewmodel.onCompleteOnboarding()
//                                }
//                            )
//                        }
//                        is OnboardingState.Completed -> NoteAppRoot()
//                    }
                }
            }
        }
    }

    private fun initializeAudioRecorder() {
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {isGranted->

        }
        permissionLauncherHolder.permissionLauncher = permissionLauncher

    }

    override fun onDestroy() {
        super.onDestroy()
        permissionLauncherHolder.permissionLauncher = null
    }
}

@Composable
fun NoteAppRoot() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.LIST
    ) {
        composable(route = Routes.LIST) {
            val viewmodel = hiltViewModel<AndroidNoteListViewModel>()
            val platformViewModel = hiltViewModel<AndroidPlatformViewModel>()
            NoteListScreen(
                androidNoteListViewModel = viewmodel,
                platformViewModel = platformViewModel,
                onFloatingActionButtonClicked = {
                    navController.navigate(Routes.DETAIL + ROUTE_SEPARATOR + DEFAULT_NOTE_ID)
                },
                onNoteClicked = {
                    navController.navigate(Routes.DETAIL + ROUTE_SEPARATOR + it)
                }
            )
        }

        composable(
            route = Routes.DETAIL + ROUTE_SEPARATOR + "{$NOTE_ID_PARAM}",
            arguments = listOf(
                navArgument(NOTE_ID_PARAM) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString(NOTE_ID_PARAM) ?: DEFAULT_NOTE_ID
            NoteDetailWrapper(
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun NoteDetailWrapper(
    noteId: String,
    onNavigateBack: () -> Unit
) {
    val audioPlayerViewModel = hiltViewModel<AndroidAudioPlayerViewModel>()
    val audioRecorderViewModel = hiltViewModel<AndroidAudioRecorderViewModel>()
    val transcriptionViewModel = hiltViewModel<AndroidTranscriptionViewModel>()
    val downloaderViewModel = hiltViewModel<AndroidModelDownloaderViewModel>()
    val editorViewModel = hiltViewModel<AndroidTextEditorViewModel>()
    val platformViewModel = hiltViewModel<AndroidPlatformViewModel>()

    if(noteId.toLong() > 0L) {
        editorViewModel.onGetNoteById(noteId)
    }

    val audioPlayerState = audioPlayerViewModel.state.collectAsState().value
        .let { audioPlayerViewModel.onGetUiState(it) }

    val audioRecorderState = audioRecorderViewModel.state.collectAsState().value
        .let { audioRecorderViewModel.onGetUiState(it) }

    val transcriptionState = transcriptionViewModel.state.collectAsState().value


    val downloaderState = downloaderViewModel.state.collectAsState().value
    val downloaderEffect = downloaderViewModel.effect

    val editorState = editorViewModel.state.collectAsState().value
        .let { editorViewModel.onGetUiState(it) }

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
        setupRecorder = {},
        finishRecorder = {},
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

    val shareActions = ShareActions(
        shareText = platformViewModel::shareText,
        shareRecording = platformViewModel::shareRecording
    )



    val noteActions = NoteActions(
        onDeleteNote = {
            editorViewModel.onDeleteNote()
            onNavigateBack()
        },
        onStarNote = editorViewModel::onToggleStar
    )

    val downloaderActions = DownloaderActions(
        checkModelAvailability = downloaderViewModel::checkModelAvailability
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
        isRecordPaused = audioRecorderState.isRecordPaused,
        onTranscriptionActions = transcriptionActions,
        transcriptionUiState = transcriptionState,
        downloaderUiState = downloaderState,
        downloaderEffect = downloaderEffect,
        onDownloaderActions = downloaderActions,
        onShareActions = shareActions
    )
}
