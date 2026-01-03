package com.decagon.domain.usecase.wallet

import com.decagon.core.util.LoadingState
import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Observes all wallets reactively.
 *
 * Returns:
 * - Loading state during initial fetch
 * - Success with list of wallets
 * - Error if database access fails
 */
class ObserveWalletsUseCase(
    private val walletRepository: DecagonWalletRepository
) {
    operator fun invoke(): Flow<LoadingState<List<DecagonWallet>>> {
        return walletRepository.observeAllWallets()
            .map<List<DecagonWallet>, LoadingState<List<DecagonWallet>>> { wallets ->
                LoadingState.Success(wallets)
            }
            .catch { e ->
                emit(LoadingState.Error(e) as LoadingState<List<DecagonWallet>>)
            }
    }
}
