package com.decagon.data.local.dao

import com.decagon.data.local.entity.TokenBalanceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTokenBalanceDao : TokenBalanceDao {
    private val balances = MutableStateFlow<List<TokenBalanceEntity>>(emptyList())

    override fun getByWallet(address: String): Flow<List<TokenBalanceEntity>> {
        return balances.map { list ->
            list.filter { it.walletAddress == address }
                .sortedByDescending { it.valueUsd }
        }
    }

    override fun getBalance(address: String, mint: String): Flow<TokenBalanceEntity?> {
        return balances.map { list ->
            list.find { it.walletAddress == address && it.mint == mint }
        }
    }

    override suspend fun insertAll(balances: List<TokenBalanceEntity>) {
        val current = this.balances.value.toMutableList()
        balances.forEach { new ->
            current.removeIf { it.id == new.id }
            current.add(new)
        }
        this.balances.value = current
    }

    override suspend fun insert(balance: TokenBalanceEntity) {
        insertAll(listOf(balance))
    }

    override suspend fun deleteByWallet(address: String) {
        balances.value = balances.value.filter { it.walletAddress != address }
    }

    override suspend fun deleteStale(timestamp: Long) {
        balances.value = balances.value.filter { it.lastUpdated >= timestamp }
    }

    override fun getCount(address: String): Flow<Int> {
        return balances.map { list ->
            list.count { it.walletAddress == address }
        }
    }

    override fun exists(address: String, mint: String): Flow<Boolean> {
        return balances.map { list ->
            list.any { it.walletAddress == address && it.mint == mint }
        }
    }

    // Test helpers
    fun insert(balances: List<TokenBalanceEntity>) {
        this.balances.value = balances
    }

    fun clear() {
        balances.value = emptyList()
    }
}