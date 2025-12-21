package com.wallet.core.blockchain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Estimates Solana transaction priority fees.
 * Provides low/medium/high fee options for user selection.
 */
class GasFeeEstimator {

    /**
     * Estimate priority fees for a transaction.
     * @param transactionSize Estimated transaction size in bytes
     * @return Flow of fee estimates
     */
    fun estimatePriorityFee(
        transactionSize: Int = 300 // Average transaction size
    ): Flow<PriorityFeeEstimate> = flow {
        // TODO: Query recent priority fees from RPC
        // For now, return static estimates

        val estimate = PriorityFeeEstimate(
            low = PriorityFee(
                microLamports = 5_000,
                estimatedConfirmationTime = "~30-60s",
                successRate = 0.50
            ),
            medium = PriorityFee(
                microLamports = 10_000,
                estimatedConfirmationTime = "~15-30s",
                successRate = 0.75
            ),
            high = PriorityFee(
                microLamports = 50_000,
                estimatedConfirmationTime = "~5-15s",
                successRate = 0.95
            ),
            custom = null
        )

        emit(estimate)
    }

    /**
     * Calculate total transaction cost.
     * @param priorityFee Selected priority fee
     * @param baseFee Base transaction fee (typically 5000 lamports)
     */
    fun calculateTotalFee(
        priorityFee: Long,
        baseFee: Long = 5_000
    ): Long {
        return baseFee + (priorityFee / 1_000) // Convert microLamports
    }

    /**
     * Parse recent priority fees from RPC response.
     */
    private fun parseRecentFees(fees: List<Long>): PriorityFeeEstimate {
        val sorted = fees.sorted()

        return PriorityFeeEstimate(
            low = PriorityFee(
                microLamports = sorted[sorted.size * 25 / 100],
                estimatedConfirmationTime = "~30-60s",
                successRate = 0.50
            ),
            medium = PriorityFee(
                microLamports = sorted[sorted.size * 50 / 100],
                estimatedConfirmationTime = "~15-30s",
                successRate = 0.75
            ),
            high = PriorityFee(
                microLamports = sorted[sorted.size * 75 / 100],
                estimatedConfirmationTime = "~5-15s",
                successRate = 0.95
            ),
            custom = null
        )
    }
}

/**
 * Priority fee estimate with multiple tiers.
 */
data class PriorityFeeEstimate(
    val low: PriorityFee,
    val medium: PriorityFee,
    val high: PriorityFee,
    val custom: PriorityFee?
)

/**
 * Individual priority fee tier.
 */
data class PriorityFee(
    val microLamports: Long,
    val estimatedConfirmationTime: String,
    val successRate: Double // 0.0 - 1.0
) {
    val solAmount: Double
        get() = microLamports / 1_000_000_000.0
}