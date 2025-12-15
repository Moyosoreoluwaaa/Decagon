package com.decagon.core.network

enum class NetworkEnvironment(val displayName: String) {
    MAINNET("Mainnet"),
    DEVNET("Devnet"), 
    TESTNET("Testnet");
    
    companion object {
        fun fromString(value: String): NetworkEnvironment = 
            values().find { it.name.equals(value, ignoreCase = true) } ?: DEVNET
    }
}