package com.decagon.core.util

/**
 * Type-safe loading state for any async operation.
 * Prevents impossible states (loading + success simultaneously).
 * 
 * @param T The type of data when successful
 */
sealed interface DecagonLoadingState<out T> {
    /**
     * Initial state before any operation.
     * Use when screen hasn't started loading yet.
     */
    data object Idle : DecagonLoadingState<Nothing>
    
    /**
     * Operation in progress.
     * Show loading indicator, disable inputs.
     */
    data object Loading : DecagonLoadingState<Nothing>
    
    /**
     * Operation completed successfully.
     * @param data The loaded data
     */
    data class Success<T>(val data: T) : DecagonLoadingState<T>
    
    /**
     * Operation failed.
     * @param throwable The error that occurred
     * @param message User-friendly error message
     */
    data class Error(
        val throwable: Throwable,
        val message: String = throwable.message ?: "Unknown error"
    ) : DecagonLoadingState<Nothing>
    
    // Convenience properties
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isIdle: Boolean get() = this is Idle
    
    /**
     * Get data if success, null otherwise.
     */
    fun getOrNull(): T? = (this as? Success)?.data
    
    /**
     * Get data if success, throw if error.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw throwable
        is Loading -> throw IllegalStateException("Data not available while loading")
        is Idle -> throw IllegalStateException("Data not available in idle state")
    }
}

// Extension: Transform data
fun <T, R> DecagonLoadingState<T>.map(transform: (T) -> R): DecagonLoadingState<R> {
    return when (this) {
        is DecagonLoadingState.Success -> DecagonLoadingState.Success(transform(data))
        is DecagonLoadingState.Loading -> DecagonLoadingState.Loading
        is DecagonLoadingState.Error -> DecagonLoadingState.Error(throwable, message)
        is DecagonLoadingState.Idle -> DecagonLoadingState.Idle
    }
}