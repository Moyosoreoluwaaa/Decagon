package com.decagon.core.chains

data class ChainConfig(
    val type: ChainType,
    val rpcUrl: String,
    val explorerUrl: String,
    val nativeCurrency: String,
    val symbol: String,
    val decimals: Int,
    val iconUrl: String // Change to URL
)