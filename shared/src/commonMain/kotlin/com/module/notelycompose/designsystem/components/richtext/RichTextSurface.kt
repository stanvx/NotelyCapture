package com.module.notelycompose.designsystem.components.richtext

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Foundation surface component for rich text toolbars with consistent Material 3 styling.
 * 
 * Features:
 * - Multiple surface styles (standard, glassmorphism, floating)
 * - Consistent elevation and shadow handling
 * - Material 3 color scheme integration
 * - Flexible shape and padding options
 * - Premium visual effects for Apple-quality experience
 * 
 * @param modifier Modifier for customization
 * @param shape Surface shape (defaults to rounded corners)
 * @param style Surface visual style
 * @param contentPadding Internal padding for content
 * @param content Surface content
 */
@Composable
fun RichTextSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    style: RichTextSurfaceStyle = RichTextSurfaceStyle.Standard(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    when (style) {
        is RichTextSurfaceStyle.Standard -> {
            StandardRichTextSurface(
                modifier = modifier,
                shape = shape,
                elevation = style.elevation,
                contentPadding = contentPadding,
                content = content
            )
        }
        
        is RichTextSurfaceStyle.Glassmorphism -> {
            GlassmorphismRichTextSurface(
                modifier = modifier,
                shape = shape,
                backgroundAlpha = style.backgroundAlpha,
                blurRadius = style.blurRadius,
                contentPadding = contentPadding,
                content = content
            )
        }
        
        is RichTextSurfaceStyle.Floating -> {
            FloatingRichTextSurface(
                modifier = modifier,
                shape = shape,
                elevation = style.elevation,
                shadowColor = style.shadowColor,
                contentPadding = contentPadding,
                content = content
            )
        }
    }
}

/**
 * Standard Material 3 surface with elevation and subtle transparency.
 */
@Composable
private fun StandardRichTextSurface(
    modifier: Modifier,
    shape: Shape,
    elevation: Dp,
    contentPadding: PaddingValues,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        ),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
        tonalElevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
                )
                .padding(contentPadding)
        ) {
            content()
        }
    }
}

/**
 * Glassmorphism surface with blur effect and transparency.
 */
@Composable
private fun GlassmorphismRichTextSurface(
    modifier: Modifier,
    shape: Shape,
    backgroundAlpha: Float,
    blurRadius: Dp,
    contentPadding: PaddingValues,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.shadow(
            elevation = 12.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.1f)
        ),
        shape = shape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = backgroundAlpha + 0.1f),
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = backgroundAlpha - 0.05f),
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = backgroundAlpha)
                    )
                )
            )
        ) {
            // Glassmorphism highlight overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    .align(Alignment.TopCenter)
            )
            
            Box(modifier = Modifier.padding(contentPadding)) {
                content()
            }
        }
    }
}

/**
 * Floating surface with enhanced shadow and premium elevation.
 */
@Composable
private fun FloatingRichTextSurface(
    modifier: Modifier,
    shape: Shape,
    elevation: Dp,
    shadowColor: Color,
    contentPadding: PaddingValues,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = shadowColor.copy(alpha = 0.08f),
            spotColor = shadowColor.copy(alpha = 0.15f)
        ),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            // Drag handle for floating toolbars
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(2.dp)
                    )
                    .align(Alignment.CenterHorizontally)
            )
            
            content()
        }
    }
}

/**
 * Surface styles for different rich text toolbar contexts.
 */
sealed class RichTextSurfaceStyle {
    /**
     * Standard surface with Material 3 elevation.
     */
    data class Standard(val elevation: Dp = 8.dp) : RichTextSurfaceStyle()
    
    /**
     * Glassmorphism surface with blur and transparency.
     */
    data class Glassmorphism(
        val backgroundAlpha: Float = 0.7f,
        val blurRadius: Dp = 20.dp
    ) : RichTextSurfaceStyle()
    
    /**
     * Floating surface with enhanced shadow.
     */
    data class Floating(
        val elevation: Dp = 16.dp,
        val shadowColor: Color = Color.Black
    ) : RichTextSurfaceStyle()
}

/**
 * Pre-configured surfaces for common use cases.
 */
object RichTextSurfaces {
    /**
     * Bottom-aligned toolbar surface for keyboard-aware positioning.
     */
    @Composable
    fun BottomToolbar(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        RichTextSurface(
            modifier = modifier,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            style = RichTextSurfaceStyle.Standard(elevation = 8.dp),
            contentPadding = PaddingValues(16.dp),
            content = content
        )
    }
    
    /**
     * Floating toolbar surface for overlay positioning.
     */
    @Composable
    fun FloatingToolbar(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        RichTextSurface(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            style = RichTextSurfaceStyle.Floating(elevation = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
            content = content
        )
    }
    
    /**
     * Glassmorphism toolbar surface for premium overlay experience.
     */
    @Composable
    fun GlassToolbar(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        RichTextSurface(
            modifier = modifier,
            shape = RoundedCornerShape(24.dp),
            style = RichTextSurfaceStyle.Glassmorphism(backgroundAlpha = 0.8f),
            contentPadding = PaddingValues(20.dp),
            content = content
        )
    }
}