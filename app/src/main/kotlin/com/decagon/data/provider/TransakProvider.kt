// ============================================================================
// FILE: data/provider/TransakProvider.kt
// PURPOSE: Transak on-ramp implementation
// IMMEDIATE TESTING: Instant sandbox access
// ============================================================================

package com.decagon.data.provider

import com.decagon.core.config.OnRampConfig
import com.decagon.core.config.OnRampProviderType
import com.decagon.domain.model.DecagonWallet
import timber.log.Timber
import java.net.URLEncoder

/**
 * Transak provider implementation.
 * 
 * Documentation: https://docs.transak.com
 * Dashboard: https://transak.com/developers
 * 
 * Benefits:
 * - ✅ Instant sandbox access (no approval delay)
 * - ✅ Emerging markets focus (Africa, Latin America, Southeast Asia)
 * - ✅ Supports Nigerian Naira (NGN)
 * - ✅ Mobile money integration
 * - ✅ Lower fees for African users
 * 
 * Integration Steps:
 * 1. Sign up at transak.com/developers
 * 2. Get API key instantly (sandbox)
 * 3. Update OnRampConfig.TransakCredentials.API_KEY
 * 4. Test with widget URL
 */
class TransakProvider : OnRampProvider {
    
    override val providerType = OnRampProviderType.TRANSAK
    
