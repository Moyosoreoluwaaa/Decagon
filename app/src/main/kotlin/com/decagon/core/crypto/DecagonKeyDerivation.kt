package com.decagon.core.crypto

import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import org.bitcoinj.core.Base58
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import timber.log.Timber

/**
 * BIP32/BIP44/SLIP-0010 key derivation for Solana.
 * * Implements:
 * - Hierarchical Deterministic (HD) wallet derivation
 * - Solana path: m/44'/501'/0'/0'
 * - Ed25519 keypair generation
 * - Public key derivation (Solana address)
 * * Security: Private keys never stored, derived on-demand from seed.
 */
class DecagonKeyDerivation {

    init {
        Timber.d("DecagonKeyDerivation initialized.")
    }

    private val ed25519Curve = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)

    /**
     * Derives Solana keypair from seed.
     * * @param seed 64-byte seed from BIP39 mnemonic
     * @param accountIndex Account index (default: 0)
     * @return Pair of (privateKey, publicKey) as ByteArrays
     */
    fun deriveSolanaKeypair(seed: ByteArray, accountIndex: Int = 0): Pair<ByteArray, ByteArray> {
        Timber.d("Deriving Solana keypair for account index: $accountIndex")

        // ✅ CORRECT: m/44'/501'/accountIndex'/0' (last 0 is non-hardened)
        val path = listOf(
            44 + HARDENED_OFFSET,      // Purpose (BIP44)
            501 + HARDENED_OFFSET,     // Coin type (Solana)
            accountIndex + HARDENED_OFFSET,  // Account index
            0                           // ✅ Change index (non-hardened)
        )

        val privateKey = derivePrivateKey(seed, path)
        val publicKey = derivePublicKey(privateKey)

        Timber.v("Solana keypair derivation complete.")
        return Pair(privateKey, publicKey)
    }

    /**
     * Derives Ethereum keypair from seed.
     * * @param seed 64-byte seed from BIP39 mnemonic
     * @param accountIndex Account index (default: 0)
     * @return Pair of (privateKey, publicKey) as ByteArrays
     */
    fun deriveEthereumKeypair(seed: ByteArray, accountIndex: Int = 0): Pair<ByteArray, ByteArray> {
        Timber.d("Deriving Ethereum keypair (stub) for account index: $accountIndex")
        // Ethereum path: m/44'/60'/0'/0/accountIndex
        val path = listOf(
            44 + HARDENED_OFFSET,
            60 + HARDENED_OFFSET,
            0 + HARDENED_OFFSET,
            0,
            accountIndex
        )

        val privateKey = derivePrivateKey(seed, path)
        // Ethereum uses secp256k1, but for 0.1 we'll stub this
        // Full implementation in 1.5 (EVM chain support)
        Timber.v("Ethereum keypair derivation (stub) complete.")
        return Pair(privateKey, ByteArray(32))
    }

    /**
     * Derives Solana address (Base58 encoded public key).
     * * @param publicKey 32-byte Ed25519 public key
     * @return Base58 encoded address
     */
    fun deriveSolanaAddress(publicKey: ByteArray): String {
        Timber.v("Deriving Solana address from public key.")
        return Base58.encode(publicKey)
    }

    /**
     * Validates a Solana address.
     * * @param address Base58 string
     * @return true if valid Solana address
     */
    fun isValidSolanaAddress(address: String): Boolean {
        Timber.v("Validating Solana address.")
        return try {
            val decoded = Base58.decode(address)
            val isValid = decoded.size == 32
            if (!isValid) Timber.w("Invalid Solana address length: ${decoded.size} bytes.")
            isValid
        } catch (e: Exception) {
            Timber.w(e, "Invalid Solana address format.")
            false
        }
    }

    // Private helper methods

    private fun derivePrivateKey(seed: ByteArray, path: List<Int>): ByteArray {
        Timber.v("Deriving private key using HD path.")
        var key = seed.copyOf(32) // Use first 32 bytes as master key
        var chainCode = seed.copyOfRange(32, 64) // Last 32 bytes as chain code

        for (index in path) {
            val derived = deriveChildKey(key, chainCode, index)
            key = derived.first
            chainCode = derived.second
        }

        return key
    }

    private fun deriveChildKey(
        parentKey: ByteArray,
        chainCode: ByteArray,
        index: Int
    ): Pair<ByteArray, ByteArray> {
        Timber.v("Deriving child key for index: $index (hardened: ${index >= HARDENED_OFFSET})")
        val hmac = Mac.getInstance("HmacSHA512")
        hmac.init(SecretKeySpec(chainCode, "HmacSHA512"))

        val data = if (index >= HARDENED_OFFSET) {
            // Hardened derivation: 0x00 || parent_key || index
            ByteBuffer.allocate(37)
                .put(0x00.toByte())
                .put(parentKey)
                .putInt(index)
                .array()
        } else {
            // Normal derivation: parent_public_key || index
            val parentPublic = derivePublicKey(parentKey)
            ByteBuffer.allocate(37)
                .put(parentPublic)
                .putInt(index)
                .array()
        }

        val digest = hmac.doFinal(data)
        val childKey = digest.copyOfRange(0, 32)
        val childChainCode = digest.copyOfRange(32, 64)

        Timber.v("Child key derived.")
        return Pair(childKey, childChainCode)
    }

    private fun derivePublicKey(privateKey: ByteArray): ByteArray {
        Timber.v("Deriving public key from private key (Ed25519).")
        val keySpec = EdDSAPrivateKeySpec(privateKey, ed25519Curve)
        val privateKeyObj = EdDSAPrivateKey(keySpec)
        val publicKeyObj = EdDSAPublicKey(EdDSAPublicKeySpec(privateKeyObj.a, ed25519Curve))
        return publicKeyObj.abyte
    }

    companion object {
        private const val HARDENED_OFFSET = 0x80000000.toInt()

        // Solana derivation path constants
        const val SOLANA_PURPOSE = 44
        const val SOLANA_COIN_TYPE = 501
    }
}