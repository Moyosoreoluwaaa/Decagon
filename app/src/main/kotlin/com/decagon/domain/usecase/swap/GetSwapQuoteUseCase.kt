package com.decagon.domain.usecase.swap

import com.decagon.domain.model.CommonTokens
import com.decagon.domain.model.SecurityWarning
import com.decagon.domain.model.SwapOrder
import com.decagon.domain.model.TokenBalance
import com.decagon.domain.model.TokenInfo
import com.decagon.domain.model.WarningSeverity
import com.decagon.domain.repository.SwapRepository
import timber.log.Timber
import kotlin.math.pow

class GetSwapQuoteUseCase(
    private val repository: SwapRepository
) {
    suspend operator fun invoke(
        inputToken: TokenInfo,
        outputToken: TokenInfo,
        inputAmount: Double,
        userPublicKey: String,
        slippageTolerance: Double = 0.5
    ): Result<SwapOrder> {

        // Validate inputs first
        if (inputAmount <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        }

        if (inputToken.address == outputToken.address) {
            return Result.failure(IllegalArgumentException("Cannot swap the same token"))
        }

        // Convert UI amount to smallest unit
        val amountInSmallestUnit = (inputAmount * 10.0.pow(inputToken.decimals)).toLong()

        if (amountInSmallestUnit <= 0) {
            return Result.failure(IllegalArgumentException("Amount too small"))
        }

        // Convert slippage % to basis points
        val slippageBps = (slippageTolerance * 100).toInt()

        Timber.d("Getting quote: ${inputToken.symbol} -> ${outputToken.symbol}, amount: $inputAmount ($amountInSmallestUnit smallest units)")

        return repository.getSwapQuote(
            inputMint = inputToken.address,
            outputMint = outputToken.address,
            amount = amountInSmallestUnit,
            userPublicKey = userPublicKey,
            slippageBps = if (slippageBps > 0) slippageBps else null
        )
//            .mapCatching { order ->
//            // Validate the response
//            when {
//                order.transaction.isBlank() -> {
//                    throw IllegalStateException(
//                        "Jupiter API returned empty transaction. " +
//                                "This may indicate: insufficient liquidity, invalid token pair, " +
//                                "or the tokens are not available on Jupiter."
//                    )
//                }
//                order.outAmount.toDoubleOrNull() == null || order.outAmount.toDouble() <= 0 -> {
//                    throw IllegalStateException(
//                        "Invalid output amount received from Jupiter API. " +
//                                "The token pair may not have sufficient liquidity."
//                    )
//                }
//                else -> order
//            }
//        }
    }
}

class SearchTokensUseCase(
    private val repository: SwapRepository
) {
    suspend operator fun invoke(
        query: String,
        limit: Int = 20
    ): Result<List<TokenInfo>> {

        if (query.isBlank()) {
            return Result.success(CommonTokens.ALL)
        }

        return repository.searchTokens(query, limit)
    }
}

class GetTokenBalancesUseCase(
    private val repository: SwapRepository
) {
    suspend operator fun invoke(
        publicKey: String
    ): Result<List<TokenBalance>> {
        return repository.getTokenBalances(publicKey)
    }
}

class ValidateTokenSecurityUseCase(
    private val repository: SwapRepository
) {
    suspend operator fun invoke(
        tokenMint: String
    ): Result<List<SecurityWarning>> {
        return repository.getTokenSecurity(listOf(tokenMint))
            .map { shieldResponse ->
                shieldResponse[tokenMint] ?: emptyList()
            }
    }

    fun shouldBlockSwap(warnings: List<SecurityWarning>): Boolean {
        return warnings.any { it.severity == WarningSeverity.CRITICAL }
    }

    fun shouldWarnUser(warnings: List<SecurityWarning>): Boolean {
        return warnings.any {
            it.severity == WarningSeverity.WARNING ||
                    it.severity == WarningSeverity.CRITICAL
        }
    }
}

class GetSwapHistoryUseCase(
    private val repository: SwapRepository
) {
    operator fun invoke(walletId: String) = repository.getSwapHistory(walletId)
}