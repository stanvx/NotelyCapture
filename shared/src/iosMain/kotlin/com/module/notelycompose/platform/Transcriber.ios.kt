package com.module.notelycompose.platform

import com.module.notelycompose.core.debugPrintln
import com.module.notelycompose.transcription.domain.WhisperModelLoader
import com.module.notelycompose.whisper.WhisperCallback
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfURL
import kotlin.math.max
import kotlin.math.min

actual class Transcriber : KoinComponent {
    private var canTranscribe: Boolean = false
    private var isTranscribing = false
    private var isModelLoaded = false
    
    // Inject the WhisperModelLoader from Koin
    private val whisperModelLoader: WhisperModelLoader by inject()


    actual fun hasRecordingPermission(): Boolean {
        return true
    }


    actual suspend fun requestRecordingPermission(): Boolean {
       return true
    }


    actual suspend fun initialize() {
        debugPrintln{"speech: initialize model"}
        // Model loading is now handled by WhisperModelManager
        // This method is kept for compatibility but actual loading happens in the manager
        canTranscribe = whisperModelLoader.doesModelExist()
        isModelLoaded = canTranscribe
        debugPrintln { "Transcriber (iOS): Model availability checked, canTranscribe=$canTranscribe" }
    }

    actual fun doesModelExists() : Boolean{
        return whisperModelLoader.doesModelExist()
    }

    actual fun isValidModel() : Boolean{
        return whisperModelLoader.isValidModel()
    }

    actual suspend fun stop() {
        isTranscribing = false
        whisperModelLoader.getContext().stopTranscribing()
    }

    actual suspend fun finish() {
        // Don't release the shared context anymore - it's managed by WhisperModelManager
        // Just reset local state
        canTranscribe = false
        isModelLoaded = false
        debugPrintln { "Transcriber (iOS): local state reset, shared context remains managed by WhisperModelManager" }
    }

    actual suspend fun start(
        filePath: String, language: String,
        onProgress : (Int) -> Unit,
        onNewSegment : (Long, Long,String) -> Unit,
        onComplete : () -> Unit
    ) {
        if (!canTranscribe) {
            debugPrintln{"Model not loaded yet"}
            return
        }

        canTranscribe = false

        try {
            debugPrintln{"Reading wave samples... "}
            val data = decodeWaveFile(filePath)
            debugPrintln{"${data.size / (16000 / 1000)} ms\n"}
            debugPrintln{"Transcribing data...\n"}
           whisperModelLoader.getContext().fullTranscribe(data, language, object : WhisperCallback{
                override fun onProgress(progress: Int) {
                    onProgress(progress)
                }

                override fun onNewSegment(l1: Long, l2: Long, text: String) {
                    onNewSegment(l1,l2,text)
                }

               override fun onComplete() {
                   onComplete()
               }

            })
        } catch (e: Exception) {
            e.printStackTrace()
            debugPrintln{"${e.message}\n"}
        }

        canTranscribe = true

    }

    @OptIn(ExperimentalForeignApi::class)
    fun decodeWaveFile(path: String): FloatArray {
        val url = NSURL.fileURLWithPath(path)
        val data = NSData.dataWithContentsOfURL(url) ?: throw Exception("Failed to read file")

        val length = data.length.toInt()
        val bytes = data.bytes?.reinterpret<ByteVar>() ?: throw Exception("Invalid WAV file")

        // Skip 44-byte WAV header
        val start = 44
        val sampleCount = (length - start) / 2
        val floatArray = FloatArray(sampleCount)

        var i = 0
        while (i < sampleCount) {
            val byteIndex = start + i * 2
            val low = bytes[byteIndex].toInt() and 0xFF
            val high = bytes[byteIndex + 1].toInt()
            val shortVal = (high shl 8) or low
            floatArray[i] = max(-1.0f, min(shortVal / 32767.0f, 1.0f))
            i++
        }

        return floatArray
    }


}