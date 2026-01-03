package com.decagon.domain.usecase.staking

import com.decagon.core.util.LoadingState
import com.decagon.domain.model.StakingPosition
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.domain.repository.StakingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val walletRepository: DecagonWalletRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
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