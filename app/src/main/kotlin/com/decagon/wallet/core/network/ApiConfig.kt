package com.octane.wallet.core.network
/**
 * API Configuration - Centralized base URLs
 * ⚠ CRITICAL: All Ktorfit base URLs MUST end with "/"
 */
object ApiConfig {
    // ===== SOLANA RPC ENDPOINTS =====
    object Solana {
        const val MAINNET_PUBLIC = "https://api.mainnet-beta.solana.com/"
        const val MAINNET_ANKR = "https://rpc.ankr.com/solana/"
        const val DEVNET = "https://api.devnet.solana.com/"
        const val TESTNET = "https://api.testnet.solana.com/"
        fun alchemyUrl(apiKey: String) = "https://solana-mainnet.g.alchemy.com/v2/$apiKey/"
        fun heliusUrl(apiKey: String) = "https://rpc.helius.xyz/?api-key=$apiKey/"
    }
    // ===== PRICE & MARKET DATA =====
    const val COINGECKO_BASE_URL = "https://api.coingecko.com/api/v3/"
    // ===== DEFI PROTOCOLS =====
    const val DEFILLAMA_BASE_URL = "https://api.llama.fi/"
    // ===== DEX AGGREGATORS =====
    const val JUPITER_BASE_URL = "https://quote-api.jup.ag/v6/"
    // ===== PERPETUALS & DERIVATIVES =====
    const val DRIFT_URL = "https://data.api.drift.trade/"
    // ===== TOKEN LOGOS (CDN FALLBACKS) =====
    object Logos {
        const val COINGECKO_CDN = "https://assets.coingecko.com/coins/images/"
        const val GITHUB_TOKEN_LIST = "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/"
    }
    // ===== RATE LIMITING =====
    object RateLimits {
        const val COINGECKO_FREE_RPM = 50
        const val DEFILLAMA_RPM = 300
        const val JUPITER_RPM = 600
        const val DRIFT_RPM = 300
    }
    // ===== TIMEOUTS (milliseconds) =====
    object Timeouts {
        const val RPC_CONNECT = 30_000L
        const val RPC_READ = 30_000L
        const val API_CONNECT = 15_000L
        const val API_READ = 15_000L
    }
}