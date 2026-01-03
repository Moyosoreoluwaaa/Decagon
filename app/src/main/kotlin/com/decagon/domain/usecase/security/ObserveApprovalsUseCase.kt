package com.decagon.domain.usecase.security

import com.decagon.core.util.LoadingState
import com.decagon.domain.model.Approval
import com.decagon.domain.repository.ApprovalRepository
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Observes all token approvals for the active wallet.
 * Used in Security Dashboard (V1.8) to audit spend allowances.
 */
class ObserveApprovalsUseCase(
    private val approvalRepository: ApprovalRepository,
    private val walletRepository: DecagonWalletRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<LoadingState<List<Approval>>> {
        return walletRepository.observeActiveWallet()
            .flatMapLatest { wallet ->
                if (wallet == null) {
                    flowOf(LoadingState.Error(IllegalStateException("No active wallet")))
                } else {
                    approvalRepository.observeApprovals(wallet.id)
                        .map<List<Approval>, LoadingState<List<Approval>>> { approvals ->
                            LoadingState.Success(approvals.filter { !it.isRevoked })
                        }
                }
            }
    }
}