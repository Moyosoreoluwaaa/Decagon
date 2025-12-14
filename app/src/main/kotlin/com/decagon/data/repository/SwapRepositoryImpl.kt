
package com.decagon.data.repository

import com.decagon.data.local.dao.SwapHistoryDao
import com.decagon.data.local.dao.TokenCacheDao
import com.decagon.data.mapper.toDomain
import com.decagon.data.mapper.toEntity
import com.decagon.data.remote.JupiterUltraApiService
import com.decagon.data.remote.dto.JupiterExecuteRequest
import com.decagon.data.remote.dto.JupiterOrderRequest
import com.decagon.domain.model.SecurityWarning
import com.decagon.domain.model.SwapHistory
import com.decagon.domain.model.SwapOrder
import com.decagon.domain.model.SwapStatus
import com.decagon.domain.model.TokenBalance
import com.decagon.domain.model.TokenInfo
import com.decagon.domain.repository.SwapRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SwapRepositoryImpl(
    private val apiService: JupiterUltraApiService,
    private val swapHistoryDao: SwapHistoryDao,
    private val tokenCacheDao: TokenCacheDao
) : SwapRepository {

    init {
        Timber.d("SwapRepositoryImpl initialized")
    }

    override suspend fun getSwapQuote(
        inputMint: String,
        outputMint: String,
        amount: Long,
        userPublicKey: String,
        slippageBps: Int?
    ): Result<SwapOrder> = withContext(Dispatchers.IO) {
        try {
            // Get security warnings first
            val shieldResult = apiService.getShield(listOf(inputMint, outputMint))
            val warnings = shieldResult.getOrNull()?.warnings?.mapValues { (_, dtos) ->
                dtos.map { it.toDomain() }
            } ?: emptyMap()

            // Get order
            val orderRequest = JupiterOrderRequest(
                inputMint = inputMint,
                outputMint = outputMint,
                amount = amount,
                taker = userPublicKey,
                slippageBps = slippageBps
            )

            val orderResult = apiService.getOrder(orderRequest)

            orderResult.map { orderResponse ->
                orderResponse.toDomain(securityWarnings = warnings)
            }

        } catch (e: Exception) {
            Timber.e(e, "Failed to get swap quote")
            Result.failure(e)
        }
    }

    override suspend fun executeSwap(
        swapOrder: SwapOrder,
        signedTransaction: ByteArray
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val base64Tx = android.util.Base64.encodeToString(
                signedTransaction,
                android.util.Base64.NO_WRAP
            )

            val executeRequest = JupiterExecuteRequest(
                signedTransaction = base64Tx,
                requestId = swapOrder.requestId
            )

            val executeResult = apiService.executeOrder(executeRequest)

            executeResult.fold(
                onSuccess = { response ->
                    when {
                        response.status == "Success" && response.signature != null -> {
                            Timber.i("Swap executed: ${response.signature}")
                            Result.success(response.signature)
                        }
                        response.status == "Failed" -> {
                            val error = response.error ?: "Unknown error"
                            Timber.e("Swap failed: $error")
                            Result.failure(Exception("Swap failed: $error"))
                        }
                        else -> {
                            Result.failure(Exception("Swap pending"))
                        }
                    }
                },
                onFailure = { Result.failure(it) }
            )

        } catch (e: Exception) {
            Timber.e(e, "Swap execution exception")
            Result.failure(e)
        }
    }

    override suspend fun searchTokens(query: String, limit: Int): Result<List<TokenInfo>> {
        return withContext(Dispatchers.IO) {
            try {
                // Check cache first
                val cachedTokens = tokenCacheDao.search(query)
                if (cachedTokens.isNotEmpty()) {
                    Timber.d("Returning ${cachedTokens.size} cached tokens")
                    return@withContext Result.success(cachedTokens.map { it.toDomain() })
                }

                // API call
                val searchResult = apiService.searchTokens(query, limit)

                searchResult.map { response ->
                    val tokens = response.results.map { it.toDomain() }

                    // Cache results
                    tokenCacheDao.insertAll(tokens.map { it.toEntity() })

                    tokens
                }

            } catch (e: Exception) {
                Timber.e(e, "Token search failed")
                Result.failure(e)
            }
        }
    }

    override suspend fun getTokenBalances(publicKey: String): Result<List<TokenBalance>> {
        return withContext(Dispatchers.IO) {
            try {
                val balancesResult = apiService.getBalances(publicKey)

                balancesResult.map { response ->
                    val mints = response.holdings.map { it.mint }

                    // Try to enrich with cached token info
                    val cachedTokens = tokenCacheDao.getByAddresses(mints)
                        .associateBy { it.address }

                    response.holdings.map { holding ->
                        holding.toDomain(
                            tokenInfo = cachedTokens[holding.mint]?.toDomain()
                        )
                    }
                }

            } catch (e: Exception) {
                Timber.e(e, "Failed to get balances")
                Result.failure(e)
            }
        }
    }

    override suspend fun getTokenSecurity(
        mints: List<String>
    ): Result<Map<String, List<SecurityWarning>>> {
        return withContext(Dispatchers.IO) {
            try {
                val shieldResult = apiService.getShield(mints)

                shieldResult.map { response ->
                    response.warnings.mapValues { (_, dtos) ->
                        dtos.map { it.toDomain() }
                    }
                }

            } catch (e: Exception) {
                Timber.e(e, "Shield check failed")
                Result.failure(e)
            }
        }
    }

    override suspend fun saveSwapHistory(swap: SwapHistory) {
        withContext(Dispatchers.IO) {
            swapHistoryDao.insert(swap.toEntity())
        }
    }

    override suspend fun updateSwapStatus(
        swapId: String,
        signature: String?,
        status: SwapStatus,
        error: String?
    ) {
        withContext(Dispatchers.IO) {
            if (error != null) {
                swapHistoryDao.updateStatusWithError(swapId, status.name, error)
            } else {
                swapHistoryDao.updateStatus(swapId, status.name, signature)
            }
        }
    }

    override fun getSwapHistory(walletId: String): Flow<List<SwapHistory>> {
        return swapHistoryDao.getByWallet(walletId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSwapById(swapId: String): Flow<SwapHistory?> {
        return swapHistoryDao.getById(swapId).map { it?.toDomain() }
    }

    companion object {
        private val CACHE_DURATION = TimeUnit.HOURS.toMillis(24)
    }
}