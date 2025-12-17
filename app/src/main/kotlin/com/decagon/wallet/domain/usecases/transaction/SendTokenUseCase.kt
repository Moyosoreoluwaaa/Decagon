package com.octane.wallet.domain.usecases.transaction

import com.octane.wallet.core.security.EncryptedPrivateKey
import com.octane.wallet.core.security.KeystoreManager
import com.octane.wallet.data.remote.api.SolanaRpcApi
import com.octane.wallet.data.remote.dto.solana.RpcRequest
import com.octane.wallet.domain.models.Transaction
import com.octane.wallet.domain.models.TransactionStatus
import com.octane.wallet.domain.models.TransactionType
import com.octane.wallet.domain.repository.TransactionRepository
import com.octane.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * Fixed SendTokenUseCase with correct Transaction model fields
 */

class SendTokenUseCase(
    private val solanaRpcApi: SolanaRpcApi,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val keystoreManager: KeystoreManager
) {
    suspend operator fun invoke(
        recipient: String,
        tokenSymbol: String,
        amount: Double
    ): Result<String> {
        return try {
            // Get active wallet
            val wallet = walletRepository.observeActiveWallet().first()
                ?: return Result.failure(IllegalStateException("No active wallet"))

            // Get private key
            val privateKey = keystoreManager.getPrivateKey(wallet.id)
                .getOrThrow()

            // Get token mint address
            val tokenMint = getTokenMintAddress(tokenSymbol)

            // Build transaction
            val transaction = buildSendTransaction(
                sender = wallet.publicKey,
                recipient = recipient,
                amount = amount,
                tokenMint = tokenMint
            )

            // Sign transaction
            val signedTx = signTransaction(transaction, privateKey)

            // Submit to RPC
            val response = solanaRpcApi.sendTransaction(
                RpcRequest(
                    method = "sendTransaction",
                    params = listOf(signedTx)
                )
            )

            val txHash = response.result as String

            // Store in database with CORRECT Transaction model fields
            transactionRepository.insertTransaction(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    walletId = wallet.id,
                    chainId = "solana", // ADDED
                    txHash = txHash,
                    type = TransactionType.SEND,
                    status = TransactionStatus.PENDING,
                    fromAddress = wallet.publicKey, // ADDED
                    toAddress = recipient, // CHANGED from recipientAddress
                    amount = amount.toString(), // CHANGED to String
                    tokenSymbol = tokenSymbol,
                    tokenMint = tokenMint, // ADDED
                    fee = "0.000005", // CHANGED to String
                    feePriority = null, // ADDED
                    blockNumber = null, // ADDED
                    confirmationCount = 0, // ADDED
                    errorMessage = null, // ADDED
                    memo = null, // ADDED
                    timestamp = System.currentTimeMillis(),
                    simulated = false, // ADDED
                    simulationSuccess = null // ADDED
                )
            )

            Result.success(txHash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getTokenMintAddress(symbol: String): String {
        return when (symbol) {
            "SOL" -> "So11111111111111111111111111111111111111112"
            "USDC" -> "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"
            "USDT" -> "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB"
            "BONK" -> "DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263"
            else -> throw IllegalArgumentException("Unsupported token: $symbol")
        }
    }

    private fun buildSendTransaction(
        sender: String,
        recipient: String,
        amount: Double,
        tokenMint: String
    ): String {
        // TODO: Implement Solana transaction building
        // Libraries:
        // - sol4k (pure Kotlin): https://github.com/sol4k/sol4k
        // - Solana Kotlin SDK: https://github.com/metaplex-foundation/solana-kotlin
        return ""
    }

    private fun signTransaction(transaction: String, privateKey: EncryptedPrivateKey): String {
        // TODO: Implement transaction signing with Ed25519
        return ""
    }
}