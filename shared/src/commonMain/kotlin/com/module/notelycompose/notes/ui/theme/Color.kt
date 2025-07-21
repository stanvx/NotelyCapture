package com.module.notelycompose.notes.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

// Material 3 Color Palette - Generated from Material Theme Builder
val primaryLight = Color(0xFF415F91)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFD6E3FF)
val onPrimaryContainerLight = Color(0xFF284777)
val secondaryLight = Color(0xFF565F71)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFDAE2F9)
val onSecondaryContainerLight = Color(0xFF3E4759)
val tertiaryLight = Color(0xFF705575)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFFAD8FD)
val onTertiaryContainerLight = Color(0xFF573E5C)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF93000A)
val backgroundLight = Color(0xFFF9F9FF)
val onBackgroundLight = Color(0xFF191C20)
val surfaceLight = Color(0xFFF9F9FF)
val onSurfaceLight = Color(0xFF191C20)
val surfaceVariantLight = Color(0xFFE0E2EC)
val onSurfaceVariantLight = Color(0xFF44474E)
val outlineLight = Color(0xFF74777F)
val outlineVariantLight = Color(0xFFC4C6D0)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF2E3036)
val inverseOnSurfaceLight = Color(0xFFF0F0F7)
val inversePrimaryLight = Color(0xFFAAC7FF)
val surfaceDimLight = Color(0xFFD9D9E0)
val surfaceBrightLight = Color(0xFFF9F9FF)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFF3F3FA)
val surfaceContainerLight = Color(0xFFEDEDF4)
val surfaceContainerHighLight = Color(0xFFE7E8EE)
val surfaceContainerHighestLight = Color(0xFFE2E2E9)

// Record Blue - Custom color for voice recording actions
val recordBlueLight = Color(0xFF1E88E5)
val onRecordBlueLight = Color(0xFFFFFFFF)
val recordBlueContainerLight = Color(0xFFBBDEFB)
val onRecordBlueContainerLight = Color(0xFF0D47A1)

// Pink Theme - Custom color for voice recording actions (Material 3 compliant)
val recordPinkLight = Color(0xFFE91E63)
val onRecordPinkLight = Color(0xFFFFFFFF)
val recordPinkContainerLight = Color(0xFFFCE4EC)
val onRecordPinkContainerLight = Color(0xFF880E4F)

val primaryDark = Color(0xFFAAC7FF)
val onPrimaryDark = Color(0xFF0A305F)
val primaryContainerDark = Color(0xFF284777)
val onPrimaryContainerDark = Color(0xFFD6E3FF)
val secondaryDark = Color(0xFFBEC6DC)
val onSecondaryDark = Color(0xFF283141)
val secondaryContainerDark = Color(0xFF3E4759)
val onSecondaryContainerDark = Color(0xFFDAE2F9)
val tertiaryDark = Color(0xFFDDBCE0)
val onTertiaryDark = Color(0xFF3F2844)
val tertiaryContainerDark = Color(0xFF573E5C)
val onTertiaryContainerDark = Color(0xFFFAD8FD)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF111318)
val onBackgroundDark = Color(0xFFE2E2E9)
val surfaceDark = Color(0xFF111318)
val onSurfaceDark = Color(0xFFE2E2E9)
val surfaceVariantDark = Color(0xFF44474E)
val onSurfaceVariantDark = Color(0xFFC4C6D0)
val outlineDark = Color(0xFF8E9099)
val outlineVariantDark = Color(0xFF44474E)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFE2E2E9)
val inverseOnSurfaceDark = Color(0xFF2E3036)
val inversePrimaryDark = Color(0xFF415F91)
val surfaceDimDark = Color(0xFF111318)
val surfaceBrightDark = Color(0xFF37393E)
val surfaceContainerLowestDark = Color(0xFF0C0E13)
val surfaceContainerLowDark = Color(0xFF191C20)
val surfaceContainerDark = Color(0xFF1D2024)
val surfaceContainerHighDark = Color(0xFF282A2F)
val surfaceContainerHighestDark = Color(0xFF33353A)

// Record Blue Dark - Custom color for voice recording actions
val recordBlueDark = Color(0xFF90CAF9)
val onRecordBlueDark = Color(0xFF003258)
val recordBlueContainerDark = Color(0xFF004881)
val onRecordBlueContainerDark = Color(0xFFCBE6FF)

// Record Pink Dark - Custom color for voice recording actions (Material 3 compliant)
val recordPinkDark = Color(0xFFF06292)
val onRecordPinkDark = Color(0xFF4A0E2B)
val recordPinkContainerDark = Color(0xFF6D1A3E)
val onRecordPinkContainerDark = Color(0xFFFCE4EC)

