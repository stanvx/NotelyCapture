package com.module.notelycompose.designsystem.components.richtext

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.twotone.FormatAlignCenter
import androidx.compose.material.icons.twotone.FormatAlignLeft
import androidx.compose.material.icons.twotone.FormatAlignRight
import androidx.compose.material.icons.twotone.FormatBold
import androidx.compose.material.icons.twotone.FormatClear
import androidx.compose.material.icons.twotone.FormatItalic
import androidx.compose.material.icons.twotone.FormatListBulleted
import androidx.compose.material.icons.twotone.FormatListNumbered
import androidx.compose.material.icons.twotone.FormatUnderlined
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Container for grouping related rich text formatting buttons with optional title and visual separation.
 * 
 * Features:
 * - Consistent spacing between buttons within groups
 * - Optional group titles for better UX
 * - Material 3 design system integration
 * - Flexible layout with proper alignment
 * 
 * @param title Optional title displayed above the button group
 * @param modifier Modifier for customization
 * @param content Row content containing RichTextButton components
 */
@Composable
fun RichTextButtonGroup(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        title?.let { groupTitle ->
            Text(
                text = groupTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
    }
}

/**
 * Visual divider between formatting groups in horizontal toolbars.
 * 
 * Features:
 * - Subtle Material 3 outline color
 * - Appropriate height for toolbar context
 * - Low opacity for non-intrusive separation
 */
@Composable
fun RichTextGroupDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(1.dp)
            .height(48.dp)
            .background(
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
    )
}

/**
 * Comprehensive formatting group for text style options (Bold, Italic, Underline).
 * 
 * @param isBold Current bold state
 * @param isItalic Current italic state  
 * @param isUnderlined Current underline state
 * @param onToggleBold Bold toggle callback
 * @param onToggleItalic Italic toggle callback
 * @param onToggleUnderline Underline toggle callback
 * @param modifier Modifier for customization
 */
@Composable
fun TextStyleButtonGroup(
    isBold: Boolean,
    isItalic: Boolean,
    isUnderlined: Boolean,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    modifier: Modifier = Modifier
) {
    RichTextButtonGroup(
        title = "Format",
        modifier = modifier
    ) {
        RichTextIconButton(
            icon = Icons.TwoTone.FormatBold,
            onClick = onToggleBold,
            isSelected = isBold,
            contentDescription = "Bold"
        )
        
        RichTextIconButton(
            icon = Icons.TwoTone.FormatItalic,
            onClick = onToggleItalic,
            isSelected = isItalic,
            contentDescription = "Italic"
        )
        
        RichTextIconButton(
            icon = Icons.TwoTone.FormatUnderlined,
            onClick = onToggleUnderline,
            isSelected = isUnderlined,
            contentDescription = "Underline"
        )
    }
}

/**
 * Alignment formatting group (Left, Center, Right).
 * 
 * @param currentAlignment Current text alignment
 * @param onSetAlignment Alignment change callback
 * @param modifier Modifier for customization
 */
@Composable
fun AlignmentButtonGroup(
    currentAlignment: androidx.compose.ui.text.style.TextAlign,
    onSetAlignment: (androidx.compose.ui.text.style.TextAlign) -> Unit,
    modifier: Modifier = Modifier
) {
    RichTextButtonGroup(
        title = "Align",
        modifier = modifier
    ) {
        RichTextIconButton(
            icon = Icons.TwoTone.FormatAlignLeft,
            onClick = { onSetAlignment(androidx.compose.ui.text.style.TextAlign.Start) },
            isSelected = currentAlignment == androidx.compose.ui.text.style.TextAlign.Start,
            contentDescription = "Align Left"
        )
        
        RichTextIconButton(
            icon = Icons.TwoTone.FormatAlignCenter,
            onClick = { onSetAlignment(androidx.compose.ui.text.style.TextAlign.Center) },
            isSelected = currentAlignment == androidx.compose.ui.text.style.TextAlign.Center,
            contentDescription = "Align Center"
        )
        
        RichTextIconButton(
            icon = Icons.TwoTone.FormatAlignRight,
            onClick = { onSetAlignment(androidx.compose.ui.text.style.TextAlign.End) },
            isSelected = currentAlignment == androidx.compose.ui.text.style.TextAlign.End,
            contentDescription = "Align Right"
        )
    }
}

/**
 * List formatting group (Bullet List, Numbered List).
 * 
 * @param isUnorderedList Current unordered list state
 * @param isOrderedList Current ordered list state
 * @param onToggleUnorderedList Bullet list toggle callback
 * @param onToggleOrderedList Numbered list toggle callback
 * @param modifier Modifier for customization
 */
@Composable
fun ListButtonGroup(
    isUnorderedList: Boolean,
    isOrderedList: Boolean,
    onToggleUnorderedList: () -> Unit,
    onToggleOrderedList: () -> Unit,
    modifier: Modifier = Modifier
) {
    RichTextButtonGroup(
        title = "Lists",
        modifier = modifier
    ) {
        RichTextIconButton(
            icon = Icons.TwoTone.FormatListBulleted,
            onClick = onToggleUnorderedList,
            isSelected = isUnorderedList,
            contentDescription = "Bullet List"
        )
        
        RichTextIconButton(
            icon = Icons.TwoTone.FormatListNumbered,
            onClick = onToggleOrderedList,
            isSelected = isOrderedList,
            contentDescription = "Numbered List"
        )
    }
}

/**
 * Heading formatting group (H1, H2, H3).
 * 
 * @param onAddHeading Heading level callback
 * @param modifier Modifier for customization
 */
@Composable
fun HeadingButtonGroup(
    onAddHeading: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    RichTextButtonGroup(
        title = "Headings",
        modifier = modifier
    ) {
        RichTextTextButton(
            text = "H1",
            onClick = { onAddHeading(1) },
            contentDescription = "Heading 1"
        )
        
        RichTextTextButton(
            text = "H2",
            onClick = { onAddHeading(2) },
            contentDescription = "Heading 2"
        )
        
        RichTextTextButton(
            text = "H3",
            onClick = { onAddHeading(3) },
            contentDescription = "Heading 3"
        )
    }
}

/**
 * Action button group for formatting operations (Clear Formatting, etc.).
 * 
 * @param onClearFormatting Clear formatting callback
 * @param modifier Modifier for customization
 */
@Composable
fun ActionButtonGroup(
    onClearFormatting: () -> Unit,
    modifier: Modifier = Modifier
) {
    RichTextButtonGroup(
        title = "Actions",
        modifier = modifier
    ) {
        RichTextIconButton(
            icon = Icons.TwoTone.FormatClear,
            onClick = onClearFormatting,
            contentDescription = "Clear Formatting",
            tint = MaterialTheme.colorScheme.error
        )
    }
}