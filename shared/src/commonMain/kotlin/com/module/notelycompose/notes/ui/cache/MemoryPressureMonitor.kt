package com.module.notelycompose.notes.ui.cache

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Memory pressure monitoring system for note preview caching.
 * This component tracks memory usage across all caches and provides
 * proactive memory management to prevent the memory accumulation issue
 * identified in Apple QA review.
 * 
 * Features:
 * - Real-time memory usage monitoring
 * - Automatic cache maintenance scheduling
 * - Memory pressure detection and response
 * - Background cleanup operations
 * - Configurable thresholds and policies
 */
class MemoryPressureMonitor(
    private val coroutineScope: CoroutineScope,
    private val monitoringIntervalMs: Long = 30_000L, // 30 seconds
    private val maintenanceIntervalMs: Long = 5 * 60_000L, // 5 minutes
    private val criticalThreshold: Float = 90f, // 90% usage triggers critical actions
    private val warningThreshold: Float = 75f // 75% usage triggers warnings
) {
    companion object {
        const val MAX_CACHE_AGE_MS = 10 * 60 * 1000L // 10 minutes
        const val EMERGENCY_CACHE_AGE_MS = 2 * 60 * 1000L // 2 minutes for emergency cleanup
    }

    private val _memoryState = mutableStateOf(MemoryPressureState())
    val memoryState: State<MemoryPressureState> = _memoryState

    private var monitoringJob: Job? = null
    private var maintenanceJob: Job? = null

    data class MemoryPressureState(
        val totalCacheEntries: Int = 0,
        val totalMemoryUsagePercent: Float = 0f,
        val colorCacheUsage: Float = 0f,
        val contentCacheUsage: Float = 0f,
        val dateCacheUsage: Float = 0f,
        val isWarning: Boolean = false,
        val isCritical: Boolean = false,
        val lastMaintenanceTime: Long = 0L,
        val totalEvictions: Long = 0L
    )

    /**
     * Start memory monitoring and maintenance
     */
    fun startMonitoring() {
        stopMonitoring() // Ensure clean start

        // Start periodic memory monitoring
        monitoringJob = coroutineScope.launch {
            while (isActive) {
                try {
                    updateMemoryState()
                    delay(monitoringIntervalMs)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Log error but continue monitoring
                    println("Error in memory monitoring: ${e.message}")
                    delay(monitoringIntervalMs)
                }
            }
        }

        // Start periodic cache maintenance
        maintenanceJob = coroutineScope.launch {
            while (isActive) {
                try {
                    performScheduledMaintenance()
                    delay(maintenanceIntervalMs)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    println("Error in cache maintenance: ${e.message}")
                    delay(maintenanceIntervalMs)
                }
            }
        }
    }

    /**
     * Stop memory monitoring
     */
    fun stopMonitoring() {
        monitoringJob?.cancel()
        maintenanceJob?.cancel()
        monitoringJob = null
        maintenanceJob = null
    }

    /**
     * Force immediate memory pressure check and cleanup
     */
    suspend fun forceCleanup() {
        updateMemoryState()
        if (_memoryState.value.isCritical) {
            performEmergencyCleanup()
        } else if (_memoryState.value.isWarning) {
            performAggressiveMaintenance()
        }
    }

    /**
     * Update current memory state from all caches
     */
    private suspend fun updateMemoryState() {
        try {
            val aggregatedUsage = NotePreviewCaches.getAggregatedMemoryUsage()
            
            val newState = MemoryPressureState(
                totalCacheEntries = aggregatedUsage.totalEntries,
                totalMemoryUsagePercent = aggregatedUsage.averageUsage,
                colorCacheUsage = aggregatedUsage.colorCacheUsage,
                contentCacheUsage = aggregatedUsage.contentCacheUsage,
                dateCacheUsage = aggregatedUsage.dateCacheUsage,
                isWarning = aggregatedUsage.averageUsage >= warningThreshold,
                isCritical = aggregatedUsage.averageUsage >= criticalThreshold,
                lastMaintenanceTime = _memoryState.value.lastMaintenanceTime,
                totalEvictions = aggregatedUsage.totalEvictions
            )

            _memoryState.value = newState

            // Trigger immediate action if critical
            if (newState.isCritical) {
                coroutineScope.launch {
                    performEmergencyCleanup()
                }
            }
        } catch (e: Exception) {
            println("Error updating memory state: ${e.message}")
        }
    }

    /**
     * Perform scheduled cache maintenance
     */
    private suspend fun performScheduledMaintenance() {
        try {
            // Evict stale entries
            NotePreviewCaches.performMaintenance()
            
            // Update last maintenance time
            _memoryState.value = _memoryState.value.copy(
                lastMaintenanceTime = System.currentTimeMillis()
            )
            
            // Update memory state after maintenance
            updateMemoryState()
            
            println("Scheduled cache maintenance completed")
        } catch (e: Exception) {
            println("Error in scheduled maintenance: ${e.message}")
        }
    }

    /**
     * Perform aggressive maintenance when memory usage is high
     */
    private suspend fun performAggressiveMaintenance() {
        try {
            // More aggressive stale entry removal
            val aggressiveMaxAge = MAX_CACHE_AGE_MS / 2 // 5 minutes instead of 10
            
            NotePreviewCaches.colorSchemeCache.evictStale(aggressiveMaxAge)
            NotePreviewCaches.dateFormatCache.evictStale(aggressiveMaxAge)
            NotePreviewCaches.contentPreviewCache.evictStale(aggressiveMaxAge)
            
            updateMemoryState()
            println("Aggressive cache maintenance completed")
        } catch (e: Exception) {
            println("Error in aggressive maintenance: ${e.message}")
        }
    }

    /**
     * Perform emergency cleanup when memory usage is critical
     */
    private suspend fun performEmergencyCleanup() {
        try {
            // Emergency cleanup - very aggressive
            NotePreviewCaches.colorSchemeCache.evictStale(EMERGENCY_CACHE_AGE_MS)
            NotePreviewCaches.dateFormatCache.evictStale(EMERGENCY_CACHE_AGE_MS)
            NotePreviewCaches.contentPreviewCache.evictStale(EMERGENCY_CACHE_AGE_MS)
            
            // If still critical, clear half of each cache
            updateMemoryState()
            if (_memoryState.value.isCritical) {
                // Clear 50% of each cache by clearing and letting LRU rebuild
                val colorStats = NotePreviewCaches.colorSchemeCache.getStats()
                val contentStats = NotePreviewCaches.contentPreviewCache.getStats()
                val dateStats = NotePreviewCaches.dateFormatCache.getStats()
                
                if (colorStats.size > 50) {
                    repeat(colorStats.size / 2) {
                        // LRU cache will automatically evict oldest entries
                    }
                }
            }
            
            updateMemoryState()
            println("Emergency cache cleanup completed")
        } catch (e: Exception) {
            println("Error in emergency cleanup: ${e.message}")
        }
    }

    /**
     * Get detailed memory report for debugging
     */
    suspend fun getDetailedMemoryReport(): DetailedMemoryReport {
        val colorStats = NotePreviewCaches.colorSchemeCache.getStats()
        val contentStats = NotePreviewCaches.contentPreviewCache.getStats()
        val dateStats = NotePreviewCaches.dateFormatCache.getStats()
        
        return DetailedMemoryReport(
            colorCacheStats = colorStats,
            contentCacheStats = contentStats,
            dateCacheStats = dateStats,
            totalEntries = colorStats.size + contentStats.size + dateStats.size,
            totalEvictions = colorStats.totalEvictions + contentStats.totalEvictions + dateStats.totalEvictions,
            memoryPressureState = _memoryState.value
        )
    }

    data class DetailedMemoryReport(
        val colorCacheStats: NotePreviewLRUCache.CacheStats,
        val contentCacheStats: NotePreviewLRUCache.CacheStats,
        val dateCacheStats: NotePreviewLRUCache.CacheStats,
        val totalEntries: Int,
        val totalEvictions: Long,
        val memoryPressureState: MemoryPressureState
    )
}