    override fun buildWidgetUrl(
        wallet: DecagonWallet,
        cryptoAsset: String,
        fiatAmount: Double?,
        fiatCurrency: String,
        isTestMode: Boolean
    ): Result<String> {
        Timber.d("🔄 Transak: Building widget URL")
        
        // Validate configuration
        validateConfiguration().onFailure { 
            return Result.failure(Exception(it.message))
        }
        
        // Base URL (same for test and production)
        val baseUrl = "https://global.transak.com"
        
        // Map crypto asset to Transak network
        val transakNetwork = mapChainIdToTransakNetwork(
            wallet.activeChain?.chainType?.id ?: "solana"
        ).getOrElse { error ->
            Timber.e("❌ Chain mapping failed: ${error.message}")
            return Result.failure(error)
        }
        
        // Map crypto asset code
        val transakCryptoCode = mapCryptoAssetToTransakCode(
            cryptoAsset,
            transakNetwork
        ).getOrElse { error ->
            Timber.e("❌ Asset mapping failed: ${error.message}")
            return Result.failure(error)
        }
        
        // Build parameters
        val params = buildMap<String, String> {
            put("apiKey", OnRampConfig.TransakCredentials.API_KEY)
            
            // Asset and network
            put("cryptoCurrencyCode", transakCryptoCode)
            put("network", transakNetwork)
            put("walletAddress", wallet.address)
            
            // Fiat configuration
            put("fiatCurrency", fiatCurrency.uppercase())
            fiatAmount?.let { put("fiatAmount", it.toString()) }
            
            // UI configuration
            put("disableWalletAddressForm", "true") // Lock wallet address
            put("hideMenu", "true") // Simplified UI for mobile
            put("themeColor", "6366F1") // Your app's primary color
            
            // Test mode
            if (isTestMode) {
                put("environment", "STAGING")
                put("isAutoFillUserData", "true") // Pre-fill test data
            }
            
            // Mobile optimization
            put("redirectURL", "decagon://onramp/complete")
            
            // Payment methods (optimize for Nigeria)
            put("defaultPaymentMethod", "credit_debit_card,mobile_money,bank_transfer")
        }
        
        // Build query string
        val queryString = params.entries.joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, "UTF-8")}"
        }
        
        val fullUrl = "$baseUrl?$queryString"
        
        Timber.d("🔄 Transak URL: $baseUrl")
        Timber.d("  Network: $transakNetwork")
        Timber.d("  Asset: $transakCryptoCode")
        Timber.d("  Address: ${wallet.address.take(8)}...")
        Timber.d("  Test Mode: $isTestMode")
        
        return Result.success(fullUrl)
    }
    
    override fun validateConfiguration(): Result<Unit> {
        return when {
            OnRampConfig.TransakCredentials.API_KEY.isBlank() -> {
                Timber.e("❌ Transak: API key missing")
                Result.failure(Exception(
                    OnRampProviderError.MissingApiKey("Transak").message
                ))
            }
            OnRampConfig.TransakCredentials.API_KEY == "your_transak_api_key" -> {
                Timber.e("❌ Transak: Default API key detected - please replace")
                Result.failure(Exception(
                    OnRampProviderError.InvalidConfiguration(
                        "Replace placeholder API key"
                    ).message
                ))
            }
            else -> {
                Timber.d("✅ Transak: Configuration valid")
                Result.success(Unit)
            }
        }
    }
    
    override fun getDisplayName(): String = "Transak"
    
    override fun supportsCurrency(fiatCurrency: String): Boolean {
        // Transak supported fiat currencies (comprehensive list)
        val supported = setOf(
            "NGN", // Nigerian Naira ✅
            "USD", "EUR", "GBP", "AUD", "BRL",
            "CAD", "CHF", "CZK", "DKK", "HKD",
            "ILS", "JPY", "KRW", "MXN", "NOK",
            "NZD", "PLN", "SEK", "SGD", "THB",
            "TRY", "ZAR", "AED", "ARS", "INR",
            "IDR", "KES", "GHS", "UGX" // African currencies
        )
        
        return fiatCurrency.uppercase() in supported
    }
    
    override fun supportsCryptoAsset(cryptoAsset: String, chainId: String): Boolean {
        return try {
            val network = mapChainIdToTransakNetwork(chainId).getOrNull()
            network != null && mapCryptoAssetToTransakCode(cryptoAsset, network).isSuccess
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Map Decagon chain ID to Transak network identifier.
     * 
     * Transak Networks: https://docs.transak.com/docs/query-parameters
     */
    private fun mapChainIdToTransakNetwork(chainId: String): Result<String> {
        val transakNetwork = when (chainId.lowercase()) {
            "solana" -> "solana"
            "ethereum" -> "ethereum"
            "polygon" -> "polygon"
            "arbitrum" -> "arbitrum"
            "optimism" -> "optimism"
            "avalanche" -> "avalanche_cchain"
            "bsc", "binance" -> "bsc"
            else -> null
        }
        
        return if (transakNetwork != null) {
            Result.success(transakNetwork)
        } else {
            Result.failure(Exception(
                OnRampProviderError.UnsupportedAsset(
                    chainId,
                    "Transak"
                ).message
            ))
        }
    }
    
    /**
     * Map Decagon crypto asset to Transak currency code.
     * 
     * Transak Currencies: https://docs.transak.com/docs/supported-cryptocurrencies
     */
    private fun mapCryptoAssetToTransakCode(
        decagonAsset: String,
        transakNetwork: String
    ): Result<String> {
        val transakCode = when {
            // Native tokens
            decagonAsset.contains("SOL", ignoreCase = true) && 
            transakNetwork == "solana" -> "SOL"
            
            decagonAsset.contains("ETH", ignoreCase = true) && 
            transakNetwork == "ethereum" -> "ETH"
            
            decagonAsset.contains("MATIC", ignoreCase = true) && 
            transakNetwork == "polygon" -> "MATIC"
            
            // USDC variants
            decagonAsset.contains("USDC", ignoreCase = true) -> when (transakNetwork) {
                "solana" -> "USDC"
                "ethereum" -> "USDC"
                "polygon" -> "USDC"
                else -> null
            }
            
            // USDT variants
            decagonAsset.contains("USDT", ignoreCase = true) -> when (transakNetwork) {
                "solana" -> "USDT"
                "ethereum" -> "USDT"
                "polygon" -> "USDT"
                else -> null
            }
            
            else -> null
        }
        
        return if (transakCode != null) {
            Result.success(transakCode)
        } else {
            Result.failure(Exception(
                OnRampProviderError.UnsupportedAsset(
                    "$decagonAsset on $transakNetwork",
                    "Transak"
                ).message
            ))
        }
    }
    
    /**
     * Parse Transak webhook event.
     * Transak sends webhook notifications for transaction status updates.
     * 
     * Event Types:
     * - ORDER_CREATED
     * - ORDER_PROCESSING
     * - ORDER_COMPLETED
     * - ORDER_FAILED
     */
    fun parseWebhookEvent(eventData: Map<String, Any>): TransakWebhookEvent {
        val status = eventData["status"] as? String ?: "UNKNOWN"
        val orderId = eventData["orderId"] as? String ?: ""
        val cryptoAmount = eventData["cryptoAmount"] as? Double
        
        return when (status.uppercase()) {
            "COMPLETED" -> TransakWebhookEvent.Completed(orderId, cryptoAmount ?: 0.0)
            "PROCESSING" -> TransakWebhookEvent.Processing(orderId)
            "FAILED" -> TransakWebhookEvent.Failed(orderId, "Transaction failed")
            "CANCELLED" -> TransakWebhookEvent.Cancelled(orderId)
            else -> TransakWebhookEvent.Unknown(orderId, status)
        }
    }
}

/**
 * Transak webhook event types.
 */
sealed class TransakWebhookEvent {
    data class Completed(val orderId: String, val cryptoAmount: Double) : TransakWebhookEvent()
    data class Processing(val orderId: String) : TransakWebhookEvent()
    data class Failed(val orderId: String, val reason: String) : TransakWebhookEvent()
    data class Cancelled(val orderId: String) : TransakWebhookEvent()
    data class Unknown(val orderId: String, val status: String) : TransakWebhookEvent()
}