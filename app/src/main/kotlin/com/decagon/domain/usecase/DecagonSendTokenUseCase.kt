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
        private const val DEFAULT_PRIORITY_FEE = 50_000L
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
            require(keyDerivation.isValidSolanaAddress(toAddress)) {
                "Invalid recipient address"
            }

            val wallet = withContext(Dispatchers.IO) {
                walletRepository.getActiveWallet().first()
                    ?: throw IllegalStateException("No active wallet")
            }

            Timber.d("Using wallet: ${wallet.address.take(8)}...")

            val lamports = (amountSol * 1_000_000_000).toLong()
            val baseFee = 5000L
            val priorityFeeLamports = priorityFeeMicroLamports / 1_000_000
            val totalFee = baseFee + priorityFeeLamports

            val balance = rpcClient.getBalance(wallet.address).getOrThrow()
            val requiredLamports = lamports + totalFee
            require(balance >= requiredLamports) {
                val balanceSol = balance / 1_000_000_000.0
                val requiredSol = requiredLamports / 1_000_000_000.0
                "Insufficient balance: $balanceSol SOL (need $requiredSol SOL)"
            }

            Timber.i("Balance check passed: $balance lamports available")

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

            val seedResult = withContext(Dispatchers.IO) {
                walletRepository.decryptSeed(wallet.id)
            }
            val seed = seedResult.getOrThrow()

            val (privateKey, _) = keyDerivation.deriveSolanaKeypair(seed, wallet.accountIndex)
            val keypair = Keypair.fromSecretKey(privateKey)

            Timber.d("Keypair derived")

            val blockhash = rpcClient.getLatestBlockhash().getOrThrow()
            Timber.d("Got fresh blockhash: $blockhash")

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

            transaction.sign(keypair)
            Timber.d("Transaction signed")

            val serializedTx = transaction.serialize()
            Timber.d("Transaction serialized: ${serializedTx.size} bytes")

            val simulation = rpcClient.simulateTransaction(serializedTx).getOrThrow()
            if (!simulation.willSucceed) {
                throw IllegalStateException("Simulation failed: ${simulation.errorMessage}")
            }
            Timber.i("Simulation passed")

            Timber.i("Sending transaction to network...")
            val signature = rpcClient.sendTransaction(serializedTx).getOrThrow()
            Timber.i("✅ Transaction sent! Signature: $signature")

            val txId = UUID.randomUUID().toString()
            val txRecord = DecagonTransaction(
                id = txId,
                from = wallet.address,
                to = toAddress,
                amount = amountSol,
                lamports = lamports,
                signature = signature,
                status = TransactionStatus.PENDING,
                timestamp = System.currentTimeMillis(),
                fee = baseFee,
                priorityFee = priorityFeeMicroLamports
            )

            withContext(Dispatchers.IO) {
                transactionRepository.insertTransaction(txRecord)
            }

            Timber.i("✅ Transaction saved:")
            Timber.i("   ID: $txId")
            Timber.i("   Signature: $signature")
            Timber.i("   Status: ${txRecord.status}")
            Timber.i("   From: ${txRecord.from.take(8)}...")
            Timber.i("   To: ${txRecord.to.take(8)}...")
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