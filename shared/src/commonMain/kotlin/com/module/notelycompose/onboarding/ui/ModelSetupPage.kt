package com.module.notelycompose.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.modelDownloader.DownloaderEffect
import com.module.notelycompose.modelDownloader.DownloaderUiState
import com.module.notelycompose.modelDownloader.ModelDownloaderViewModel
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.notes.ui.theme.PoppingsFontFamily
import com.module.notelycompose.platform.presentation.PlatformUiState
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.onboarding_android_four
import com.module.notelycompose.resources.onboarding_android_tablet_four
import com.module.notelycompose.resources.onboarding_ios_four
import com.module.notelycompose.resources.onboarding_ios_tablet_four
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ModelSetupPage(
    onComplete: () -> Unit,
    onError: (String) -> Unit,
    downloaderViewModel: ModelDownloaderViewModel,
    platformState: PlatformUiState,
    modifier: Modifier = Modifier
) {
    val downloaderUiState by downloaderViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    // Handle downloader effects
    LaunchedEffect(Unit) {
        downloaderViewModel.effects.collect { effect ->
            when (effect) {
                is DownloaderEffect.ModelsAreReady -> {
                    onComplete()
                }
                is DownloaderEffect.ErrorEffect -> {
                    onError("Failed to download transcription model. Please check your internet connection and try again.")
                }
                is DownloaderEffect.AskForUserAcceptance -> {
                    // Auto-start download during onboarding
                    downloaderViewModel.startDownload()
                }
                is DownloaderEffect.CheckingEffect -> {
                    // Checking in progress - UI will show loading state
                }
                is DownloaderEffect.DownloadEffect -> {
                    // Download in progress - UI will show progress
                }
            }
        }
    }

    // Start model availability check when component loads
    LaunchedEffect(Unit) {
        downloaderViewModel.checkTranscriptionAvailability()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFAD0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Bar Spacer
            Spacer(modifier = Modifier.height(16.dp))

            // Skip button (hidden during setup to prevent incomplete setup)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ModelSetupContent(
                    downloaderUiState = downloaderUiState,
                    platformState = platformState
                )
            }

            // Bottom spacer
            Spacer(modifier = Modifier.height(76.dp))
        }
    }
}

@Composable
private fun ModelSetupContent(
    downloaderUiState: DownloaderUiState,
    platformState: PlatformUiState
) {
    val resource = if (platformState.isAndroid) {
        if (platformState.isTablet) {
            painterResource(Res.drawable.onboarding_android_tablet_four)
        } else {
            painterResource(Res.drawable.onboarding_android_four)
        }
    } else {
        if (platformState.isTablet) {
            painterResource(Res.drawable.onboarding_ios_tablet_four)
        } else {
            painterResource(Res.drawable.onboarding_ios_four)
        }
    }

    val descriptionFontSize = if (platformState.isTablet) 20.sp else 18.sp
    val imageIllustrationWidth = if (platformState.isTablet) 800.dp else 360.dp

    Text(
        text = "Setting up Notely Capture",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PoppingsFontFamily(),
        color = Color(0xFFCA7F58),
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        lineHeight = 32.sp
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = resource,
                contentDescription = "Setting up transcription",
                modifier = Modifier.width(imageIllustrationWidth),
                contentScale = ContentScale.FillWidth
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress Section
        ModelSetupProgressSection(downloaderUiState = downloaderUiState)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "We're downloading the AI transcription model to enable voice-to-text features. This will make your voice notes instantly searchable and editable.",
            fontSize = descriptionFontSize,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun ModelSetupProgressSection(
    downloaderUiState: DownloaderUiState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = LocalCustomColors.current.bodyBackgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Downloading Transcription Model",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = LocalCustomColors.current.bodyContentColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            if (downloaderUiState.progress == 0f) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    strokeCap = StrokeCap.Round,
                    color = Color(0xFFCA7F58)
                )
            } else {
                LinearProgressIndicator(
                    progress = downloaderUiState.progress / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    strokeCap = StrokeCap.Round,
                    color = Color(0xFFCA7F58)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Download Progress Text
            if (downloaderUiState.progress > 0f) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${downloaderUiState.downloaded} / ${downloaderUiState.total}",
                        fontSize = 14.sp,
                        color = LocalCustomColors.current.bodyContentColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${downloaderUiState.progress.toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = LocalCustomColors.current.bodyContentColor
                    )
                }
            }
        }
    }
}