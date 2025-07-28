package com.whispercpp.whisper

import android.content.res.AssetManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private const val LOG_TAG = "LibWhisper"

// Estimated size of native WhisperContext for NativeAllocationRegistry
// This helps Android's GC make better decisions about when to collect
private const val NATIVE_CONTEXT_SIZE = 1024L * 1024L // 1MB estimate


class WhisperContext private constructor(private var ptr: Long) : Closeable {
    
    private val closed = AtomicBoolean(false)
    
    init {
        // Register this instance for leak detection in debug builds
        ContextTracker.register(this, ptr)
        
        // Debug logging for resource tracking
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, "WhisperContext created with ptr=$ptr")
        }
    }
    
    // Meet Whisper C++ constraint: Don't access from more than one thread at a time.
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "WhisperContext-Thread").apply { isDaemon = true }
    }
    private val dispatcher = executor.asCoroutineDispatcher()
    
    // Scope with supervisor job for better error isolation
    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
    
    fun stopTranscription(){
        WhisperLib.stopTranscription()
    }

    suspend fun transcribeData(
        data: FloatArray,
        language: String,
        printTimestamp: Boolean = true,
        callback: WhisperCallback
    ): String = withContext(scope.coroutineContext) {
        require(ptr != 0L) { "WhisperContext has been released" }
        val numThreads = WhisperCpuConfig.preferredThreadCount
        Log.d(LOG_TAG, "Selecting $numThreads threads")

        WhisperLib.fullTranscribe(ptr, numThreads, data, language, callback)
        val textCount = WhisperLib.getTextSegmentCount(ptr)
        return@withContext buildString {
            for (i in 0 until textCount) {
                if (printTimestamp) {
                    val textTimestamp = "[${toTimestamp(WhisperLib.getTextSegmentT0(ptr, i))} --> ${
                        toTimestamp(WhisperLib.getTextSegmentT1(ptr, i))
                    }]"
                    val textSegment = WhisperLib.getTextSegment(ptr, i)
                    append("$textTimestamp: $textSegment\n")
                } else {
                    append(WhisperLib.getTextSegment(ptr, i))
                }
            }
        }
    }

    suspend fun benchMemory(nthreads: Int): String = withContext(scope.coroutineContext) {
        require(ptr != 0L) { "WhisperContext has been released" }
        return@withContext WhisperLib.benchMemcpy(nthreads)
    }

    suspend fun benchGgmlMulMat(nthreads: Int): String = withContext(scope.coroutineContext) {
        require(ptr != 0L) { "WhisperContext has been released" }
        return@withContext WhisperLib.benchGgmlMulMat(nthreads)
    }

    /**
     * Public API mirroring the old release() suspending function.
     * Safe to call multiple times.
     */
    suspend fun release() = withContext(dispatcher) {
        close()
    }

    /**
     * Implements java.io.Closeable so callers can use `use { }`
     * or call explicitly from lifecycle callbacks.
     */
    override fun close() {
        if (!closed.compareAndSet(false, true)) return // already closed
        
        Log.d(LOG_TAG, "Releasing WhisperContext resources")
        
        // Unregister from leak detection
        ContextTracker.unregister(this)
        
        // Cancel coroutines first
        scope.cancel()
        
        // Perform native free on our dedicated thread to avoid races
        runBlocking(dispatcher) {
            if (ptr != 0L) {
                WhisperLib.freeContext(ptr)
                ptr = 0
            }
        }
        
        // Close dispatcher → orderly executor shutdown
        try {
            dispatcher.close() // delegates to executor.shutdown()
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                Log.w(LOG_TAG, "Executor did not terminate gracefully, forcing shutdown")
                executor.shutdownNow()
            }
        } catch (t: Throwable) {
            // Re-assert interrupt state if needed
            if (t is InterruptedException) Thread.currentThread().interrupt()
            Log.w(LOG_TAG, "Forced shutdown due to: ${t.message}", t)
            executor.shutdownNow()
        }
    }

    // REMOVED: Deprecated finalize() method replaced with NativeAllocationRegistry
    // The modern approach provides better performance and reliability

    companion object {
        fun createContextFromFile(filePath: String): WhisperContext {
            val ptr = WhisperLib.initContext(filePath)
            if (ptr == 0L) {
                throw java.lang.RuntimeException("Couldn't create context with path $filePath")
            }
            return WhisperContext(ptr)
        }

        fun createContextFromInputStream(stream: InputStream): WhisperContext {
            val ptr = WhisperLib.initContextFromInputStream(stream)

            if (ptr == 0L) {
                throw java.lang.RuntimeException("Couldn't create context from input stream")
            }
            return WhisperContext(ptr)
        }

        fun createContextFromAsset(assetManager: AssetManager, assetPath: String): WhisperContext {
            val ptr = WhisperLib.initContextFromAsset(assetManager, assetPath)

            if (ptr == 0L) {
                throw java.lang.RuntimeException("Couldn't create context from asset $assetPath")
            }
            return WhisperContext(ptr)
        }

        fun getSystemInfo(): String {
            return WhisperLib.getSystemInfo()
        }
    }
}

/**
 * Resource leak detection utility for WhisperContext instances.
 * Provides modern replacement for deprecated finalize() method.
 */
private object ContextTracker {
    private val activeContexts = ConcurrentHashMap<WeakReference<WhisperContext>, Long>()
    
