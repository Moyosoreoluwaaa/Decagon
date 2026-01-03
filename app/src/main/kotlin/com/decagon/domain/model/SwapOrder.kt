package com.decagon.domain.model

/**
 * Domain models for swap feature.
 * Pure Kotlin with no Android dependencies.
 */

// ==================== SWAP ORDER ====================

data class SwapOrder(
    val inputMint: String,
    val outputMint: String,
    val inAmount: String,
    val outAmount: String,
    val slippageBps: Int,
    val priceImpactPct: Double,
    val routePlan: List<RoutePlan>,
    val feeBps: Int,
    val transaction: String,
    val requestId: String,
    val securityWarnings: Map<String, List<SecurityWarning>>,
    val otherAmountThreshold: String = outAmount, // Minimum received
    val expectedOutputAmount: String = outAmount,
    val inputToken: TokenInfo = CommonTokens.SOL, // Will be set by ViewModel
    val outputToken: TokenInfo = CommonTokens.USDC, // Will be set by ViewModel
    val fees: List<SwapFee> = emptyList() // Calculated fees
)

data class SwapFee(
    val label: String,
    val amount: String,
    val token: TokenInfo
)

data class RoutePlan(
    val ammLabel: String,                   // "Metis", "Jupiter Z", etc.
    val inputMint: String,
    val outputMint: String,
    val inAmount: String,
    val outAmount: String,
    val feeAmount: String,
    val percent: Int
)

// ==================== TOKEN INFO ====================

data class TokenInfo(
    val address: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val logoURI: String? = null,
    val tags: List<String> = emptyList(),
    val isVerified: Boolean = false,
    val isStrict: Boolean = false,
    val dailyVolume: Double? = null,
    val hasFreezableAuthority: Boolean = false,
    val hasMintableAuthority: Boolean = false,
    val coingeckoId: String? = null
)

// ==================== TOKEN BALANCE ====================

data class TokenBalance(
    val mint: String,
    val amount: String,
    val decimals: Int,
    val uiAmount: Double,
    val tokenAccount: String,
    val isNative: Boolean,
    val symbol: String = "???",
    val name: String = "Unknown",
    val tokenInfo: TokenInfo? = null,
    // Additional properties for UI display
    val logoUrl: String? = tokenInfo?.logoURI,
    val valueUsd: Double = 0.0,
    val change24h: Double? = null
)

// ==================== PORTFOLIO HISTORY ====================

data class PortfolioHistoryPoint(
    val timestamp: Long,
    val totalValueUsd: Double,
    val tokens: Map<String, Double> = emptyMap() // token mint -> value
)

// ==================== SECURITY WARNING ====================

data class SecurityWarning(
    val type: WarningType,
    val message: String,
    val severity: WarningSeverity,
    val category: String = type.name.replace("_", " ").lowercase().capitalize(),
    val description: String = message
)

enum class WarningType {
    NOT_VERIFIED,
    LOW_ORGANIC_ACTIVITY,
    NEW_LISTING,
    HAS_FREEZE_AUTHORITY,
    HAS_MINT_AUTHORITY,
    POTENTIAL_RUG_PULL,
    IMPERSONATOR,
    UNKNOWN;                                // Fallback for new types

    companion object {
        fun fromString(value: String): WarningType {
            return entries.find { it.name == value } ?: UNKNOWN
        }
    }
}

enum class WarningSeverity {
    INFO,
    LOW,
    MEDIUM,
    HIGH,
    WARNING,
    CRITICAL;

    companion object {
        fun fromString(value: String): WarningSeverity {
            return when (value.uppercase()) {
                "INFO" -> INFO
                "LOW" -> LOW
                "MEDIUM" -> MEDIUM
                "HIGH" -> HIGH
                "WARNING" -> WARNING
                "CRITICAL" -> CRITICAL
                else -> WARNING
            }
        }
    }
}

// ==================== SWAP HISTORY ====================

data class SwapHistory(
    val id: String,
    val walletId: String,
    val inputMint: String,
    val outputMint: String,
    val inputAmount: Double,
    val outputAmount: Double,
    val inputSymbol: String,
    val outputSymbol: String,
    val signature: String?,
    val status: SwapStatus,
    val slippageBps: Int,
    val priceImpactPct: Double,
    val feeBps: Int,
    val priorityFee: Long,
    val timestamp: Long,
    val errorMessage: String? = null
)

enum class SwapStatus {
    PENDING,
    CONFIRMED,
    FAILED
}