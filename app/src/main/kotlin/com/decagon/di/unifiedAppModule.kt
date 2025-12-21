package com.decagon.di

import com.octane.wallet.core.di.coreModule
import com.octane.wallet.data.di.preferencesModule
import com.octane.wallet.domain.di.domainModule
import com.wallet.di.networkModule
import com.wallet.di.viewModelModule
import org.koin.dsl.module

val unifiedAppModule = module {
    // Merge core modules (prioritize Decagon's)
    includes(
        // Decagon Core (network-aware, real crypto)
        decagonCoreModule,
        decagonNetworkModule,
        
        // Octane Features (UI, Discover, DApps)
        coreModule,  // For Octane-specific utilities
        networkModule,
        preferencesModule,
        domainModule,
        
        // Feature modules
        decagonWalletModule,
        decagonTransactionModule,
        decagonSwapModule,
        decagonOnRampModule,
//        viewModelModule  // Octane VMs for Discover/DApps
    )
}