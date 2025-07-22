package com.module.notelycompose.notes.ui.capture

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.module.notelycompose.notes.ui.components.MaterialIcon
import com.module.notelycompose.notes.ui.components.MaterialIconStyle
import com.module.notelycompose.notes.ui.theme.MaterialSymbols
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.module.notelycompose.notes.ui.components.ExtendedVoiceFAB
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import com.module.notelycompose.notes.ui.components.ExtendedVoiceFAB
import com.module.notelycompose.notes.ui.theme.voiceNoteIndicatorContainer
import com.module.notelycompose.notes.ui.theme.onVoiceNoteIndicatorContainer
import com.module.notelycompose.notes.ui.theme.textNoteIndicatorContainer
import com.module.notelycompose.notes.ui.theme.onTextNoteIndicatorContainer
import com.module.notelycompose.notes.ui.theme.heroGradientStart
import com.module.notelycompose.notes.ui.theme.heroGradientMiddle
import com.module.notelycompose.notes.ui.theme.heroGradientEnd
import com.module.notelycompose.notes.ui.theme.capturePhotographyContainer
import com.module.notelycompose.notes.ui.theme.onCapturePhotographyContainer
import com.module.notelycompose.notes.ui.theme.captureVideoContainer
import com.module.notelycompose.notes.ui.theme.onCaptureVideoContainer
import com.module.notelycompose.notes.ui.theme.captureWhiteboardContainer
import com.module.notelycompose.notes.ui.theme.onCaptureWhiteboardContainer
import com.module.notelycompose.notes.ui.theme.captureFilesContainer
import com.module.notelycompose.notes.ui.theme.onCaptureFilesContainer
import com.module.notelycompose.notes.ui.theme.pinnedTemplateGreen
import com.module.notelycompose.notes.ui.theme.pinnedTemplateOrange
import com.module.notelycompose.notes.ui.theme.pinnedTemplateTeal
import com.module.notelycompose.notes.ui.theme.pinnedTemplatePurple
import com.module.notelycompose.notes.ui.theme.pinnedTemplateBrown
import com.module.notelycompose.notes.ui.theme.pinnedTemplatePink

/**
 * Material 3 Expressive Capture Hub Screen for Notely Capture.
 * 
 * Features a colorful grid of capture methods, pinned templates,
 * and tag-based quick capture options.
 */
