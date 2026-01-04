package com.decagon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Wallet entity - persists encrypted wallet data.
 *
 * Version 0.2.1: Added encryptedMnemonic field for recovery phrase backup.
 */

@Entity(tableName = "decagon_wallets")
data class DecagonWalletEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val encryptedSeed: ByteArray,
    val encryptedMnemonic: ByteArray,
    val publicKey: String,
    val address: String,
    val accountIndex: Int = 0,
    val createdAt: Long,
    val isActive: Boolean = false,
    val chains: String = "[]",
    val activeChainId: String = "solana",

    // ✅ NEW: Cached balance fields
    val cachedBalance: Double = 0.0,
    val lastBalanceFetch: Long = 0L,
    val balanceStale: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DecagonWalletEntity

        return id == other.id &&
                name == other.name &&
                encryptedSeed.contentEquals(other.encryptedSeed) &&
                encryptedMnemonic?.contentEquals(other.encryptedMnemonic ?: byteArrayOf()) ?: (other.encryptedMnemonic == null) &&
                publicKey == other.publicKey &&
                address == other.address &&
                accountIndex == other.accountIndex &&
                createdAt == other.createdAt &&
                isActive == other.isActive &&
                chains == other.chains &&
                activeChainId == other.activeChainId &&
                cachedBalance == other.cachedBalance &&
                lastBalanceFetch == other.lastBalanceFetch &&
                balanceStale == other.balanceStale
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + encryptedSeed.contentHashCode()
        result = 31 * result + (encryptedMnemonic?.contentHashCode() ?: 0)
        result = 31 * result + publicKey.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + accountIndex
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + isActive.hashCode()
        result = 31 * result + chains.hashCode()
        result = 31 * result + activeChainId.hashCode()
        result = 31 * result + cachedBalance.hashCode()
        result = 31 * result + lastBalanceFetch.hashCode()
        result = 31 * result + balanceStale.hashCode()
        return result
    }
}