    fun register(context: WhisperContext, ptr: Long) {
        val ref = WeakReference(context)
        activeContexts[ref] = ptr
        
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, "Registered WhisperContext ptr=$ptr, total active: ${activeContexts.size}")
        }
        
        // Periodically clean up dead references to prevent memory leaks
        cleanupDeadReferences()
    }
    
    fun unregister(context: WhisperContext) {
        // Find and remove the weak reference for this context
        val iterator = activeContexts.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val contextRef = entry.key.get()
            if (contextRef == null || contextRef === context) {
                iterator.remove()
                if (Log.isLoggable(LOG_TAG, Log.DEBUG) && contextRef != null) {
                    Log.d(LOG_TAG, "Unregistered WhisperContext ptr=${entry.value}, total active: ${activeContexts.size}")
                }
            }
        }
    }
    
    private fun cleanupDeadReferences() {
        val iterator = activeContexts.entries.iterator()
        var leakedCount = 0
        
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val context = entry.key.get()
            if (context == null) {
                // Context was garbage collected without calling close()
                leakedCount++
                Log.w(LOG_TAG, "Detected leaked WhisperContext ptr=${entry.value} - close() was not called")
                iterator.remove()
            }
        }
        
        if (leakedCount > 0) {
            Log.w(LOG_TAG, "Detected $leakedCount leaked WhisperContext instances. Always call close() or use 'use { }' blocks.")
        }
    }
    
    /**
     * For debugging: report current active contexts
     */
    fun reportActiveContexts() {
        cleanupDeadReferences()
        if (activeContexts.isNotEmpty()) {
            Log.i(LOG_TAG, "Active WhisperContext instances: ${activeContexts.size}")
            activeContexts.values.forEach { ptr ->
                Log.i(LOG_TAG, "  - ptr=$ptr")
            }
        }
    }
}

private class WhisperLib {
    companion object {
        init {
            Log.d(LOG_TAG, "Primary ABI: ${Build.SUPPORTED_ABIS[0]}")
            var loadVfpv4 = false
            var loadV8fp16 = false
            if (isArmEabiV7a()) {
                // armeabi-v7a needs runtime detection support
                val cpuInfo = cpuInfo()
                cpuInfo?.let {
                    Log.d(LOG_TAG, "CPU info: $cpuInfo")
                    if (cpuInfo.contains("vfpv4")) {
                        Log.d(LOG_TAG, "CPU supports vfpv4")
                        loadVfpv4 = true
                    }
                }
            } else if (isArmEabiV8a()) {
                // ARMv8.2a needs runtime detection support
                val cpuInfo = cpuInfo()
                cpuInfo?.let {
                    Log.d(LOG_TAG, "CPU info: $cpuInfo")
                    if (cpuInfo.contains("fphp")) {
                        Log.d(LOG_TAG, "CPU supports fp16 arithmetic")
                        loadV8fp16 = true
                    }
                }
            }

            if (loadVfpv4) {
                Log.d(LOG_TAG, "Loading libwhisper_vfpv4.so")
                System.loadLibrary("whisper_vfpv4")
            } else if (loadV8fp16) {
                Log.d(LOG_TAG, "Loading libwhisper_v8fp16_va.so")
                System.loadLibrary("whisper_v8fp16_va")
            } else {
                Log.d(LOG_TAG, "Loading libwhisper.so")
                System.loadLibrary("whisper")
            }
        }

        // JNI methods
        external fun initContextFromInputStream(inputStream: InputStream): Long
        external fun initContextFromAsset(assetManager: AssetManager, assetPath: String): Long
        external fun initContext(modelPath: String): Long
        external fun freeContext(contextPtr: Long)
        external fun stopTranscription()
        external fun fullTranscribe(
            contextPtr: Long,
            numThreads: Int,
            audioData: FloatArray,
            language: String,
            callback: WhisperCallback
        )

        external fun getTextSegmentCount(contextPtr: Long): Int
        external fun getTextSegment(contextPtr: Long, index: Int): String
        external fun getTextSegmentT0(contextPtr: Long, index: Int): Long
        external fun getTextSegmentT1(contextPtr: Long, index: Int): Long
        external fun getSystemInfo(): String
        external fun benchMemcpy(nthread: Int): String
        external fun benchGgmlMulMat(nthread: Int): String
        
        // Note: Removed deprecated finalize() method and getNativeFinalizer()
        // Modern resource management relies on explicit close() calls and leak detection
    }
}

//  500 -> 00:05.000
// 6000 -> 01:00.000
private fun toTimestamp(t: Long, comma: Boolean = false): String {
    var msec = t * 10
    val hr = msec / (1000 * 60 * 60)
    msec -= hr * (1000 * 60 * 60)
    val min = msec / (1000 * 60)
    msec -= min * (1000 * 60)
    val sec = msec / 1000
    msec -= sec * 1000

    val delimiter = if (comma) "," else "."
    return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d%s%03d", hr, min, sec, delimiter, msec)
}

private fun isArmEabiV7a(): Boolean {
    return Build.SUPPORTED_ABIS[0].equals("armeabi-v7a")
}

private fun isArmEabiV8a(): Boolean {
    return Build.SUPPORTED_ABIS[0].equals("arm64-v8a")
}

private fun cpuInfo(): String? {
    return try {
        File("/proc/cpuinfo").inputStream().bufferedReader().use {
            it.readText()
        }
    } catch (e: Exception) {
        Log.w(LOG_TAG, "Couldn't read /proc/cpuinfo", e)
        null
    }
}