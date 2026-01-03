package com.decagon.data.repository

import com.decagon.data.local.dao.SwapHistoryDao
import com.decagon.data.local.dao.TokenBalanceDao
import com.decagon.data.local.dao.TokenCacheDao
import com.decagon.data.mapper.*
import com.decagon.data.remote.api.JupiterUltraApiService
import com.decagon.data.remote.dto.JupiterExecuteRequest
import com.decagon.data.remote.dto.JupiterOrderRequest
import com.decagon.domain.model.*
import com.decagon.domain.repository.SwapRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SwapRepositoryImpl(
    private val apiService: JupiterUltraApiService,
    private val swapHistoryDao: SwapHistoryDao,
    private val tokenCacheDao: TokenCacheDao,
    private val tokenBalanceDao: TokenBalanceDao  // ✅ NEW
) : SwapRepository {

    init {
        Timber.d("SwapRepositoryImpl initialized with balance caching")
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
                // ✅ Check cache first (offline-first)
                val cachedBalances = tokenBalanceDao.getByWallet(publicKey).first()

                if (cachedBalances.isNotEmpty()) {
                    val cacheAge = System.currentTimeMillis() - cachedBalances.first().lastUpdated

                    // If cache is fresh (< 5 min), return it immediately
                    if (cacheAge < CACHE_DURATION) {
                        Timber.d("Returning ${cachedBalances.size} cached balances (age: ${cacheAge}ms)")
                        return@withContext Result.success(cachedBalances.map { it.toDomain() })
                    }
                }

                // Fetch fresh data from API
                val balancesResult = apiService.getBalances(publicKey)

                balancesResult.map { response ->
                    val holdings = response.actualBalances
                    val mints = holdings.map { it.mint }

                    // Enrich with cached token info
                    val cachedTokens = tokenCacheDao.getByAddresses(mints)
                        .associateBy { it.address }

                    val balances = holdings.map { holding ->
                        holding.toDomain(
                            tokenInfo = cachedTokens[holding.mint]?.toDomain()
                        )
                    }

                    // ✅ Cache the fresh balances
                    tokenBalanceDao.insertAll(balances.toEntities(publicKey))
                    Timber.i("Cached ${balances.size} fresh token balances")

                    balances
                }

            } catch (e: Exception) {
                // ✅ On error, return stale cache if available
                val staleCache = tokenBalanceDao.getByWallet(publicKey).first()
                if (staleCache.isNotEmpty()) {
                    Timber.w(e, "API failed, returning ${staleCache.size} stale cached balances")
                    return@withContext Result.success(staleCache.map { it.toDomain() })
                }

                Timber.e(e, "Failed to get balances (no cache available)")
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
        private val CACHE_DURATION = TimeUnit.MINUTES.toMillis(5)  // 5 minutes
    }
}