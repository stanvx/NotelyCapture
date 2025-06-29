package com.module.notelycompose.notes.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.module.notelycompose.platform.getPlatform
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.resources.vectors.IcChevronLeft
import com.module.notelycompose.resources.vectors.Images
import notelycompose.shared.generated.resources.Res
import notelycompose.shared.generated.resources.top_bar_back
import org.jetbrains.compose.resources.stringResource

@Composable
fun DetailNoteTopBar(
    title: String = "My Note",
    onNavigateBack: () -> Unit,
    onShare: () -> Unit = {}
) {
    if (getPlatform().isAndroid) {
        DetailAndroidNoteTopBar(
            title = title,
            onNavigateBack = onNavigateBack,
            onShare = onShare
        )
    } else {
        DetailIOSNoteTopBar(
            onNavigateBack = onNavigateBack,
            onShare = onShare
        )
    }
}

@Composable
fun DetailAndroidNoteTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onShare: () -> Unit,
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = { onNavigateBack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.top_bar_back)
                )
            }
        },
        actions = {
            IconButton(onClick = { onShare() }) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share note"
                )
            }
        },
        backgroundColor = LocalCustomColors.current.bodyBackgroundColor,
        contentColor = LocalCustomColors.current.bodyContentColor,
        elevation = elevation
    )
}

@Composable
fun DetailIOSNoteTopBar(
    onNavigateBack: () -> Unit,
    onShare: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onNavigateBack()
                }
            ) {
                Icon(
                    imageVector = Images.Icons.IcChevronLeft,
                    contentDescription = stringResource(Res.string.top_bar_back),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.top_bar_back),
                    style = MaterialTheme.typography.body1,
                )
            }
        },
        actions = {
            IconButton(onClick = { onShare() }) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share note",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        contentColor = LocalCustomColors.current.iOSBackButtonColor,
        backgroundColor = LocalCustomColors.current.bodyBackgroundColor,
        modifier = Modifier.padding(start = 0.dp),
        elevation = 0.dp
    )
}
