package com.octane.wallet.domain.usecases.wallet

import com.octane.wallet.domain.models.Wallet
import com.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe the currently active wallet.
 * Used by DAppBrowserViewModel to sign transactions and manage connections.
 */
class ObserveActiveWalletUseCase(
    private val walletRepository: WalletRepository
) {
    operator fun invoke(): Flow<Wallet?> {
        return walletRepository.observeActiveWallet()
    }
}