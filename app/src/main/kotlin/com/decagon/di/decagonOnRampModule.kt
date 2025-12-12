// ============================================================================
// FILE: di/decagonOnRampModule.kt
// UPDATED: Multi-provider dependency injection
// ============================================================================

package com.decagon.di

import com.decagon.data.provider.MoonPayProvider
import com.decagon.data.provider.RampProvider
import com.decagon.data.provider.TransakProvider
import com.decagon.data.repository.OnRampRepositoryImpl
import com.decagon.domain.provider.OnRampProviderFactory
import com.decagon.domain.repository.OnRampRepository
import com.decagon.ui.screen.onramp.DecagonOnRampViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * On-ramp dependency injection module.
 *
 * Provides:
 * - Provider implementations (Ramp, MoonPay, Transak)
 * - Provider factory (orchestrator)
 * - Repository
 * - ViewModel
 *
 * Pattern: Orchestration via factory
 */
val decagonOnRampModule = module {

    // ========================================================================
    // PROVIDER IMPLEMENTATIONS
    // ========================================================================

    /**
     * Ramp Network provider.
     * Requires API key from partnership registration.
     */
    single { RampProvider() }

    /**
     * MoonPay provider.
     * Recommended for immediate testing (sandbox available).
     */
    single { MoonPayProvider() }

    /**
     * Transak provider.
     * Alternative for immediate testing (instant sandbox).
     */
    single { TransakProvider() }

    // ========================================================================
    // PROVIDER FACTORY (ORCHESTRATOR)
    // ========================================================================

    /**
     * Provider factory for automatic provider selection.
     *
     * Handles:
     * - Feature flag evaluation
     * - Configuration validation
     * - Fallback logic
     *
     * ViewModel depends on factory (not specific providers).
     */
    single { OnRampProviderFactory() }

    // ========================================================================
    // REPOSITORY
    // ========================================================================

    /**
     * On-ramp repository for transaction persistence.
     */
    single<OnRampRepository> {
        OnRampRepositoryImpl(
            onRampDao = get()
        )
    }

    // ========================================================================
    // VIEWMODEL
    // ========================================================================

    /**
     * On-ramp ViewModel with multi-provider support.
     */
    viewModel {
        DecagonOnRampViewModel(
            onRampRepository = get(),
            rpcClient = get(),
            providerFactory = get() // ✅ Inject factory, not specific provider
        )
    }
}

/**
 * Extension: Validate on-ramp module configuration.
 * Call this at app startup to catch configuration issues early.
 */
fun validateOnRampConfiguration() {
    val factory = OnRampProviderFactory()

    val providerResult = factory.getProvider()
    if (providerResult.isFailure) {
        timber.log.Timber.e(
            """
            ⚠️ ON-RAMP CONFIGURATION WARNING
            
            ${providerResult.exceptionOrNull()?.message}
            
            The on-ramp feature will not work until you configure at least one provider.
            """.trimIndent()
        )
    } else {
        val provider = providerResult.getOrThrow()
        timber.log.Timber.i("✅ On-ramp ready with ${provider.getDisplayName()}")
    }
}