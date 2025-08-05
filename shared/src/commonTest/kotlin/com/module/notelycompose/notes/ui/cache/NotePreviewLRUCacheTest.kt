package com.module.notelycompose.notes.ui.cache

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for the memory-optimized LRU cache system.
 * These tests validate the memory management fixes for the Apple QA review issue.
 */
class NotePreviewLRUCacheTest {

    @Test
    fun testBasicCacheOperations() = runTest {
        val cache = NotePreviewLRUCache<String, String>(maxSize = 3)
        
        // Test basic put and get
        cache.put("key1", "value1")
        assertEquals("value1", cache.get("key1"))
        
        // Test getOrPut
        val computed = cache.getOrPut("key2") { "computed_value2" }
        assertEquals("computed_value2", computed)
        assertEquals("computed_value2", cache.get("key2"))
    }

    @Test
    fun testLRUEvictionPolicy() = runTest {
        val cache = NotePreviewLRUCache<String, String>(maxSize = 3)
        
        // Fill cache to capacity
        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.put("key3", "value3")
        
        // Access key1 to make it most recently used
        cache.get("key1")
        
        // Add new item, should evict key2 (least recently used)
        cache.put("key4", "value4")
        
        assertNotNull(cache.get("key1")) // Should still exist
        assertNull(cache.get("key2"))    // Should be evicted
        assertNotNull(cache.get("key3")) // Should still exist
        assertNotNull(cache.get("key4")) // Should exist
    }

    @Test
    fun testMemoryPressureMonitoring() = runTest {
        var memoryPressureTriggered = false
        val cache = NotePreviewLRUCache<String, String>(
            maxSize = 5,
            onMemoryPressure = { memoryPressureTriggered = true }
        )
        
        // Fill cache beyond memory pressure threshold (85% = 4.25, so 5 items)
        repeat(5) { i ->
            cache.put("key$i", "value$i")
        }
        
        assertTrue(memoryPressureTriggered, "Memory pressure callback should be triggered")
        assertTrue(cache.memoryUsage.value.isMemoryPressure, "Memory pressure state should be true")
    }

    @Test
    fun testEmergencyCleanup() = runTest {
        val cache = NotePreviewLRUCache<String, String>(maxSize = 5)
        
        // Fill cache to trigger emergency cleanup (95% = 4.75, so 5 items triggers it)
        repeat(6) { i ->
            cache.put("key$i", "value$i")
        }
        
        val stats = cache.getStats()
        assertTrue(stats.size <= 5, "Cache size should be constrained after emergency cleanup")
        assertTrue(stats.totalEvictions > 0, "Should have performed evictions")
    }

    @Test
    fun testStaleEntryEviction() = runTest {
        val cache = NotePreviewLRUCache<String, String>(maxSize = 10)
        
        // Add some entries
        cache.put("key1", "value1")
        cache.put("key2", "value2")
        
        // Simulate old entries by evicting with very short max age
        cache.evictStale(1L) // 1ms max age, should evict everything
        
        assertNull(cache.get("key1"), "Stale entry should be evicted")
        assertNull(cache.get("key2"), "Stale entry should be evicted")
    }

    @Test
    fun testCacheStatistics() = runTest {
        val cache = NotePreviewLRUCache<String, String>(maxSize = 5)
        
        // Add items and trigger some evictions
        repeat(7) { i ->
            cache.put("key$i", "value$i")
        }
        
        val stats = cache.getStats()
        assertTrue(stats.size <= 5, "Cache size should not exceed maximum")
        assertTrue(stats.usagePercentage <= 100f, "Usage percentage should be valid")
        assertTrue(stats.totalEvictions >= 2, "Should have evicted at least 2 items")
        assertTrue(stats.averageAge >= 0, "Average age should be non-negative")
    }

    @Test
    fun testCacheKeyEfficiency() = runTest {
        // Test that cache keys are lightweight and don't cause memory bloat
        val key1 = NotePreviewCacheKey.fromNoteData(
            id = 1L,
            title = "Very long title that could potentially cause memory issues if stored in full",
            content = "Very long content that spans multiple paragraphs and could cause memory issues",
            isVoice = true,
            isStarred = false
        )
        
        val key2 = NotePreviewCacheKey.fromNoteData(
            id = 2L,
            title = "Very long title that could potentially cause memory issues if stored in full",
            content = "Very long content that spans multiple paragraphs and could cause memory issues",
            isVoice = true,
            isStarred = false
        )
        
        // Keys should be different for different IDs
        assertTrue(key1 != key2, "Keys should be different for different note IDs")
        
        // Content hash should be the same for same content
        assertEquals(key1.contentHash, key2.contentHash, "Content hash should be same for same content")
    }

