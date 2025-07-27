package com.module.notelycompose.transcription.domain

import com.module.notelycompose.core.debugPrintln
import com.module.notelycompose.whisper.WhisperContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation of WhisperModelLoader.
 * Manages the actual WhisperContext lifecycle on iOS platform.
 */
actual class WhisperModelLoader {
    private var whisperContext: WhisperContext? = null
    
    actual suspend fun loadModel() {
        // Release any existing context first
        releaseModel()
        
        try {
            val modelPath = getModelPath()
            if (!NSFileManager.defaultManager.fileExistsAtPath(modelPath)) {
                throw IllegalStateException("Model file not found: $modelPath")
            }
            
            debugPrintln { "WhisperModelLoader (iOS): Loading model from $modelPath" }
            whisperContext = WhisperContext.createContext(modelPath)
            debugPrintln { "WhisperModelLoader (iOS): Model loaded successfully" }
            
        } catch (e: Exception) {
            debugPrintln { "WhisperModelLoader (iOS): Failed to load model: ${e.message}" }
            whisperContext = null
            throw e
        }
    }
    
    actual suspend fun releaseModel() {
        whisperContext?.let { context ->
            debugPrintln { "WhisperModelLoader (iOS): Releasing existing model context" }
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
        return NSFileManager.defaultManager.fileExistsAtPath(getModelPath())
    }
    
    /**
     * Validates the model file integrity.
     */
    fun isValidModel(): Boolean {
        return try {
            val modelPath = getModelPath()
            NSFileManager.defaultManager.fileExistsAtPath(modelPath)
        } catch (e: Exception) {
            debugPrintln { "WhisperModelLoader (iOS): Model validation failed: ${e.message}" }
            false
        }
    }
    
    private fun getModelPath(): String {
        val documentsDirectory = NSFileManager.defaultManager.URLsForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask
        ).first() as NSURL

        return documentsDirectory.URLByAppendingPathComponent("ggml-base.bin")?.path ?: ""
    }
}