// ============================================================================
// FILE: data/provider/MoonPayProvider.kt
// PURPOSE: MoonPay on-ramp implementation
// IMMEDIATE TESTING: Sandbox available without approval
// ============================================================================

package com.decagon.data.provider

import com.decagon.core.config.OnRampConfig
import com.decagon.core.config.OnRampProviderType
import com.decagon.domain.model.DecagonWallet
import timber.log.Timber
import java.net.URLEncoder

/**
 * MoonPay provider implementation.
 * 
 * Documentation: https://www.moonpay.com/documentation
 * Dashboard: https://www.moonpay.com/dashboard
 * 
 * Benefits:
 * - ✅ Sandbox available immediately (no approval wait)
 * - ✅ Supports Nigerian Naira (NGN)
 * - ✅ Strong African market presence
 * - ✅ Mobile-optimized widget
 * 
 * Integration Steps:
 * 1. Sign up at moonpay.com/dashboard
 * 2. Get API key instantly (sandbox)
 * 3. Update OnRampConfig.MoonPayCredentials.API_KEY
 * 4. Test with widget URL
 */
class MoonPayProvider : OnRampProvider {
    
    override val providerType = OnRampProviderType.MOONPAY
    
    override fun buildWidgetUrl(
        wallet: DecagonWallet,
        cryptoAsset: String,
        fiatAmount: Double?,
        fiatCurrency: String,
        isTestMode: Boolean
    ): Result<String> {
        Timber.d("🌙 MoonPay: Building widget URL")
        
        // Validate configuration first
        validateConfiguration().onFailure { 
            return Result.failure(Exception(it.message))
        }
        
        // Determine base URL
        val baseUrl = if (isTestMode) {
            "https://buy-sandbox.moonpay.com"
        } else {
            "https://buy.moonpay.com"
        }
        
        // Map Decagon crypto asset to MoonPay currency code
        val moonPayCurrencyCode = mapCryptoAssetToMoonPayCode(
            cryptoAsset, 
            wallet.activeChain?.chainType?.id ?: "solana"
        ).getOrElse { error ->
            Timber.e("❌ Asset mapping failed: ${error.message}")
            return Result.failure(error)
        }
        
        // Build parameters
        val params = buildMap<String, String> {
            put("apiKey", OnRampConfig.MoonPayCredentials.API_KEY)
            put("currencyCode", moonPayCurrencyCode)
            put("walletAddress", wallet.address)
            put("baseCurrencyCode", fiatCurrency.lowercase())
            
            // Optional: Pre-fill amount
            fiatAmount?.let { put("baseCurrencyAmount", it.toString()) }
            
            // UI customization
            put("colorCode", "#6366F1") // Your app's primary color
            put("showWalletAddressForm", "false") // Disable address input
            put("lockAmount", "false") // Allow user to change amount
            
            // Mobile optimization
            put("redirectURL", "decagon://onramp/complete") // Deep link callback
        }
        
        // Build query string
        val queryString = params.entries.joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, "UTF-8")}"
        }
        
        val fullUrl = "$baseUrl?$queryString"
        
        Timber.d("🌙 MoonPay URL: $baseUrl")
        Timber.d("  Currency: $moonPayCurrencyCode")
        Timber.d("  Address: ${wallet.address.take(8)}...")
        Timber.d("  Test Mode: $isTestMode")
        
        return Result.success(fullUrl)
    }
    
    override fun validateConfiguration(): Result<Unit> {
        return when {
            OnRampConfig.MoonPayCredentials.API_KEY.isBlank() -> {
                Timber.e("❌ MoonPay: API key missing")
                Result.failure(Exception(
                    OnRampProviderError.MissingApiKey("MoonPay").message
                ))
            }
            OnRampConfig.MoonPayCredentials.API_KEY.startsWith("pk_test_") && 
            !OnRampConfig.TEST_MODE -> {
                Timber.w("⚠️ MoonPay: Using test key in production mode")
                Result.success(Unit) // Allow but warn
            }
            else -> {
                Timber.d("✅ MoonPay: Configuration valid")
                Result.success(Unit)
            }
        }
    }
    
    override fun getDisplayName(): String = "MoonPay"
    
    override fun supportsCurrency(fiatCurrency: String): Boolean {
        // MoonPay supported fiat currencies
        val supported = setOf(
            "NGN", // Nigerian Naira ✅
            "USD", // US Dollar
            "EUR", // Euro
            "GBP", // British Pound
            "AUD", "BRL", "CAD", "CHF", "CZK", 
            "DKK", "HKD", "ILS", "JPY", "KRW",
            "MXN", "NOK", "NZD", "PLN", "SEK", 
            "SGD", "THB", "TRY", "ZAR"
        )
        
        return fiatCurrency.uppercase() in supported
    }
    
    override fun supportsCryptoAsset(cryptoAsset: String, chainId: String): Boolean {
        // Check if MoonPay supports this asset
        return try {
            mapCryptoAssetToMoonPayCode(cryptoAsset, chainId).isSuccess
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Map Decagon's asset naming to MoonPay's currency codes.
     * 
     * MoonPay Documentation: https://www.moonpay.com/currencies
     */
    private fun mapCryptoAssetToMoonPayCode(
        decagonAsset: String, 
        chainId: String
    ): Result<String> {
        val moonPayCode = when {
            // Solana
            decagonAsset.contains("SOL", ignoreCase = true) && 
            chainId == "solana" -> "sol"
            
            // Ethereum
            decagonAsset.contains("ETH", ignoreCase = true) && 
            chainId == "ethereum" -> "eth"
            
            // Polygon
            decagonAsset.contains("MATIC", ignoreCase = true) && 
            chainId == "polygon" -> "matic_polygon"
            
            // USDC variants
            decagonAsset.contains("USDC", ignoreCase = true) -> when (chainId) {
                "solana" -> "usdc_sol"
                "ethereum" -> "usdc"
                "polygon" -> "usdc_polygon"
                else -> null
            }
            
            // USDT variants
            decagonAsset.contains("USDT", ignoreCase = true) -> when (chainId) {
                "ethereum" -> "usdt"
                "polygon" -> "usdt_polygon"
                else -> null
            }
            
            else -> null
        }
        
        return if (moonPayCode != null) {
            Result.success(moonPayCode)
        } else {
            Result.failure(Exception(
                OnRampProviderError.UnsupportedAsset(
                    "$decagonAsset on $chainId", 
                    "MoonPay"
                ).message
            ))
        }
    }
    
    /**
     * Parse MoonPay callback URL for transaction status.
     * Called when user returns from widget via deep link.
     * 
     * Format: decagon://onramp/complete?transactionId=xxx&status=completed
     */
    fun parseCallback(callbackUrl: String): MoonPayCallbackResult {
        // Implementation for handling deep link callbacks
        // Extract transactionId and status from URL
        return MoonPayCallbackResult.Pending("transaction_id")
    }
}

/**
 * MoonPay callback result types.
 */
sealed class MoonPayCallbackResult {
    data class Completed(val transactionId: String, val cryptoAmount: Double) : MoonPayCallbackResult()
    data class Pending(val transactionId: String) : MoonPayCallbackResult()
    data class Failed(val transactionId: String, val reason: String) : MoonPayCallbackResult()
    data class Cancelled(val transactionId: String) : MoonPayCallbackResult()
}