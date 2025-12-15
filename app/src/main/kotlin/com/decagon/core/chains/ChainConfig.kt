package com.decagon.core.chains

import com.decagon.core.network.NetworkConfig

data class ChainConfig(
    val type: ChainType,
    val networks: NetworkConfig,  // ← CHANGED from rpcUrl: String
    val explorerUrl: String,
    val nativeCurrency: String,
    val symbol: String,
    val decimals: Int,
    val iconUrl: String
)