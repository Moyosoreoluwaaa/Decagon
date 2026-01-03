package com.decagon.core.util

/**
 * Type-safe loading state for any async operation.
 * Prevents impossible states (loading + success simultaneously).
 */

sealed interface LoadingState<out T> {
    /**
     * Initial state before any operation.
     */
    data object Idle : LoadingState<Nothing>

    /**
     * Operation in progress.
     */
    data object Loading : LoadingState<Nothing>

    /**
     * Transaction simulation in progress (Octane-specific).
     */
    data object Simulating : LoadingState<Nothing>

    /**
     * Operation completed successfully.
     */
    data class Success<T>(val data: T) : LoadingState<T>

    /**
     * Operation failed.
     */
    data class Error(
        val throwable: Throwable,
        val message: String = throwable.message ?: "Unknown error"
    ) : LoadingState<Nothing>

    /**
     * Data is stale (offline mode with cached data).
     */
    data class Stale<T>(val data: T) : LoadingState<T>

    // Convenience properties
    val isLoading: Boolean get() = this is Loading || this is Simulating
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isIdle: Boolean get() = this is Idle
    val isStale: Boolean get() = this is Stale

    /**
     * Get data if success/stale, null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Stale -> data
        else -> null
    }

    /**
     * Get data if success, throw if error.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw throwable
        is Loading, is Simulating -> throw IllegalStateException("Data not available while loading")
        is Idle -> throw IllegalStateException("Data not available in idle state")
        is Stale -> throw IllegalStateException("Data is stale")
    }
}

// Extension: Transform data
fun <T, R> LoadingState<T>.map(transform: (T) -> R): LoadingState<R> {
    return when (this) {
        is LoadingState.Success -> LoadingState.Success(transform(data))
        is LoadingState.Stale -> LoadingState.Stale(transform(data))
        is LoadingState.Loading -> LoadingState.Loading
        is LoadingState.Simulating -> LoadingState.Simulating
        is LoadingState.Error -> LoadingState.Error(throwable, message)
        is LoadingState.Idle -> LoadingState.Idle
    }
}
