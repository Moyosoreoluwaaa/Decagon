package com.octane.wallet.domain.models

/**
 * Domain model for decentralized applications (dApps).
 * Used by Discover screen "Lists" tab.
 */
data class DApp(
    val id: String,                    // Unique identifier
    val name: String,                  // dApp name (e.g., "Uniswap", "Magic Eden")
    val description: String,           // Short description
    val logoUrl: String?,              // Logo URL
    val category: DAppCategory,        // Category (DeFi, NFT, Gaming, etc.)
    val url: String,                   // Official website URL
    val tvl: Double?,                  // Total Value Locked (for DeFi)
    val volume24h: Double?,            // 24h volume
    val users24h: Int?,                // 24h active users
    val isVerified: Boolean = true,    // Verified/audited dApp
    val chains: List<String> = listOf("Solana"), // Supported chains
    val rating: Double = 0.0,          // User rating (0-5)
    val tags: List<String> = emptyList() // Additional tags
) {
    /**
     * Formatted TVL for UI.
     */
    val formattedTvl: String?
        get() = tvl?.let {
            when {
                it >= 1_000_000_000 -> "$%.2fB".format(it / 1_000_000_000)
                it >= 1_000_000 -> "$%.2fM".format(it / 1_000_000)
                else -> "$%.2fK".format(it / 1_000)
            }
        }

    /**
     * Formatted volume for UI.
     */
    val formattedVolume: String?
        get() = volume24h?.let {
            when {
                it >= 1_000_000_000 -> "$%.2fB".format(it / 1_000_000_000)
                it >= 1_000_000 -> "$%.2fM".format(it / 1_000_000)
                else -> "$%.2fK".format(it / 1_000)
            }
        }
}

/**
 * dApp categories for filtering.
 */
enum class DAppCategory {
    DEFI,       // Decentralized Finance (DEXs, lending)
    NFT,        // NFT marketplaces
    GAMING,     // Blockchain games
    SOCIAL,     // Social platforms
    BRIDGE,     // Cross-chain bridges
    WALLET,     // Wallet services
    TOOLS,      // Developer tools
    OTHER
}