// ============================================================================
// FILE: domain/provider/OnRampProviderFactory.kt
// PURPOSE: Provider selection orchestrator (Mediator Pattern)
// PATTERN: Orchestration from complete_impl_logic.txt
// ============================================================================

package com.decagon.domain.provider

import com.decagon.core.config.OnRampConfig
import com.decagon.core.config.OnRampProviderType
import com.decagon.data.provider.MoonPayProvider
import com.decagon.data.provider.OnRampProvider
import com.decagon.data.provider.ProviderInfoHelper
import com.decagon.data.provider.RampProvider
import com.decagon.data.provider.TransakProvider
import timber.log.Timber
import kotlin.collections.filter
import kotlin.getOrThrow

/**
 * On-ramp provider factory and orchestrator.
 *
 * Responsibilities:
 * - Select active provider based on feature flags
 * - Validate provider configuration
 * - Provide fallback logic if primary provider unavailable
 * - Abstract provider selection from ViewModel/UI
 *
 * Benefits:
 * - ✅ Single point of provider switching (change one flag)
 * - ✅ No ViewModel changes needed when swapping providers
 * - ✅ Automatic fallback to available providers
 * - ✅ Testable in isolation (inject fake providers)
 *
 * Usage:
 * ```kotlin
 * val factory = OnRampProviderFactory()
 * val provider = factory.getProvider() // Returns best available provider
 * val url = provider.buildWidgetUrl(wallet, ...)
 * ```
 */
class OnRampProviderFactory {

    // Provider instances (lazy initialization)
    private val rampProvider by lazy { RampProvider() }
    private val moonPayProvider by lazy { MoonPayProvider() }
    private val transakProvider by lazy { TransakProvider() }

    /**
     * Get active provider based on feature flags.
     *
     * Priority:
     * 1. Check OnRampConfig.DEFAULT_PROVIDER
     * 2. Validate provider configuration
     * 3. Fallback to next available provider if validation fails
     * 4. Return error if no providers available
     *
     * @param preferredType Optional preferred provider (overrides default)
     * @return Result with provider or error
     */
    fun getProvider(
        preferredType: OnRampProviderType? = null
    ): Result<OnRampProvider> {
        Timber.d("🏭 OnRampProviderFactory: Selecting provider")

        // Determine provider type to use (with regional awareness)
        val targetType = preferredType ?: run {
            val userCountry = ProviderInfoHelper.detectUserCountryCode()
            OnRampConfig.getDefaultProviderForRegion(userCountry)
        }

        // Try to get target provider
        val targetProvider = getProviderInstance(targetType)
        if (targetProvider != null) {
            // Validate configuration
            targetProvider.validateConfiguration().fold(
                onSuccess = {
                    Timber.i("✅ Selected provider: ${targetProvider.getDisplayName()}")
                    return Result.success(targetProvider)
                },
                onFailure = { error ->
                    Timber.w("⚠️ ${targetProvider.getDisplayName()} validation failed: ${error.message}")
                    // Continue to fallback logic
                }
            )
        }

        // Fallback: Try other enabled providers
        val fallbackResult = findFirstAvailableProvider(excludeType = targetType)
        if (fallbackResult != null) {
            Timber.i("🔄 Fallback to: ${fallbackResult.getDisplayName()}")
            return Result.success(fallbackResult)
        }

        // No providers available
        val errorMessage = buildNoProvidersErrorMessage()
        Timber.e("❌ $errorMessage")
        return Result.failure(Exception(errorMessage))
    }

    /**
     * Get provider instance by type (if enabled).
     *
     * @param type Provider type
     * @return Provider instance or null if disabled
     */
    private fun getProviderInstance(type: OnRampProviderType): OnRampProvider? {
        return when (type) {
            OnRampProviderType.RAMP -> {
                if (OnRampConfig.RAMP_ENABLED) rampProvider else null
            }
            OnRampProviderType.MOONPAY -> {
                if (OnRampConfig.MOONPAY_ENABLED) moonPayProvider else null
            }
            OnRampProviderType.TRANSAK -> {
                if (OnRampConfig.TRANSAK_ENABLED) transakProvider else null
            }
            OnRampProviderType.ONRAMPER -> {
                // Not implemented yet
                null
            }
        }
    }

