package com.module.notelycompose.platform

import androidx.compose.runtime.Composable

@Composable
expect fun HandlePlatformBackNavigation(enabled: Boolean, onBack: () -> Unit)