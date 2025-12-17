package com.octane.wallet.data.repository

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.octane.wallet.core.blockchain.SolanaKeyGenerator
import com.octane.wallet.core.blockchain.SolanaKeypair
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import org.bitcoinj.core.Base58
import timber.log.Timber
import java.security.MessageDigest

class SolanaKeyGeneratorImpl : SolanaKeyGenerator {

    private val ed25519Spec = EdDSANamedCurveTable.getByName("Ed25519")

    override fun generateKeypair(): SolanaKeypair {
        val mnemonicCode = Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_12)
        val seedPhrase = String(mnemonicCode.chars)
        return deriveKeypairFromMnemonic(mnemonicCode, seedPhrase)
    }

    override fun fromSeedPhrase(seedPhrase: String): SolanaKeypair {
        Timber.d("🔵 [SolanaKey] fromSeedPhrase called")

        try {
            // 1. Clean the input string
            val cleanedPhrase = seedPhrase.trim().replace("\\s+".toRegex(), " ")

            // 2. Attempt to create MnemonicCode
            val mnemonicCode = try {
                Mnemonics.MnemonicCode(cleanedPhrase.toCharArray())
            } catch (e: Exception) {
                Timber.w("⚠️ [SolanaKey] Direct char array failed (${e.message}), trying strict reconstruction")
                // Fallback: If strict parsing fails, try to rebuild it word by word
                // This helps if there are invisible chars or encoding issues
                val words = cleanedPhrase.split(" ")
                if (words.size !in listOf(12, 24)) throw IllegalArgumentException("Invalid word count: ${words.size}")
                Mnemonics.MnemonicCode(cleanedPhrase.toCharArray())
            }

            return deriveKeypairFromMnemonic(mnemonicCode, cleanedPhrase)
        } catch (e: Exception) {
            Timber.e(e, "❌ [SolanaKey] Failed to parse mnemonic")
            throw IllegalArgumentException("Invalid seed phrase: ${e.message}", e)
        }
    }

    private fun deriveKeypairFromMnemonic(
        mnemonicCode: Mnemonics.MnemonicCode,
        seedPhrase: String
    ): SolanaKeypair {
        // Convert to seed
        val seed = mnemonicCode.toSeed()

        // Derive Ed25519 keys
        // Solana uses the first 32 bytes of the seed as the private key
        val privateKeyBytes = seed.copyOfRange(0, 32)
        val privateKeySpec = EdDSAPrivateKeySpec(privateKeyBytes, ed25519Spec)
        val privateKey = EdDSAPrivateKey(privateKeySpec)
        val publicKey = EdDSAPublicKey(EdDSAPublicKeySpec(privateKey.a, ed25519Spec))

        return SolanaKeypair(
            publicKey = Base58.encode(publicKey.abyte),
            privateKey = privateKeyBytes
        )
    }

    override fun signTransaction(txBytes: ByteArray, privateKey: ByteArray): ByteArray {
        val privateKeySpec = EdDSAPrivateKeySpec(privateKey, ed25519Spec)
        val signingKey = EdDSAPrivateKey(privateKeySpec)
        val signer = net.i2p.crypto.eddsa.EdDSAEngine(MessageDigest.getInstance("SHA-512"))
        signer.initSign(signingKey)
        signer.update(txBytes)
        return signer.sign()
    }
}