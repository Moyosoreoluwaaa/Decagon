package com.decagon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for wallet storage.
 * 
 * Storage:
 * - id: Unique wallet identifier
 * - name: User-friendly name
 * - encryptedSeed: AES-GCM encrypted seed (IV + ciphertext)
 * - publicKey: Solana public key (Base58)
 * - accountIndex: BIP44 account index
 * - createdAt: Timestamp
 * 
 * Security: encryptedSeed never stored as plaintext.
 */
@Entity(tableName = "decagon_wallets")
data class DecagonWalletEntity(
    @PrimaryKey
    val id: String,
    
    val name: String,
    
    /**
     * Encrypted seed (IV + ciphertext).
     * NEVER store as plaintext.
     */
    val encryptedSeed: ByteArray,
    
    /**
     * Base58 encoded Solana public key.
     */
    val publicKey: String,

    val address: String, // ✅ Store the Base58 address

    /**
     * BIP44 account index (default: 0).
     */
    val accountIndex: Int = 0,
    
    /**
     * Creation timestamp (millis).
     */
    val createdAt: Long = System.currentTimeMillis(),
    
    /**
     * Is this the active wallet?
     */
    val isActive: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DecagonWalletEntity
        return id == other.id &&
                name == other.name &&
                encryptedSeed.contentEquals(other.encryptedSeed) &&
                publicKey == other.publicKey &&
                address == other.address &&
                accountIndex == other.accountIndex &&
                createdAt == other.createdAt &&
                isActive == other.isActive
    }
    
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + encryptedSeed.contentHashCode()
        result = 31 * result + publicKey.hashCode()
        result = 31 * result + accountIndex
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + isActive.hashCode()
        return result
    }
}