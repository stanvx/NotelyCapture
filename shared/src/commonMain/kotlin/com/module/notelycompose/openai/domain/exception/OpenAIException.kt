package com.module.notelycompose.openai.domain.exception

/**
 * Base exception class for OpenAI-related errors.
 */
sealed class OpenAIException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Exception thrown when API key is missing or invalid.
     */
    class InvalidApiKeyException(
        message: String = "OpenAI API key is missing or invalid",
        cause: Throwable? = null
    ) : OpenAIException(message, cause)

    /**
     * Exception thrown when network connection is unavailable.
     */
    class NetworkUnavailableException(
        message: String = "Network connection is not available",
        cause: Throwable? = null
    ) : OpenAIException(message, cause)

    /**
     * Exception thrown when request times out.
     */
    class RequestTimeoutException(
        message: String = "Request timed out",
        cause: Throwable? = null
    ) : OpenAIException(message, cause)

    /**
     * Exception thrown when API rate limit is exceeded.
     */
    class RateLimitExceededException(
        message: String = "API rate limit exceeded",
        val retryAfterSeconds: Int? = null,
        cause: Throwable? = null
    ) : OpenAIException(message, cause)

    /**
     * Exception thrown when API quota is exceeded.
     */
    class QuotaExceededException(
        message: String = "API quota exceeded",
        cause: Throwable? = null
    ) : OpenAIException(message, cause)

    /**
     * Exception thrown when audio file is invalid or unsupported.
     */
    class InvalidAudioFileException(
        message: String = "Audio file is invalid or unsupported",
        val filePath: String? = null,
        cause: Throwable? = null
    ) : OpenAIException(message, cause)

    /**
     * Exception thrown when text input is invalid.
     */
    class InvalidTextInputException(
        message: String = "Text input is invalid",
        cause: Throwable? = null
    ) : OpenAIException(message, cause)

    /**
     * Exception thrown when API returns an error response.
     */
    class ApiResponseException(
        message: String,
        val errorCode: String? = null,
        val errorType: String? = null,
        cause: Throwable? = null
    ) : OpenAIException(message, cause)

    /**
     * Exception thrown when model is unavailable or unsupported.
     */
    class ModelUnavailableException(
        message: String = "Requested model is unavailable",
        val modelName: String? = null,
        cause: Throwable? = null
    ) : OpenAIException(message, cause)

    /**
     * Exception thrown when file size exceeds limits.
     */
    class FileSizeExceededException(
        message: String = "File size exceeds maximum allowed limit",
        val fileSize: Long? = null,
        val maxSize: Long? = null,
        cause: Throwable? = null
    ) : OpenAIException(message, cause)

    /**
     * Exception thrown when security validation fails.
     */
    class SecurityValidationException(
        message: String = "Security validation failed",
        cause: Throwable? = null
    ) : OpenAIException(message, cause)

    /**
     * Exception thrown for unknown or unexpected errors.
     */
    class UnknownException(
        message: String = "An unknown error occurred",
        cause: Throwable? = null
    ) : OpenAIException(message, cause)
}

/**
 * Extension function to convert generic exceptions to OpenAI-specific exceptions.
 */
fun Throwable.toOpenAIException(): OpenAIException {
    return when (this) {
        is OpenAIException -> this
        is java.net.SocketTimeoutException -> OpenAIException.RequestTimeoutException(
            message = "Request timed out: ${this.message}",
            cause = this
        )
        is java.net.UnknownHostException -> OpenAIException.NetworkUnavailableException(
            message = "Network unavailable: ${this.message}",
            cause = this
        )
        is java.io.FileNotFoundException -> OpenAIException.InvalidAudioFileException(
            message = "Audio file not found: ${this.message}",
            cause = this
        )
        is SecurityException -> OpenAIException.SecurityValidationException(
            message = "Security validation failed: ${this.message}",
            cause = this
        )
        else -> OpenAIException.UnknownException(
            message = "Unexpected error: ${this.message}",
            cause = this
        )
    }
}

/**
 * Extension function to check if an exception is recoverable.
 */
fun OpenAIException.isRecoverable(): Boolean {
    return when (this) {
        is OpenAIException.NetworkUnavailableException,
        is OpenAIException.RequestTimeoutException,
        is OpenAIException.RateLimitExceededException -> true
        is OpenAIException.InvalidApiKeyException,
        is OpenAIException.QuotaExceededException,
        is OpenAIException.InvalidAudioFileException,
        is OpenAIException.InvalidTextInputException,
        is OpenAIException.ModelUnavailableException,
        is OpenAIException.FileSizeExceededException,
        is OpenAIException.SecurityValidationException -> false
        is OpenAIException.ApiResponseException -> errorCode != "invalid_api_key"
        is OpenAIException.UnknownException -> false
    }
}

/**
 * Extension function to get retry delay for recoverable exceptions.
 */
fun OpenAIException.getRetryDelaySeconds(): Int? {
    return when (this) {
        is OpenAIException.RateLimitExceededException -> retryAfterSeconds ?: 60
        is OpenAIException.NetworkUnavailableException -> 5
        is OpenAIException.RequestTimeoutException -> 10
        else -> null
    }
}