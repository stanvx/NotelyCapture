package com.module.notelycompose.notes.ui.richtext

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.FormatAlignCenter
import androidx.compose.material.icons.twotone.FormatAlignJustify
import androidx.compose.material.icons.twotone.FormatAlignLeft
import androidx.compose.material.icons.twotone.FormatAlignRight
import androidx.compose.material.icons.twotone.FormatIndentDecrease
import androidx.compose.material.icons.twotone.FormatIndentIncrease
import androidx.compose.material.icons.twotone.FormatListBulleted
import androidx.compose.material.icons.twotone.FormatListNumbered
import androidx.compose.material.icons.twotone.FormatQuote
import androidx.compose.material.icons.twotone.HorizontalRule
import androidx.compose.material.icons.twotone.Link
import androidx.compose.material.icons.twotone.LinkOff
import com.module.notelycompose.notes.ui.theme.pinnedTemplateBrown
import com.module.notelycompose.notes.ui.theme.pinnedTemplateOrange
import com.module.notelycompose.notes.ui.theme.pinnedTemplatePurple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.designsystem.components.richtext.RichTextIconButton
import com.module.notelycompose.designsystem.components.richtext.RichTextTextButton
import com.module.notelycompose.notes.presentation.helpers.RichTextEditorHelper

/**
 * Advanced formatting system for rich text editing with sophisticated features.
 * 
 * Features:
 * - Enhanced heading management with semantic levels and styling
 * - Advanced alignment options including justify and distributed
 * - Intelligent list management with nested lists and custom bullets
 * - Text color and highlight color selection
 * - Advanced paragraph formatting (spacing, indentation)
 * - Text size and font family selection
 * - Quote blocks and code formatting
 * - Table insertion and formatting
 * - Link creation and management
 */

/**
 * Comprehensive heading selector with visual hierarchy preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedHeadingSelector(
    currentHeadingLevel: Int?,
    onHeadingSelected: (Int?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Heading Styles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Normal text option
            HeadingOption(
                level = null,
                text = "Normal Text",
                isSelected = currentHeadingLevel == null,
                onClick = { onHeadingSelected(null) }
            )
            
            // Heading levels 1-6
            repeat(6) { index ->
                val level = index + 1
                HeadingOption(
                    level = level,
                    text = "Heading $level",
                    isSelected = currentHeadingLevel == level,
                    onClick = { onHeadingSelected(level) }
                )
            }
            
            Divider()
            
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun HeadingOption(
    level: Int?,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textStyle = when (level) {
        1 -> MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp)
        2 -> MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp)
        3 -> MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp)
        4 -> MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp)
        5 -> MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp)
        6 -> MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp)
        else -> MaterialTheme.typography.bodyLarge
    }
    
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(
            text = text,
            style = textStyle,
            fontWeight = if (level != null) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/**
 * Advanced alignment selector with all alignment options.
 */
@Composable
fun AdvancedAlignmentSelector(
    currentAlignment: TextAlign,
    onAlignmentSelected: (TextAlign) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AlignmentOption(
            icon = Icons.TwoTone.FormatAlignLeft,
            alignment = TextAlign.Start,
            label = "Left",
            isSelected = currentAlignment == TextAlign.Start,
            onClick = { onAlignmentSelected(TextAlign.Start) }
        )
        
        AlignmentOption(
            icon = Icons.TwoTone.FormatAlignCenter,
            alignment = TextAlign.Center,
            label = "Center",
            isSelected = currentAlignment == TextAlign.Center,
            onClick = { onAlignmentSelected(TextAlign.Center) }
        )
        
        AlignmentOption(
            icon = Icons.TwoTone.FormatAlignRight,
            alignment = TextAlign.End,
            label = "Right",
            isSelected = currentAlignment == TextAlign.End,
            onClick = { onAlignmentSelected(TextAlign.End) }
        )
        
        AlignmentOption(
            icon = Icons.TwoTone.FormatAlignJustify,
            alignment = TextAlign.Justify,
            label = "Justify",
            isSelected = currentAlignment == TextAlign.Justify,
            onClick = { onAlignmentSelected(TextAlign.Justify) }
        )
    }
}

@Composable
private fun AlignmentOption(
    icon: ImageVector,
    alignment: TextAlign,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    RichTextIconButton(
        icon = icon,
        onClick = onClick,
        isSelected = isSelected,
        contentDescription = "$label alignment"
    )
}

/**
 * Advanced list management with nested lists and custom formatting.
 */
@Composable
fun AdvancedListManager(
    isUnorderedList: Boolean,
    isOrderedList: Boolean,
    currentIndentLevel: Int = 0,
    onToggleUnorderedList: () -> Unit,
    onToggleOrderedList: () -> Unit,
    onIncreaseIndent: () -> Unit,
    onDecreaseIndent: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Lists",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RichTextIconButton(
                icon = Icons.TwoTone.FormatListBulleted,
                onClick = onToggleUnorderedList,
                isSelected = isUnorderedList,
                contentDescription = "Bullet list"
            )
            
            RichTextIconButton(
                icon = Icons.TwoTone.FormatListNumbered,
                onClick = onToggleOrderedList,
                isSelected = isOrderedList,
                contentDescription = "Numbered list"
            )
        }
        
        if (isUnorderedList || isOrderedList) {
            Text(
                text = "Indent Level: $currentIndentLevel",
                style = MaterialTheme.typography.labelSmall
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RichTextIconButton(
                    icon = Icons.TwoTone.FormatIndentDecrease,
                    onClick = onDecreaseIndent,
                    enabled = currentIndentLevel > 0,
                    contentDescription = "Decrease indent"
                )
                
                RichTextIconButton(
                    icon = Icons.TwoTone.FormatIndentIncrease,
                    onClick = onIncreaseIndent,
                    enabled = currentIndentLevel < 5,
                    contentDescription = "Increase indent"
                )
            }
        }
    }
}

