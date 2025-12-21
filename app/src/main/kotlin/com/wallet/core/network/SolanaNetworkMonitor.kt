package com.octane.wallet.core.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Monitors Solana RPC endpoint health.
 * Tracks latency and automatically switches to fallback on failures.
 */
class SolanaNetworkMonitor(
    private val networkMonitor: NetworkMonitor
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ✅ FIX: Explicitly declare type as RpcHealth (not RpcHealth.Unknown)
    private val _rpcHealth = MutableStateFlow<RpcHealth>(RpcHealth.Unknown)
    val rpcHealth: StateFlow<RpcHealth> = _rpcHealth.asStateFlow()

    // ✅ FIX: Explicitly declare type as RpcEndpoint (not RpcEndpoint.PRIMARY)
    private val _currentEndpoint = MutableStateFlow<RpcEndpoint>(RpcEndpoint.PRIMARY)
    val currentEndpoint: StateFlow<RpcEndpoint> = _currentEndpoint.asStateFlow()

    init {
        startHealthChecks()
    }

    private fun startHealthChecks() {
        scope.launch {
            while (isActive) {
                if (networkMonitor.isConnected.value) {
                    checkRpcHealth()
                } else {
                    _rpcHealth.value = RpcHealth.Offline // ✅ Now works
                }
                delay(10.seconds)
            }
        }
    }

    private fun checkRpcHealth() {
        val endpoint = _currentEndpoint.value
        val startTime = System.currentTimeMillis()

        try {
            // TODO: Implement actual RPC ping when HTTP client is ready
            val latency = System.currentTimeMillis() - startTime

            _rpcHealth.value = when {
                latency < 500 -> RpcHealth.Healthy(endpoint, latency)
                latency < 2000 -> RpcHealth.Slow(endpoint, latency)
                else -> {
                    switchToFallback()
                    RpcHealth.Degraded(endpoint, latency)
                }
            } // ✅ Now works
        } catch (e: Exception) {
            switchToFallback()
            _rpcHealth.value = RpcHealth.Down(endpoint, e.message ?: "Unknown error") // ✅ Now works
        }
    }

    private fun switchToFallback() {
        _currentEndpoint.value = when (_currentEndpoint.value) {
            RpcEndpoint.PRIMARY -> RpcEndpoint.FALLBACK
            RpcEndpoint.FALLBACK -> RpcEndpoint.CUSTOM
            RpcEndpoint.CUSTOM -> RpcEndpoint.PRIMARY
        }
    }

    fun setCustomEndpoint(url: String) {
        _currentEndpoint.value = RpcEndpoint.CUSTOM
    }
}

enum class RpcEndpoint {
    PRIMARY,    // Helius or Alchemy
    FALLBACK,   // QuickNode or public RPC
    CUSTOM      // User-provided RPC
}

sealed interface RpcHealth {
    data object Unknown : RpcHealth
    data object Offline : RpcHealth
    data class Healthy(val endpoint: RpcEndpoint, val latencyMs: Long) : RpcHealth
    data class Slow(val endpoint: RpcEndpoint, val latencyMs: Long) : RpcHealth
    data class Degraded(val endpoint: RpcEndpoint, val latencyMs: Long) : RpcHealth
    data class Down(val endpoint: RpcEndpoint, val error: String) : RpcHealth

    val isOperational: Boolean
        get() = this is Healthy || this is Slow
}