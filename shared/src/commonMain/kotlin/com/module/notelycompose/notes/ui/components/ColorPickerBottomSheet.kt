package com.module.notelycompose.notes.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.ui.components.MaterialIcon
import com.module.notelycompose.notes.ui.theme.MaterialSymbols

/**
 * Color picker mode for distinguishing between text color and highlight color selection.
 */
enum class ColorPickerMode {
    TEXT_COLOR,
    HIGHLIGHT_COLOR
}

/**
 * Color picker data class representing a color option with metadata.
 */
data class ColorPickerOption(
    val color: Color?,
    val name: String,
    val contentDescription: String,
    val isDefault: Boolean = false
)

/**
 * Mobile-friendly color picker bottom sheet with Material 3 design.
 * 
 * Features:
 * - Material 3 bottom sheet design with expressive styling
 * - Curated color palette optimized for text and highlighting
 * - Touch-friendly 44dp minimum targets
 * - Haptic feedback for selection
 * - Accessibility support with semantic descriptions
 * - Smooth animations and state transitions
 * - Remove color option for clearing formatting
 * 
 * @param isVisible Whether the bottom sheet is currently visible
 * @param mode The color picker mode (text color or highlight color)
 * @param selectedColor The currently selected color (null for default/none)
 * @param onColorSelected Callback when a color is selected
 * @param onDismiss Callback when the bottom sheet should be dismissed
 * @param modifier Modifier for the bottom sheet container
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerBottomSheet(
    isVisible: Boolean,
    mode: ColorPickerMode,
    selectedColor: Color?,
    onColorSelected: (Color?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    
    // Color palettes optimized for Material 3 design
    val textColors = remember {
        listOf(
            ColorPickerOption(
                color = null,
                name = "Default",
                contentDescription = "Default text color",
                isDefault = true
            ),
            ColorPickerOption(
                color = Color(0xFF1C1B1F), // Material onSurface
                name = "Black",
                contentDescription = "Black text color"
            ),
            ColorPickerOption(
                color = Color(0xFF6750A4), // Material primary
                name = "Primary",
                contentDescription = "Primary color"
            ),
            ColorPickerOption(
                color = Color(0xFFBA1A1A), // Material error
                name = "Red",
                contentDescription = "Red text color"
            ),
            ColorPickerOption(
                color = Color(0xFF1565C0), // Material blue
                name = "Blue",
                contentDescription = "Blue text color"
            ),
            ColorPickerOption(
                color = Color(0xFF2E7D32), // Material green
                name = "Green",
                contentDescription = "Green text color"
            ),
            ColorPickerOption(
                color = Color(0xFFF57C00), // Material orange
                name = "Orange", 
                contentDescription = "Orange text color"
            ),
            ColorPickerOption(
                color = Color(0xFF7B1FA2), // Material purple
                name = "Purple",
                contentDescription = "Purple text color"
            ),
            ColorPickerOption(
                color = Color(0xFF00796B), // Material teal
                name = "Teal",
                contentDescription = "Teal text color"
            ),
            ColorPickerOption(
                color = Color(0xFF5D4037), // Material brown
                name = "Brown",
                contentDescription = "Brown text color"
            ),
            ColorPickerOption(
                color = Color(0xFF616161), // Material gray
                name = "Gray",
                contentDescription = "Gray text color"
            ),
            ColorPickerOption(
                color = Color(0xFFAD1457), // Material pink
                name = "Pink",
                contentDescription = "Pink text color"
            )
        )
    }
    
    val highlightColors = remember {
        listOf(
            ColorPickerOption(
                color = null,
                name = "None",
                contentDescription = "No highlight color",
                isDefault = true
            ),
            ColorPickerOption(
                color = Color(0xFFFFF59D), // Light yellow
                name = "Yellow",
                contentDescription = "Yellow highlight"
            ),
            ColorPickerOption(
                color = Color(0xFFE1F5FE), // Light blue
                name = "Blue",
                contentDescription = "Blue highlight"
            ),
            ColorPickerOption(
                color = Color(0xFFE8F5E8), // Light green
                name = "Green",
                contentDescription = "Green highlight"
            ),
            ColorPickerOption(
                color = Color(0xFFFFE0E0), // Light red
                name = "Red",
                contentDescription = "Red highlight"
            ),
            ColorPickerOption(
                color = Color(0xFFFFF3E0), // Light orange
                name = "Orange",
                contentDescription = "Orange highlight"
            ),
            ColorPickerOption(
                color = Color(0xFFF3E5F5), // Light purple
                name = "Purple",
                contentDescription = "Purple highlight"
            ),
            ColorPickerOption(
                color = Color(0xFFE0F2F1), // Light teal
                name = "Teal",
                contentDescription = "Teal highlight"
            ),
            ColorPickerOption(
                color = Color(0xFFEFEBE9), // Light brown
                name = "Brown",
                contentDescription = "Brown highlight"
            ),
            ColorPickerOption(
                color = Color(0xFFFCE4EC), // Light pink
                name = "Pink",
                contentDescription = "Pink highlight"
            ),
            ColorPickerOption(
                color = Color(0xFFF1F8E9), // Light lime
                name = "Lime",
                contentDescription = "Lime highlight"
            ),
            ColorPickerOption(
                color = Color(0xFFE8EAF6), // Light indigo
                name = "Indigo",
                contentDescription = "Indigo highlight"
            )
        )
    }
    
    val colors = if (mode == ColorPickerMode.TEXT_COLOR) textColors else highlightColors
    val titleText = if (mode == ColorPickerMode.TEXT_COLOR) "Text Color" else "Highlight Color"
    
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState,
            modifier = modifier,
            dragHandle = {
                // Custom drag handle with Material 3 styling
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(width = 32.dp, height = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        ) {
            ColorPickerContent(
                title = titleText,
                colors = colors,
                selectedColor = selectedColor,
                onColorSelected = { color ->
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onColorSelected(color)
                },
                onDismiss = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Content composable for the color picker with grid layout and animations.
 */
