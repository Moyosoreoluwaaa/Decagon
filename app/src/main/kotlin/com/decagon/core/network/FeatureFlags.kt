package com.decagon.core.network

object FeatureFlags {
    fun isBuyEnabled(network: NetworkEnvironment): Boolean {
        return network == NetworkEnvironment.MAINNET
    }
    
    fun isSwapEnabled(network: NetworkEnvironment): Boolean {
        return network == NetworkEnvironment.MAINNET
    }
    
    fun isSendEnabled(network: NetworkEnvironment): Boolean {
        return true // Allow on all networks
    }
    
    fun getNetworkLabel(network: NetworkEnvironment): String = when (network) {
        NetworkEnvironment.MAINNET -> ""
        NetworkEnvironment.DEVNET -> " (Spectating)"
        NetworkEnvironment.TESTNET -> " (Spectating)"
    }
}