// Material 3 Custom Colors for Dark Theme
val DarkCustomColors = CustomColors(
    sortAscendingIconColor = primaryDark,
    backgroundViewColor = backgroundDark,
    dateContentColorViewColor = onBackgroundDark,
    dateContentIconColor = onSurfaceVariantDark,
    bottomBarBackgroundColor = surfaceContainerDark,
    bottomBarIconColor = onSurfaceDark,
    noteListBackgroundColor = surfaceDark,
    bodyBackgroundColor = backgroundDark,
    onBodyColor = onBackgroundDark,
    bodyContentColor = onSurfaceDark,
    contentTopColor = onSurfaceDark,
    floatActionButtonBorderColor = outlineDark,
    floatActionButtonIconColor = onPrimaryDark,
    searchOutlinedTextFieldColor = onSurfaceVariantDark,
    topButtonIconColor = onSurfaceDark,
    noteTextColor = onPrimaryDark,
    noteIconColor = onSurfaceVariantDark,
    iOSBackButtonColor = primaryDark,
    transparentColor = Color.Transparent,
    bottomFormattingContainerColor = surfaceContainerHighDark,
    bottomFormattingContentColor = onSurfaceDark,
    activeThumbTrackColor = primaryDark,
    playerBoxBackgroundColor = surfaceContainerDark,
    starredColor = tertiaryDark,
    settingsIconColor = onSurfaceVariantDark,
    settingCancelBackgroundColor = surfaceContainerHighDark,
    settingCancelTextColor = onSurfaceDark,
    settingLanguageBackgroundColor = surfaceContainerDark,
    languageSearchBorderColor = outlineVariantDark,
    languageSearchCancelButtonColor = onSurfaceVariantDark,
    languageSearchCancelIconTintColor = onSurfaceVariantDark,
    languageListHeaderColor = onSurfaceVariantDark,
    languageListTextColor = onSurfaceDark,
    languageListBackgroundColor = surfaceDark,
    languageListDividerColor = outlineVariantDark,
    languageSearchUnfocusedColor = onSurfaceVariantDark,
    shareDialogBackgroundColor = surfaceContainerHighDark,
    shareDialogButtonColor = onSurfaceDark,
    statusBarBackgroundColor = surfaceContainerHighestDark
)

// Material 3 Custom Colors for Light Theme
val LightCustomColors = CustomColors(
    sortAscendingIconColor = primaryLight,
    backgroundViewColor = backgroundLight,
    dateContentColorViewColor = onBackgroundLight,
    dateContentIconColor = onSurfaceVariantLight,
    bottomBarBackgroundColor = surfaceContainerLight,
    bottomBarIconColor = onSurfaceLight,
    noteListBackgroundColor = surfaceLight,
    bodyBackgroundColor = backgroundLight,
    onBodyColor = onBackgroundLight,
    contentTopColor = onSurfaceLight,
    bodyContentColor = onSurfaceLight,
    floatActionButtonBorderColor = outlineLight,
    floatActionButtonIconColor = onPrimaryLight,
    searchOutlinedTextFieldColor = onSurfaceVariantLight,
    topButtonIconColor = onSurfaceLight,
    noteTextColor = onPrimaryLight,
    noteIconColor = onSurfaceVariantLight,
    iOSBackButtonColor = primaryLight,
    transparentColor = Color.Transparent,
    bottomFormattingContainerColor = surfaceContainerHighLight,
    bottomFormattingContentColor = onSurfaceLight,
    activeThumbTrackColor = primaryLight,
    playerBoxBackgroundColor = surfaceContainerLight,
    starredColor = tertiaryLight,
    settingsIconColor = onSurfaceVariantLight,
    settingCancelBackgroundColor = surfaceContainerHighLight,
    settingCancelTextColor = onSurfaceLight,
    settingLanguageBackgroundColor = surfaceContainerLight,
    languageSearchBorderColor = outlineVariantLight,
    languageSearchCancelButtonColor = onSurfaceVariantLight,
    languageSearchCancelIconTintColor = onSurfaceVariantLight,
    languageListHeaderColor = onSurfaceVariantLight,
    languageListTextColor = onSurfaceLight,
    languageListBackgroundColor = surfaceLight,
    languageListDividerColor = outlineVariantLight,
    languageSearchUnfocusedColor = onSurfaceVariantLight,
    shareDialogBackgroundColor = surfaceContainerHighLight,
    shareDialogButtonColor = onSurfaceLight,
    statusBarBackgroundColor = surfaceContainerHighestLight
)

// Material 3 Custom Colors - Semantic color tokens using Material 3 palette
val LocalCustomColors = compositionLocalOf {
    LightCustomColors // Default value for custom colors
}


// Extension properties for ColorScheme to provide semantic color tokens
val ColorScheme.recordButtonContainer: Color
    @Composable get() = if(LocalCustomColors.current.bodyBackgroundColor == backgroundDark) recordPinkDark else recordPinkLight

val ColorScheme.onRecordButtonContainer: Color
    @Composable get() = if(LocalCustomColors.current.bodyBackgroundColor == backgroundDark) onRecordPinkDark else onRecordPinkLight

// Calendar Note Item colors - Voice notes
val ColorScheme.voiceNoteIndicatorContainer: Color
    @Composable get() = if(LocalCustomColors.current.bodyBackgroundColor == backgroundDark) Color(0xFF1A237E) else Color(0xFFE8F0FE)

val ColorScheme.onVoiceNoteIndicatorContainer: Color
    @Composable get() = if(LocalCustomColors.current.bodyBackgroundColor == backgroundDark) Color(0xFF90CAF9) else Color(0xFF4285F4)

// Calendar Note Item colors - Text notes  
val ColorScheme.textNoteIndicatorContainer: Color
    @Composable get() = if(LocalCustomColors.current.bodyBackgroundColor == backgroundDark) Color(0xFFE65100) else Color(0xFFFFF8E1)

val ColorScheme.onTextNoteIndicatorContainer: Color
    @Composable get() = if(LocalCustomColors.current.bodyBackgroundColor == backgroundDark) Color(0xFFFFB74D) else Color(0xFFFF9800)

// Additional semantic colors for unified theming
val ColorScheme.captureMethodContainer: Color
    @Composable get() = surfaceContainerLow

val ColorScheme.onCaptureMethodContainer: Color
    @Composable get() = onSurfaceVariant

val ColorScheme.statsCardBackground: Color
    @Composable get() = surfaceContainer

val ColorScheme.onStatsCardBackground: Color
    @Composable get() = onSurface