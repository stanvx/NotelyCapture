package com.module.notelycompose.designsystem.components.richtext

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Foundation rich text formatting button component following Material 3 design principles.
 * 
 * Features:
 * - Consistent styling across all rich text toolbars
 * - Smooth color transitions with Material 3 semantics
 * - Built-in haptic feedback support
 * - Flexible size and content options
 * - Apple-quality interaction feedback
 * 
 * @param onClick Callback for button press
 * @param isSelected Whether this formatting option is currently active
 * @param modifier Modifier for customization
 * @param size Button size (default 36.dp for optimal touch target)
 * @param contentDescription Accessibility description
 * @param enabled Whether the button is interactive
 * @param hapticFeedback Whether to provide haptic feedback on press
 * @param content Button content (icon or text)
 */
@Composable
fun RichTextButton(
    onClick: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    contentDescription: String? = null,
    enabled: Boolean = true,
    hapticFeedback: Boolean = true,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> Color.Transparent
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "background_color"
    )
    
    Surface(
        onClick = {
            if (enabled) {
                if (hapticFeedback) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onClick()
            }
        },
        modifier = modifier.size(size),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        enabled = enabled
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}

/**
 * Rich text button with icon content.
 */
@Composable
fun RichTextIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    contentDescription: String? = null,
    enabled: Boolean = true,
    hapticFeedback: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val iconTint by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> tint
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "icon_tint"
    )
    
    RichTextButton(
        onClick = onClick,
        isSelected = isSelected,
        modifier = modifier,
        size = size,
        contentDescription = contentDescription,
        enabled = enabled,
        hapticFeedback = hapticFeedback
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Rich text button with text content (e.g., "H1", "H2").
 */
@Composable
fun RichTextTextButton(
    text: String,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    contentDescription: String? = null,
    enabled: Boolean = true,
    hapticFeedback: Boolean = true,
    fontSize: TextUnit = 10.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    val textColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "text_color"
    )
    
    RichTextButton(
        onClick = onClick,
        isSelected = isSelected,
        modifier = modifier,
        size = size,
        contentDescription = contentDescription,
        enabled = enabled,
        hapticFeedback = hapticFeedback
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = fontSize,
                fontWeight = fontWeight
            ),
            color = textColor
        )
    }
}