    /**
     * Find first available and configured provider.
     *
     * @param excludeType Provider type to exclude from search
     * @return First valid provider or null
     */
    private fun findFirstAvailableProvider(
        excludeType: OnRampProviderType
    ): OnRampProvider? {
        val candidates = listOf(
            OnRampProviderType.MOONPAY,
            OnRampProviderType.TRANSAK,
            OnRampProviderType.RAMP
        ).filter { it != excludeType }

        for (type in candidates) {
            val provider = getProviderInstance(type) ?: continue

            // Try to validate
            val validationResult = provider.validateConfiguration()
            if (validationResult.isSuccess) {
                return provider
            }
        }

        return null
    }

    /**
     * Build helpful error message with actionable steps.
     */
    private fun buildNoProvidersErrorMessage(): String {
        val enabledProviders = buildList {
            if (OnRampConfig.RAMP_ENABLED) add("Ramp")
            if (OnRampConfig.MOONPAY_ENABLED) add("MoonPay")
            if (OnRampConfig.TRANSAK_ENABLED) add("Transak")
        }

        return when {
            enabledProviders.isEmpty() -> {
                """
                No on-ramp providers enabled.
                
                Action Required:
                1. Enable at least one provider in OnRampConfig
                2. Set MOONPAY_ENABLED = true for immediate testing
                3. Or set TRANSAK_ENABLED = true
                
                Recommended: Enable MoonPay for fastest setup
                """.trimIndent()
            }
            else -> {
                """
                All enabled providers failed validation.
                
                Enabled: ${enabledProviders.joinToString(", ")}
                
                Common Issues:
                - Missing API keys in OnRampConfig
                - Placeholder keys not replaced (e.g., "pk_test_123")
                
                For MoonPay:
                1. Sign up at moonpay.com/dashboard
                2. Get sandbox API key (instant)
                3. Update OnRampConfig.MoonPayCredentials.API_KEY
                
                For Transak:
                1. Sign up at transak.com/developers
                2. Get API key (instant)
                3. Update OnRampConfig.TransakCredentials.API_KEY
                
                For Ramp:
                1. Register at ramp.network/partners
                2. Wait 24-72 hours for approval
                3. Update OnRampConfig.RampCredentials.API_KEY
                """.trimIndent()
            }
        }
    }

    /**
     * Get all available providers (for UI selection).
     *
     * @return List of configured and enabled providers
     */
    fun getAvailableProviders(): List<OnRampProvider> {
        return buildList {
            if (OnRampConfig.MOONPAY_ENABLED) {
                moonPayProvider.validateConfiguration().onSuccess {
                    add(moonPayProvider)
                }
            }
            if (OnRampConfig.TRANSAK_ENABLED) {
                transakProvider.validateConfiguration().onSuccess {
                    add(transakProvider)
                }
            }
            if (OnRampConfig.RAMP_ENABLED) {
                rampProvider.validateConfiguration().onSuccess {
                    add(rampProvider)
                }
            }
        }
    }

    /**
     * Check if specific provider is available.
     *
     * @param type Provider type to check
     * @return true if provider is enabled and configured
     */
    fun isProviderAvailable(type: OnRampProviderType): Boolean {
        val provider = getProviderInstance(type) ?: return false
        return provider.validateConfiguration().isSuccess
    }

    /**
     * Get provider recommendations for specific requirements.
     *
     * @param fiatCurrency Required fiat currency (e.g., "NGN")
     * @param cryptoAsset Required crypto asset
     * @param chainId Blockchain identifier
     * @return List of providers supporting requirements, ordered by preference
     */
    fun getRecommendedProviders(
        fiatCurrency: String,
        cryptoAsset: String,
        chainId: String
    ): List<OnRampProvider> {
        return getAvailableProviders()
            .filter { provider ->
                provider.supportsCurrency(fiatCurrency) &&
                        provider.supportsCryptoAsset(cryptoAsset, chainId)
            }
            .sortedBy { provider ->
                // Preference order for Nigerian users
                when (provider.providerType) {
                    OnRampProviderType.TRANSAK -> 1  // Best for Africa
                    OnRampProviderType.MOONPAY -> 2  // Good Nigerian support
                    OnRampProviderType.RAMP -> 3     // Limited NGN support
                    OnRampProviderType.ONRAMPER -> 4
                }
            }
    }
}

/**
 * Extension: Result helpers for cleaner provider selection.
 */
fun OnRampProviderFactory.getProviderOrThrow(
    preferredType: OnRampProviderType? = null
): OnRampProvider {
    return getProvider(preferredType).getOrThrow()
}

fun OnRampProviderFactory.getProviderOrNull(
    preferredType: OnRampProviderType? = null
): OnRampProvider? {
    return getProvider(preferredType).getOrNull()
}