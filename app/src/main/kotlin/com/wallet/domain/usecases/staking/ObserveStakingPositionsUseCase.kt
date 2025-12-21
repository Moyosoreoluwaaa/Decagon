package com.octane.wallet.domain.usecases.staking

import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.StakingPosition
import com.wallet.domain.repository.StakingRepository
import com.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Observes all staking positions for the active wallet.
 * Returns a reactive Flow that updates when positions change.
 */
class ObserveStakingPositionsUseCase(
    private val stakingRepository: StakingRepository,
    private val walletRepository: WalletRepository
) {
    operator fun invoke(): Flow<LoadingState<List<StakingPosition>>> {
        return walletRepository.observeActiveWallet()
            .flatMapLatest { wallet ->
                if (wallet == null) {
                    flowOf(LoadingState.Error(IllegalStateException("No active wallet")))
                } else {
                    stakingRepository.observeStakingPositions(wallet.id)
                        .map<List<StakingPosition>, LoadingState<List<StakingPosition>>> { positions ->
                            LoadingState.Success(positions)
                        }
                }
            }
    }
}