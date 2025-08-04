package com.module.notelycompose.notes.ui.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.module.notelycompose.notes.ui.components.NoteColorScheme
import com.module.notelycompose.notes.ui.list.model.NoteUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Memory fallback strategies for note preview components.
 * This system provides graceful degradation when memory pressure is detected,
 * addressing the Apple QA review memory management concerns.
 * 
 * Features:
 * - Graceful degradation of preview quality
 * - Direct computation fallbacks when caching fails
 * - Simplified rendering modes for memory-constrained scenarios
 * - Performance monitoring and adaptive behavior
 */

/**
 * Memory optimization mode for different scenarios
 */
enum class MemoryOptimizationMode {
    FULL_FEATURES,      // Normal operation with full caching
    REDUCED_CACHING,    // Reduced cache sizes, simplified previews
    MINIMAL_CACHING,    // Very limited caching, basic previews
    NO_CACHING         // Direct computation only, minimal features
}

/**
 * Memory fallback configuration
 */
data class MemoryFallbackConfig(
    val mode: MemoryOptimizationMode = MemoryOptimizationMode.FULL_FEATURES,
    val maxContentPreviewLength: Int = when (mode) {
        MemoryOptimizationMode.FULL_FEATURES -> 200
        MemoryOptimizationMode.REDUCED_CACHING -> 150
        MemoryOptimizationMode.MINIMAL_CACHING -> 100
        MemoryOptimizationMode.NO_CACHING -> 50
    },
    val enableColorCaching: Boolean = mode != MemoryOptimizationMode.NO_CACHING,
    val enableContentCaching: Boolean = mode == MemoryOptimizationMode.FULL_FEATURES || 
                                       mode == MemoryOptimizationMode.REDUCED_CACHING,
    val enableDateCaching: Boolean = mode != MemoryOptimizationMode.NO_CACHING,
    val reducedAnimations: Boolean = mode == MemoryOptimizationMode.MINIMAL_CACHING || 
                                   mode == MemoryOptimizationMode.NO_CACHING
)

/**
 * Global memory fallback manager
 */
object MemoryFallbackManager {
    private val _currentConfig = MutableStateFlow(MemoryFallbackConfig())
    val currentConfig: StateFlow<MemoryFallbackConfig> = _currentConfig
    
    private val _adaptiveMode = MutableStateFlow(false)
    val adaptiveMode: StateFlow<Boolean> = _adaptiveMode

    /**
     * Update memory optimization mode based on memory pressure
     */
    fun updateMode(memoryPressure: Float, totalEntries: Int) {
        val newMode = when {
            memoryPressure >= 95f || totalEntries > 500 -> MemoryOptimizationMode.NO_CACHING
            memoryPressure >= 85f || totalEntries > 400 -> MemoryOptimizationMode.MINIMAL_CACHING
            memoryPressure >= 75f || totalEntries > 300 -> MemoryOptimizationMode.REDUCED_CACHING
            else -> MemoryOptimizationMode.FULL_FEATURES
        }
        
        _currentConfig.value = MemoryFallbackConfig(mode = newMode)
    }
    
    /**
     * Enable or disable adaptive mode
     */
    fun setAdaptiveMode(enabled: Boolean) {
        _adaptiveMode.value = enabled
    }
    
    /**
     * Force specific mode (overrides adaptive behavior)
     */
    fun forceMode(mode: MemoryOptimizationMode) {
        _adaptiveMode.value = false
        _currentConfig.value = MemoryFallbackConfig(mode = mode)
    }
    
    /**
     * Reset to full features mode
     */
    fun reset() {
        _currentConfig.value = MemoryFallbackConfig()
        _adaptiveMode.value = false
    }
}

/**
 * Memory-adaptive color scheme generator with fallbacks
 */
