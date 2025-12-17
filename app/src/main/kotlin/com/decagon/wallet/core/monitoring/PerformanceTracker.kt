package com.octane.wallet.core.monitoring

import kotlin.system.measureTimeMillis

/**
 * Tracks performance metrics for Octane operations.
 */
class PerformanceTracker(
    private val analyticsLogger: AnalyticsLogger
) {

    /**
     * Track RPC call latency.
     */
    suspend fun trackRpcCall(
        endpoint: String,
        method: String,
        block: suspend () -> Unit
    ) {
        val duration = measureTimeMillis {
            block()
        }

        analyticsLogger.logPerformance(
            metricName = OctaneEvents.RPC_LATENCY,
            durationMillis = duration,
            attributes = mapOf(
                "endpoint" to endpoint,
                "method" to method
            )
        )
    }

    /**
     * Track transaction confirmation time.
     */
    suspend fun trackTransactionConfirmation(
        signature: String,
        block: suspend () -> Unit
    ) {
        val duration = measureTimeMillis {
            block()
        }

        analyticsLogger.logPerformance(
            metricName = OctaneEvents.TX_CONFIRMATION_TIME,
            durationMillis = duration / 1000, // Convert to seconds
            attributes = mapOf(
                "signature" to signature
            )
        )
    }

    /**
     * Track swap execution time.
     */
    suspend fun trackSwapExecution(
        inputToken: String,
        outputToken: String,
        block: suspend () -> Unit
    ) {
        val duration = measureTimeMillis {
            block()
        }

        analyticsLogger.logPerformance(
            metricName = OctaneEvents.SWAP_EXECUTION_TIME,
            durationMillis = duration / 1000,
            attributes = mapOf(
                "input_token" to inputToken,
                "output_token" to outputToken
            )
        )
    }
}