@Composable
private fun ColorPickerContent(
    title: String,
    colors: List<ColorPickerOption>,
    selectedColor: Color?,
    onColorSelected: (Color?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header with title and close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(40.dp)
            ) {
                MaterialIcon(
                    symbol = MaterialSymbols.Close,
                    contentDescription = "Close color picker",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Color grid with optimized layout
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(colors) { colorOption ->
                ColorPickerItem(
                    colorOption = colorOption,
                    isSelected = colorOption.color == selectedColor,
                    onClick = { onColorSelected(colorOption.color) }
                )
            }
        }
        
        // Bottom spacing for sheet content
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Individual color picker item with Material 3 styling and animations.
 */
@Composable
private fun ColorPickerItem(
    colorOption: ColorPickerOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )
    
    val animatedElevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "elevation_animation"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .semantics {
                contentDescription = colorOption.contentDescription + if (isSelected) ", selected" else ""
            }
    ) {
        // Color swatch with selection indication
        Box(
            modifier = Modifier.size(56.dp)
        ) {
            // Selection ring
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
            
            // Color swatch
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.Center),
                shape = CircleShape,
                color = colorOption.color ?: MaterialTheme.colorScheme.surface,
                tonalElevation = animatedElevation,
                shadowElevation = if (isSelected) 4.dp else 1.dp,
                border = if (colorOption.isDefault || colorOption.color == null) {
                    BorderStroke(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else null
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (colorOption.isDefault) {
                        // Default/None indicator
                        if (colorOption.name == "Default") {
                            Text(
                                text = "A",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            // None/Clear indicator for highlights
                            MaterialIcon(
                                symbol = MaterialSymbols.FormatColorReset,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // Selection checkmark
                    if (isSelected && !colorOption.isDefault) {
                        Surface(
                            modifier = Modifier.size(16.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ) {
                            MaterialIcon(
                                symbol = MaterialSymbols.Check,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Color name label
        Text(
            text = colorOption.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}