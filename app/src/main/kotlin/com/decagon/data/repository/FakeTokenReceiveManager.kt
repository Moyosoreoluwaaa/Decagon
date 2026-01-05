package com.decagon.data.repository

import com.decagon.domain.model.TokenBalance

class FakeTokenReceiveManager : TokenReceiveManager {
    var discoverCalled = false
    var lastAddress: String? = null
    private var balancesToReturn: List<TokenBalance> = emptyList()

    override suspend fun discoverNewTokens(walletAddress: String): Result<List<TokenBalance>> {
        discoverCalled = true
        lastAddress = walletAddress
        return Result.success(balancesToReturn)
    }

    override suspend fun ensureTokenAccount(
        walletAddress: String,
        tokenMint: String,
        chainId: String
    ): Result<String> {
        return Result.success("fake_ata_address")
    }

    // Test helpers
    fun setBalances(balances: List<TokenBalance>) {
        balancesToReturn = balances
    }

    fun reset() {
        discoverCalled = false
        lastAddress = null
        balancesToReturn = emptyList()
    }
}