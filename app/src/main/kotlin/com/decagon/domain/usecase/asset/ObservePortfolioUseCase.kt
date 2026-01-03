package com.decagon.domain.usecase.asset

import com.decagon.core.util.LoadingState
import com.decagon.domain.model.Asset
import com.decagon.domain.repository.AssetRepository
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Observes portfolio assets for a wallet.
 *
 * Features:
 * - Real-time updates via Flow
 * - Sorted by USD value (highest first)
 * - Filters hidden assets
 * - Calculates total portfolio value
 * - Returns empty state when no wallet exists (instead of error)
 */
class ObservePortfolioUseCase(
    private val assetRepository: AssetRepository,
    private val walletRepository: DecagonWalletRepository
) {
    operator fun invoke(): Flow<LoadingState<PortfolioState>> {
        return walletRepository.observeActiveWallet()
            .combine(assetRepository.observeAssets()) { wallet, assets ->
                // ✅ FIX: Return empty portfolio instead of error when no wallet
                if (wallet == null) {
                    return@combine LoadingState.Success(
                        PortfolioState(
                            assets = emptyList(),
                            totalValueUsd = 0.0,
                            change24hPercent = 0.0
                        )
                    )
                }

                val visibleAssets = assets.filter { !it.isHidden }
                val totalValue = visibleAssets.sumOf { it.valueUsd ?: 0.0 }
                val total24hChange = calculatePortfolioChange(visibleAssets)

                LoadingState.Success(
                    PortfolioState(
                        assets = visibleAssets.sortedByDescending { it.valueUsd },
                        totalValueUsd = totalValue,
                        change24hPercent = total24hChange
                    )
                )
            }
    }

    private fun calculatePortfolioChange(assets: List<Asset>): Double {
        val totalValue = assets.sumOf { it.valueUsd ?: 0.0 }
        if (totalValue == 0.0) return 0.0

        val totalChange = assets.sumOf { asset ->
            val value = asset.valueUsd ?: 0.0
            val change = asset.priceChange24h ?: 0.0
            value * (change / 100.0)
        }

        return (totalChange / totalValue) * 100.0
    }
}

data class PortfolioState(
    val assets: List<Asset>,
    val totalValueUsd: Double,
    val change24hPercent: Double
)