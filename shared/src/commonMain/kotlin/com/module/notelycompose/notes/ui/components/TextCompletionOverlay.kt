package com.module.notelycompose.notes.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.module.notelycompose.notes.domain.TextCompletion
import com.module.notelycompose.notes.domain.TextCompletionType
import com.module.notelycompose.notes.domain.FormattingSuggestion
import com.module.notelycompose.notes.domain.FormattingSuggestionType
import com.module.notelycompose.notes.ui.theme.MaterialSymbols
import com.module.notelycompose.notes.ui.components.MaterialIcon

/**
 * Overlay component for displaying text completions and formatting suggestions in rich text editor.
 * 
 * Features:
 * - Compact horizontal layout for text completions
 * - Quick formatting suggestion chips
 * - Animated appearance with minimal intrusion
 * - Smart positioning to avoid keyboard overlap
 */
@Composable
fun TextCompletionOverlay(
    textCompletions: List<TextCompletion>,
    formattingSuggestions: List<FormattingSuggestion>,
    isVisible: Boolean,
    onTextCompletionClick: (TextCompletion) -> Unit,
    onFormattingSuggestionClick: (FormattingSuggestion) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible && (textCompletions.isNotEmpty() || formattingSuggestions.isNotEmpty())) {
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = true
            )
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                CompletionOverlayContent(
                    textCompletions = textCompletions,
                    formattingSuggestions = formattingSuggestions,
                    onTextCompletionClick = onTextCompletionClick,
                    onFormattingSuggestionClick = onFormattingSuggestionClick,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun CompletionOverlayContent(
    textCompletions: List<TextCompletion>,
    formattingSuggestions: List<FormattingSuggestion>,
    onTextCompletionClick: (TextCompletion) -> Unit,
    onFormattingSuggestionClick: (FormattingSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(min = 200.dp, max = 350.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Text completions section
            if (textCompletions.isNotEmpty()) {
                Text(
                    text = "Suggestions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(textCompletions.take(4)) { completion ->
                        TextCompletionChip(
                            completion = completion,
                            onClick = { onTextCompletionClick(completion) }
                        )
                    }
                }
            }
            
            // Divider if both sections are present
            if (textCompletions.isNotEmpty() && formattingSuggestions.isNotEmpty()) {
                Divider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
            
            // Formatting suggestions section
            if (formattingSuggestions.isNotEmpty()) {
                Text(
                    text = "Formatting",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(formattingSuggestions.take(3)) { suggestion ->
                        FormattingSuggestionChip(
                            suggestion = suggestion,
                            onClick = { onFormattingSuggestionClick(suggestion) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextCompletionChip(
    completion: TextCompletion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipColor = when (completion.type) {
        TextCompletionType.WORD -> MaterialTheme.colorScheme.primaryContainer
        TextCompletionType.PHRASE -> MaterialTheme.colorScheme.secondaryContainer
        TextCompletionType.COMMON_WORD -> MaterialTheme.colorScheme.tertiaryContainer
    }
    
    val contentColor = when (completion.type) {
        TextCompletionType.WORD -> MaterialTheme.colorScheme.onPrimaryContainer
        TextCompletionType.PHRASE -> MaterialTheme.colorScheme.onSecondaryContainer
        TextCompletionType.COMMON_WORD -> MaterialTheme.colorScheme.onTertiaryContainer
    }
    
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = completion.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingIcon = {
            CompletionTypeIcon(
                type = completion.type,
                modifier = Modifier.size(14.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = chipColor,
            labelColor = contentColor,
            leadingIconContentColor = contentColor
        ),
        border = null,
        modifier = modifier
    )
}

@Composable
private fun FormattingSuggestionChip(
    suggestion: FormattingSuggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingIcon = {
            FormattingSuggestionIcon(
                type = suggestion.type,
                modifier = Modifier.size(14.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            leadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
    )
}

@Composable
private fun CompletionTypeIcon(
    type: TextCompletionType,
    modifier: Modifier = Modifier
) {
    val iconSymbol = when (type) {
        TextCompletionType.WORD -> MaterialSymbols.TextFields
        TextCompletionType.PHRASE -> MaterialSymbols.FormatQuote
        TextCompletionType.COMMON_WORD -> MaterialSymbols.TrendingUp
    }
    
    MaterialIcon(
        symbol = iconSymbol,
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
private fun FormattingSuggestionIcon(
    type: FormattingSuggestionType,
    modifier: Modifier = Modifier
) {
    val iconSymbol = when (type) {
        FormattingSuggestionType.BULLET_LIST -> MaterialSymbols.FormatListBulleted
        FormattingSuggestionType.HEADING -> MaterialSymbols.TextFields
        FormattingSuggestionType.BOLD -> MaterialSymbols.FormatBold
        FormattingSuggestionType.ITALIC -> MaterialSymbols.FormatItalic
        FormattingSuggestionType.CODE_BLOCK -> MaterialSymbols.Code
    }
    
    MaterialIcon(
        symbol = iconSymbol,
        contentDescription = null,
        modifier = modifier
    )
}

/**
 * Compact inline version for smaller spaces or minimal interference.
 */
@Composable
fun InlineTextCompletionBar(
    textCompletions: List<TextCompletion>,
    isVisible: Boolean,
    onTextCompletionClick: (TextCompletion) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible && textCompletions.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                .padding(8.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(textCompletions.take(3)) { completion ->
                    CompactCompletionChip(
                        completion = completion,
                        onClick = { onTextCompletionClick(completion) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactCompletionChip(
    completion: TextCompletion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = completion.text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}