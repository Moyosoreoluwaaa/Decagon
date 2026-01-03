package com.decagon.domain.usecase.asset

import com.decagon.core.network.NetworkMonitor
import com.decagon.core.util.LoadingState
import com.decagon.domain.repository.AssetRepository
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.first

/**
 * Refreshes asset balances and prices.
 *
 * Business Rules:
 * - Fetches on-chain balances from Solana RPC
 * - Updates prices from CoinGecko
 * - Only refreshes if online
 * - Returns stale data if offline
 */

class RefreshAssetsUseCase(
    private val assetRepository: AssetRepository,
    private val walletRepository: DecagonWalletRepository,
    private val networkMonitor: NetworkMonitor
) {
    suspend operator fun invoke(): LoadingState<Unit> {
        // Check network connectivity
        if (!networkMonitor.isConnected.value) {
            return LoadingState.Error(
                Exception("No internet connection. Showing cached data.")
            )
        }

        // Get active wallet
        val wallet = walletRepository.observeActiveWallet().first()
            ?: return LoadingState.Error(
                IllegalStateException("No active wallet")
            )

        // Refresh assets
        return assetRepository.refreshAssets(wallet.id, wallet.publicKey)
    }
}
