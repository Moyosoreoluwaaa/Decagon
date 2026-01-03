package com.decagon.data.provider

import com.decagon.core.config.OnRampProviderType
import com.decagon.domain.model.DecagonWallet

/**
 * On-ramp provider abstraction.
 * 
 * Each provider implements this interface to provide:
 * - Widget URL generation
 * - Configuration parameters
 * - Provider-specific setup
 * 
 * Benefits:
 * - Easy provider swapping (change one line in DI module)
 * - No UI changes required (same WebView loading pattern)
 * - Testable in isolation (fake implementation for tests)
 * - KMP compatible (pure Kotlin interface)
 */
interface OnRampProvider {
    
    /**
     * Provider type identifier.
     */
    val providerType: OnRampProviderType
    
    /**
     * Build complete widget URL with all parameters.
     * 
     * @param wallet User's wallet containing address and chain info
     * @param cryptoAsset Asset to purchase (e.g., "SOLANA_SOL", "ETH")
     * @param fiatAmount Optional pre-filled amount
     * @param fiatCurrency Fiat currency code (e.g., "NGN", "USD")
     * @param isTestMode Use sandbox environment if true
     * 
     * @return Complete URL ready for WebView loading
     */
    fun buildWidgetUrl(
        wallet: DecagonWallet,
        cryptoAsset: String,
        fiatAmount: Double? = null,
        fiatCurrency: String = "NGN",
        isTestMode: Boolean = true
    ): Result<String>
    
    /**
     * Validate provider configuration.
     * 
     * @return Success if configured correctly, Failure with error message
     * 
     * Use to check:
     * - API keys present
     * - Required parameters valid
     * - Network connectivity
     */
    fun validateConfiguration(): Result<Unit>
    
    /**
     * Get provider display name.
     * Shows in UI when selecting provider.
     */
    fun getDisplayName(): String
    
    /**
     * Check if provider supports given currency.
     * 
     * @param fiatCurrency Currency code (e.g., "NGN")
     * @return true if supported
     */
    fun supportsCurrency(fiatCurrency: String): Boolean
    
    /**
     * Check if provider supports given crypto asset.
     * 
     * @param cryptoAsset Asset identifier
     * @param chainId Blockchain identifier
     * @return true if supported
     */
    fun supportsCryptoAsset(cryptoAsset: String, chainId: String): Boolean
}

/**
 * Result wrapper for provider operations.
 * Prevents exceptions from crashing the app.
 */
sealed class OnRampProviderResult<out T> {
    data class Success<T>(val data: T) : OnRampProviderResult<T>()
    data class Failure(val error: OnRampProviderError) : OnRampProviderResult<Nothing>()
}

/**
 * Provider-specific error types.
 */
sealed class OnRampProviderError(val message: String) {
    class MissingApiKey(provider: String) : 
        OnRampProviderError("$provider API key not configured")
    
    class UnsupportedCurrency(currency: String, provider: String) : 
        OnRampProviderError("$provider does not support $currency")
    
    class UnsupportedAsset(asset: String, provider: String) : 
        OnRampProviderError("$provider does not support $asset")
    
    class InvalidConfiguration(details: String) : 
        OnRampProviderError("Configuration error: $details")
    
    class NetworkError(details: String) : 
        OnRampProviderError("Network error: $details")
}