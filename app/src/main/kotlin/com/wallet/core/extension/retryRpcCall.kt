package com.wallet.core.extension

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.withTimeout
import java.io.IOException
import kotlin.math.pow
import kotlin.time.Duration

/**
 * Retry RPC calls with exponential backoff.
 * Specific to Solana RPC failures.
 */
fun <T> Flow<T>.retryRpcCall(
    maxRetries: Int = 3,
    initialDelay: Long = 500,
    maxDelay: Long = 5000,
    factor: Double = 2.0
): Flow<T> = retryWhen { cause, attempt ->
    if (attempt < maxRetries && (cause is IOException || isRpcError(cause))) {
        val delay = (initialDelay * factor.pow(attempt.toDouble()))
            .toLong()
            .coerceAtMost(maxDelay)
        delay(delay)
        true
    } else {
        false
    }
}

private fun isRpcError(throwable: Throwable): Boolean {
    val message = throwable.message?.lowercase() ?: ""
    return message.contains("rpc") ||
            message.contains("timeout") ||
            message.contains("429") // Rate limit
}

/**
 * Throttle emissions - emit at most once per period.
 * Use for price updates to avoid excessive recompositions.
 */
fun <T> Flow<T>.throttleFirst(period: Long): Flow<T> = flow {
    var lastEmitTime = 0L
    collect { value ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEmitTime >= period) {
            lastEmitTime = currentTime
            emit(value)
        }
    }
}

/**
 * Throttle emissions - emit latest value at interval.
 * Use for search queries, real-time price updates.
 */
fun <T> Flow<T>.throttleLatest(period: Long): Flow<T> = flow {
    conflate().collect { value ->
        emit(value)
        delay(period)
    }
}

/**
 * Emit only when value changes according to predicate.
 */
fun <T, K> Flow<T>.distinctUntilChangedBy(
    keySelector: (T) -> K
): Flow<T> = flow {
    var lastKey: K? = null
    collect { value ->
        val key = keySelector(value)
        if (key != lastKey) {
            lastKey = key
            emit(value)
        }
    }
}

/**
 * Timeout wrapper - emit error if Flow takes too long.
 */
fun <T> Flow<T>.withTimeout(timeoutMillis: Long): Flow<T> = flow {
    withTimeout(timeoutMillis) {
        collect { value -> emit(value) }
    }
}

/**
 * Catch and transform errors to a fallback value.
 */
fun <T> Flow<T>.catchWithFallback(fallback: T): Flow<T> =
    catch { emit(fallback) }

/**
 * Log each emission for debugging.
 */
fun <T> Flow<T>.logEach(tag: String = "Flow"): Flow<T> = onEach { value ->
    println("[$tag] Emitted: $value")
}

/**
 * Combine with timeout for each emission.
 * Use for RPC calls that should complete quickly.
 */
fun <T> Flow<T>.withEmissionTimeout(
    timeout: Duration
): Flow<T> = flow {
    collect { value ->
        withTimeout(timeout) {
            emit(value)
        }
    }
}