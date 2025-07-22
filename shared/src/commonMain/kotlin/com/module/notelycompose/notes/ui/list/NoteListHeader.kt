package com.module.notelycompose.notes.ui.list

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.ui.theme.Material3ShapeTokens
import kotlin.math.*

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
    onSearchClick: () -> Unit,
    noteCount: Int,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header_animation")
    
    // Enhanced floating animation for background elements
    val floatingOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_1"
    )
    
    val floatingOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_2"
    )
    
    // Add gradient animation like capture screen for more visual impact
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_offset"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = Material3ShapeTokens.surfaceContainer,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTablet) 160.dp else 140.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f)
                        ),
                        start = Offset(gradientOffset, 0f),
                        end = Offset(gradientOffset + 600f, 200f)
                    )
                )
        ) {
            // Extract theme values outside Canvas
            val isDark = isSystemInDarkTheme()
            val circleBaseColor = if (isDark) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            
            // Floating background elements
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                
                // Enhanced floating particles effect with more layers
                val particleCount = 6
                for (i in 0 until particleCount) {
                    // Large background circles with varied opacity
                    drawCircle(
                        color = circleBaseColor.copy(alpha = if (isDark) 0.08f - (i * 0.01f) else 0.04f - (i * 0.005f)),
                        radius = (120f - i * 15f),
                        center = Offset(
                            x = size.width * (0.1f + i * 0.15f),
                            y = centerY + floatingOffset1 + (i * 8f)
                        )
                    )
                }
                
                // Additional floating accent circles
                for (i in 0 until 4) {
                    drawCircle(
                        color = circleBaseColor.copy(alpha = if (isDark) 0.06f else 0.03f),
                        radius = (40f + i * 10f),
                        center = Offset(
                            x = size.width * (0.2f + i * 0.2f),
                            y = size.height * (0.3f + (i % 2) * 0.4f) + floatingOffset2 * 0.8f
                        )
                    )
                }
                
                // Additional micro particles for richness
                for (i in 0 until 8) {
                    drawCircle(
                        color = circleBaseColor.copy(alpha = if (isDark) 0.04f else 0.02f),
                        radius = (8f + i * 2f),
                        center = Offset(
                            x = size.width * (0.15f + i * 0.1f),
                            y = size.height * (0.1f + (i % 3) * 0.3f) + floatingOffset1 * 0.3f
                        )
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Enhanced main title with more visual impact
                Text(
                    text = "Your Thoughts",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = if (isTablet) 36.sp else 32.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.ExtraBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Note count and search row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Note count with animated number
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedNoteCount(
                            count = noteCount,
                            textColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        // ...existing code...
                    }
                    
                    // Search button
                    SearchButton(
                        onClick = onSearchClick,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        iconColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Animated note count display with smooth transitions.
 */
@Composable
private fun AnimatedNoteCount(
    count: Int,
    textColor: Color
) {
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Text(
        text = when (animatedCount) {
            0 -> "Start capturing ideas"
            1 -> "1 note captured"
            else -> "$animatedCount notes captured"
        },
        style = MaterialTheme.typography.bodyLarge,
        color = textColor,
        fontWeight = FontWeight.Medium
    )
}

/**
 * Floating search button with subtle animations.
 */
@Composable
private fun SearchButton(
    onClick: () -> Unit,
    backgroundColor: Color,
    iconColor: Color
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )
    
    Surface(
        onClick = {
            isPressed = true
            onClick()
        },
        shape = CircleShape,
        color = backgroundColor,
        modifier = Modifier
            .size(48.dp)
            .scale(scale),
        shadowElevation = 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search notes",
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

// ...existing code...

/**
 * Compact version for smaller screens or when space is limited.
 */
@Composable
fun CompactNoteListHeader(
    onSearchClick: () -> Unit,
    noteCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Your Thoughts",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (noteCount == 0) "Start capturing" else "$noteCount notes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}