/**
 * CaptureHubScreen composable.
 *
 * IMPORTANT: The navigateToQuickRecord lambda MUST be connected to your navigation system (e.g., navController.navigateSingleTop(Routes.QuickRecord))
 * for the FAB to work. If not set, the FAB will do nothing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureHubScreen(
    onVoiceCapture: () -> Unit,
    onCameraCapture: () -> Unit,
    onVideoCapture: () -> Unit,
    onTextCapture: () -> Unit,
    onWhiteboardCapture: () -> Unit,
    onFileCapture: () -> Unit,
    navigateToQuickRecord: (() -> Unit)? = null,
    onNavigateToSettings: () -> Unit,
    navigateBack: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    
    Scaffold(
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToSettings()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 104.dp // Account for navigation bar + FAB space
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Section
            item {
                HeroSection()
            }
            
            // Quick Stats Widget
            item {
                QuickStatsWidget()
            }
            
            // Pinned Templates Section
            item {
                PinnedTemplatesSection()
            }
            
            // Standard Capture Methods
            item {
                Text(
                    text = "Capture Methods",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
                
            // Capture Methods Grid (2 per row) - Static layout to avoid nested scrolling
            item {
                val captureMethodsList = getCaptureMethodsList()
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Group capture methods into rows of 2
                    captureMethodsList.chunked(2).forEach { rowMethods ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowMethods.forEach { captureMethod ->
                                CompactCaptureMethodCard(
                                    captureMethod = captureMethod,
                                    onClick = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        when (captureMethod.type) {
                                            CaptureType.Voice -> onVoiceCapture()
                                            CaptureType.Camera -> onCameraCapture()
                                            CaptureType.Video -> onVideoCapture()
                                            CaptureType.Text -> onTextCapture()
                                            CaptureType.Whiteboard -> onWhiteboardCapture()
                                            CaptureType.Files -> onFileCapture()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining space if odd number of items in row
                            if (rowMethods.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Animated gradient background
            val infiniteTransition = rememberInfiniteTransition(label = "hero_animation")
            val animatedOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1000f,
                animationSpec = infiniteRepeatable(
                    animation = tween(20000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "gradient_offset"
            )
            
            // Extract theme colors outside Canvas
            val gradientColors = listOf(
                MaterialTheme.colorScheme.heroGradientStart,
                MaterialTheme.colorScheme.heroGradientMiddle,
                MaterialTheme.colorScheme.heroGradientEnd,
                MaterialTheme.colorScheme.heroGradientStart
            )
            
            val isDark = isSystemInDarkTheme()
            val particleColor = if (isDark) {
                Color.White.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f)
            }
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(animatedOffset, 0f),
                        end = Offset(animatedOffset + size.width, size.height)
                    )
                )
                
                // Theme-aware floating particles effect
                val particleCount = 8
                for (i in 0 until particleCount) {
                    drawCircle(
                        color = particleColor,
                        radius = (8 + i * 3).dp.toPx(),
                        center = Offset(
                            x = size.width * (0.1f + i * 0.12f),
                            y = size.height * (0.2f + (i % 3) * 0.2f)
                        )
                    )
                }
            }
            
            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Capture Everything",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isSystemInDarkTheme()) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Ideas • Moments • Memories",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSystemInDarkTheme()) {
                        Color.White.copy(alpha = 0.9f)
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    },
                    letterSpacing = 1.sp,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun QuickStatsWidget() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = MaterialSymbols.Schedule,
                label = "Today",
                value = "3"
            )
            StatItem(
                icon = MaterialSymbols.TrendingUp,
                label = "This Week", 
                value = "12"
            )
            StatItem(
                icon = MaterialSymbols.Mic,
                label = "Voice Notes",
                value = "8"
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: String,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MaterialIcon(
            symbol = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            size = 24.dp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PinnedTemplatesSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pinned",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            MaterialIcon(
                symbol = MaterialSymbols.Add,
                contentDescription = "Add template",
                tint = MaterialTheme.colorScheme.primary,
                size = 24.dp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val pinnedTemplates = getPinnedTemplates()
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(pinnedTemplates) { template ->
                PinnedTemplateCard(template = template)
            }
        }
    }
}

@Composable
private fun PinnedTemplateCard(template: PinnedTemplate) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.4f),
        label = "template_scale"
    )
    
    Card(
        modifier = Modifier
            .size(width = 150.dp, height = 110.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 4.dp else 8.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            template.color,
                            template.color.copy(alpha = 0.7f),
                            template.color.copy(alpha = 0.5f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(200f, 200f)
                    )
                )
        ) {
            // Glassmorphism overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.White.copy(alpha = 0.1f)
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Light
                    )
                    
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.voiceNoteIndicatorContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            MaterialIcon(
                                symbol = MaterialSymbols.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onVoiceNoteIndicatorContainer,
                                size = 16.dp,
                                style = MaterialIconStyle.Filled
                            )
                        }
                    }
                }
                
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CompactCaptureMethodCard(
    captureMethod: CaptureMethod,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.4f),
        label = "card_scale"
    )
    
    Card(
        modifier = modifier
            .height(110.dp) // Reduced height for better proportions
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(20.dp), // Slightly more rounded
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = captureMethod.backgroundColor
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                // More subtle icon with softer background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            // Much more subtle background - using primary color with very low alpha
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIcon(
                        symbol = captureMethod.icon,
                        contentDescription = captureMethod.name,
                        tint = captureMethod.iconBackgroundColor.copy(alpha = 0.8f), // Slightly transparent for subtlety
                        size = 24.dp,
                        style = MaterialIconStyle.Filled
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Better text scaling and color
                Text(
                    text = captureMethod.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = captureMethod.iconBackgroundColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun QuickTagsSection() {
    Column {
        Text(
            text = "Quick Tags",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(getQuickTags()) { tag ->
                EnhancedQuickTagChip(tag = tag)
            }
        }
    }
}

@Composable
private fun EnhancedQuickTagChip(tag: String) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.clickable { 
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MaterialIcon(
                symbol = MaterialSymbols.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                size = 18.dp
            )
            
            Text(
                text = tag,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Data classes
data class CaptureMethod(
    val type: CaptureType,
    val name: String,
    val icon: String,
    val backgroundColor: Color,
    val iconBackgroundColor: Color
)

enum class CaptureType {
    Voice, Camera, Video, Text, Whiteboard, Files
}

data class PinnedTemplate(
    val name: String,
    val color: Color,
    val icon: String = MaterialSymbols.Add
)

// Data providers
@Composable
private fun getCaptureMethodsList(): List<CaptureMethod> = listOf(
    CaptureMethod(
        type = CaptureType.Voice,
        name = "Voice",
        icon = MaterialSymbols.Mic,
        backgroundColor = MaterialTheme.colorScheme.voiceNoteIndicatorContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.onVoiceNoteIndicatorContainer
    ),
    CaptureMethod(
        type = CaptureType.Camera,
        name = "Camera",
        icon = MaterialSymbols.PhotoCamera,
        backgroundColor = MaterialTheme.colorScheme.capturePhotographyContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.onCapturePhotographyContainer
    ),
    CaptureMethod(
        type = CaptureType.Video,
        name = "Video",
        icon = MaterialSymbols.Videocam,
        backgroundColor = MaterialTheme.colorScheme.captureVideoContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.onCaptureVideoContainer
    ),
    CaptureMethod(
        type = CaptureType.Text,
        name = "Text",
        icon = MaterialSymbols.Edit,
        backgroundColor = MaterialTheme.colorScheme.textNoteIndicatorContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.onTextNoteIndicatorContainer
    ),
    CaptureMethod(
        type = CaptureType.Whiteboard,
        name = "Whiteboard",
        icon = MaterialSymbols.Create,
        backgroundColor = MaterialTheme.colorScheme.captureWhiteboardContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.onCaptureWhiteboardContainer
    ),
    CaptureMethod(
        type = CaptureType.Files,
        name = "Files",
        icon = MaterialSymbols.FolderOpen,
        backgroundColor = MaterialTheme.colorScheme.captureFilesContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.onCaptureFilesContainer
    )
)

private fun getPinnedTemplates(): List<PinnedTemplate> = listOf(
    PinnedTemplate("CBTLogEntry", MaterialTheme.colorScheme.pinnedTemplateGreen),
    PinnedTemplate("Journal", MaterialTheme.colorScheme.pinnedTemplateOrange),
    PinnedTemplate("Idea", MaterialTheme.colorScheme.pinnedTemplateTeal),
    PinnedTemplate("Resource", MaterialTheme.colorScheme.pinnedTemplatePurple),
    PinnedTemplate("Memo", MaterialTheme.colorScheme.pinnedTemplateBrown),
    PinnedTemplate("Brainstorm", MaterialTheme.colorScheme.pinnedTemplatePink)
)

private fun getQuickTags(): List<String> = listOf(
    "work", "personal", "idea", "meeting", "todo", "inspiration", "research", "review"
)