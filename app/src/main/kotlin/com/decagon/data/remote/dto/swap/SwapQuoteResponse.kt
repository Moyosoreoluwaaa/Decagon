package com.decagon.data.remote.dto.swap

import kotlinx.serialization.Serializable

@Serializable
data class SwapQuoteResponse(
    val inputMint: String,
    val inAmount: String,
    val outputMint: String,
    val outAmount: String,
    val otherAmountThreshold: String,
    val swapMode: String,
    val slippageBps: Int,
    val priceImpactPct: String,
    val routePlan: List<RoutePlan>
)

@Serializable
data class RoutePlan(
    val swapInfo: SwapInfo,
    val percent: Int
)

@Serializable
data class SwapInfo(
    val ammKey: String,
    val label: String?,
    val inputMint: String,
    val outputMint: String,
    val inAmount: String,
    val outAmount: String,
    val feeAmount: String,
    val feeMint: String
)

@Serializable
data class SwapRequest(
    val userPublicKey: String,
    val quoteResponse: SwapQuoteResponse,
    val wrapUnwrapSOL: Boolean = true,
    val prioritizationFeeLamports: Long? = null,
    val asLegacyTransaction: Boolean = false,
    val dynamicComputeUnitLimit: Boolean = true
)

@Serializable
data class SwapTransactionResponse(
    val swapTransaction: String,
    val lastValidBlockHeight: Long
)