// ============================================================================
// FILE: core/config/OnRampConfig.kt
// UPDATED: Regional availability and manual selection support
// ============================================================================

package com.decagon.core.config

/**
 * Feature flags and configuration for on-ramp providers.
 *
 * CRITICAL: Regional availability varies by provider
 * Test actual availability in target markets before production deployment
 */
object OnRampConfig {

    // ========================================================================
    // PROVIDER ENABLEMENT FLAGS
    // ========================================================================

    /**
     * Ramp Network integration.
     *
     * Regional Support:
     * - ✅ Europe (primary market)
     * - ⚠️ Nigeria: Limited, requires EUR/USD intermediary
     * - ❌ Requires partnership approval
     *
     * Status: Awaiting API key from partnership registration
     */
    const val RAMP_ENABLED = false // ❌ Pending approval

    /**
     * MoonPay integration.
     *
     * Regional Support:
     * - ✅ North America, Europe
     * - ❌ Nigeria: "Coming soon" status (tested December 2024)
     * - ✅ Sandbox available immediately
     *
     * Recommendation: Disable for Nigerian users until regional expansion
     */
    const val MOONPAY_ENABLED = false // ❌ Not available in Nigeria

    /**
     * Transak integration.
     *
     * Regional Support:
     * - ✅ Nigeria (PRIMARY for African markets)
     * - ✅ Emerging markets focus
     * - ✅ Mobile money support
     * - ✅ Instant sandbox access
     *
     * Recommendation: PRIMARY PROVIDER for Nigerian users
     */
    const val TRANSAK_ENABLED = true // ✅ RECOMMENDED for Nigeria

    /**
     * Onramper aggregator.
     *
     * Regional Support:
     * - ✅ Global (routes to best available provider)
     * - ✅ Automatic provider selection
     *
     * Status: Available for future implementation
     */
    const val ONRAMPER_ENABLED = false

    // ========================================================================
    // MANUAL SELECTION
    // ========================================================================

    /**
     * Allow users to manually select provider.
     *
     * true: Show provider selection UI before loading widget
     * false: Use automatic provider selection only
     *
     * Recommendation: true for better UX and regional flexibility
     */
    const val ALLOW_MANUAL_PROVIDER_SELECTION = true

    /**
     * Remember user's provider preference.
     *
     * true: Save last used provider to DataStore
     * false: Always show provider selection
     */
    const val REMEMBER_PROVIDER_PREFERENCE = true

    // ========================================================================
    // DEFAULT PROVIDER SELECTION (REGIONAL)
    // ========================================================================

    /**
     * Default provider for Nigerian users.
     *
     * Based on real-world testing:
     * - Transak: ✅ Available, mobile money support
     * - MoonPay: ❌ "Coming soon" as of Dec 2024
     * - Ramp: ⚠️ Limited support, pending partnership
     */
    val DEFAULT_PROVIDER_NIGERIA: OnRampProviderType
        get() = when {
            TRANSAK_ENABLED -> OnRampProviderType.TRANSAK
            RAMP_ENABLED -> OnRampProviderType.RAMP
            else -> OnRampProviderType.TRANSAK // Fallback
        }

    /**
     * Default provider for European users.
     */
    val DEFAULT_PROVIDER_EUROPE: OnRampProviderType
        get() = when {
            RAMP_ENABLED -> OnRampProviderType.RAMP
            MOONPAY_ENABLED -> OnRampProviderType.MOONPAY
            TRANSAK_ENABLED -> OnRampProviderType.TRANSAK
            else -> OnRampProviderType.RAMP
        }

    /**
     * Default provider for North American users.
     */
    val DEFAULT_PROVIDER_NORTH_AMERICA: OnRampProviderType
        get() = when {
            MOONPAY_ENABLED -> OnRampProviderType.MOONPAY
            TRANSAK_ENABLED -> OnRampProviderType.TRANSAK
            RAMP_ENABLED -> OnRampProviderType.RAMP
            else -> OnRampProviderType.MOONPAY
        }

    /**
     * Get default provider based on user's detected region.
     *
     * @param countryCode ISO country code (e.g., "NG", "US", "GB")
     * @return Recommended provider for region
     */
    fun getDefaultProviderForRegion(countryCode: String): OnRampProviderType {
        return when (countryCode.uppercase()) {
            // African countries
            "NG", "KE", "GH", "ZA", "UG" -> DEFAULT_PROVIDER_NIGERIA

            // European countries
            "GB", "DE", "FR", "IT", "ES", "NL", "PL", "CH" -> DEFAULT_PROVIDER_EUROPE

            // North America
            "US", "CA" -> DEFAULT_PROVIDER_NORTH_AMERICA

            // Global fallback
            else -> when {
                TRANSAK_ENABLED -> OnRampProviderType.TRANSAK
                MOONPAY_ENABLED -> OnRampProviderType.MOONPAY
                RAMP_ENABLED -> OnRampProviderType.RAMP
                else -> OnRampProviderType.TRANSAK
            }
        }
    }

