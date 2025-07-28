package com.module.notelycompose.notes.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.ui.theme.Material3ShapeTokens
import com.module.notelycompose.notes.ui.theme.CardElevationPresets

/**
 * Unified header component used across the app for consistent design.
 * Based on NoteListHeader design with configurable content.
 */
@Composable
fun UnifiedHeaderCard(
    title: String,
    subtitle: String,
    actionButton: (@Composable () -> Unit)? = null,
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
    
    // Add gradient animation for visual impact
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
        elevation = CardElevationPresets.headerCard()
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
                // Title and subtitle
                if (actionButton != null) {
                    // Layout with action button on the right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title and subtitle column
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontSize = if (isTablet) 36.sp else 32.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.ExtraBold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // Action button
                        actionButton.invoke()
                    }
                } else {
                    // Layout without action button - centered content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = if (isTablet) 36.sp else 32.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.ExtraBold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Action button component for the unified header.
 */
@Composable
fun HeaderActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    contentDescription: String
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
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .size(48.dp)
            .scale(scale),
        shadowElevation = 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            icon()
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}