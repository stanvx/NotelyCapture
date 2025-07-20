package com.module.notelycompose.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.content.ContextCompat
import audio.utils.LauncherHolder
import com.module.notelycompose.core.debugPrintln
import com.module.notelycompose.utils.decodeWaveFile
import com.whispercpp.whisper.WhisperCallback
import com.whispercpp.whisper.WhisperContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

actual class Transcriber(
    private val context: Context,
    private val launcherHolder: LauncherHolder
) {
    private var canTranscribe: Boolean = false
    private var isTranscribing = false
    private var isFinished = false // Track if resources have been released
    private val modelsPath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    private var whisperContext: WhisperContext? = null
    private var permissionContinuation: ((Boolean) -> Unit)? = null


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
        loadBaseModel()
    }

    private suspend fun loadBaseModel(){
        debugPrintln{"Loading model...\n"}
        try {
            // Release any existing context before creating a new one
            whisperContext?.let { existingContext ->
                debugPrintln { "Transcriber: Releasing existing whisper context before loading new model" }
                existingContext.release()
            }
            
            val firstModel = File(modelsPath, "ggml-base.bin")
            whisperContext = WhisperContext.createContextFromFile(firstModel.absolutePath)
            canTranscribe = true
            debugPrintln { "Transcriber: Model loaded successfully" }
        } catch (e: Exception) {
            debugPrintln { "Transcriber: Error loading model: ${e.message}" }
            whisperContext = null
            canTranscribe = false
            throw e
        }
    }

    actual fun doesModelExists() : Boolean{
        val firstModel = File(modelsPath, "ggml-base.bin")
        return firstModel.exists()
    }

    actual fun isValidModel() : Boolean{
        try {
            val firstModel = File(modelsPath, "ggml-base.bin")
            if (!firstModel.exists()) {
                return false
            }
            
            // Try to create a temporary context just for validation
            val tempContext = WhisperContext.createContextFromFile(firstModel.absolutePath)
            
            // If we got here, the model is valid. Clean up the temp context
            // Note: We do this synchronously in a runBlocking since this is a validation method
            runBlocking {
                tempContext.release()
            }
            return true
        } catch (e: Exception) {
            debugPrintln { "Transcriber: Model validation failed: ${e.message}" }
            return false
        }
    }

    actual suspend fun stop() {
        debugPrintln { "Transcriber: stop() called" }
        isTranscribing = false
        try {
            whisperContext?.stopTranscription()
            debugPrintln { "Transcriber: transcription stopped successfully" }
        } catch (e: Exception) {
            debugPrintln { "Transcriber: Error stopping transcription: ${e.message}" }
        }
    }

    actual suspend fun finish() {
        debugPrintln { "Transcriber: finish() called - releasing whisper context" }
        
        // Prevent double cleanup
        if (isFinished) {
            debugPrintln { "Transcriber: Resources already finished, skipping cleanup" }
            return
        }
        
        try {
            whisperContext?.let { context ->
                context.release()
                debugPrintln { "Transcriber: whisper context released successfully" }
            }
        } catch (e: Exception) {
            debugPrintln { "Transcriber: Error releasing whisper context: ${e.message}" }
        } finally {
            whisperContext = null
            canTranscribe = false
            isFinished = true
            debugPrintln { "Transcriber: whisper context set to null, marked as finished" }
        }
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
                val text = whisperContext?.transcribeData(data, language, callback = object : WhisperCallback{
                    override fun onNewSegment(startMs: Long, endMs: Long, text: String) {
                        // Switch to main thread for callback invocation to ensure UI updates are safe
                        kotlinx.coroutines.MainScope().launch {
                            onNewSegment(startMs, endMs, text)
                        }
                    }

                    override fun onProgress(progress: Int) {
                        // Switch to main thread for callback invocation to ensure UI updates are safe
                        kotlinx.coroutines.MainScope().launch {
                            onProgress(progress)
                        }
                    }

                    override fun onComplete() {
                        // Switch to main thread for callback invocation to ensure UI updates are safe
                        kotlinx.coroutines.MainScope().launch {
                            onComplete()
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