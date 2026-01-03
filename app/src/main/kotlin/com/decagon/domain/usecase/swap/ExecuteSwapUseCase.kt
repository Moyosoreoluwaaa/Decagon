package com.decagon.domain.usecase.swap

import androidx.fragment.app.FragmentActivity
import com.decagon.core.crypto.DecagonKeyDerivation
import com.decagon.core.network.RpcClientFactory
import com.decagon.core.security.DecagonBiometricAuthenticator
import com.decagon.domain.model.*
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.domain.repository.SwapRepository
import com.decagon.domain.usecase.UpdateTokenBalancesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.sol4k.Keypair
import org.sol4k.Transaction
import timber.log.Timber
import java.util.UUID
import kotlin.math.pow

class ExecuteSwapUseCase(
    private val swapRepository: SwapRepository,
    private val walletRepository: DecagonWalletRepository,
    private val keyDerivation: DecagonKeyDerivation,
    private val biometricAuthenticator: DecagonBiometricAuthenticator,
    private val rpcFactory: RpcClientFactory,
    private val updateTokenBalancesUseCase: UpdateTokenBalancesUseCase
) {
    init {
        Timber.d("ExecuteSwapUseCase initialized with RpcClientFactory")
    }

    suspend operator fun invoke(
        swapOrder: SwapOrder,
        walletId: String,
        inputToken: TokenInfo,
        outputToken: TokenInfo,
        activity: FragmentActivity,
        priorityFeeLamports: Long = 0
    ): Result<String> = withContext(Dispatchers.Default) {

        try {
            Timber.d("Executing swap: ${inputToken.symbol} → ${outputToken.symbol}")

            // 1. Biometric authentication
            Timber.i("Requesting biometric authentication for swap...")
            val authenticated = withContext(Dispatchers.Main) {
                biometricAuthenticator.authenticate(
                    activity = activity,
                    title = "Authorize Swap",
                    subtitle = "Swap ${inputToken.symbol} for ${outputToken.symbol}",
                    description = "Authenticate to sign transaction"
                )
            }

            if (!authenticated) {
                throw SecurityException("Authentication required")
            }

            // 2. Decrypt wallet seed
            val seedResult = withContext(Dispatchers.IO) {
                walletRepository.decryptSeed(walletId)
            }
            if (seedResult.isFailure) {
                return@withContext Result.failure(
                    Exception("Failed to decrypt wallet: ${seedResult.exceptionOrNull()?.message}")
                )
            }

            val seed = seedResult.getOrThrow()
            val wallet = withContext(Dispatchers.IO) {
                walletRepository.getActiveWallet().first()
                    ?: throw IllegalStateException("No active wallet")
            }

            // Get active chain
            val activeChain = wallet.activeChain
                ?: throw IllegalStateException("No active chain selected")

            Timber.d("Active chain: ${activeChain.chainId} (${activeChain.chainType.name})")

            // ✅ CREATE NETWORK-AWARE RPC CLIENT
            val rpcClient = rpcFactory.createSolanaClient(activeChain.chainId)
            Timber.d("RPC client created for swap on chain: ${activeChain.chainId}")

            // 3. Derive keypair
            val (privateKey, _) = keyDerivation.deriveSolanaKeypair(seed, wallet.accountIndex)
            val keypair = Keypair.fromSecretKey(privateKey)

            Timber.d("Keypair derived for swap signing")

            // 4. Sign transaction using Sol4k
            val transaction = Transaction.from(swapOrder.transaction)
            transaction.sign(keypair)
            val signedTxBytes = transaction.serialize()

            Timber.d("Transaction signed: ${signedTxBytes.size} bytes")

            // 5. Create swap history entry (PENDING)
            val swapId = UUID.randomUUID().toString()
            val inputAmount = swapOrder.inAmount.toDoubleOrNull() ?: 0.0
            val outputAmount = swapOrder.outAmount.toDoubleOrNull() ?: 0.0

            val swapHistory = SwapHistory(
                id = swapId,
                walletId = walletId,
                inputMint = swapOrder.inputMint,
                outputMint = swapOrder.outputMint,
                inputAmount = inputAmount / (10.0.pow(inputToken.decimals)),
                outputAmount = outputAmount / (10.0.pow(outputToken.decimals)),
                inputSymbol = inputToken.symbol,
                outputSymbol = outputToken.symbol,
                signature = null,
                status = SwapStatus.PENDING,
                slippageBps = swapOrder.slippageBps,
                priceImpactPct = swapOrder.priceImpactPct,
                feeBps = swapOrder.feeBps,
                priorityFee = priorityFeeLamports,
                timestamp = System.currentTimeMillis()
            )

            withContext(Dispatchers.IO) {
                swapRepository.saveSwapHistory(swapHistory)
            }
            Timber.d("Swap history saved: $swapId")

            // 6. Execute swap via Jupiter Ultra API
            Timber.i("Executing swap on network: ${activeChain.chainId}")
            val executeResult = swapRepository.executeSwap(swapOrder, signedTxBytes)

            executeResult.fold(
                onSuccess = { signature ->
                    Timber.i("✅ Swap executed successfully: $signature")
                    Timber.i("   Network: ${activeChain.chainId}")
                    Timber.i("   Input: ${inputToken.symbol}")
                    Timber.i("   Output: ${outputToken.symbol}")

                    // Update swap history to CONFIRMED
                    withContext(Dispatchers.IO) {
                        swapRepository.updateSwapStatus(
                            swapId = swapId,
                            signature = signature,
                            status = SwapStatus.CONFIRMED
                        )
                    }
                    // ✅ NEW: Refresh balances
                    withContext(Dispatchers.IO) {
                        updateTokenBalancesUseCase(walletId)
                    }

                    Result.success(signature)
                },
                onFailure = { error ->
                    Timber.e(error, "Swap execution failed on ${activeChain.chainId}")

                    // Update swap history to FAILED
                    withContext(Dispatchers.IO) {
                        swapRepository.updateSwapStatus(
                            swapId = swapId,
                            signature = null,
                            status = SwapStatus.FAILED,
                            error = error.message
                        )
                    }

                    Result.failure(error)
                }
            )

        } catch (e: DecagonBiometricAuthenticator.BiometricAuthException) {
            Timber.e(e, "Biometric authentication failed for swap")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "Execute swap exception")
            Result.failure(e)
        }
    }
}