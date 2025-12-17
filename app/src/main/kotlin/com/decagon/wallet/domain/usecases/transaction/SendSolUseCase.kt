package com.octane.wallet.domain.usecases.transaction

import com.octane.wallet.core.blockchain.SimulationResult
import com.octane.wallet.core.blockchain.SolanaKeyGenerator
import com.octane.wallet.core.blockchain.TransactionBuilder
import com.octane.wallet.core.blockchain.TransactionSimulator
import com.octane.wallet.core.blockchain.toBase64
import com.octane.wallet.core.security.KeystoreManager
import com.octane.wallet.core.security.MaliciousSignatureDetector
import com.octane.wallet.core.security.TransactionRiskAssessment
import com.octane.wallet.core.util.Validators
import com.octane.wallet.data.remote.api.SolanaRpcApi
import com.octane.wallet.data.remote.dto.solana.RpcRequest
import com.octane.wallet.domain.models.Transaction
import com.octane.wallet.domain.models.TransactionStatus
import com.octane.wallet.domain.models.TransactionType
import com.octane.wallet.domain.repository.AssetRepository
import com.octane.wallet.domain.repository.TransactionRepository
import com.octane.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * Sends SOL to another wallet.
 *
 * Flow:
 * 1. Validate recipient address
 * 2. Check sufficient balance
 * 3. Simulate transaction (preview effects)
 * 4. Scan for malicious signatures
 * 5. Prompt user for confirmation
 * 6. Sign and broadcast transaction
 * 7. Monitor confirmation status
 *
 * Business Rules:
 * - Minimum send amount: 0.000001 SOL (1000 lamports)
 * - Reserves 0.001 SOL for rent exemption
 * - Includes network fee in calculation
 * - Supports custom memos
 */

class SendSolUseCase(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val assetRepository: AssetRepository,
    private val transactionSimulator: TransactionSimulator,
    private val maliciousSignatureDetector: MaliciousSignatureDetector,
    private val transactionBuilder: TransactionBuilder,
    private val keystoreManager: KeystoreManager,
    private val solanaKeyGenerator: SolanaKeyGenerator,
    private val solanaRpcApi: SolanaRpcApi
) {
    /**
     * Step 1: Simulate transaction to preview effects.
     */
    suspend fun simulate(
        recipientAddress: String,
        amountSol: Double,
        memo: String? = null
    ): SimulationResult {
        // Validate inputs
        if (!Validators.isValidSolanaAddress(recipientAddress)) {
            return SimulationResult.Error(
                message = "Invalid Solana address",
                logs = emptyList()
            )
        }

        if (amountSol < 0.000001) {
            return SimulationResult.Error(
                message = "Amount too small (minimum 0.000001 SOL)",
                logs = emptyList()
            )
        }

        // Get active wallet
        val wallet = walletRepository.observeActiveWallet().first()
            ?: return SimulationResult.Error(
                message = "No active wallet",
                logs = emptyList()
            )

        // Check balance
        val solAsset = assetRepository.observeAsset(wallet.id, "SOL").first()
        val currentBalance = solAsset?.balanceDouble ?: 0.0

        if (currentBalance < amountSol + 0.001) { // Reserve for rent
            return SimulationResult.Error(
                message = "Insufficient balance (need ${amountSol + 0.001} SOL)",
                logs = emptyList()
            )
        }

        // Build transaction
        val lamports = (amountSol * 1_000_000_000).toLong()
        val txBytes = transactionBuilder.buildTransfer(
            from = wallet.publicKey,
            to = recipientAddress,
            lamports = lamports,
            memo = memo
        )

        // Simulate
        return transactionSimulator.simulate(txBytes)
    }

    /**
     * Step 2: Check for malicious signatures.
     */
    suspend fun scanForMaliciousActivity(
        recipientAddress: String,
        amountSol: Double
    ): TransactionRiskAssessment {
        val wallet = walletRepository.observeActiveWallet().first()!!

        val lamports = (amountSol * 1_000_000_000).toLong()
        val txBytes = transactionBuilder.buildTransfer(
            from = wallet.publicKey,
            to = recipientAddress,
            lamports = lamports
        )

        return maliciousSignatureDetector.analyzeTransaction(txBytes)
    }

    /**
     * Step 3: Execute transaction after user confirmation.
     */
    suspend operator fun invoke(
        recipientAddress: String,
        amountSol: Double,
        memo: String? = null,
        priorityFeeMicroLamports: Long = 10_000
    ): Result<Transaction> {
        return try {
            val wallet = walletRepository.observeActiveWallet().first()!!

            // Build and sign transaction
            val lamports = (amountSol * 1_000_000_000).toLong()
            val txBytes = transactionBuilder.buildTransfer(
                from = wallet.publicKey,
                to = recipientAddress,
                lamports = lamports,
                memo = memo
            )

            // Retrieve and decrypt private key
            val encryptedKey = keystoreManager.getPrivateKey(wallet.id).getOrThrow()
            val privateKey = keystoreManager.decryptPrivateKey(encryptedKey).getOrThrow()

            // Sign transaction
            val signedTx = solanaKeyGenerator.signTransaction(txBytes, privateKey)

            // Clear private key from memory
            privateKey.fill(0)

            // Broadcast to network
            val response = solanaRpcApi.sendTransaction(
                RpcRequest(
                    method = "sendTransaction",
                    params = listOf(signedTx.toBase64())
                )
            )

            val txHash = response.result ?: throw Exception(
                response.error?.message ?: "Failed to broadcast transaction"
            )

            // Create transaction record
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                walletId = wallet.id,
                chainId = "solana",
                txHash = txHash,
                type = TransactionType.SEND,
                status = TransactionStatus.PENDING,
                fromAddress = wallet.publicKey,
                toAddress = recipientAddress,
                amount = amountSol.toString(),
                tokenSymbol = "SOL",
                tokenMint = null,
                fee = (priorityFeeMicroLamports / 1_000_000_000.0).toString(),
                feePriority = "medium",
                blockNumber = null,
                confirmationCount = 0,
                errorMessage = null,
                memo = memo,
                timestamp = System.currentTimeMillis(),
                simulated = false,
                simulationSuccess = null
            )

            // Save to database
            transactionRepository.insertTransaction(transaction)

            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


