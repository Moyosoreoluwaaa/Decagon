// domain/usecase/DecagonSendTokenUseCase.kt
package com.decagon.domain.usecase

import androidx.fragment.app.FragmentActivity
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
    private val biometricAuthenticator: DecagonBiometricAuthenticator // ✅ ADD THIS
) {

    init {
        Timber.d("DecagonSendTokenUseCase initialized")
    }

    suspend operator fun invoke(
        toAddress: String,
        amountSol: Double,
        activity: FragmentActivity // ✅ NOW USED
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

            // ✅ Calculate lamports early
            val lamports = (amountSol * 1_000_000_000).toLong()

            // ✅ Check balance before biometric auth
            val balance = rpcClient.getBalance(wallet.address).getOrThrow()
            val requiredLamports = lamports + 5000
            require(balance >= requiredLamports) {
                val balanceSol = balance / 1_000_000_000.0
                val requiredSol = requiredLamports / 1_000_000_000.0
                "Insufficient balance: $balanceSol SOL (need $requiredSol SOL)"
            }

            Timber.d("Using wallet: ${wallet.id}")

            // ✅ FIX 1: Authenticate BEFORE decryption
            Timber.i("Requesting biometric authentication for send...")
            val authenticated = withContext(Dispatchers.Main) {
                biometricAuthenticator.authenticate(
                    activity = activity,
                    title = "Authorize Transaction",
                    subtitle = "Send $amountSol SOL",
                    description = "Authenticate to sign transaction"
                )
            }

            if (!authenticated) {
                throw SecurityException("Authentication required to send transaction")
            }

            // ✅ FIX 2: Now decryption will succeed (user just authenticated)
            val seedResult = withContext(Dispatchers.IO) {
                walletRepository.decryptSeed(wallet.id)
            }
            val seed = seedResult.getOrThrow()

            // Derive keypair
            val (privateKey, _) = keyDerivation.deriveSolanaKeypair(seed, wallet.accountIndex)
            val keypair = Keypair.fromSecretKey(privateKey)

            // Build transaction
            val fromPubkey = PublicKey(wallet.address)
            val toPubkey = PublicKey(toAddress)

            // Fetch recent blockhash
            val blockhash = rpcClient.getLatestBlockhash().getOrThrow()

            val instruction = TransferInstruction(
                from = fromPubkey,
                to = toPubkey,
                lamports = lamports
            )

            val transaction = Transaction(
                recentBlockhash = blockhash,
                instructions = listOf(instruction),
                feePayer = fromPubkey
            )

            // Simulate first
            val serializedForSim = transaction.serialize()
            val simulation = rpcClient.simulateTransaction(serializedForSim).getOrThrow()

            if (!simulation.willSucceed) {
                Timber.e("Simulation failed: ${simulation.errorMessage}")
                throw IllegalStateException("Transaction simulation failed: ${simulation.errorMessage}")
            }

            // Sign transaction
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

        } catch (e: DecagonBiometricAuthenticator.BiometricAuthException) {
            Timber.e(e, "Biometric authentication failed")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "Send token failed")
            Result.failure(e)
        }
    }
}