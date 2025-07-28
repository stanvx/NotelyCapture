package com.module.notelycompose.notes.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.module.notelycompose.notes.ui.components.MaterialIcon
import com.module.notelycompose.notes.ui.theme.MaterialSymbols
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.ui.detail.AndroidNoteTopBar
import com.module.notelycompose.notes.ui.detail.IOSNoteTopBar
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.notes.presentation.settings.LanguageSelectionIntent
import com.module.notelycompose.notes.presentation.settings.LanguageSelectionViewModel
import com.module.notelycompose.platform.getPlatform
import org.koin.compose.koinInject
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.language_selection_no_languages_found
import com.module.notelycompose.resources.language_selection_supported_languages
import com.module.notelycompose.resources.language_selection_search
import com.module.notelycompose.resources.language_selection_select_language
import org.jetbrains.compose.resources.stringResource

val languageCodeMap = mapOf(
    //"auto" to "Auto detect",
    "en" to "English",
    "ar" to "Arabic",
    "ca" to "Catalan",
    "zh" to "Chinese",
    "nl" to "Dutch",
    "fi" to "Finnish",
    "fr" to "French",
    "gl" to "Galician",
    "de" to "German",
    "id" to "Indonesian",
    "it" to "Italian",
    "ja" to "Japanese",
    "ko" to "Korean",
    "ms" to "Malay",
    "no" to "Norwegian",
    "pl" to "Polish",
    "pt" to "Portuguese",
    "ru" to "Russian",
    "es" to "Spanish",
    "sv" to "Swedish",
    "tl" to "Tagalog",
    "th" to "Thai",
    "tr" to "Turkish",
    "uk" to "Ukrainian",
    "vi" to "Vietnamese",
)

@Composable
fun LanguageSelectionScreen(
    navigateBack: () -> Unit,
    viewModel: LanguageSelectionViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.bodyBackgroundColor)
    ) {
        if (getPlatform().isAndroid) {
            AndroidNoteTopBar(
                title = "",
                onNavigateBack = navigateBack
            )
        } else {
            IOSNoteTopBar(
                onNavigateBack = navigateBack
            )
        }
        // content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalCustomColors.current.bodyBackgroundColor)
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = stringResource(Res.string.language_selection_select_language),
                color = LocalCustomColors.current.bodyContentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { query ->
                    viewModel.onProcessIntent(LanguageSelectionIntent.OnSearchQueryChanged(query))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                placeholder = {
                    Text(
                        text = stringResource(Res.string.language_selection_search),
                        color = LocalCustomColors.current.languageSearchBorderColor
                    )
                },
                leadingIcon = {
                    MaterialIcon(
                        symbol = MaterialSymbols.Search,
                        contentDescription = "Search",
                        tint = LocalCustomColors.current.languageSearchBorderColor
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.onProcessIntent(LanguageSelectionIntent.OnClearSearch)
                            },
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    LocalCustomColors.current.languageSearchCancelButtonColor.copy(alpha = 0.3f),
                                    CircleShape
                                )
                        ) {
                            MaterialIcon(
                                symbol = MaterialSymbols.Clear,
                                contentDescription = "Clear search",
                                tint = LocalCustomColors.current.languageSearchCancelIconTintColor,
                                size = 14.dp
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LocalCustomColors.current.languageSearchUnfocusedColor,
                    unfocusedTextColor = LocalCustomColors.current.languageSearchUnfocusedColor,
                    focusedBorderColor = LocalCustomColors.current.languageSearchBorderColor,
                    unfocusedBorderColor = LocalCustomColors.current.languageSearchBorderColor,
                    cursorColor = LocalCustomColors.current.languageSearchUnfocusedColor
                ),
                shape = RoundedCornerShape(48.dp),
                singleLine = true
            )

            // Error handling
            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                )
                TextButton(
                    onClick = {
                        viewModel.onProcessIntent(LanguageSelectionIntent.OnRetry)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text("Retry")
                }
            }

            // Language List
            Text(
                text = stringResource(Res.string.language_selection_supported_languages),
                color = LocalCustomColors.current.languageListHeaderColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(LocalCustomColors.current.languageListBackgroundColor)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp))
            ) {
                if (state.filteredLanguages.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.language_selection_no_languages_found),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(LocalCustomColors.current.languageListBackgroundColor),
                        textAlign = TextAlign.Center,
                        color = LocalCustomColors.current.languageListTextColor
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        itemsIndexed(state.filteredLanguages.entries.toList()) { index, languageEntry ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !state.isLoading) {
                                        if (!state.isLoading) {
                                            viewModel.onProcessIntent(
                                                LanguageSelectionIntent.OnLanguageSelected(languageEntry.key)
                                            )
                                            navigateBack()
                                        }
                                    },
                                color = LocalCustomColors.current.languageListBackgroundColor,
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = languageEntry.value,
                                            color = LocalCustomColors.current.languageListTextColor,
                                            fontSize = 16.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        when {
                                            state.isLoading && languageEntry.key == state.selectedLanguageCode -> {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp,
                                                    color = LocalCustomColors.current.languageListTextColor
                                                )
                                            }
                                            languageEntry.key == state.selectedLanguageCode -> {
                                                MaterialIcon(
                                                    symbol = MaterialSymbols.Check,
                                                    contentDescription = "Selected",
                                                    tint = LocalCustomColors.current.languageListTextColor,
                                                    size = 20.dp
                                                )
                                            }
                                        }
                                    }
                                    if (index < state.filteredLanguages.size - 1) {
                                        Divider(
                                            thickness = 0.5.dp,
                                            color = LocalCustomColors.current.languageListDividerColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
