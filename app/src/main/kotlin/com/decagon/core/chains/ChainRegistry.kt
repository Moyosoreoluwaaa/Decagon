package com.decagon.core.chains

import com.decagon.core.network.NetworkConfig

object ChainRegistry {
    private val configs = mapOf(
        ChainType.Solana.id to ChainConfig(
            type = ChainType.Solana,
            networks = NetworkConfig(
                mainnetUrl = "https://api.mainnet-beta.solana.com",
                devnetUrl = "https://api.devnet.solana.com",
                testnetUrl = "https://api.testnet.solana.com"
            ),
            explorerUrl = "https://solscan.io",
            nativeCurrency = "Solana",
            symbol = "SOL",
            decimals = 9,
            iconUrl = "https://cryptologos.cc/logos/solana-sol-logo.png"
        ),
        ChainType.Ethereum.id to ChainConfig(
            type = ChainType.Ethereum,
            networks = NetworkConfig(
                mainnetUrl = "https://eth-mainnet.alchemyapi.io/v2/demo",
                devnetUrl = "https://eth-sepolia.g.alchemy.com/v2/demo",
                testnetUrl = "https://eth-goerli.g.alchemy.com/v2/demo"
            ),
            explorerUrl = "https://etherscan.io",
            nativeCurrency = "Ethereum",
            symbol = "ETH",
            decimals = 18,
            iconUrl = "https://cryptologos.cc/logos/ethereum-eth-logo.png"
        ),
        ChainType.Polygon.id to ChainConfig(
            type = ChainType.Polygon,
            networks = NetworkConfig(
                mainnetUrl = "https://polygon-rpc.com",
                devnetUrl = "https://rpc-mumbai.maticvigil.com",
                testnetUrl = "https://rpc-mumbai.maticvigil.com"
            ),
            explorerUrl = "https://polygonscan.com",
            nativeCurrency = "Polygon",
            symbol = "MATIC",
            decimals = 18,
            iconUrl = "https://cryptologos.cc/logos/polygon-matic-logo.png"
        )
    )

    fun getSupportedChains(): List<ChainConfig> = configs.values.toList()
    fun getChain(id: String): ChainConfig = configs[id]
        ?: throw IllegalArgumentException("Unknown chain: $id")
    fun getChain(type: ChainType): ChainConfig = configs[type.id]!!
}