package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.theme.Material3ShapeTokens
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.note_item_starred
import com.module.notelycompose.resources.note_item_voice
import com.module.notelycompose.resources.note_item_note
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteType(
    isStarred: Boolean,
    isVoice: Boolean,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        when {
            isStarred && isVoice -> {
                NoteTypeChip(stringResource(Res.string.note_item_starred))
                Spacer(modifier = Modifier.width(8.dp))
                NoteTypeChip(stringResource(Res.string.note_item_voice))
            }
            isStarred -> {
                NoteTypeChip(stringResource(Res.string.note_item_starred))
            }
            isVoice -> {
                NoteTypeChip(stringResource(Res.string.note_item_voice))
            }
            else -> {
                NoteTypeChip(stringResource(Res.string.note_item_note))
            }
        }
    }
}

@Composable
private fun NoteTypeChip(text: String) {
    ElevatedCard(
        modifier = Modifier.padding(vertical = 2.dp),
        shape = Material3ShapeTokens.chip,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
