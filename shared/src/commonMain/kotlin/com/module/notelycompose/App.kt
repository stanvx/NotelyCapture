package com.module.notelycompose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.module.notelycompose.audio.ui.recorder.RecordingScreen
import com.module.notelycompose.core.Routes
import com.module.notelycompose.notes.ui.detail.NoteDetailScreen
import com.module.notelycompose.notes.ui.list.InfoScreen
import com.module.notelycompose.notes.ui.list.NoteListScreen
import com.module.notelycompose.notes.ui.settings.LanguageSelectionScreen
import com.module.notelycompose.notes.ui.settings.SettingsScreen
import com.module.notelycompose.notes.ui.theme.MyApplicationTheme
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.onboarding.presentation.OnboardingViewModel
import com.module.notelycompose.onboarding.presentation.model.OnboardingState
import com.module.notelycompose.onboarding.ui.OnboardingWalkthrough
import com.module.notelycompose.platform.Theme
import com.module.notelycompose.transcription.TranscriptionScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

private const val NOTE_ID_PARAM = "noteId"
private const val DEFAULT_NOTE_ID = "0"
private const val ROUTE_SEPARATOR = "/"

@OptIn(KoinExperimentalAPI::class)
@Composable
fun App(
    preferencesRepository: PreferencesRepository = koinInject()
) {

    val uiMode by preferencesRepository.getTheme().collectAsState(Theme.SYSTEM.name)


    MyApplicationTheme(
        darkTheme = when (uiMode) {
            Theme.DARK.name -> true
            Theme.LIGHT.name -> false
            else -> isSystemInDarkTheme()
        }
    ) {


        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val viewmodel = koinViewModel<OnboardingViewModel>()
            val onboardingState by viewmodel.onboardingState.collectAsState()

            when (onboardingState) {
                is OnboardingState.Initial -> Unit
                is OnboardingState.NotCompleted -> {
                    OnboardingWalkthrough(
                        onFinish = {
                            viewmodel.onCompleteOnboarding()
                        }
                    )
                }

                is OnboardingState.Completed -> NoteAppRoot()
            }
        }
    }
}


@Composable
fun NoteAppRoot() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Routes.HOME) {
        navigation(startDestination = Routes.LIST, route = Routes.HOME) {
            composable(Routes.LIST) {
                NoteListScreen(
                    navigateToSettings = {
                        navController.navigate(Routes.SETTINGS)
                    },
                    navigateToMenu = {
                        navController.navigate(Routes.MENU)
                    },
                    navigateToNoteDetails = { noteId ->
                        navController.navigate("${Routes.DETAILS}/$noteId")
                    }
                )
            }
            composable(Routes.MENU) {
                InfoScreen(
                    navigateBack = { navController.popBackStack() },
                    onNavigateToWebPage = { title, url ->
                        // navController.navigate("${Routes.WEB_VIEW}/$title/$url")
                    }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    navigateBack = { navController.popBackStack() },
                    navigateToLanguages = { navController.navigate(Routes.LANGUAGE) }
                )
            }
            composable(Routes.LANGUAGE) {
                LanguageSelectionScreen(
                    navigateBack = { navController.popBackStack() }
                )
            }
        }
        navigation(startDestination = "${Routes.DETAILS}/{noteId}", route = Routes.DETAILS_GRAPH) {
            composable(
                route = "${Routes.DETAILS}/{noteId}",
                arguments = listOf(navArgument(NOTE_ID_PARAM) {
                    type = NavType.StringType
                    defaultValue = DEFAULT_NOTE_ID
                    nullable = false
                })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.DETAILS_GRAPH)
                }
                NoteDetailScreen(
                    noteId = backStackEntry.arguments?.getString(NOTE_ID_PARAM) ?: DEFAULT_NOTE_ID,
                    navigateBack = { navController.popBackStack() },
                    navigateToRecorder = { navController.navigate(Routes.RECORDER) },
                    navigateToTranscription = {
                        navController.navigate(Routes.TRANSCRIPTION)
                    },
                    editorViewModel = koinViewModel(viewModelStoreOwner = parentEntry),
                    navController = navController
                )
            }
            composable(
                route = Routes.TRANSCRIPTION
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.DETAILS_GRAPH)
                }
                TranscriptionScreen(
                    navigateBack = { navController.popBackStack() },
                    editorViewModel = koinViewModel(viewModelStoreOwner = parentEntry),
                )
            }
            composable(Routes.RECORDER) {
                RecordingScreen(
                    navigateBack = { navController.popBackStack() },
                    onRecordingFinished = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("recordingPath", it)
                        navController.popBackStack()
                    })
            }
        }


    }



}