    // ========================================================================
    // REGIONAL AVAILABILITY MAPPING
    // ========================================================================

    /**
     * Check if provider is available in specific region.
     *
     * Based on real-world testing and provider documentation.
     * Update this as providers expand to new markets.
     */
    fun isProviderAvailableInRegion(
        provider: OnRampProviderType,
        countryCode: String
    ): Boolean {
        val country = countryCode.uppercase()

        return when (provider) {
            OnRampProviderType.TRANSAK -> {
                // Transak: Wide emerging markets support
                country in TRANSAK_SUPPORTED_COUNTRIES
            }

            OnRampProviderType.MOONPAY -> {
                // MoonPay: Primarily developed markets
                // ❌ Nigeria confirmed unavailable (Dec 2024 testing)
                country in MOONPAY_SUPPORTED_COUNTRIES
            }

            OnRampProviderType.RAMP -> {
                // Ramp: Europe-focused
                country in RAMP_SUPPORTED_COUNTRIES
            }

            OnRampProviderType.ONRAMPER -> {
                // Onramper: Global aggregator
                true // Routes to available provider
            }
        }
    }

    /**
     * Get regional availability message for provider.
     */
    fun getRegionalAvailabilityMessage(
        provider: OnRampProviderType,
        countryCode: String
    ): String {
        val available = isProviderAvailableInRegion(provider, countryCode)

        return if (available) {
            "Available in your region"
        } else {
            when (provider) {
                OnRampProviderType.MOONPAY ->
                    "MoonPay is coming soon to your region. Try Transak for immediate access."
                OnRampProviderType.RAMP ->
                    "Ramp Network has limited support in your region. Try Transak for better local payment options."
                else ->
                    "This provider is not yet available in your region"
            }
        }
    }

    // ========================================================================
    // SUPPORTED COUNTRIES (Update as providers expand)
    // ========================================================================

    private val TRANSAK_SUPPORTED_COUNTRIES = setOf(
        // Africa
        "NG", "KE", "GH", "ZA", "UG", "TZ", "RW",
        // Europe
        "GB", "DE", "FR", "IT", "ES", "NL", "PL", "PT", "SE", "NO", "DK", "FI", "IE", "CH", "AT", "BE", "CZ",
        // North America
        "US", "CA",
        // Asia
        "IN", "SG", "MY", "TH", "ID", "PH", "VN", "JP", "KR",
        // Latin America
        "BR", "MX", "AR", "CL", "CO",
        // Oceania
        "AU", "NZ"
    )

    private val MOONPAY_SUPPORTED_COUNTRIES = setOf(
        // Europe
        "GB", "DE", "FR", "IT", "ES", "NL", "PL", "PT", "SE", "NO", "DK", "FI", "IE", "CH", "AT", "BE",
        // North America
        "US", "CA",
        // Asia (limited)
        "SG", "JP", "KR", "HK",
        // Oceania
        "AU", "NZ"
        // ❌ Nigeria explicitly excluded based on testing
    )

    private val RAMP_SUPPORTED_COUNTRIES = setOf(
        // Europe (primary)
        "GB", "DE", "FR", "IT", "ES", "NL", "PL", "PT", "SE", "NO", "DK", "FI", "IE", "CH", "AT", "BE", "CZ",
        // North America (limited)
        "US"
        // ⚠️ Nigeria requires intermediary currency (EUR/USD)
    )

    // ========================================================================
    // ENVIRONMENT CONFIGURATION
    // ========================================================================

    /**
     * Test mode flag.
     */
    const val TEST_MODE = true // ✅ Start in test mode

    /**
     * Transaction monitoring configuration.
     */
    const val MONITORING_DURATION_MINUTES = 10
    const val POLLING_INTERVAL_SECONDS = 5L

    // ========================================================================
    // PROVIDER CREDENTIALS
    // ========================================================================

    object RampCredentials {
        const val API_KEY = "" // Pending partnership
        const val WEBHOOK_SECRET = ""
    }

    object MoonPayCredentials {
        const val API_KEY = "" // Not needed for Nigeria
        const val SECRET_KEY = ""
    }

    object TransakCredentials {
        const val API_KEY = "your_transak_api_key" // ✅ REPLACE WITH YOUR KEY
    }
}

enum class OnRampProviderType {
    RAMP,
    MOONPAY,
    TRANSAK,
    ONRAMPER
}