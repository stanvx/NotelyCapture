package com.module.notelycompose.openai.domain.analytics

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Analytics tracker for OpenAI API usage, costs, and performance metrics.
 * 
 * Features:
 * - Request/response tracking
 * - Cost estimation tracking
 * - Success/failure rate monitoring
 * - Performance metrics (response times)
 * - Cache hit rate tracking
 */
class OpenAIAnalytics {
    private val mutex = Mutex()
    private val metrics = mutableMapOf<String, MetricData>()
    
    /**
     * Records a successful API request.
     */
    suspend fun recordSuccessfulRequest(
        operation: String,
        responseTimeMs: Long,
        estimatedCostCents: Int? = null,
        fromCache: Boolean = false
    ) {
        mutex.withLock {
            val key = operation
            val metric = metrics.getOrPut(key) { MetricData() }
            
            metric.totalRequests++
            metric.successfulRequests++
            metric.totalResponseTimeMs += responseTimeMs
            
            if (fromCache) {
                metric.cacheHits++
            }
            
            estimatedCostCents?.let { cost ->
                metric.totalEstimatedCostCents += cost
            }
            
            metric.lastRequestTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
    
    /**
     * Records a failed API request.
     */
    suspend fun recordFailedRequest(
        operation: String,
        errorCode: String,
        responseTimeMs: Long? = null
    ) {
        mutex.withLock {
            val key = operation
            val metric = metrics.getOrPut(key) { MetricData() }
            
            metric.totalRequests++
            metric.failedRequests++
            
            responseTimeMs?.let { time ->
                metric.totalResponseTimeMs += time
            }
            
            // Track error codes
            metric.errorCodes[errorCode] = metric.errorCodes.getOrDefault(errorCode, 0) + 1
            
            metric.lastRequestTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
    
    /**
     * Records a cache hit.
     */
    suspend fun recordCacheHit(operation: String) {
        mutex.withLock {
            val key = operation
            val metric = metrics.getOrPut(key) { MetricData() }
            metric.cacheHits++
        }
    }
    
    /**
     * Gets analytics summary for all operations.
     */
    suspend fun getAnalyticsSummary(): AnalyticsSummary {
        return mutex.withLock {
            val totalRequests = metrics.values.sumOf { it.totalRequests }
            val totalSuccessful = metrics.values.sumOf { it.successfulRequests }
            val totalFailed = metrics.values.sumOf { it.failedRequests }
            val totalCacheHits = metrics.values.sumOf { it.cacheHits }
            val totalCostCents = metrics.values.sumOf { it.totalEstimatedCostCents }
            val totalResponseTime = metrics.values.sumOf { it.totalResponseTimeMs }
            
            val avgResponseTime = if (totalRequests > 0) {
                totalResponseTime.toDouble() / totalRequests
            } else 0.0
            
            val successRate = if (totalRequests > 0) {
                totalSuccessful.toDouble() / totalRequests
            } else 0.0
            
            val cacheHitRate = if (totalRequests > 0) {
                totalCacheHits.toDouble() / totalRequests
            } else 0.0
            
            AnalyticsSummary(
                totalRequests = totalRequests,
                successfulRequests = totalSuccessful,
                failedRequests = totalFailed,
                successRate = successRate,
                cacheHits = totalCacheHits,
                cacheHitRate = cacheHitRate,
                totalEstimatedCostCents = totalCostCents,
                averageResponseTimeMs = avgResponseTime
            )
        }
    }
    
    /**
     * Gets analytics for a specific operation.
     */
    suspend fun getOperationAnalytics(operation: String): OperationAnalytics? {
        return mutex.withLock {
            metrics[operation]?.let { metric ->
                val avgResponseTime = if (metric.totalRequests > 0) {
                    metric.totalResponseTimeMs.toDouble() / metric.totalRequests
                } else 0.0
                
                val successRate = if (metric.totalRequests > 0) {
                    metric.successfulRequests.toDouble() / metric.totalRequests
                } else 0.0
                
                val cacheHitRate = if (metric.totalRequests > 0) {
                    metric.cacheHits.toDouble() / metric.totalRequests
                } else 0.0
                
                OperationAnalytics(
                    operation = operation,
                    totalRequests = metric.totalRequests,
                    successfulRequests = metric.successfulRequests,
                    failedRequests = metric.failedRequests,
                    successRate = successRate,
                    cacheHits = metric.cacheHits,
                    cacheHitRate = cacheHitRate,
                    totalEstimatedCostCents = metric.totalEstimatedCostCents,
                    averageResponseTimeMs = avgResponseTime,
                    errorCodes = metric.errorCodes.toMap(),
                    lastRequestTime = metric.lastRequestTime
                )
            }
        }
    }
    
    /**
     * Resets all analytics data.
     */
    suspend fun reset() {
        mutex.withLock {
            metrics.clear()
        }
    }
}

/**
 * Internal metric data storage.
 */
internal data class MetricData(
    var totalRequests: Long = 0,
    var successfulRequests: Long = 0,
    var failedRequests: Long = 0,
    var cacheHits: Long = 0,
    var totalResponseTimeMs: Long = 0,
    var totalEstimatedCostCents: Long = 0,
    var lastRequestTime: LocalDateTime? = null,
    val errorCodes: MutableMap<String, Int> = mutableMapOf()
)

/**
 * Overall analytics summary.
 */
data class AnalyticsSummary(
    val totalRequests: Long,
    val successfulRequests: Long,
    val failedRequests: Long,
    val successRate: Double,
    val cacheHits: Long,
    val cacheHitRate: Double,
    val totalEstimatedCostCents: Long,
    val averageResponseTimeMs: Double,
) {
    val estimatedCostDollars: Double
        get() = totalEstimatedCostCents / 100.0
}

/**
 * Analytics for a specific operation.
 */
data class OperationAnalytics(
    val operation: String,
    val totalRequests: Long,
    val successfulRequests: Long,
    val failedRequests: Long,
    val successRate: Double,
    val cacheHits: Long,
    val cacheHitRate: Double,
    val totalEstimatedCostCents: Long,
    val averageResponseTimeMs: Double,
    val errorCodes: Map<String, Int>,
    val lastRequestTime: LocalDateTime?
) {
    val estimatedCostDollars: Double
        get() = totalEstimatedCostCents / 100.0
}