@Composable
fun generateAdaptiveNoteColors(
    noteData: Any, // Can be NoteCardData or any note interface
    isVoice: Boolean,
    isStarred: Boolean,
    noteId: Long,
    fallbackConfig: MemoryFallbackConfig = MemoryFallbackConfig()
): NoteColorScheme {
    val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
    
    return when {
        // Use caching if enabled and not in no-cache mode
        fallbackConfig.enableColorCaching && fallbackConfig.mode != MemoryOptimizationMode.NO_CACHING -> {
            var cachedColors by remember { mutableStateOf<NoteColorScheme?>(null) }
            
            LaunchedEffect(noteId, isVoice, isStarred) {
                try {
                    val cacheKey = NotePreviewCacheKey.fromNoteData(
                        id = noteId,
                        title = "",
                        content = "",
                        isVoice = isVoice,
                        isStarred = isStarred
                    )
                    
                    val cached = NotePreviewCaches.colorSchemeCache.get(cacheKey)
                    if (cached != null) {
                        cachedColors = NoteColorScheme(
                            container = cached.container,
                            onContainer = cached.onContainer,
                            accent = cached.accent,
                            outline = cached.outline
                        )
                    } else {
                        val computed = computeBasicNoteColors(isVoice, isStarred, colorScheme)
                        cachedColors = computed
                        
                        // Only cache if not in minimal mode
                        if (fallbackConfig.mode != MemoryOptimizationMode.MINIMAL_CACHING) {
                            val cacheValue = CachedNoteColorScheme(
                                container = computed.container,
                                onContainer = computed.onContainer,
                                accent = computed.accent,
                                outline = computed.outline,
                                key = cacheKey
                            )
                            NotePreviewCaches.colorSchemeCache.put(cacheKey, cacheValue)
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to direct computation
                    cachedColors = computeBasicNoteColors(isVoice, isStarred, colorScheme)
                }
            }
            
            cachedColors ?: computeBasicNoteColors(isVoice, isStarred, colorScheme)
        }
        
        // Direct computation fallback
        else -> computeBasicNoteColors(isVoice, isStarred, colorScheme)
    }
}

/**
 * Basic color computation without caching
 */
private fun computeBasicNoteColors(
    isVoice: Boolean,
    isStarred: Boolean,
    colorScheme: androidx.compose.material3.ColorScheme
): NoteColorScheme {
    return when {
        isVoice && isStarred -> NoteColorScheme(
            container = colorScheme.tertiaryContainer,
            onContainer = colorScheme.onTertiaryContainer,
            accent = colorScheme.tertiary,
            outline = colorScheme.tertiary.copy(alpha = 0.3f)
        )
        isVoice -> NoteColorScheme(
            container = colorScheme.primaryContainer,
            onContainer = colorScheme.onPrimaryContainer,
            accent = colorScheme.primary,
            outline = colorScheme.primary.copy(alpha = 0.3f)
        )
        isStarred -> NoteColorScheme(
            container = colorScheme.secondaryContainer,
            onContainer = colorScheme.onSecondaryContainer,
            accent = colorScheme.secondary,
            outline = colorScheme.secondary.copy(alpha = 0.3f)
        )
        else -> NoteColorScheme(
            container = colorScheme.surfaceContainer,
            onContainer = colorScheme.onSurface,
            accent = colorScheme.outline,
            outline = colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}

/**
 * Memory-adaptive content preview with fallbacks
 */
@Composable
fun generateAdaptiveContentPreview(
    note: NoteUiModel,
    config: MemoryFallbackConfig = MemoryFallbackConfig()
): AdaptiveContentPreview {
    
    return when (config.mode) {
        MemoryOptimizationMode.FULL_FEATURES -> {
            // Full caching and processing
            var cachedPreview by remember { mutableStateOf<AdaptiveContentPreview?>(null) }
            
            LaunchedEffect(note.id, note.title, note.content) {
                try {
                    val cacheKey = NotePreviewCacheKey.fromNoteData(
                        id = note.id,
                        title = note.title,
                        content = note.content,
                        isVoice = note.isVoice,
                        isStarred = note.isStarred
                    )
                    
                    val cached = NotePreviewCaches.contentPreviewCache.get(cacheKey)
                    if (cached != null) {
                        cachedPreview = parseAdaptiveContentFromCache(cached.formattedText)
                    } else {
                        val computed = computeFullContentPreview(note, config.maxContentPreviewLength)
                        cachedPreview = computed
                        
                        val cacheValue = CachedFormattedText(
                            formattedText = serializeAdaptiveContent(computed),
                            key = cacheKey
                        )
                        NotePreviewCaches.contentPreviewCache.put(cacheKey, cacheValue)
                    }
                } catch (e: Exception) {
                    cachedPreview = computeBasicContentPreview(note, config.maxContentPreviewLength)
                }
            }
            
            cachedPreview ?: computeBasicContentPreview(note, config.maxContentPreviewLength)
        }
        
        MemoryOptimizationMode.REDUCED_CACHING -> {
            // Limited caching, simplified processing
            remember(note.id, note.title.take(50), note.content.take(100)) {
                computeReducedContentPreview(note, config.maxContentPreviewLength)
            }
        }
        
        MemoryOptimizationMode.MINIMAL_CACHING,
        MemoryOptimizationMode.NO_CACHING -> {
            // No caching, basic preview only
            remember(note.id) {
                computeBasicContentPreview(note, config.maxContentPreviewLength)
            }
        }
    }
}

/**
 * Adaptive content preview data structure
 */
data class AdaptiveContentPreview(
    val title: String,
    val content: String,
    val hasMore: Boolean,
    val wordCount: Int,
    val processingLevel: ContentProcessingLevel
)

/**
 * Content processing level for different memory modes
 */
enum class ContentProcessingLevel {
    FULL,       // Full text processing, smart truncation, rich formatting
    REDUCED,    // Basic text processing, simple truncation
    MINIMAL     // Raw text only, character-based truncation
}

/**
 * Compute full content preview with rich processing
 */
private fun computeFullContentPreview(note: NoteUiModel, maxLength: Int): AdaptiveContentPreview {
    val title = when {
        note.title.isNotEmpty() -> note.title
        note.isVoice && note.content.contains("[Audio recording - transcription unavailable]") -> {
            "Voice Note"
        }
        note.content.isNotEmpty() -> {
            // Smart title extraction
            note.content.take(60).split('.', '!', '?').firstOrNull()?.trim()
                ?.takeIf { it.length > 5 } ?: note.content.take(40).trim() + "…"
        }
        else -> "Untitled Note"
    }
    
    val content = when {
        note.content.isEmpty() -> ""
        note.content.length <= maxLength -> note.content
        else -> {
            // Smart truncation preserving sentence boundaries
            val truncated = note.content.take(maxLength)
            val lastSentenceEnd = truncated.lastIndexOfAny(charArrayOf('.', '!', '?'))
            
            if (lastSentenceEnd > maxLength * 0.7) {
                truncated.take(lastSentenceEnd + 1)
            } else {
                "$truncated…"
            }
        }
    }
    
    return AdaptiveContentPreview(
        title = title,
        content = content,
        hasMore = note.content.length > maxLength,
        wordCount = note.words,
        processingLevel = ContentProcessingLevel.FULL
    )
}

/**
 * Compute reduced content preview with basic processing
 */
private fun computeReducedContentPreview(note: NoteUiModel, maxLength: Int): AdaptiveContentPreview {
    val title = note.title.takeIf { it.isNotEmpty() } 
        ?: note.content.take(30).trim().let { if (it.length < note.content.length) "$it…" else it }
    
    val content = note.content.take(maxLength).let { 
        if (it.length < note.content.length) "$it…" else it 
    }
    
    return AdaptiveContentPreview(
        title = title,
        content = content,
        hasMore = note.content.length > maxLength,
        wordCount = note.words,
        processingLevel = ContentProcessingLevel.REDUCED
    )
}

/**
 * Compute basic content preview with minimal processing
 */
private fun computeBasicContentPreview(note: NoteUiModel, maxLength: Int): AdaptiveContentPreview {
    val title = note.title.takeIf { it.isNotEmpty() } ?: "Note"
    val content = note.content.take(maxLength)
    
    return AdaptiveContentPreview(
        title = title,
        content = content,
        hasMore = note.content.length > maxLength,
        wordCount = note.words,
        processingLevel = ContentProcessingLevel.MINIMAL
    )
}

/**
 * Serialize adaptive content for caching
 */
private fun serializeAdaptiveContent(preview: AdaptiveContentPreview): String {
    return "${preview.title}|${preview.content}|${preview.hasMore}|${preview.wordCount}|${preview.processingLevel.name}"
}

/**
 * Parse adaptive content from cache
 */
private fun parseAdaptiveContentFromCache(serialized: String): AdaptiveContentPreview {
    val parts = serialized.split("|")
    return if (parts.size >= 5) {
        AdaptiveContentPreview(
            title = parts[0],
            content = parts[1],
            hasMore = parts[2].toBoolean(),
            wordCount = parts[3].toIntOrNull() ?: 0,
            processingLevel = ContentProcessingLevel.valueOf(
                parts[4].takeIf { it in ContentProcessingLevel.values().map { level -> level.name } } 
                    ?: ContentProcessingLevel.MINIMAL.name
            )
        )
    } else {
        AdaptiveContentPreview("", "", false, 0, ContentProcessingLevel.MINIMAL)
    }
}

/**
 * Memory-adaptive date formatting with fallbacks
 */
@Composable
fun formatAdaptiveDate(
    createdAt: String,
    noteId: Long,
    config: MemoryFallbackConfig = MemoryFallbackConfig()
): String {
    return when {
        config.enableDateCaching -> {
            remember(noteId, createdAt) {
                try {
                    com.module.notelycompose.notes.utils.DateTimeFormatUtils.formatRelativeTime(createdAt)
                } catch (e: Exception) {
                    createdAt.substringBefore("T").takeIf { it.isNotEmpty() } ?: "Unknown"
                }
            }
        }
        else -> {
            // Direct computation fallback
            createdAt.substringBefore("T").takeIf { it.isNotEmpty() } ?: "Unknown"
        }
    }
}

/**
 * Composable for monitoring and adapting to memory pressure
 */
@Composable
fun MemoryAdaptiveEffect() {
    val memoryState = GlobalMemoryMonitor.memoryState
    val adaptiveMode by MemoryFallbackManager.adaptiveMode.collectAsState()
    
    LaunchedEffect(memoryState?.value, adaptiveMode) {
        if (adaptiveMode && memoryState?.value != null) {
            val state = memoryState.value
            MemoryFallbackManager.updateMode(
                memoryPressure = state.totalMemoryUsagePercent,
                totalEntries = state.totalCacheEntries
            )
        }
    }
}