    @Test
    fun testLargeNoteCollectionPerformance() = runTest {
        val cache = NotePreviewLRUCache<NotePreviewCacheKey, String>(maxSize = 150)
        
        // Simulate large note collection (500 notes)
        val noteCount = 500
        val keys = mutableListOf<NotePreviewCacheKey>()
        
        // Generate cache keys for large collection
        repeat(noteCount) { i ->
            val key = NotePreviewCacheKey.fromNoteData(
                id = i.toLong(),
                title = "Note $i",
                content = "Content for note $i with some text to make it realistic",
                isVoice = i % 3 == 0, // Every 3rd note is voice
                isStarred = i % 5 == 0 // Every 5th note is starred
            )
            keys.add(key)
            
            cache.put(key, "processed_content_$i")
        }
        
        val stats = cache.getStats()
        
        // Verify cache constraints are maintained
        assertTrue(stats.size <= 150, "Cache should not exceed max size with large collection")
        assertTrue(stats.totalEvictions > 0, "Should have performed evictions with large collection")
        assertTrue(stats.usagePercentage <= 100f, "Usage percentage should be valid")
        
        // Verify most recent entries are still cached
        val lastKey = keys.last()
        assertNotNull(cache.get(lastKey), "Most recent entry should still be cached")
        
        // Verify old entries have been evicted
        val firstKey = keys.first()
        assertNull(cache.get(firstKey), "Oldest entries should be evicted")
    }
}

/**
 * Tests for the global memory monitor and fallback strategies
 */
class MemoryPressureMonitorTest {

    @Test
    fun testMemoryStateCalculation() = runTest {
        // This test validates the memory state calculation logic
        val aggregatedUsage = NotePreviewCaches.AggregatedMemoryUsage(
            totalEntries = 200,
            totalEvictions = 50,
            colorCacheUsage = 80f,
            dateCacheUsage = 70f,
            contentCacheUsage = 90f
        )
        
        assertEquals(80f, aggregatedUsage.averageUsage, "Average usage should be calculated correctly")
        assertTrue(aggregatedUsage.isMemoryPressure, "Should detect memory pressure at 80%")
    }

    @Test
    fun testMemoryFallbackConfiguration() = runTest {
        // Test that fallback configurations are correctly set based on memory pressure
        val fullConfig = MemoryFallbackConfig(mode = MemoryOptimizationMode.FULL_FEATURES)
        val reducedConfig = MemoryFallbackConfig(mode = MemoryOptimizationMode.REDUCED_CACHING)
        val minimalConfig = MemoryFallbackConfig(mode = MemoryOptimizationMode.MINIMAL_CACHING)
        val noCacheConfig = MemoryFallbackConfig(mode = MemoryOptimizationMode.NO_CACHING)
        
        // Verify configurations scale appropriately
        assertTrue(fullConfig.maxContentPreviewLength > reducedConfig.maxContentPreviewLength)
        assertTrue(reducedConfig.maxContentPreviewLength > minimalConfig.maxContentPreviewLength)
        assertTrue(minimalConfig.maxContentPreviewLength > noCacheConfig.maxContentPreviewLength)
        
        // Verify caching is disabled appropriately
        assertTrue(fullConfig.enableColorCaching)
        assertTrue(reducedConfig.enableColorCaching)
        assertTrue(minimalConfig.enableColorCaching)
        assertTrue(!noCacheConfig.enableColorCaching)
    }
}

/**
 * Integration tests for the complete memory optimization system
 */
class MemoryOptimizationIntegrationTest {

