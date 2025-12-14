package com.decagon.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Jupiter Ultra API v1 DTOs
 * Base URL: https://lite-api.jup.ag/ultra/v1/
 * Migrate to: https://api.jup.ag/ultra/v1/ after Dec 31, 2025
 */

// ==================== ORDER ENDPOINT ====================

@Serializable
data class JupiterOrderRequest(
    val inputMint: String,
    val outputMint: String,
    val amount: Long,
    val taker: String,
    val slippageBps: Int? = null
)

@Serializable
data class JupiterOrderResponse(
    val mode: String,                    // "ultra" or "exact"
    val inputMint: String,
    val outputMint: String,
    val inAmount: String,
    val outAmount: String,
    val otherAmountThreshold: String,
    val swapMode: String,                // "ExactIn" or "ExactOut"
    val slippageBps: Int,
    val priceImpactPct: String,
    val routePlan: List<RoutePlanDto>,
    val feeBps: Int,                     // Jupiter's fee (10 or 5 bps)
    val transaction: String,             // Base64 encoded unsigned transaction
    val requestId: String                // Required for execute endpoint
)

@Serializable
data class RoutePlanDto(
    val swapInfo: SwapInfoDto,
    val percent: Int
)

// In JupiterOrderRequest.kt, update SwapInfoDto:
@Serializable
data class SwapInfoDto(
    val ammKey: String,
    val label: String,
    val inputMint: String,
    val outputMint: String,
    val inAmount: String,
    val outAmount: String,
    val feeAmount: String = "0",      // Make optional with default
    val feeMint: String = ""          // Make optional with default
)

// ==================== EXECUTE ENDPOINT ====================

@Serializable
data class JupiterExecuteRequest(
    val signedTransaction: String,       // Base64 encoded signed transaction
    val requestId: String                // From order response
)

@Serializable
data class JupiterExecuteResponse(
    val status: String,                  // "Success", "Failed", "Pending"
    val signature: String?,              // May be null if failed
    val error: String? = null,           // Error description if failed
    val code: Int? = null,               // Error code if failed
    val slot: String? = null
)

// ==================== BALANCES ENDPOINT ====================

@Serializable
data class JupiterBalancesResponse(
    val holdings: List<TokenHoldingDto>
)

@Serializable
data class TokenHoldingDto(
    val mint: String,
    val amount: String,                  // Raw amount in smallest unit
    val decimals: Int,
    val uiAmount: Double,                // Human-readable amount
    val tokenAccount: String,            // SPL token account address
    val isNative: Boolean                // True for SOL
)

// ==================== SEARCH ENDPOINT ====================

@Serializable
data class JupiterSearchResponse(
    val results: List<TokenInfoDto>
)

@Serializable
data class TokenInfoDto(
    val address: String,                 // Mint address
    val name: String,
    val symbol: String,
    val decimals: Int,
    val logoURI: String? = null,
    val tags: List<String> = emptyList(), // ["verified", "strict", "community"]
    val dailyVolume: Double? = null,
    val freezeAuthority: String? = null, // Null = immutable
    val mintAuthority: String? = null,   // Null = fixed supply
    val extensions: ExtensionsDto? = null
)

@Serializable
data class ExtensionsDto(
    val coingeckoId: String? = null
)

// ==================== SHIELD API ====================

@Serializable
data class JupiterShieldResponse(
    val warnings: Map<String, List<TokenWarningDto>>
)

@Serializable
data class TokenWarningDto(
    val type: String,                    // Warning type identifier
    val message: String,
    val severity: String                 // "INFO", "WARNING", "CRITICAL"
)

// ==================== ERROR RESPONSE ====================

@Serializable
data class JupiterErrorResponse(
    val error: String,
    val code: Int
)