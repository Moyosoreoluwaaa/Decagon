package com.decagon.core.chains

sealed interface ChainType {
    val id: String
    val name: String
    val coinType: Int

    data object Solana : ChainType {
        override val id = "solana"  // lowercase
        override val name = "Solana"
        override val coinType = 501
    }

    data object Ethereum : ChainType {
        override val id = "ethereum"  // lowercase
        override val name = "Ethereum"
        override val coinType = 60
    }

    data object Polygon : ChainType {
        override val id = "polygon"  // lowercase
        override val name = "Polygon"
        override val coinType = 966
    }

    companion object {
        fun fromId(id: String): ChainType = when(id.lowercase()) {
            Solana.id -> Solana
            Ethereum.id -> Ethereum
            Polygon.id -> Polygon
            else -> throw IllegalArgumentException("Unknown chain: $id")
        }
    }
}