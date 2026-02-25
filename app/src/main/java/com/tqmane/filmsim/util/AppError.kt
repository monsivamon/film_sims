package com.tqmane.filmsim.util

/**
 * Unified error hierarchy for the FilmSims application.
 * Each error carries a user-facing message and a technical log message.
 */
sealed class AppError {
    abstract val userMessage: String
    abstract val logMessage: String
    abstract val cause: Throwable?

    data class NetworkError(
        override val userMessage: String,
        override val logMessage: String,
        override val cause: Throwable? = null
    ) : AppError()

    data class StorageError(
        override val userMessage: String,
        override val logMessage: String,
        override val cause: Throwable? = null
    ) : AppError()

    data class ProcessingError(
        override val userMessage: String,
        override val logMessage: String,
        override val cause: Throwable? = null
    ) : AppError()

    data class MemoryError(
        override val userMessage: String,
        override val logMessage: String,
        override val cause: Throwable? = null
    ) : AppError()
}

/**
 * Maps common exceptions to the appropriate [AppError] subtype.
 * Used as a default mapper for error handling pipelines.
 */
fun defaultErrorMapper(throwable: Throwable): AppError = when (throwable) {
    is java.io.IOException -> AppError.NetworkError(
        userMessage = "Network error",
        logMessage = throwable.message ?: "IOException",
        cause = throwable
    )
    is OutOfMemoryError -> AppError.MemoryError(
        userMessage = "Out of memory",
        logMessage = throwable.message ?: "OOM",
        cause = throwable
    )
    is SecurityException -> AppError.StorageError(
        userMessage = "Permission denied",
        logMessage = throwable.message ?: "SecurityException",
        cause = throwable
    )
    else -> AppError.ProcessingError(
        userMessage = "An error occurred",
        logMessage = throwable.message ?: throwable.javaClass.simpleName,
        cause = throwable
    )
}
