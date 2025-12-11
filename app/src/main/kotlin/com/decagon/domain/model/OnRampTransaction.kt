package com.decagon.domain.model

data class OnRampTransaction(
    val id: String,
    val walletId: String,
    val walletAddress: String,
    val chainId: String,
    val fiatAmount: Double,
    val fiatCurrency: String,
    val cryptoAmount: Double?,
    val cryptoAsset: String,
    val provider: OnRampProvider,
    val providerTxId: String?,
    val status: OnRampStatus,
    val createdAt: Long,
    val completedAt: Long? = null,
    val signature: String?,
    val errorMessage: String?
) {
    val formattedFiatAmount: String
        get() = "%.2f %s".format(fiatAmount, fiatCurrency.uppercase())

    val formattedCryptoAmount: String?
        get() = cryptoAmount?.let { "%.4f %s".format(it, cryptoAsset) }
}

enum class OnRampProvider(val displayName: String) {
    RAMP("Ramp Network"),
    ONRAMPER("Onramper"),
    YELLOW_CARD("Yellow Card");

    companion object {
        fun from(name: String): OnRampProvider = when (name.lowercase()) {
            "ramp" -> RAMP
            "onramper" -> ONRAMPER
            "yellow_card" -> YELLOW_CARD
            else -> RAMP
        }
    }
}

enum class OnRampStatus {
    INITIATED,   // User clicked "Buy", widget opened
    PENDING,     // Payment processing
    COMPLETED,   // Funds received on-chain
    FAILED,      // Transaction failed
    CANCELLED    // User cancelled
}