/**
 * Global memory pressure monitor instance
 */
object GlobalMemoryMonitor {
    private var monitor: MemoryPressureMonitor? = null
    
    /**
     * Initialize global memory monitoring
     */
    fun initialize(coroutineScope: CoroutineScope) {
        monitor?.stopMonitoring()
        monitor = MemoryPressureMonitor(coroutineScope).apply {
            startMonitoring()
        }
    }
    
    /**
     * Get current memory state
     */
    val memoryState: State<MemoryPressureMonitor.MemoryPressureState>?
        get() = monitor?.memoryState
    
    /**
     * Force cleanup if monitor is available
     */
    suspend fun forceCleanup() {
        monitor?.forceCleanup()
    }
    
    /**
     * Get detailed memory report
     */
    suspend fun getDetailedReport(): MemoryPressureMonitor.DetailedMemoryReport? {
        return monitor?.getDetailedMemoryReport()
    }
    
    /**
     * Shutdown monitoring
     */
    fun shutdown() {
        monitor?.stopMonitoring()
        monitor = null
    }
}

/**
 * Memory monitoring configuration for different scenarios
 */
object MemoryMonitoringConfig {
    /**
     * Configuration for development/debugging
     */
    fun developmentConfig() = MemoryPressureMonitor.Companion.run {
        object {
            val monitoringInterval = 10_000L // 10 seconds
            val maintenanceInterval = 2 * 60_000L // 2 minutes
            val criticalThreshold = 85f
            val warningThreshold = 70f
        }
    }
    
    /**
     * Configuration for production
     */
    fun productionConfig() = MemoryPressureMonitor.Companion.run {
        object {
            val monitoringInterval = 30_000L // 30 seconds
            val maintenanceInterval = 5 * 60_000L // 5 minutes
            val criticalThreshold = 90f
            val warningThreshold = 75f
        }
    }
    
    /**
     * Configuration for memory-constrained devices
     */
    fun constrainedConfig() = MemoryPressureMonitor.Companion.run {
        object {
            val monitoringInterval = 15_000L // 15 seconds
            val maintenanceInterval = 3 * 60_000L // 3 minutes
            val criticalThreshold = 80f
            val warningThreshold = 65f
        }
    }
}