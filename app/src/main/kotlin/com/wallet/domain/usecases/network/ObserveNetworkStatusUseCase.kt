package com.octane.wallet.domain.usecases.network

import com.octane.wallet.core.network.ConnectionType
import com.octane.wallet.core.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Observes network connectivity status.
 * Used to show offline banners and disable network-dependent features.
 */
class ObserveNetworkStatusUseCase(
    private val networkMonitor: NetworkMonitor
) {
    operator fun invoke(): Flow<NetworkStatus> {
        return combine(
            networkMonitor.isConnected,
            networkMonitor.connectionType
        ) { connected, type ->
            NetworkStatus(
                isConnected = connected,
                connectionType = type,
                isMetered = type == ConnectionType.CELLULAR
            )
        }
    }
}

data class NetworkStatus(
    val isConnected: Boolean,
    val connectionType: ConnectionType,
    val isMetered: Boolean
)
