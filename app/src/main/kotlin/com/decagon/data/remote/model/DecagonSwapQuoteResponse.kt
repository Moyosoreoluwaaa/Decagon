package com.decagon.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Jupiter V6 Quote Response
 * 
 * Represents a swap quote from Jupiter Aggregator.
 * This response contains routing information, price impact,
 * and expected output amounts for a token swap.
 * 
 * Official API: https://station.jup.ag/docs/apis/swap-api
 */
@Serializable
data class DecagonSwapQuoteResponse(
    @SerialName("inputMint")
    val inputMint: String,
    
    @SerialName("inAmount")
    val inAmount: String,
    
    @SerialName("outputMint")
    val outputMint: String,
    
    @SerialName("outAmount")
    val outAmount: String,
    
    @SerialName("otherAmountThreshold")
    val otherAmountThreshold: String,
    
    @SerialName("swapMode")
    val swapMode: String,
    
    @SerialName("slippageBps")
    val slippageBps: Int,
    
    @SerialName("platformFee")
    val platformFee: DecagonPlatformFee? = null,
    
    @SerialName("priceImpactPct")
    val priceImpactPct: String,
    
    @SerialName("routePlan")
    val routePlan: List<DecagonRoutePlan>,
    
    @SerialName("contextSlot")
    val contextSlot: Long? = null,
    
    @SerialName("timeTaken")
    val timeTaken: Double? = null
)

/**
 * Platform fee information for referral programs
 */
@Serializable
data class DecagonPlatformFee(
    @SerialName("amount")
    val amount: String,
    
    @SerialName("feeBps")
    val feeBps: Int
)

/**
 * Route plan step
 * 
 * Jupiter may split swaps across multiple DEXs
 * for optimal pricing. Each route plan represents
 * one step in the multi-hop swap.
 */
@Serializable
data class DecagonRoutePlan(
    @SerialName("swapInfo")
    val swapInfo: DecagonSwapInfo,
    
    @SerialName("percent")
    val percent: Int
)

/**
 * Detailed swap information for a route step
 */
@Serializable
data class DecagonSwapInfo(
    @SerialName("ammKey")
    val ammKey: String,
    
    @SerialName("label")
    val label: String? = null,
    
    @SerialName("inputMint")
    val inputMint: String,
    
    @SerialName("outputMint")
    val outputMint: String,
    
    @SerialName("inAmount")
    val inAmount: String,
    
    @SerialName("outAmount")
    val outAmount: String,
    
    @SerialName("feeAmount")
    val feeAmount: String,
    
    @SerialName("feeMint")
    val feeMint: String
)

/**
 * Jupiter V6 Swap Request
 * 
 * Request body sent to Jupiter to get a serialized
 * swap transaction ready for signing and submission.
 */
@Serializable
data class DecagonSwapTransactionRequest(
    @SerialName("userPublicKey")
    val userPublicKey: String,
    
    @SerialName("quoteResponse")
    val quoteResponse: DecagonSwapQuoteResponse,
    
    @SerialName("wrapAndUnwrapSol")
    val wrapAndUnwrapSol: Boolean = true,
    
    @SerialName("useSharedAccounts")
    val useSharedAccounts: Boolean = true,
    
    @SerialName("feeAccount")
    val feeAccount: String? = null,
    
    @SerialName("trackingAccount")
    val trackingAccount: String? = null,
    
    @SerialName("computeUnitPriceMicroLamports")
    val computeUnitPriceMicroLamports: String? = null,
    
    @SerialName("prioritizationFeeLamports")
    val prioritizationFeeLamports: String? = null,
    
    @SerialName("asLegacyTransaction")
    val asLegacyTransaction: Boolean = false,
    
    @SerialName("useTokenLedger")
    val useTokenLedger: Boolean = false,
    
    @SerialName("destinationTokenAccount")
    val destinationTokenAccount: String? = null,
    
    @SerialName("dynamicComputeUnitLimit")
    val dynamicComputeUnitLimit: Boolean = true,
    
    @SerialName("skipUserAccountsRpcCalls")
    val skipUserAccountsRpcCalls: Boolean = false
)

