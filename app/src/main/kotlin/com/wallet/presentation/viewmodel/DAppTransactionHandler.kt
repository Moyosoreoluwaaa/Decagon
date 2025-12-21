package com.octane.wallet.presentation.viewmodel

import android.util.Base64
import com.wallet.core.blockchain.SolanaKeyGenerator
import com.wallet.core.blockchain.TransactionRequest
import com.octane.wallet.core.security.KeystoreManager
import com.octane.wallet.domain.models.Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Handles DApp transaction signing and message signing.
 * Separates signing logic from UI layer.
 */
class DAppTransactionHandler(
    private val keystoreManager: KeystoreManager,
    private val solanaKeyGenerator: SolanaKeyGenerator,
    private val scope: CoroutineScope
) {
    /**
     * Sign transaction for DApp.
     */
    fun signTransaction(
        wallet: Wallet,
        request: TransactionRequest,
        onSuccess: (signedTxBase64: String) -> Unit,
        onError: (error: String) -> Unit
    ) {
        scope.launch {
            try {
                // Decode transaction from base64
                val txBytes = Base64.decode(request.transactionBase64, Base64.DEFAULT)

                // Get encrypted private key
                val encryptedKey = keystoreManager.getPrivateKey(wallet.id).getOrThrow()

                // Decrypt private key
                val privateKey = keystoreManager.decryptPrivateKey(encryptedKey).getOrThrow()

                // Sign transaction
                val signature = solanaKeyGenerator.signTransaction(txBytes, privateKey)

                // Clear private key from memory
                privateKey.fill(0)

                // Combine transaction + signature and encode to base64
                val signedTx = txBytes + signature
                val signedTxBase64 = Base64.encodeToString(signedTx, Base64.NO_WRAP)

                onSuccess(signedTxBase64)
            } catch (e: Exception) {
                onError("Failed to sign transaction: ${e.message}")
            }
        }
    }

    /**
     * Sign message for DApp (for authentication/verification).
     */
    fun signMessage(
        wallet: Wallet,
        request: TransactionRequest,
        onSuccess: (signatureBase64: String) -> Unit,
        onError: (error: String) -> Unit
    ) {
        scope.launch {
            try {
                // Decode message from base64
                val messageBytes = Base64.decode(request.transactionBase64, Base64.DEFAULT)

                // Get encrypted private key
                val encryptedKey = keystoreManager.getPrivateKey(wallet.id).getOrThrow()

                // Decrypt private key
                val privateKey = keystoreManager.decryptPrivateKey(encryptedKey).getOrThrow()

                // Sign message
                val signature = solanaKeyGenerator.signTransaction(messageBytes, privateKey)

                // Clear private key from memory
                privateKey.fill(0)

                // Encode signature to base64
                val signatureBase64 = Base64.encodeToString(signature, Base64.NO_WRAP)

                onSuccess(signatureBase64)
            } catch (e: Exception) {
                onError("Failed to sign message: ${e.message}")
            }
        }
    }
}