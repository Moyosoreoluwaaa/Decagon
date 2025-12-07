// domain/usecase/DecagonSendTokenUseCase.kt
package com.decagon.domain.usecase

import com.decagon.core.crypto.DecagonKeyDerivation
import com.decagon.data.remote.SolanaRpcClient
import com.decagon.domain.model.DecagonTransaction
import com.decagon.domain.model.TransactionStatus
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.sol4k.Keypair
import org.sol4k.PublicKey
import org.sol4k.Transaction
import org.sol4k.instruction.TransferInstruction
import timber.log.Timber
import java.util.UUID

class DecagonSendTokenUseCase(
    private val walletRepository: DecagonWalletRepository,
    private val transactionRepository: DecagonTransactionRepository,
    private val rpcClient: SolanaRpcClient,
    private val keyDerivation: DecagonKeyDerivation
) {

    init {
        Timber.d("DecagonSendTokenUseCase initialized")
    }

    suspend operator fun invoke(
        toAddress: String,
        amountSol: Double,
        // activity: FragmentActivity // Remove this if not used in decryptSeed
    ): Result<DecagonTransaction> = withContext(Dispatchers.Default) {
        Timber.d("Executing send token: $amountSol SOL to ${toAddress.take(4)}...")

        try {
            // Validate address
            require(keyDerivation.isValidSolanaAddress(toAddress)) {
                "Invalid recipient address"
            }

            // Get active wallet
            val wallet = withContext(Dispatchers.IO) {
                walletRepository.getActiveWallet().first()
                    ?: throw IllegalStateException("No active wallet")
            }

            Timber.d("Using wallet: ${wallet.id}")

            // Decrypt seed
            val seedResult = walletRepository.decryptSeed(wallet.id)
            val seed = seedResult.getOrThrow()

            // Derive keypair
            val (privateKey, _) = keyDerivation.deriveSolanaKeypair(seed, wallet.accountIndex)
            val keypair = Keypair.fromSecretKey(privateKey)

            // Convert SOL to lamports
            val lamports = (amountSol * 1_000_000_000).toLong()

            // Build transaction
            val fromPubkey = PublicKey(wallet.address)
            val toPubkey = PublicKey(toAddress)

            // ✅ FIX 1: Fetch recent blockhash from network FIRST
            // You may need to add getLatestBlockhash() to your SolanaRpcClient
            val blockhash = rpcClient.getLatestBlockhash().getOrThrow()

            // ✅ FIX 2: Correct parameter names (from, to)
            val instruction = TransferInstruction(
                from = fromPubkey,
                to = toPubkey,
                lamports = lamports
            )

            // ✅ FIX 3: Pass recentBlockhash to constructor
            val transaction = Transaction(
                recentBlockhash = blockhash,
                instructions = listOf(instruction),
                feePayer = fromPubkey
            )

            // Simulate first (Optional but recommended)
            val serializedForSim = transaction.serialize()
            val simulation = rpcClient.simulateTransaction(serializedForSim).getOrThrow()

            if (!simulation.willSucceed) {
                Timber.e("Simulation failed: ${simulation.errorMessage}")
                throw IllegalStateException("Transaction simulation failed: ${simulation.errorMessage}")
            }

            // Sign transaction
            // ✅ FIX 4: sign() usually works on the instance if the constructor is correct
            transaction.sign(keypair)

            // Serialize
            val signedTx = transaction.serialize()

            // Send transaction
            val signature = rpcClient.sendTransaction(signedTx).getOrThrow()

            // Create transaction record
            val txId = UUID.randomUUID().toString()
            val txRecord = DecagonTransaction(
                id = txId,
                from = wallet.address,
                to = toAddress,
                amount = amountSol,
                lamports = lamports,
                signature = signature,
                status = TransactionStatus.PENDING,
                timestamp = System.currentTimeMillis()
            )

            // Save to database
            withContext(Dispatchers.IO) {
                transactionRepository.insertTransaction(txRecord)
            }

            Timber.i("Transaction sent successfully: $signature")
            Result.success(txRecord)

        } catch (e: Exception) {
            Timber.e(e, "Send token failed")
            Result.failure(e)
        }
    }
}