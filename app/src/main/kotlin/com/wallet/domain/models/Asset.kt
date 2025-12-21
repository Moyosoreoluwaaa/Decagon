package com.octane.wallet.domain.models

data class Asset(
    val id: String,
    val walletId: String,
    val chainId: String,
    val symbol: String,
    val name: String,
    val mintAddress: String?,
    val balance: String,
    val decimals: Int,
    val priceUsd: Double? = null,
    val valueUsd: Double? = null,
    val priceChange24h: Double? = null,
    val iconUrl: String? = null,
    val isNative: Boolean = false,
    val isHidden: Boolean = false,
    val costBasisUsd: Double? = null,
    val lastUpdated: Long
) {
    val balanceDouble: Double
        get() = balance.toDoubleOrNull() ?: 0.0
    
    val profitLossUsd: Double?
        get() = if (valueUsd != null && costBasisUsd != null) {
            valueUsd - costBasisUsd
        } else null
    
    val profitLossPercentage: Double?
        get() = if (profitLossUsd != null && costBasisUsd != null && costBasisUsd > 0) {
            (profitLossUsd!! / costBasisUsd) * 100
        } else null
}