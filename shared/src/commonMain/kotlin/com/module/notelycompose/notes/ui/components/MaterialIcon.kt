package com.module.notelycompose.notes.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.ui.theme.MaterialSymbolsOutlined
import com.module.notelycompose.notes.ui.theme.MaterialSymbolsFilled
import com.module.notelycompose.notes.ui.theme.MaterialSymbolsLarge

/**
 * Material Symbols Icon composable for easy icon usage throughout the app.
 * 
 * @param symbol The Material Symbol codepoint (e.g., MaterialSymbols.Add)
 * @param contentDescription Accessibility description
 * @param modifier Modifier to be applied to the icon
 * @param size Icon size in Dp - automatically converts to appropriate font size
 * @param tint Icon color
 * @param style Icon style (Outlined, Filled, or Large)
 */
@Composable
fun MaterialIcon(
    symbol: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    style: MaterialIconStyle = MaterialIconStyle.Outlined
) {
    Text(
        text = symbol,
        fontFamily = when (style) {
            MaterialIconStyle.Outlined -> MaterialSymbolsOutlined
            MaterialIconStyle.Filled -> MaterialSymbolsFilled
            MaterialIconStyle.Large -> MaterialSymbolsLarge
        },
        fontSize = size.value.sp,
        color = tint,
        modifier = modifier.size(size)
    )
}

/**
 * Convenience composable for standard 24dp icons
 */
@Composable
fun Icon24(
    symbol: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    style: MaterialIconStyle = MaterialIconStyle.Outlined
) {
    MaterialIcon(
        symbol = symbol,
        contentDescription = contentDescription,
        modifier = modifier,
        size = 24.dp,
        tint = tint,
        style = style
    )
}

/**
 * Convenience composable for small 16dp icons
 */
@Composable
fun Icon16(
    symbol: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    style: MaterialIconStyle = MaterialIconStyle.Outlined
) {
    MaterialIcon(
        symbol = symbol,
        contentDescription = contentDescription,
        modifier = modifier,
        size = 16.dp,
        tint = tint,
        style = style
    )
}

/**
 * Convenience composable for large 48dp icons
 */
@Composable
fun Icon48(
    symbol: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    style: MaterialIconStyle = MaterialIconStyle.Large
) {
    MaterialIcon(
        symbol = symbol,
        contentDescription = contentDescription,
        modifier = modifier,
        size = 48.dp,
        tint = tint,
        style = style
    )
}

/**
 * Material Icon style variants
 */
enum class MaterialIconStyle {
    Outlined,
    Filled,
    Large
}
