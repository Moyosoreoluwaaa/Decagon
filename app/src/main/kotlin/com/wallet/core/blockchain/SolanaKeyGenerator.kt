package com.wallet.core.blockchain

import android.util.Base64

/**
 * Solana keypair generator and signer.
 * Abstracts cryptographic operations.
 */
interface SolanaKeyGenerator {
    /**
     * Generate a new random keypair.
     */
    fun generateKeypair(): SolanaKeypair
    
    /**
     * Derive keypair from BIP39 seed phrase.
     */
    fun fromSeedPhrase(seedPhrase: String): SolanaKeypair
    
    /**
     * Sign transaction bytes with private key.
     */
    fun signTransaction(txBytes: ByteArray, privateKey: ByteArray): ByteArray
}

data class SolanaKeypair(
    val publicKey: String,  // Base58 encoded
    val privateKey: ByteArray  // Raw bytes (32 bytes)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SolanaKeypair) return false
        return publicKey == other.publicKey
    }
    
    override fun hashCode(): Int = publicKey.hashCode()
}

// Extension: ByteArray to Base64
fun ByteArray.toBase64(): String = 
    Base64.encodeToString(this, Base64.NO_WRAP)
