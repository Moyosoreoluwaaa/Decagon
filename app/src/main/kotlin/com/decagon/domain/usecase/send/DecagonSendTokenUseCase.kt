package com.decagon.domain.usecase.send

import androidx.fragment.app.FragmentActivity
import com.decagon.core.crypto.ComputeBudgetProgram
import com.decagon.core.crypto.DecagonKeyDerivation
import com.decagon.core.network.RpcClientFactory
import com.decagon.core.security.DecagonBiometricAuthenticator
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
    private val rpcFactory: RpcClientFactory,  // ← CHANGED: Factory instead of client
    private val keyDerivation: DecagonKeyDerivation,
    private val biometricAuthenticator: DecagonBiometricAuthenticator
) {
    companion object {
        private const val DEFAULT_PRIORITY_FEE = 50_000L
        private const val COMPUTE_UNIT_LIMIT = 200_000
    }

    init {
        Timber.Forest.d("DecagonSendTokenUseCase initialized with RpcClientFactory")
    }

    suspend operator fun invoke(
        toAddress: String,
        amountSol: Double,
        activity: FragmentActivity,
        priorityFeeMicroLamports: Long = DEFAULT_PRIORITY_FEE
    ): Result<DecagonTransaction> = withContext(Dispatchers.Default) {
        Timber.Forest.d("Executing send token: $amountSol SOL to ${toAddress.take(4)}...")

        try {
            // Validate recipient address
            require(keyDerivation.isValidSolanaAddress(toAddress)) {
                "Invalid recipient address"
            }

            // Get active wallet
            val wallet = withContext(Dispatchers.IO) {
                walletRepository.getActiveWallet().first()
                    ?: throw IllegalStateException("No active wallet")
            }

            Timber.Forest.d("Using wallet: ${wallet.address.take(8)}...")

            // Get active chain
            val activeChain = wallet.activeChain
                ?: throw IllegalStateException("No active chain selected")

            Timber.Forest.d("Active chain: ${activeChain.chainId} (${activeChain.chainType.name})")

            // ✅ CREATE NETWORK-AWARE RPC CLIENT
            val rpcClient = rpcFactory.createSolanaClient(activeChain.chainId)
            Timber.Forest.d("RPC client created for chain: ${activeChain.chainId}")

            // Calculate amounts
            val lamports = (amountSol * 1_000_000_000).toLong()
            val baseFee = 5000L
            val priorityFeeLamports = priorityFeeMicroLamports / 1_000_000
            val totalFee = baseFee + priorityFeeLamports

            // Check balance using network-aware client
            val balance = rpcClient.getBalance(wallet.address).getOrThrow()
            val requiredLamports = lamports + totalFee
            require(balance >= requiredLamports) {
                val balanceSol = balance / 1_000_000_000.0
                val requiredSol = requiredLamports / 1_000_000_000.0
                "Insufficient balance: $balanceSol SOL (need $requiredSol SOL)"
            }

            Timber.Forest.i("Balance check passed: $balance lamports available")

            // Biometric authentication
            Timber.Forest.i("Requesting biometric authentication...")
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

            // Decrypt seed and derive keypair
            val seedResult = withContext(Dispatchers.IO) {
                walletRepository.decryptSeed(wallet.id)
            }
            val seed = seedResult.getOrThrow()

            val (privateKey, _) = keyDerivation.deriveSolanaKeypair(seed, wallet.accountIndex)
            val keypair = Keypair.Companion.fromSecretKey(privateKey)

            Timber.Forest.d("Keypair derived")

            // Get fresh blockhash from network-aware client
            val blockhash = rpcClient.getLatestBlockhash().getOrThrow()
            Timber.Forest.d("Got fresh blockhash: $blockhash")

            val fromPubkey = PublicKey(wallet.address)
            val toPubkey = PublicKey(toAddress)

            // Build transaction
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
            Timber.Forest.d("Transaction signed")

            val serializedTx = transaction.serialize()
            Timber.Forest.d("Transaction serialized: ${serializedTx.size} bytes")

            // Simulate transaction on correct network
            val simulation = rpcClient.simulateTransaction(serializedTx).getOrThrow()
            if (!simulation.willSucceed) {
                throw IllegalStateException("Simulation failed: ${simulation.errorMessage}")
            }
            Timber.Forest.i("Simulation passed")

            // Send transaction to correct network
            Timber.Forest.i("Sending transaction to network...")
            val signature = rpcClient.sendTransaction(serializedTx).getOrThrow()
            Timber.Forest.i("✅ Transaction sent! Signature: $signature")

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
                timestamp = System.currentTimeMillis(),
                fee = baseFee,
                priorityFee = priorityFeeMicroLamports
            )

            withContext(Dispatchers.IO) {
                transactionRepository.insertTransaction(txRecord)
            }

            Timber.Forest.i("✅ Transaction saved:")
            Timber.Forest.i("   ID: $txId")
            Timber.Forest.i("   Signature: $signature")
            Timber.Forest.i("   Status: ${txRecord.status}")
            Timber.Forest.i("   From: ${txRecord.from.take(8)}...")
            Timber.Forest.i("   To: ${txRecord.to.take(8)}...")
            Timber.Forest.i("   Network: ${activeChain.chainId}")

            Result.success(txRecord)

        } catch (e: DecagonBiometricAuthenticator.BiometricAuthException) {
            Timber.Forest.e(e, "Biometric auth failed")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.Forest.e(e, "Send token failed")
            Result.failure(e)
        }
    }
}