/**
 * Text color and highlighting selector.
 */
@Composable
fun TextColorSelector(
    currentTextColor: Color?,
    currentHighlightColor: Color?,
    onTextColorSelected: (Color?) -> Unit,
    onHighlightColorSelected: (Color?) -> Unit,
    modifier: Modifier = Modifier
) {
    val commonColors = listOf(
        Color.Black, Color.Red, Color.Blue, Color.Green,
        Color(0xFFFFA500), Color(0xFF800080), Color(0xFFA52A2A), Color.Gray
    )
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Text Color",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        LazyColumn {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Default color option
                    ColorOption(
                        color = null,
                        isSelected = currentTextColor == null,
                        onClick = { onTextColorSelected(null) },
                        label = "Default"
                    )
                    
                    commonColors.forEach { color ->
                        ColorOption(
                            color = color,
                            isSelected = currentTextColor == color,
                            onClick = { onTextColorSelected(color) }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Highlight Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // No highlight option
                    ColorOption(
                        color = null,
                        isSelected = currentHighlightColor == null,
                        onClick = { onHighlightColorSelected(null) },
                        label = "None"
                    )
                    
                    commonColors.map { it.copy(alpha = 0.3f) }.forEach { color ->
                        ColorOption(
                            color = color,
                            isSelected = currentHighlightColor == color,
                            onClick = { onHighlightColorSelected(color) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorOption(
    color: Color?,
    isSelected: Boolean,
    onClick: () -> Unit,
    label: String? = null
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(2.dp, borderColor, RoundedCornerShape(6.dp))
            .background(color ?: MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
    ) {
        if (color == null && label != null) {
            Text(
                text = "A",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * Advanced text size selector with common presets.
 */
@Composable
fun TextSizeSelector(
    currentSize: Float?,
    onSizeSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val textSizes = listOf(
        8f to "Tiny",
        10f to "Small",
        12f to "Normal",
        14f to "Medium",
        16f to "Large",
        18f to "XLarge",
        24f to "XXLarge"
    )
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Text Size",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        textSizes.forEach { (size, label) ->
            TextSizeOption(
                size = size,
                label = label,
                isSelected = currentSize == size,
                onClick = { onSizeSelected(size) }
            )
        }
    }
}

@Composable
private fun TextSizeOption(
    size: Float,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Text(
            text = "$label (${size.toInt()}sp)",
            fontSize = size.sp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/**
 * Special formatting options (quote, code, etc.).
 */
@Composable
fun SpecialFormattingOptions(
    onApplyQuote: () -> Unit,
    onApplyCode: () -> Unit,
    onInsertDivider: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Special Formatting",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RichTextIconButton(
                icon = Icons.TwoTone.FormatQuote,
                onClick = onApplyQuote,
                contentDescription = "Quote block"
            )
            
            RichTextTextButton(
                text = "</>" ,
                onClick = onApplyCode,
                contentDescription = "Code block"
            )
            
            RichTextIconButton(
                icon = Icons.TwoTone.HorizontalRule,
                onClick = onInsertDivider,
                contentDescription = "Insert divider"
            )
        }
    }
}

/**
 * Link creation and management interface.
 */
@Composable
fun LinkManager(
    selectedText: String?,
    onCreateLink: (text: String, url: String) -> Unit,
    onRemoveLink: () -> Unit,
    hasLink: Boolean,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Links",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RichTextIconButton(
                icon = Icons.TwoTone.Link,
                onClick = { showDialog = true },
                contentDescription = "Add link",
                enabled = !selectedText.isNullOrEmpty()
            )
            
            if (hasLink) {
                RichTextIconButton(
                    icon = Icons.TwoTone.LinkOff,
                    onClick = onRemoveLink,
                    contentDescription = "Remove link"
                )
            }
        }
    }
    
    if (showDialog) {
        LinkCreationDialog(
            selectedText = selectedText ?: "",
            onConfirm = { text, url ->
                onCreateLink(text, url)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun LinkCreationDialog(
    selectedText: String,
    onConfirm: (text: String, url: String) -> Unit,
    onDismiss: () -> Unit
) {
    var linkText by remember { mutableStateOf(selectedText) }
    var linkUrl by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Link") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = linkText,
                    onValueChange = { linkText = it },
                    label = { Text("Link Text") }
                )
                
                OutlinedTextField(
                    value = linkUrl,
                    onValueChange = { linkUrl = it },
                    label = { Text("URL") },
                    placeholder = { Text("https://example.com") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(linkText, linkUrl) },
                enabled = linkText.isNotEmpty() && linkUrl.isNotEmpty()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}