/**
 * Jupiter V6 Swap Response
 * 
 * Contains the serialized transaction ready for signing.
 * The transaction is Base64 encoded and must be deserialized,
 * signed with the user's private key, then submitted to Solana.
 */
@Serializable
data class DecagonSwapTransactionResponse(
    @SerialName("swapTransaction")
    val swapTransaction: String,
    
    @SerialName("lastValidBlockHeight")
    val lastValidBlockHeight: Long,
    
    @SerialName("prioritizationFeeLamports")
    val prioritizationFeeLamports: Long? = null
)

/**
 * Token metadata for UI display
 * 
 * This model represents SPL token information
 * fetched from Jupiter's token list API or stored locally.
 */
@Serializable
data class DecagonTokenInfo(
    @SerialName("address")
    val address: String,
    
    @SerialName("chainId")
    val chainId: Int,
    
    @SerialName("decimals")
    val decimals: Int,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("symbol")
    val symbol: String,
    
    @SerialName("logoURI")
    val logoURI: String? = null,
    
    @SerialName("tags")
    val tags: List<String> = emptyList(),
    
    @SerialName("extensions")
    val extensions: DecagonTokenExtensions? = null
) {
    /**
     * Predefined popular tokens for quick access
     */
    companion object {
        val SOL = DecagonTokenInfo(
            address = "So11111111111111111111111111111111111111112",
            chainId = 101,
            decimals = 9,
            name = "Solana",
            symbol = "SOL",
            logoURI = "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/So11111111111111111111111111111111111111112/logo.png",
            tags = listOf("verified", "native")
        )
        
        val USDC = DecagonTokenInfo(
            address = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v",
            chainId = 101,
            decimals = 6,
            name = "USD Coin",
            symbol = "USDC",
            logoURI = "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v/logo.png",
            tags = listOf("verified", "stablecoin")
        )
        
        val USDT = DecagonTokenInfo(
            address = "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB",
            chainId = 101,
            decimals = 6,
            name = "Tether USD",
            symbol = "USDT",
            logoURI = "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB/logo.png",
            tags = listOf("verified", "stablecoin")
        )
        
        val USDS = DecagonTokenInfo(
            address = "USDSwr9ApdHk5bvJKMjzff41FfuX8bSxdKcR81vTwcA",
            chainId = 101,
            decimals = 6,
            name = "USDS",
            symbol = "USDS",
            logoURI = null,
            tags = listOf("verified", "stablecoin")
        )
        
        val JUP = DecagonTokenInfo(
            address = "JUPyiwrYJFskUPiHa7hkeR8VUtAeFoSYbKedZNsDvCN",
            chainId = 101,
            decimals = 6,
            name = "Jupiter",
            symbol = "JUP",
            logoURI = "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/JUPyiwrYJFskUPiHa7hkeR8VUtAeFoSYbKedZNsDvCN/logo.png",
            tags = listOf("verified")
        )
        
        val BONK = DecagonTokenInfo(
            address = "DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263",
            chainId = 101,
            decimals = 5,
            name = "Bonk",
            symbol = "BONK",
            logoURI = "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263/logo.svg",
            tags = listOf("verified", "community")
        )
        
        val WIF = DecagonTokenInfo(
            address = "EKpQGSJtjMFqKZ9KQanSqYXRcF8fBopzLHYxdM65zcjm",
            chainId = 101,
            decimals = 6,
            name = "dogwifhat",
            symbol = "WIF",
            logoURI = "https://bafkreibk3covs5ltyqxa272uodhculbr6kea6betidfwy3ajsav2vjzyum.ipfs.nftstorage.link",
            tags = listOf("verified", "community")
        )
        
        /**
         * Returns default token list for swap UI
         */
        fun getDefaultTokenList(): List<DecagonTokenInfo> {
            return listOf(SOL, USDC, USDT, USDS, JUP, BONK, WIF)
        }
    }
}

/**
 * Additional token metadata from token extensions
 */
@Serializable
data class DecagonTokenExtensions(
    @SerialName("coingeckoId")
    val coingeckoId: String? = null,
    
    @SerialName("website")
    val website: String? = null,
    
    @SerialName("twitter")
    val twitter: String? = null
)