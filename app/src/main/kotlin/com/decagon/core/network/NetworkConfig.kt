package com.decagon.core.network

data class NetworkConfig(
    val mainnetUrl: String,
    val devnetUrl: String,
    val testnetUrl: String
) {
    fun getUrl(env: NetworkEnvironment): String = when (env) {
        NetworkEnvironment.MAINNET -> mainnetUrl
        NetworkEnvironment.DEVNET -> devnetUrl
        NetworkEnvironment.TESTNET -> testnetUrl
    }
}