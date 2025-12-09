package com.decagon.domain.model

import com.decagon.core.chains.ChainType
import kotlinx.serialization.Serializable

@Serializable
data class ChainWallet(
    val chainId: String,
    val address: String,
    val publicKey: String,
    val balance: Double = 0.0
) {
    val chainType: ChainType get() = ChainType.fromId(chainId)
}
