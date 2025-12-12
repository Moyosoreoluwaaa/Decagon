// ============================================================================
// FILE: data/provider/RampProvider.kt
// PURPOSE: Ramp Network on-ramp implementation (requires partnership)
// STATUS: Pending API key from partnership registration
// ============================================================================

package com.decagon.data.provider

import com.decagon.core.config.OnRampConfig
import com.decagon.core.config.OnRampProviderType
import com.decagon.domain.model.DecagonWallet
import timber.log.Timber
import java.net.URLEncoder

/**
 * Ramp Network provider implementation.
 * 
 * Documentation: https://docs.ramp.network
 * Partnership: https://ramp.network/partners
 * 
 * Requirements:
 * - ❌ Requires hostApiKey from partnership registration
 * - ⏳ Approval typically takes 24-72 hours
 * - ✅ Production-ready once approved
 * 
 * Benefits (Post-Approval):
 * - Compliance-focused (good for regulated markets)
 * - Bank transfer support
 * - European payment methods
 * 
 * Status:
 * - Current: Awaiting partnership approval
 * - Next: Receive API credentials via email
 * - Then: Update OnRampConfig.RampCredentials.API_KEY
 */
class RampProvider : OnRampProvider {
    
    override val providerType = OnRampProviderType.RAMP
    
