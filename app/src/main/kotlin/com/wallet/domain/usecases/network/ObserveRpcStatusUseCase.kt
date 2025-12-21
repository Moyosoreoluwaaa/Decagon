package com.octane.wallet.domain.usecases.network

import com.wallet.core.blockchain.SolanaRpcManager
import com.octane.wallet.domain.models.NetworkHealth
import kotlinx.coroutines.flow.Flow

/**
 * Observes the status of the current RPC endpoint.
 * Returns health status for the Network Status Indicator (V1.9).
 */
class ObserveRpcStatusUseCase(
    private val rpcManager: SolanaRpcManager
) {
    operator fun invoke(): Flow<NetworkHealth> {
        return rpcManager.observeNetworkHealth()
    }
}