package com.decagon.domain.usecase

import androidx.fragment.app.FragmentActivity
import com.decagon.core.crypto.ComputeBudgetProgram
import com.decagon.core.crypto.DecagonKeyDerivation
import com.decagon.core.security.DecagonBiometricAuthenticator
import com.decagon.data.remote.SolanaRpcClient
import com.decagon.domain.model.DecagonTransaction
import com.decagon.domain.model.TransactionStatus
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val keyDerivation: DecagonKeyDerivation,
    private val biometricAuthenticator: DecagonBiometricAuthenticator
) {
    companion object {
        private const val DEFAULT_PRIORITY_FEE = 10_000L
        private const val COMPUTE_UNIT_LIMIT = 200_000
    }

    init {
        Timber.d("DecagonSendTokenUseCase initialized")
    }

    suspend operator fun invoke(
        toAddress: String,
        amountSol: Double,
        activity: FragmentActivity,
        priorityFeeMicroLamports: Long = DEFAULT_PRIORITY_FEE
    ): Result<DecagonTransaction> = withContext(Dispatchers.Default) {
        Timber.d("Executing send token: $amountSol SOL to ${toAddress.take(4)}...")

        try {
            // Validate
            require(keyDerivation.isValidSolanaAddress(toAddress)) {
                "Invalid recipient address"
            }

            // Get wallet
            val wallet = withContext(Dispatchers.IO) {
                walletRepository.getActiveWallet().first()
                    ?: throw IllegalStateException("No active wallet")
            }

            Timber.d("Using wallet: ${wallet.address.take(8)}...")

            // Calculate amounts
            val lamports = (amountSol * 1_000_000_000).toLong()
            val baseFee = 5000L
            val priorityFeeLamports = priorityFeeMicroLamports / 1_000_000
            val totalFee = baseFee + priorityFeeLamports

            // Check balance
            val balance = rpcClient.getBalance(wallet.address).getOrThrow()
            val requiredLamports = lamports + totalFee
            require(balance >= requiredLamports) {
                val balanceSol = balance / 1_000_000_000.0
                val requiredSol = requiredLamports / 1_000_000_000.0
                "Insufficient balance: $balanceSol SOL (need $requiredSol SOL)"
            }

            Timber.i("Balance check passed: $balance lamports available")

            // Authenticate
            Timber.i("Requesting biometric authentication...")
            val authenticated = withContext(Dispatchers.Main) {
                biometricAuthenticator.authenticate(
                    activity = activity,
                    title = "Authorize Transaction",
                    subtitle = "Send $amountSol SOL",
                    description = "Authenticate to sign transaction"
                )
            }

            if (!authenticated) {
                throw SecurityException("Authentication required")
            }

            // Decrypt seed
            val seedResult = withContext(Dispatchers.IO) {
                walletRepository.decryptSeed(wallet.id)
            }
            val seed = seedResult.getOrThrow()

            // Derive keypair
            val (privateKey, _) = keyDerivation.deriveSolanaKeypair(seed, wallet.accountIndex)
            val keypair = Keypair.fromSecretKey(privateKey)

            Timber.d("Keypair derived")

            // Get fresh blockhash
            val blockhash = rpcClient.getLatestBlockhash().getOrThrow()
            Timber.d("Got fresh blockhash: $blockhash")

            // Build transaction
            val fromPubkey = PublicKey(wallet.address)
            val toPubkey = PublicKey(toAddress)

            val transaction = Transaction(
                recentBlockhash = blockhash,
                instructions = listOf(
                    ComputeBudgetProgram.setComputeUnitLimit(COMPUTE_UNIT_LIMIT),
                    ComputeBudgetProgram.setComputeUnitPrice(priorityFeeMicroLamports),
                    TransferInstruction(
                        from = fromPubkey,
                        to = toPubkey,
                        lamports = lamports
                    )
                ),
                feePayer = fromPubkey
            )

            // CRITICAL: Sign BEFORE serialization
            transaction.sign(keypair)
            Timber.d("Transaction signed")

            // Serialize signed transaction
            val serializedTx = transaction.serialize()
            Timber.d("Transaction serialized: ${serializedTx.size} bytes")

            // Simulate
            val simulation = rpcClient.simulateTransaction(serializedTx).getOrThrow()
            if (!simulation.willSucceed) {
                throw IllegalStateException("Simulation failed: ${simulation.errorMessage}")
            }
            Timber.i("Simulation passed")

            // Send (ONLY ONCE)
            Timber.i("Sending transaction to network...")
            val signature = rpcClient.sendTransaction(serializedTx).getOrThrow()
            Timber.i("✅ Transaction sent! Signature: $signature")

            // Wait for confirmation
            Timber.d("Waiting for confirmation...")
            var confirmed = false
            var attempts = 0

            while (!confirmed && attempts < 30) {
                delay(1000)

                try {
                    val statusResult = rpcClient.getTransactionStatus(signature)
                    if (statusResult.isSuccess) {
                        when (val status = statusResult.getOrNull()) {
                            "processed", "confirmed", "finalized" -> {
                                confirmed = true
                                Timber.i("✅ Confirmed: $status")
                            }
                            "failed" -> {
                                throw IllegalStateException("Transaction failed on-chain")
                            }
                            else -> {
                                Timber.d("Pending... (${attempts + 1}/30)")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Error checking status")
                }

                attempts++
            }

            if (!confirmed) {
                Timber.w("Not confirmed after 30s, but signature is valid")
            }

            // Save to database
            val txId = UUID.randomUUID().toString()
            val txRecord = DecagonTransaction(
                id = txId,
                from = wallet.address,
                to = toAddress,
                amount = amountSol,
                lamports = lamports,
                signature = signature,
                status = if (confirmed) TransactionStatus.CONFIRMED else TransactionStatus.PENDING,
                timestamp = System.currentTimeMillis(),
                fee = baseFee,
                priorityFee = priorityFeeMicroLamports
            )

            withContext(Dispatchers.IO) {
                transactionRepository.insertTransaction(txRecord)
            }

            Timber.i("✅ Saved to database: $txId")
            Result.success(txRecord)

        } catch (e: DecagonBiometricAuthenticator.BiometricAuthException) {
            Timber.e(e, "Biometric auth failed")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "Send token failed")
            Result.failure(e)
        }
    }
}