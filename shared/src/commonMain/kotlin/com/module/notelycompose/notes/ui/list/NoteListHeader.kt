package com.module.notelycompose.notes.ui.list

import com.module.notelycompose.notes.ui.components.UnifiedHeaderCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Vibrant header component for the note list screen.
 * 
 * Features:
 * - Gradient background with Material 3 colors
 * - Floating animated elements
 * - Note count display
 * - Integrated search functionality
 * - Responsive design for different screen sizes
 */
@Composable
fun NoteListHeader(
    noteCount: Int,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false
) {
    UnifiedHeaderCard(
        title = "Your Captures",
        subtitle = when (noteCount) {
            0 -> "Start capturing ideas"
            1 -> "1 note captured"
            else -> "$noteCount notes captured"
        },
        modifier = modifier,
        isTablet = isTablet
    )
}

