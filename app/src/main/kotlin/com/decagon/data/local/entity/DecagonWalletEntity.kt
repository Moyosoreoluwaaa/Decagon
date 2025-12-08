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
    val encryptedSeed: ByteArray,        // 64-byte BIP39 seed (encrypted)
    val encryptedMnemonic: ByteArray,    // âœ… NEW: 12/24 word phrase (encrypted)
    val publicKey: String,               // Hex-encoded Ed25519 public key
    val address: String,                 // Base58 Solana address
    val accountIndex: Int = 0,           // BIP44 account index
    val createdAt: Long,
    val isActive: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DecagonWalletEntity

        if (id != other.id) return false
        if (name != other.name) return false
        if (!encryptedSeed.contentEquals(other.encryptedSeed)) return false
        if (!encryptedMnemonic.contentEquals(other.encryptedMnemonic)) return false
        if (publicKey != other.publicKey) return false
        if (address != other.address) return false
        if (accountIndex != other.accountIndex) return false
        if (createdAt != other.createdAt) return false
        if (isActive != other.isActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + encryptedSeed.contentHashCode()
        result = 31 * result + encryptedMnemonic.contentHashCode()
        result = 31 * result + publicKey.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + accountIndex
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + isActive.hashCode()
        return result
    }
}