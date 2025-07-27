package com.module.notelycompose.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.content.ContextCompat
import audio.utils.LauncherHolder
import com.module.notelycompose.core.debugPrintln
import com.module.notelycompose.transcription.domain.WhisperModelLoader
import com.module.notelycompose.utils.decodeWaveFile
import com.whispercpp.whisper.WhisperCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class Transcriber(
    private val context: Context,
    private val launcherHolder: LauncherHolder
) : KoinComponent {
    private var canTranscribe: Boolean = false
    private var isTranscribing = false
    private var isFinished = false // Track if resources have been released
    private val modelsPath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    private var permissionContinuation: ((Boolean) -> Unit)? = null
    
    // Inject the WhisperModelLoader from Koin
    private val whisperModelLoader: WhisperModelLoader by inject()


    actual fun hasRecordingPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }


    actual suspend fun requestRecordingPermission(): Boolean {
        if (hasRecordingPermission()) {
            return true
        }

        return suspendCancellableCoroutine { continuation ->
            permissionContinuation = { isGranted ->
                continuation.resume(isGranted)
            }

            if (launcherHolder.permissionLauncher != null) {
                launcherHolder.permissionLauncher?.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            } else {
                continuation.resume(false)
            }

            continuation.invokeOnCancellation {
                permissionContinuation = null
            }
        }
    }


    actual suspend fun initialize() {
        debugPrintln{"speech: initialize model"}
        isFinished = false // Reset finished state when reinitializing
        // Model loading is now handled by WhisperModelManager
        // This method is kept for compatibility but actual loading happens in the manager
        canTranscribe = whisperModelLoader.doesModelExist()
        debugPrintln { "Transcriber: Model availability checked, canTranscribe=$canTranscribe" }
    }

    actual fun doesModelExists() : Boolean{
        return whisperModelLoader.doesModelExist()
    }

    actual fun isValidModel() : Boolean{
        return whisperModelLoader.isValidModel()
    }

    actual suspend fun stop() {
        debugPrintln { "Transcriber: stop() called" }
        isTranscribing = false
        try {
            whisperModelLoader.getContext().stopTranscription()
            debugPrintln { "Transcriber: transcription stopped successfully" }
        } catch (e: Exception) {
            debugPrintln { "Transcriber: Error stopping transcription: ${e.message}" }
        }
    }

    actual suspend fun finish() {
        debugPrintln { "Transcriber: finish() called" }
        
        // Prevent double cleanup
        if (isFinished) {
            debugPrintln { "Transcriber: Resources already finished, skipping cleanup" }
            return
        }
        
        // Don't release the shared context anymore - it's managed by WhisperModelManager
        // Just reset local state
        canTranscribe = false
        isFinished = true
        debugPrintln { "Transcriber: local state reset, shared context remains managed by WhisperModelManager" }
    }

    actual suspend fun start(
        filePath: String, language: String,
        onProgress : (Int) -> Unit,
        onNewSegment : (Long, Long,String) -> Unit,
        onComplete : () -> Unit
    ) {
        if (!canTranscribe || isFinished) {
            debugPrintln { "Transcriber: Cannot start - canTranscribe: $canTranscribe, isFinished: $isFinished" }
            return
        }

        canTranscribe = false

        try {
            debugPrintln{"Reading wave samples... "}
            val file = File(filePath)
            val data = decodeWaveFile(file)
            debugPrintln{"${data.size / (16000 / 1000)} ms\n"}
            debugPrintln{"Transcribing data...\n"}
            val start = System.currentTimeMillis()
            
            // Execute transcription on IO dispatcher to avoid blocking
            withContext(Dispatchers.IO) {
                val text = whisperModelLoader.getContext().transcribeData(data, language, callback = object : WhisperCallback{
                    override fun onNewSegment(startMs: Long, endMs: Long, text: String) {
                        // Switch to main thread for callback invocation using structured concurrency
                        runBlocking {
                            withContext(Dispatchers.Main) {
                                onNewSegment(startMs, endMs, text)
                            }
                        }
                    }

                    override fun onProgress(progress: Int) {
                        // Switch to main thread for callback invocation using structured concurrency
                        runBlocking {
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                    }

                    override fun onComplete() {
                        // Switch to main thread for callback invocation using structured concurrency
                        runBlocking {
                            withContext(Dispatchers.Main) {
                                onComplete()
                            }
                        }
                    }

                })
                val elapsed = System.currentTimeMillis() - start
                debugPrintln{"Done ($elapsed ms): \n$text\n"}
            }
        } catch (e: Exception) {
            e.printStackTrace()
            debugPrintln{"${e.localizedMessage}\n"}
        }

        canTranscribe = true

    }
}