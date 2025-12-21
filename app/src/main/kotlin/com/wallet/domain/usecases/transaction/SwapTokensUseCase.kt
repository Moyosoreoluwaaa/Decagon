package com.octane.wallet.domain.usecases.transaction

import com.wallet.core.blockchain.JupiterApiService
import com.wallet.core.blockchain.JupiterSwapRequest
import com.octane.wallet.core.security.EncryptedPrivateKey
import com.octane.wallet.core.security.KeystoreManager
import com.octane.wallet.data.remote.api.SolanaRpcApi
import com.octane.wallet.data.remote.dto.solana.RpcRequest
import com.octane.wallet.domain.models.Transaction
import com.octane.wallet.domain.models.TransactionStatus
import com.octane.wallet.domain.models.TransactionType
import com.wallet.domain.repository.TransactionRepository
import com.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * Fixed SwapTokensUseCase with correct Transaction model fields
 */

class SwapTokensUseCase(
    private val jupiterApi: JupiterApiService,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val keystoreManager: KeystoreManager,
    private val solanaRpcApi: SolanaRpcApi
) {
    suspend operator fun invoke(
        fromToken: String,
        toToken: String,
        amount: Double,
        slippageBps: Int = 100 // 1% default
    ): Result<String> {
        return try {
            // Get active wallet
            val wallet = walletRepository.observeActiveWallet().first()
                ?: return Result.failure(IllegalStateException("No active wallet"))

            // Get token mint addresses
            val inputMint = getTokenMintAddress(fromToken)
            val outputMint = getTokenMintAddress(toToken)

            // Convert amount to lamports (smallest unit)
            val amountLamports = (amount * 1_000_000_000).toLong()

            // Get quote from Jupiter
            val quote = jupiterApi.getQuote(
                inputMint = inputMint,
                outputMint = outputMint,
                amount = amountLamports,
                slippageBps = slippageBps
            )

            // Get swap transaction
            val swapRequest = JupiterSwapRequest(
                quoteResponse = quote,
                userPublicKey = wallet.publicKey,
                wrapAndUnwrapSol = true,
                useSharedAccounts = true
            )

            val swapResponse = jupiterApi.getSwapTransaction(swapRequest)

            // Sign transaction
            val privateKey = keystoreManager.getPrivateKey(wallet.id).getOrThrow()
            val signedTx = signTransaction(swapResponse.swapTransaction, privateKey)

            // Submit transaction
            val rpcResponse = solanaRpcApi.sendTransaction(
                RpcRequest(
                    method = "sendTransaction",
                    params = listOf(signedTx)
                )
            )

            val txHash = rpcResponse.result as String

            // Calculate output amount
            val outAmountDecimal = quote.outAmount.toLong() / 1_000_000_000.0

            // Store in database with CORRECT fields
            transactionRepository.insertTransaction(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    walletId = wallet.id,
                    chainId = "solana",
                    txHash = txHash,
                    type = TransactionType.SWAP,
                    status = TransactionStatus.PENDING,
                    fromAddress = wallet.publicKey,
                    toAddress = wallet.publicKey, // Swap to self
                    amount = amount.toString(),
                    tokenSymbol = fromToken,
                    tokenMint = inputMint,
                    fee = (swapResponse.prioritizationFeeLamports / 1_000_000_000.0).toString(),
                    feePriority = "medium", // Based on Jupiter's recommendation
                    blockNumber = null,
                    confirmationCount = 0,
                    errorMessage = null,
                    memo = "Swapped $amount $fromToken → ${outAmountDecimal} $toToken",
                    timestamp = System.currentTimeMillis(),
                    simulated = false,
                    simulationSuccess = null
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
            "JUP" -> "JUPyiwrYJFskUPiHa7hkeR8VUtAeFoSYbKedZNsDvCN"
            "RAY" -> "4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R"
            "ORCA" -> "orcaEKTdK7LKz57vaAYr9QeNsVEPfiu6QeMU1kektZE"
            else -> throw IllegalArgumentException("Unsupported token: $symbol")
        }
    }

    private fun signTransaction(transaction: String, privateKey: EncryptedPrivateKey): String {
        // TODO: Implement transaction signing
        // Jupiter returns base64-encoded transaction
        // 1. Decode from base64
        // 2. Sign with Ed25519
        // 3. Re-encode to base64
        return ""
    }
}