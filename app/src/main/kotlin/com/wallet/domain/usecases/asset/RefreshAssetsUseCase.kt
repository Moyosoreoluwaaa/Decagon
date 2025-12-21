package com.octane.wallet.domain.usecases.asset

import com.octane.wallet.core.network.NetworkMonitor
import com.wallet.core.util.LoadingState
import com.wallet.domain.repository.AssetRepository
import com.wallet.domain.repository.WalletRepository
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
    private val walletRepository: WalletRepository,
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