    @Test
    fun testEndToEndMemoryOptimization() = runTest {
        // Test the complete flow from cache usage to memory pressure detection
        // and fallback strategy activation
        
        // 1. Fill caches beyond normal capacity
        repeat(200) { i ->
            val key = NotePreviewCacheKey.fromNoteData(
                id = i.toLong(),
                title = "Test Note $i",
                content = "Content for note $i with sufficient length to test memory usage",
                isVoice = i % 2 == 0,
                isStarred = i % 3 == 0
            )
            
            NotePreviewCaches.colorSchemeCache.put(key, CachedNoteColorScheme(
                container = androidx.compose.ui.graphics.Color.White,
                onContainer = androidx.compose.ui.graphics.Color.Black,
                accent = androidx.compose.ui.graphics.Color.Blue,
                outline = androidx.compose.ui.graphics.Color.Gray,
                key = key
            ))
            
            NotePreviewCaches.contentPreviewCache.put(key, CachedFormattedText(
                formattedText = "Formatted content $i",
                key = key
            ))
        }
        
        // 2. Check that memory pressure is detected
        val aggregatedUsage = NotePreviewCaches.getAggregatedMemoryUsage()
        assertTrue(aggregatedUsage.totalEntries > 0, "Should have cached entries")
        
        // 3. Trigger maintenance and verify cleanup
        NotePreviewCaches.performMaintenance()
        
        val afterMaintenance = NotePreviewCaches.getAggregatedMemoryUsage()
        assertTrue(afterMaintenance.totalEvictions > 0, "Should have performed evictions during maintenance")
        
        // 4. Test emergency cleanup
        NotePreviewCaches.clearAllCaches()
        
        val afterClear = NotePreviewCaches.getAggregatedMemoryUsage()
        assertEquals(0, afterClear.totalEntries, "All caches should be cleared")
    }

    @Test
    fun testMemoryOptimizationWithRealWorldData() = runTest {
        // Simulate real-world usage patterns with mixed note types and sizes
        val noteTypes = listOf(
            Triple("Short text note", "Brief content", false),
            Triple("Long article note", "This is a very long article with multiple paragraphs and extensive content that would normally consume significant memory if not properly optimized. ".repeat(10), false),
            Triple("Voice note", "[Audio recording - transcription unavailable]", true),
            Triple("Mixed content note", "This note has both text and formatting with various elements that need to be processed efficiently", false)
        )
        
        // Test with realistic note collection
        repeat(300) { i ->
            val (title, content, isVoice) = noteTypes[i % noteTypes.size]
            val key = NotePreviewCacheKey.fromNoteData(
                id = i.toLong(),
                title = "$title $i",
                content = "$content (Note $i)",
                isVoice = isVoice,
                isStarred = i % 10 == 0 // 10% starred
            )
            
            // Cache both color schemes and content
            NotePreviewCaches.colorSchemeCache.put(key, CachedNoteColorScheme(
                container = androidx.compose.ui.graphics.Color.White,
                onContainer = androidx.compose.ui.graphics.Color.Black,
                accent = if (isVoice) androidx.compose.ui.graphics.Color.Blue else androidx.compose.ui.graphics.Color.Green,
                outline = androidx.compose.ui.graphics.Color.Gray,
                key = key
            ))
            
            NotePreviewCaches.contentPreviewCache.put(key, CachedFormattedText(
                formattedText = "Processed: ${content.take(100)}",
                key = key
            ))
        }
        
        // Verify system handles large collections gracefully
        val usage = NotePreviewCaches.getAggregatedMemoryUsage()
        assertTrue(usage.totalEntries > 0, "Should have cached real-world data")
        
        // Verify caches respect size limits
        val colorStats = NotePreviewCaches.colorSchemeCache.getStats()
        val contentStats = NotePreviewCaches.contentPreviewCache.getStats()
        
        assertTrue(colorStats.size <= 100, "Color cache should respect size limits")
        assertTrue(contentStats.size <= 150, "Content cache should respect size limits")
    }

    @Test
    fun testMemoryLeakPrevention() = runTest {
        // Test that caches don't grow unbounded and cause memory leaks
        val initialUsage = NotePreviewCaches.getAggregatedMemoryUsage()
        
        // Simulate heavy usage
        repeat(1000) { i ->
            val key = NotePreviewCacheKey.fromNoteData(
                id = i.toLong(),
                title = "Memory test note $i",
                content = "Content that could potentially cause memory leaks if not properly managed",
                isVoice = false,
                isStarred = false
            )
            
            // Add to cache
            NotePreviewCaches.contentPreviewCache.put(key, CachedFormattedText(
                formattedText = "Processed content $i",
                key = key
            ))
        }
        
        val afterHeavyUsage = NotePreviewCaches.getAggregatedMemoryUsage()
        
        // Verify caches don't grow unbounded
        assertTrue(afterHeavyUsage.totalEntries < 500, "Caches should not grow unbounded")
        assertTrue(afterHeavyUsage.totalEvictions > 0, "Should have performed evictions to prevent leaks")
        
        // Test cleanup
        NotePreviewCaches.performMaintenance()
        
        val afterCleanup = NotePreviewCaches.getAggregatedMemoryUsage()
        assertTrue(afterCleanup.totalEntries <= afterHeavyUsage.totalEntries, "Maintenance should not increase cache size")
    }
}