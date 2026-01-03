package com.decagon.domain.usecase.wallet

import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe the currently active wallet.
 * Used by DAppBrowserViewModel to sign transactions and manage connections.
 */
class ObserveActiveWalletUseCase(
    private val walletRepository: DecagonWalletRepository
) {
    operator fun invoke(): Flow<DecagonWallet?> {
        return walletRepository.observeActiveWallet()
    }
}