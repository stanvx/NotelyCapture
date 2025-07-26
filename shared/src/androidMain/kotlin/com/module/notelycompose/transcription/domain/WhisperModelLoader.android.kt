package com.module.notelycompose.transcription.domain

import android.content.Context
import android.os.Environment
import com.module.notelycompose.core.debugPrintln
import com.whispercpp.whisper.WhisperContext
import java.io.File

/**
 * Android implementation of WhisperModelLoader.
 * Manages the actual WhisperContext lifecycle on Android platform.
 */
actual class WhisperModelLoader(
    private val context: Context
) {
    private var whisperContext: WhisperContext? = null
    private val modelsPath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    
    actual suspend fun loadModel() {
        // Release any existing context first
        releaseModel()
        
        try {
            val modelFile = File(modelsPath, "ggml-base.bin")
            if (!modelFile.exists()) {
                throw IllegalStateException("Model file not found: ${modelFile.absolutePath}")
            }
            
            debugPrintln { "WhisperModelLoader (Android): Loading model from ${modelFile.absolutePath}" }
            whisperContext = WhisperContext.createContextFromFile(modelFile.absolutePath)
            debugPrintln { "WhisperModelLoader (Android): Model loaded successfully" }
            
        } catch (e: Exception) {
            debugPrintln { "WhisperModelLoader (Android): Failed to load model: ${e.message}" }
            whisperContext = null
            throw e
        }
    }
    
    actual suspend fun releaseModel() {
        whisperContext?.let { context ->
            debugPrintln { "WhisperModelLoader (Android): Releasing existing model context" }
            context.release()
            whisperContext = null
        }
    }
    
    /**
     * Gets the loaded WhisperContext. Throws if not loaded.
     */
    fun getContext(): WhisperContext {
        return whisperContext ?: throw IllegalStateException(
            "WhisperContext not loaded. Call WhisperModelManager.ensureModelLoaded() first."
        )
    }
    
    /**
     * Checks if the model file exists on disk.
     */
    fun doesModelExist(): Boolean {
        val modelFile = File(modelsPath, "ggml-base.bin")
        return modelFile.exists()
    }
    
    /**
     * Validates the model file integrity.
     */
    fun isValidModel(): Boolean {
        return try {
            val modelFile = File(modelsPath, "ggml-base.bin")
            modelFile.exists() && modelFile.length() > 0
        } catch (e: Exception) {
            debugPrintln { "WhisperModelLoader (Android): Model validation failed: ${e.message}" }
            false
        }
    }
}