    override fun buildWidgetUrl(
        wallet: DecagonWallet,
        cryptoAsset: String,
        fiatAmount: Double?,
        fiatCurrency: String,
        isTestMode: Boolean
    ): Result<String> {
        Timber.d("🟣 Ramp: Building widget URL")
        
        // CRITICAL: Validate API key exists
        validateConfiguration().onFailure { 
            Timber.e("❌ Ramp: Configuration invalid - ${it.message}")
            return Result.failure(Exception(it.message))
        }
        
        // Determine base URL
        val baseUrl = if (isTestMode) {
            "https://app.demo.rampnetwork.com"
        } else {
            "https://app.ramp.network"
        }
        
        // Map crypto asset to Ramp's naming convention
        val rampAssetCode = mapCryptoAssetToRampCode(
            cryptoAsset,
            wallet.activeChain?.chainType?.id ?: "solana"
        ).getOrElse { error ->
            Timber.e("❌ Asset mapping failed: ${error.message}")
            return Result.failure(error)
        }
        
        // Build parameters
        val params = buildMap<String, String> {
            // ✅ CRITICAL: hostApiKey must be provided
            put("hostApiKey", OnRampConfig.RampCredentials.API_KEY)
            
            // App identification
            put("hostAppName", "Decagon Wallet")
            put("hostLogoUrl", "https://your-domain.com/logo.png") // Optional
            
            // Transaction parameters
            put("swapAsset", rampAssetCode)
            put("userAddress", wallet.address)
            put("fiatCurrency", fiatCurrency.uppercase())
            
            // Optional: Pre-fill amount
            fiatAmount?.let { put("fiatValue", it.toString()) }
            
            // UI configuration
            put("variant", "mobile") // Optimized for mobile
            
            // Webhook (optional, for backend integration)
            if (OnRampConfig.RampCredentials.WEBHOOK_SECRET.isNotBlank()) {
                put("webhookUrl", "https://your-backend.com/webhooks/ramp")
            }
        }
        
        // Build query string
        val queryString = params.entries.joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, "UTF-8")}"
        }
        
        val fullUrl = "$baseUrl?$queryString"
        
        Timber.d("🟣 Ramp URL: $baseUrl")
        Timber.d("  Asset: $rampAssetCode")
        Timber.d("  Address: ${wallet.address.take(8)}...")
        Timber.d("  API Key: ${OnRampConfig.RampCredentials.API_KEY.take(8)}...")
        Timber.d("  Test Mode: $isTestMode")
        
        return Result.success(fullUrl)
    }
    
    override fun validateConfiguration(): Result<Unit> {
        return when {
            // Check if API key is missing or placeholder
            OnRampConfig.RampCredentials.API_KEY.isBlank() -> {
                Timber.e("❌ Ramp: API key missing")
                Timber.e("📝 Action Required: Register at https://ramp.network/partners")
                Result.failure(Exception(
                    OnRampProviderError.MissingApiKey("Ramp").message + 
                    " - Register at https://ramp.network/partners"
                ))
            }
            
            // Warn if webhook secret missing (optional but recommended)
            OnRampConfig.RampCredentials.WEBHOOK_SECRET.isBlank() -> {
                Timber.w("⚠️ Ramp: Webhook secret not configured (optional)")
                Timber.w("   Transaction status updates will rely on polling only")
                Result.success(Unit) // Not critical, allow
            }
            
            else -> {
                Timber.d("✅ Ramp: Configuration valid")
                Result.success(Unit)
            }
        }
    }
    
    override fun getDisplayName(): String = "Ramp Network"
    
    override fun supportsCurrency(fiatCurrency: String): Boolean {
        // Ramp supported fiat currencies
        val supported = setOf(
            "EUR", // Euro (primary)
            "GBP", // British Pound
            "USD", // US Dollar
            "CHF", // Swiss Franc
            "CZK", "DKK", "NOK", "PLN", "SEK",
            // Note: NGN not directly supported as of documentation
            // Users may need to use EUR/USD intermediary
        )
        
        val isSupported = fiatCurrency.uppercase() in supported
        
        if (!isSupported && fiatCurrency.uppercase() == "NGN") {
            Timber.w("⚠️ Ramp: NGN not directly supported")
            Timber.w("   Users will need to convert via EUR/USD")
        }
        
        return isSupported
    }
    
    override fun supportsCryptoAsset(cryptoAsset: String, chainId: String): Boolean {
        return try {
            mapCryptoAssetToRampCode(cryptoAsset, chainId).isSuccess
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Map Decagon crypto asset to Ramp's asset identifiers.
     * 
     * Ramp Asset Format: <TICKER>_<NETWORK>
     * Example: SOLANA_SOL, ETH, MATIC_POLYGON
     * 
     * Documentation: https://docs.ramp.network/configuration
     */
    private fun mapCryptoAssetToRampCode(
        decagonAsset: String,
        chainId: String
    ): Result<String> {
        val rampCode = when {
            // Solana
            decagonAsset.contains("SOL", ignoreCase = true) && 
            chainId == "solana" -> "SOLANA_SOL"
            
            // Ethereum
            decagonAsset.contains("ETH", ignoreCase = true) && 
            chainId == "ethereum" -> "ETH"
            
            // Polygon
            decagonAsset.contains("MATIC", ignoreCase = true) && 
            chainId == "polygon" -> "MATIC_POLYGON"
            
            // USDC variants
            decagonAsset.contains("USDC", ignoreCase = true) -> when (chainId) {
                "solana" -> "USDC_SOLANA"
                "ethereum" -> "USDC_ETH"
                "polygon" -> "USDC_POLYGON"
                else -> null
            }
            
            // USDT variants
            decagonAsset.contains("USDT", ignoreCase = true) -> when (chainId) {
                "ethereum" -> "USDT_ETH"
                "polygon" -> "USDT_POLYGON"
                else -> null
            }
            
            else -> null
        }
        
        return if (rampCode != null) {
            Result.success(rampCode)
        } else {
            Result.failure(Exception(
                OnRampProviderError.UnsupportedAsset(
                    "$decagonAsset on $chainId",
                    "Ramp"
                ).message
            ))
        }
    }
    
    /**
     * Parse Ramp webhook event.
     * Ramp sends webhook notifications for purchase status updates.
     * 
     * Event Types:
     * - INITIALIZED: User opened widget
     * - PAYMENT_STARTED: User initiated payment
     * - PAYMENT_IN_PROGRESS: Payment being processed
     * - PAYMENT_EXECUTED: Payment successful
     * - RELEASED: Crypto released to user's wallet
     * - EXPIRED: Purchase expired
     */
    fun parseWebhookEvent(eventData: Map<String, Any>): RampWebhookEvent {
        val type = eventData["type"] as? String ?: "UNKNOWN"
        val purchaseId = eventData["purchase"]?.let { 
            (it as? Map<*, *>)?.get("id") as? String 
        } ?: ""
        
        return when (type.uppercase()) {
            "RELEASED" -> {
                val cryptoAmount = eventData["purchase"]?.let { 
                    (it as? Map<*, *>)?.get("cryptoAmount") as? Double 
                } ?: 0.0
                RampWebhookEvent.Completed(purchaseId, cryptoAmount)
            }
            "PAYMENT_EXECUTED" -> RampWebhookEvent.Processing(purchaseId)
            "EXPIRED" -> RampWebhookEvent.Expired(purchaseId)
            else -> RampWebhookEvent.Unknown(purchaseId, type)
        }
    }
}

/**
 * Ramp webhook event types.
 */
sealed class RampWebhookEvent {
    data class Completed(val purchaseId: String, val cryptoAmount: Double) : RampWebhookEvent()
    data class Processing(val purchaseId: String) : RampWebhookEvent()
    data class Expired(val purchaseId: String) : RampWebhookEvent()
    data class Unknown(val purchaseId: String, val type: String) : RampWebhookEvent()
}