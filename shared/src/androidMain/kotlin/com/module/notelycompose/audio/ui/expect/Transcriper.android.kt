package com.module.notelycompose.audio.ui.expect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.module.notelycompose.utils.decodeWaveFile
import com.whispercpp.whisper.WhisperCallback
import com.whispercpp.whisper.WhisperContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

actual class Transcriper(
    private val context: Context,
    private val permissionLauncher: ActivityResultLauncher<String>?
) {
    private var canTranscribe: Boolean = false
    private var isTranscribing = false
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

            if (permissionLauncher != null) {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                continuation.resume(false)
            }

            continuation.invokeOnCancellation {
                permissionContinuation = null
            }
        }
    }


    actual suspend fun initialize() {
        println("speech: initialize model")
        loadBaseModel()
    }

    private fun loadBaseModel(){
        println("Loading model...\n")
        val firstModel = File(modelsPath, "ggml-base.bin")
        whisperContext = WhisperContext.createContextFromFile(firstModel.absolutePath)
        canTranscribe = true
    }

    actual fun doesModelExists() : Boolean{
        val firstModel = File(modelsPath, "ggml-base.bin")
        return firstModel.exists()
    }

    actual fun isValidModel() : Boolean{
      try {
          loadBaseModel()
      }catch (e:Exception){
          return false
      }
        return true
    }

    actual suspend fun stop() {
        isTranscribing = false
        whisperContext?.stopTranscription()
    }

    actual suspend fun finish() {
        whisperContext?.release()
    }

    actual suspend fun start(
        filePath: String, language: String,
        onProgress : (Int) -> Unit,
        onNewSegment : (Long, Long,String) -> Unit,
        onComplete : () -> Unit
    ) {
        if (!canTranscribe) {
            return
        }

        canTranscribe = false

        try {
            println("Reading wave samples... ")
            val file = File(filePath)
            val data = decodeWaveFile(file)
            println("${data.size / (16000 / 1000)} ms\n")
            println("Transcribing data...\n")
            val start = System.currentTimeMillis()
            val text = whisperContext?.transcribeData(data, language, callback = object : WhisperCallback{
                override fun onNewSegment(startMs: Long, endMs: Long, text: String) {
                    onNewSegment(startMs, endMs, text)
                }

                override fun onProgress(progress: Int) {
                    onProgress(progress)
                }

                override fun onComplete() {
                    onComplete()
                }

            })
            val elapsed = System.currentTimeMillis() - start
            println("Done ($elapsed ms): \n$text\n")
        } catch (e: Exception) {
            e.printStackTrace()
            println("${e.localizedMessage}\n")
        }

        canTranscribe = true

    }
}