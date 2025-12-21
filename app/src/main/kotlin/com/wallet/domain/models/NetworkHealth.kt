package com.octane.wallet.domain.models

/**
 * Network health status.
 */
sealed interface NetworkHealth {
    data object Unknown : NetworkHealth
    data object Offline : NetworkHealth
    data class Healthy(val latencyMs: Long) : NetworkHealth
    data class Slow(val latencyMs: Long) : NetworkHealth
    data class Degraded(val latencyMs: Long) : NetworkHealth
    data class Down(val error: String) : NetworkHealth
}