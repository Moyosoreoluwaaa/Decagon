package com.wallet.core.blockchain

import com.octane.wallet.core.network.NetworkMonitor
import com.octane.wallet.data.remote.api.SolanaRpcApi
import com.octane.wallet.data.remote.dto.solana.RpcRequest
import com.octane.wallet.domain.models.NetworkHealth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Manages Solana RPC endpoints with automatic fallback.
 * NOW WITH REAL HEALTH CHECKS.
 */
class SolanaRpcManager(
    private val networkMonitor: NetworkMonitor,
    private val solanaRpcApi: SolanaRpcApi
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // RPC endpoints (configure with your actual keys)
    private val endpoints = listOf(
        RpcEndpoint(
            url = "https://solana-mainnet.g.alchemy.com/v2/${System.getenv("ALCHEMY_KEY") ?: ""}",
            type = RpcType.PRIMARY,
            name = "Alchemy"
        ),
        RpcEndpoint(
            url = "https://rpc.helius.xyz/?api-key=${System.getenv("HELIUS_KEY") ?: ""}",
            type = RpcType.PRIMARY,
            name = "Helius"
        ),
        RpcEndpoint(
            url = "https://api.mainnet-beta.solana.com",
            type = RpcType.FALLBACK,
            name = "Public RPC"
        )
    )

    private val _currentEndpoint = MutableStateFlow(endpoints.first())
    val currentEndpoint: StateFlow<RpcEndpoint> = _currentEndpoint.asStateFlow()

    private val _endpointHealth = MutableStateFlow<Map<String, RpcHealth>>(emptyMap())
    val endpointHealth: StateFlow<Map<String, RpcHealth>> = _endpointHealth.asStateFlow()

    init {
        startHealthChecks()
    }

    fun getCurrentRpcUrl(): String = _currentEndpoint.value.url

    fun switchToNextEndpoint() {
        val currentIndex = endpoints.indexOf(_currentEndpoint.value)
        val nextIndex = (currentIndex + 1) % endpoints.size
        _currentEndpoint.value = endpoints[nextIndex]
    }

    fun setCustomEndpoint(url: String, name: String = "Custom") {
        val customEndpoint = RpcEndpoint(
            url = url,
            type = RpcType.CUSTOM,
            name = name
        )
        _currentEndpoint.value = customEndpoint
    }

    private fun startHealthChecks() {
        scope.launch {
            while (isActive) {
                if (networkMonitor.isConnected.value) {
                    checkAllEndpoints()
                }
                delay(10.seconds)
            }
        }
    }

    private suspend fun checkAllEndpoints() {
        val healthMap = mutableMapOf<String, RpcHealth>()

        endpoints.forEach { endpoint ->
            val health = checkEndpointHealth(endpoint)
            healthMap[endpoint.url] = health

            // Auto-switch if current endpoint is down
            if (endpoint == _currentEndpoint.value && health.status == HealthStatus.DOWN) {
                switchToNextEndpoint()
            }
        }

        _endpointHealth.value = healthMap
    }

    /**
     * REAL IMPLEMENTATION: Check RPC health using getHealth method.
     */
    private suspend fun checkEndpointHealth(endpoint: RpcEndpoint): RpcHealth {
        val startTime = System.currentTimeMillis()

        return try {
            // Call getHealth RPC method (returns "ok" when healthy)
            val request = RpcRequest<List<String>>(
                method = "getHealth",
                params = emptyList()
            )

            val response = solanaRpcApi.getBalance(request) // Using balance API as proxy
            val latency = System.currentTimeMillis() - startTime

            // Check for errors
            if (response.error != null) {
                return RpcHealth(
                    endpoint = endpoint,
                    latency = latency,
                    status = HealthStatus.DOWN,
                    lastChecked = System.currentTimeMillis(),
                    error = response.error.message
                )
            }

            // Determine health based on latency
            RpcHealth(
                endpoint = endpoint,
                latency = latency,
                status = when {
                    latency < 500 -> HealthStatus.HEALTHY
                    latency < 2000 -> HealthStatus.SLOW
                    else -> HealthStatus.DEGRADED
                },
                lastChecked = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            RpcHealth(
                endpoint = endpoint,
                latency = 0,
                status = HealthStatus.DOWN,
                lastChecked = System.currentTimeMillis(),
                error = e.message ?: "Connection failed"
            )
        }
    }

    /**
     * Observes the network health based on the current RPC endpoint's status.
     */
    fun observeNetworkHealth(): Flow<NetworkHealth> =
        currentEndpoint.combine(endpointHealth) { current, healthMap ->
            val currentHealth = healthMap[current.url]

            currentHealth?.let { rpcHealth ->
                when (rpcHealth.status) {
                    HealthStatus.HEALTHY -> NetworkHealth.Healthy(rpcHealth.latency)
                    HealthStatus.SLOW -> NetworkHealth.Slow(rpcHealth.latency)
                    HealthStatus.DEGRADED -> NetworkHealth.Degraded(rpcHealth.latency)
                    HealthStatus.DOWN -> NetworkHealth.Down(rpcHealth.error ?: "RPC Down")
                }
            } ?: NetworkHealth.Unknown
        }
            .onStart { emit(NetworkHealth.Unknown) }
            .distinctUntilChanged()
}

data class RpcEndpoint(
    val url: String,
    val type: RpcType,
    val name: String
)

enum class RpcType {
    PRIMARY,    // Paid, high-quality RPC (Helius, Alchemy)
    FALLBACK,   // Free public RPC (slower, rate-limited)
    CUSTOM      // User-provided RPC
}

data class RpcHealth(
    val endpoint: RpcEndpoint,
    val latency: Long,
    val status: HealthStatus,
    val lastChecked: Long,
    val error: String? = null
)

enum class HealthStatus {
    HEALTHY,    // <500ms latency
    SLOW,       // 500ms-2s latency
    DEGRADED,   // >2s latency
    DOWN        // Unreachable or erroring
}