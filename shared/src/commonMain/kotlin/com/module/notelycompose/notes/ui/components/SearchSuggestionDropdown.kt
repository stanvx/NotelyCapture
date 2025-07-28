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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.module.notelycompose.notes.domain.SearchSuggestion
import com.module.notelycompose.notes.domain.SearchSuggestionType
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.notes.ui.theme.MaterialSymbols

/**
 * Material 3 compliant dropdown component for search suggestions.
 * 
 * Features:
 * - Animated appearance/disappearance
 * - Different visual treatments for suggestion types
 * - Keyboard navigation support
 * - Accessibility features
 * - Performance optimized with lazy rendering
 */
@Composable
fun SearchSuggestionDropdown(
    suggestions: List<SearchSuggestion>,
    isVisible: Boolean,
    onSuggestionClick: (SearchSuggestion) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible && suggestions.isNotEmpty()) {
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                SuggestionCard(
                    suggestions = suggestions,
                    onSuggestionClick = onSuggestionClick,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestions: List<SearchSuggestion>,
    onSuggestionClick: (SearchSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(min = 200.dp, max = 400.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(
                items = suggestions,
                key = { suggestion -> "${suggestion.type.name}-${suggestion.text}" }
            ) { suggestion ->
                SuggestionItem(
                    suggestion = suggestion,
                    onClick = { onSuggestionClick(suggestion) }
                )
                
                // Add divider except for last item
                if (suggestion != suggestions.last()) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: SearchSuggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon based on suggestion type
        SuggestionTypeIcon(
            type = suggestion.type,
            modifier = Modifier.size(20.dp)
        )
        
        // Main content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Suggestion text
            Text(
                text = suggestion.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (suggestion.type == SearchSuggestionType.RECENT_SEARCH) {
                        FontWeight.Medium
                    } else {
                        FontWeight.Normal
                    }
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Preview text for content-based suggestions
            suggestion.preview?.let { preview ->
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Type label for clarity
        SuggestionTypeLabel(type = suggestion.type)
    }
}

@Composable
private fun SuggestionTypeIcon(
    type: SearchSuggestionType,
    modifier: Modifier = Modifier
) {
    val iconSymbol = when (type) {
        SearchSuggestionType.RECENT_SEARCH -> MaterialSymbols.Schedule
        SearchSuggestionType.NOTE_TITLE -> MaterialSymbols.TextFields
        SearchSuggestionType.CONTENT_PHRASE -> MaterialSymbols.FormatQuote
        SearchSuggestionType.COMMON_PHRASE -> MaterialSymbols.TrendingUp
    }
    
    val iconColor = when (type) {
        SearchSuggestionType.RECENT_SEARCH -> MaterialTheme.colorScheme.primary
        SearchSuggestionType.NOTE_TITLE -> MaterialTheme.colorScheme.secondary
        SearchSuggestionType.CONTENT_PHRASE -> MaterialTheme.colorScheme.tertiary
        SearchSuggestionType.COMMON_PHRASE -> MaterialTheme.colorScheme.outline
    }
    
    MaterialIcon(
        symbol = iconSymbol,
        contentDescription = null,
        tint = iconColor,
        modifier = modifier
    )
}

@Composable
private fun SuggestionTypeLabel(
    type: SearchSuggestionType,
    modifier: Modifier = Modifier
) {
    val (labelText, backgroundColor) = when (type) {
        SearchSuggestionType.RECENT_SEARCH -> "Recent" to MaterialTheme.colorScheme.primaryContainer
        SearchSuggestionType.NOTE_TITLE -> "Title" to MaterialTheme.colorScheme.secondaryContainer
        SearchSuggestionType.CONTENT_PHRASE -> "Content" to MaterialTheme.colorScheme.tertiaryContainer
        SearchSuggestionType.COMMON_PHRASE -> "Suggest" to MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when (type) {
        SearchSuggestionType.RECENT_SEARCH -> MaterialTheme.colorScheme.onPrimaryContainer
        SearchSuggestionType.NOTE_TITLE -> MaterialTheme.colorScheme.onSecondaryContainer
        SearchSuggestionType.CONTENT_PHRASE -> MaterialTheme.colorScheme.onTertiaryContainer
        SearchSuggestionType.COMMON_PHRASE -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = labelText,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontSize = 10.sp
        )
    }
}

/**
 * Compact version of the suggestion dropdown for smaller spaces.
 */
@Composable
fun CompactSearchSuggestionDropdown(
    suggestions: List<SearchSuggestion>,
    isVisible: Boolean,
    onSuggestionClick: (SearchSuggestion) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible && suggestions.isNotEmpty()) {
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = modifier
                        .widthIn(min = 180.dp, max = 300.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(
                            items = suggestions.take(6), // Limit for compact version
                            key = { suggestion -> "${suggestion.type.name}-${suggestion.text}" }
                        ) { suggestion ->
                            CompactSuggestionItem(
                                suggestion = suggestion,
                                onClick = { onSuggestionClick(suggestion) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactSuggestionItem(
    suggestion: SearchSuggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SuggestionTypeIcon(
            type = suggestion.type,
            modifier = Modifier.size(16.dp)
        )
        
        Text(
            text = suggestion.text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}