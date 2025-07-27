package com.module.notelycompose.transcription.error

/**
 * Sealed class representing different types of transcription errors
 * with specific error handling strategies for each type.
 */
sealed class TranscriptionError(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    
    /**
     * Error during transcription model initialization
     */
    data class InitializationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : TranscriptionError(message, cause)
    
    /**
     * Error during audio file processing
     */
    data class AudioProcessingError(
        override val message: String,
        override val cause: Throwable? = null
    ) : TranscriptionError(message, cause)
    
    /**
     * Error during note creation after successful transcription
     */
    data class NoteCreationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : TranscriptionError(message, cause)
    
    /**
     * Service has been disposed or is in invalid state
     */
    data class ServiceDisposedError(
        override val message: String = "Transcription service has been disposed"
    ) : TranscriptionError(message)
    
    /**
     * Timeout occurred during transcription process
     */
    data class TranscriptionTimeoutError(
        override val message: String,
        override val cause: Throwable? = null
    ) : TranscriptionError(message, cause)
    
    /**
     * Audio file validation failed
     */
    data class AudioFileValidationError(
        override val message: String,
        val filePath: String
    ) : TranscriptionError(message)
    
    /**
     * Unknown or unexpected error during transcription
     */
    data class UnknownError(
        override val message: String,
        override val cause: Throwable? = null
    ) : TranscriptionError(message, cause)
}

/**
 * Extension function to determine if an error is recoverable
 */
val TranscriptionError.isRecoverable: Boolean
    get() = when (this) {
        is TranscriptionError.InitializationError -> true // Can retry initialization
        is TranscriptionError.AudioProcessingError -> false // Usually indicates bad audio file
        is TranscriptionError.NoteCreationError -> true // Can retry note creation
        is TranscriptionError.ServiceDisposedError -> false // Service needs to be recreated
        is TranscriptionError.TranscriptionTimeoutError -> true // Can retry with different timeout
        is TranscriptionError.AudioFileValidationError -> false // Bad file path/format
        is TranscriptionError.UnknownError -> false // Unknown errors are not safe to retry
    }

/**
 * Extension function to get user-friendly error messages
 */
val TranscriptionError.userMessage: String
    get() = when (this) {
        is TranscriptionError.InitializationError -> "Failed to initialize transcription. Please try again."
        is TranscriptionError.AudioProcessingError -> "Unable to process audio file. Please check the recording."
        is TranscriptionError.NoteCreationError -> "Transcription completed but failed to save note. Please try again."
        is TranscriptionError.ServiceDisposedError -> "Transcription service is no longer available. Please restart the app."
        is TranscriptionError.TranscriptionTimeoutError -> "Transcription took too long. Please try with a shorter recording."
        is TranscriptionError.AudioFileValidationError -> "Invalid audio file. Please record again."
        is TranscriptionError.UnknownError -> "An unexpected error occurred. Please try again."
    }