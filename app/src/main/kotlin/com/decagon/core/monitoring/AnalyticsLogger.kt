package com.wallet.core.monitoring

/**
 * Analytics abstraction for Octane Wallet.
 * Tracks user actions, performance, and errors.
 */
interface AnalyticsLogger {

    /**
     * Log screen view.
     */
    fun logScreenView(
        screenName: String,
        screenClass: String? = null
    )

    /**
     * Log user event.
     */
    fun logEvent(
        eventName: String,
        params: Map<String, Any> = emptyMap()
    )

    /**
     * Log error/exception.
     */
    fun logError(
        throwable: Throwable,
        context: String? = null,
        fatal: Boolean = false
    )

    /**
     * Set user ID for tracking.
     */
    fun setUserId(userId: String?)

    /**
     * Set user property.
     */
    fun setUserProperty(
        propertyName: String,
        value: String
    )

    /**
     * Log performance metric.
     */
    fun logPerformance(
        metricName: String,
        durationMillis: Long,
        attributes: Map<String, String> = emptyMap()
    )
}

/**
 * Standard event names for Octane.
 */
object OctaneEvents {
    // Wallet Lifecycle
    const val WALLET_CREATED = "wallet_created"
    const val WALLET_IMPORTED = "wallet_imported"
    const val WALLET_DELETED = "wallet_deleted"

    // Transactions
    const val SEND_INITIATED = "send_sol_initiated"
    const val SEND_COMPLETED = "send_sol_completed"
    const val SEND_FAILED = "send_sol_failed"

    const val SWAP_INITIATED = "swap_initiated"
    const val SWAP_COMPLETED = "swap_completed"
    const val SWAP_FAILED = "swap_failed"

    const val NFT_MINTED = "nft_minted"
    const val NFT_SENT = "nft_sent"

    // Security
    const val BIOMETRIC_ENABLED = "biometric_enabled"
    const val BIOMETRIC_USED = "biometric_auth_used"
    const val MALICIOUS_TX_BLOCKED = "malicious_transaction_blocked"

    // Performance
    const val RPC_LATENCY = "rpc_latency_ms"
    const val TX_CONFIRMATION_TIME = "tx_confirmation_seconds"
    const val SWAP_EXECUTION_TIME = "swap_execution_seconds"

    // User Actions
    const val SCREEN_VIEWED = "screen_viewed"
    const val BUTTON_CLICKED = "button_clicked"
    const val SEARCH_PERFORMED = "search_performed"

    // Errors
    const val ERROR_OCCURRED = "error_occurred"
    const val API_ERROR = "api_error"
    const val RPC_ERROR = "rpc_error"
}

/**
 * Standard parameter names.
 */
object OctaneParams {
    const val SCREEN_NAME = "screen_name"
    const val BUTTON_ID = "button_id"
    const val WALLET_ID = "wallet_id"
    const val TOKEN_SYMBOL = "token_symbol"
    const val AMOUNT_USD = "amount_usd"
    const val ERROR_MESSAGE = "error_message"
    const val RPC_ENDPOINT = "rpc_endpoint"
    const val NETWORK = "network"
    const val SUCCESS = "success"
}
