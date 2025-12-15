package com.decagon.core.chains

// In ChainType.kt
sealed interface ChainType {
    val id: String
    val name: String
    val coinType: Int
    val symbol: String  // Add this

    data object Solana : ChainType {
        override val id = "solana"
        override val name = "Solana"
        override val coinType = 501
        override val symbol = "SOL"  // Add this
    }

    data object Ethereum : ChainType {
        override val id = "ethereum"
        override val name = "Ethereum"
        override val coinType = 60
        override val symbol = "ETH"  // Add this
    }

    data object Polygon : ChainType {
        override val id = "polygon"
        override val name = "Polygon"
        override val coinType = 966
        override val symbol = "MATIC"  // Add this
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