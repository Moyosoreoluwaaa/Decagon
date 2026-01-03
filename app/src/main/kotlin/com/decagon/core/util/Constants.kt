package com.decagon.core.util

/**
 * App-wide constants for Octane Wallet.
 */
object Constants {

    // Network
    const val DEFAULT_RPC_TIMEOUT_MS = 30_000L
    const val RPC_HEALTH_CHECK_INTERVAL_MS = 10_000L
    const val MAX_RPC_RETRIES = 3

    // Transaction
    const val DEFAULT_PRIORITY_FEE_MICROLAMPORTS = 10_000L
    const val MAX_TRANSACTION_SIZE_BYTES = 1232
    const val DEFAULT_SLIPPAGE_PERCENT = 1.0

    // Security
    const val BIOMETRIC_AUTO_APPROVE_THRESHOLD_USD = 100.0
    const val MAX_FAILED_AUTH_ATTEMPTS = 3
    const val AUTH_LOCKOUT_DURATION_MS = 300_000L // 5 minutes

    // UI
    const val PRICE_UPDATE_THROTTLE_MS = 1000L
    const val PORTFOLIO_REFRESH_INTERVAL_MS = 30_000L
    const val TRANSACTION_HISTORY_PAGE_SIZE = 50

    // Cache
    const val PRICE_CACHE_TTL_MS = 60_000L // 1 minute
    const val TRANSACTION_CACHE_TTL_MS = 300_000L // 5 minutes
    const val NFT_METADATA_CACHE_TTL_MS = 3_600_000L // 1 hour

    // API Keys (store in BuildConfig in production)
    const val HELIUS_API_KEY_PLACEHOLDER = "YOUR_HELIUS_KEY"
    const val QUICKNODE_API_KEY_PLACEHOLDER = "YOUR_QUICKNODE_KEY"
}