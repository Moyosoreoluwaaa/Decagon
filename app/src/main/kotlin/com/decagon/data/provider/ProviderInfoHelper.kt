// ============================================================================
// FILE: ui/screen/onramp/ProviderInfoHelper.kt
// PURPOSE: Generate provider information with regional awareness
// ============================================================================

package com.decagon.data.provider

import com.decagon.core.config.OnRampConfig
import com.decagon.core.config.OnRampProviderType
import com.decagon.domain.provider.OnRampProviderFactory
import com.decagon.ui.components.ProviderInfo

/**
 * Helper for generating provider information for UI display.
 * 
 * Responsibilities:
 * - Detect user's region
 * - Check provider regional availability
 * - Generate provider descriptions and capabilities
 * - Mark recommended providers for region
 */
object ProviderInfoHelper {
    
    /**
     * Get provider information for display.
     * 
     * @param factory Provider factory for availability checking
     * @param userCountryCode User's country code (e.g., "NG", "US")
     * @return List of providers with regional information
     */
    fun getProvidersForDisplay(
        factory: OnRampProviderFactory,
        userCountryCode: String
    ): List<ProviderInfo> {
        val availableProviders = factory.getAvailableProviders()
        
        return availableProviders.map { provider ->
            createProviderInfo(provider, userCountryCode)
        }.sortedWith(
            compareByDescending<ProviderInfo> { it.isAvailableInRegion }
                .thenByDescending { it.isRecommended }
                .thenBy { it.displayName }
        )
    }
    
    /**
     * Create provider information for UI.
     */
    private fun createProviderInfo(
        provider: OnRampProvider,
        userCountryCode: String
    ): ProviderInfo {
        val type = provider.providerType
        val isAvailable = OnRampConfig.isProviderAvailableInRegion(type, userCountryCode)
        val isRecommended = isProviderRecommendedForRegion(type, userCountryCode)
        
        return when (type) {
            OnRampProviderType.TRANSAK -> ProviderInfo(
                type = type,
                displayName = "Transak",
                description = "Emerging markets specialist with mobile money support",
                isAvailableInRegion = isAvailable,
                regionalAvailabilityMessage = OnRampConfig.getRegionalAvailabilityMessage(
                    type, 
                    userCountryCode
                ),
                isRecommended = isRecommended,
                paymentMethods = "Card, Bank Transfer, Mobile Money",
                supportedCurrencies = "NGN, USD, EUR, and 100+ more",
                processingTime = "Instant - 10 minutes",
                feeStructure = "2.99% + network fees",
                supportsMobileWallet = true
            )
            
            OnRampProviderType.MOONPAY -> ProviderInfo(
                type = type,
                displayName = "MoonPay",
                description = "Popular provider for developed markets",
                isAvailableInRegion = isAvailable,
                regionalAvailabilityMessage = OnRampConfig.getRegionalAvailabilityMessage(
                    type, 
                    userCountryCode
                ),
                isRecommended = isRecommended,
                paymentMethods = "Card, Bank Transfer, Apple Pay",
                supportedCurrencies = "USD, EUR, GBP, and 30+ more",
                processingTime = "Instant - 5 minutes",
                feeStructure = "3.5% + network fees",
                supportsMobileWallet = false
            )
            
            OnRampProviderType.RAMP -> ProviderInfo(
                type = type,
                displayName = "Ramp Network",
                description = "European-focused with bank transfer support",
                isAvailableInRegion = isAvailable,
                regionalAvailabilityMessage = OnRampConfig.getRegionalAvailabilityMessage(
                    type, 
                    userCountryCode
                ),
                isRecommended = isRecommended,
                paymentMethods = "Bank Transfer, Card, Open Banking",
                supportedCurrencies = "EUR, GBP, USD, and 20+ more",
                processingTime = "Instant - 30 minutes",
                feeStructure = "2.9% + network fees",
                supportsMobileWallet = false
            )
            
            OnRampProviderType.ONRAMPER -> ProviderInfo(
                type = type,
                displayName = "Onramper",
                description = "Smart aggregator routing to best provider",
                isAvailableInRegion = true, // Aggregator always available
                regionalAvailabilityMessage = "Available worldwide",
                isRecommended = false,
                paymentMethods = "Varies by routed provider",
                supportedCurrencies = "100+ currencies",
                processingTime = "Varies by provider",
                feeStructure = "Provider fees apply",
                supportsMobileWallet = true
            )
        }
    }
    
    /**
     * Check if provider is recommended for user's region.
     */
    private fun isProviderRecommendedForRegion(
        provider: OnRampProviderType,
        countryCode: String
    ): Boolean {
        val defaultForRegion = OnRampConfig.getDefaultProviderForRegion(countryCode)
        return provider == defaultForRegion
    }
    
    /**
     * Detect user's country code.
     * 
     * Priority:
     * 1. Saved preference
     * 2. Device locale
     * 3. Network-based detection (future)
     * 
     * @return ISO country code (e.g., "NG", "US", "GB")
     */
    fun detectUserCountryCode(): String {
        // Implementation depends on your architecture
        // For now, return Nigeria as detected in your testing
        return "NG"
        
        // Production implementation:
        // 1. Check DataStore for saved country preference
        // 2. Fall back to device locale: Locale.getDefault().country
        // 3. Fall back to network-based geolocation API
    }
    
    /**
     * Get friendly region name for display.
     */
    fun getRegionDisplayName(countryCode: String): String {
        return when (countryCode.uppercase()) {
            "NG" -> "Nigeria"
            "KE" -> "Kenya"
            "GH" -> "Ghana"
            "ZA" -> "South Africa"
            "US" -> "United States"
            "GB" -> "United Kingdom"
            "DE" -> "Germany"
            "FR" -> "France"
            else -> countryCode.uppercase()
        }
    }
}