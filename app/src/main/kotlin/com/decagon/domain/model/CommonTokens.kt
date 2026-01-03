package com.decagon.domain.model

/**
 * Pre-defined popular tokens for quick access.
 * Cached locally to avoid repeated API calls.
 * 
 * ✅ UPDATED: Added logoURI for proper image caching
 */
object CommonTokens {
    val SOL = TokenInfo(
        address = "So11111111111111111111111111111111111111112",
        name = "Solana",
        symbol = "SOL",
        decimals = 9,
        logoURI = "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/So11111111111111111111111111111111111111112/logo.png",
        isVerified = true,
        isStrict = true
    )

    val USDC = TokenInfo(
        address = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v",
        name = "USD Coin",
        symbol = "USDC",
        decimals = 6,
        logoURI = "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v/logo.png",
        isVerified = true,
        isStrict = true
    )

    val USDT = TokenInfo(
        address = "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB",
        name = "Tether USD",
        symbol = "USDT",
        decimals = 6,
        logoURI = "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB/logo.png",
        isVerified = true,
        isStrict = true
    )

    val ALL = listOf(SOL, USDC, USDT)
}