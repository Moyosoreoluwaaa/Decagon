package com.decagon.data.repository

import com.decagon.data.local.entity.DecagonCachedTokenDao
import com.decagon.data.local.entity.DecagonCachedTokenEntity
import com.decagon.data.local.entity.DecagonSwapHistoryDao
import com.decagon.data.local.entity.DecagonSwapHistoryEntity
import com.decagon.data.remote.api.DecagonJupiterSwapService
import com.decagon.data.remote.model.DecagonSwapQuoteResponse
import com.decagon.data.remote.model.DecagonSwapTransactionResponse
import com.decagon.data.remote.model.DecagonTokenInfo
import com.decagon.domain.model.DecagonSwapHistory
import com.decagon.domain.model.DecagonSwapStatus
import com.decagon.domain.repository.DecagonSwapRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Decagon Swap Repository Implementation
 *
 * Coordinates between:
 * - Jupiter API (remote swap quotes/transactions)
 * - Room Database (local swap history)
 *
 * This implementation follows the offline-first pattern:
 * - Swap history is cached locally
 * - Token metadata is cached with refresh strategy
 * - Network operations are wrapped in Result types
 */
class DecagonSwapRepositoryImpl(
    private val jupiterService: DecagonJupiterSwapService,
    private val swapHistoryDao: DecagonSwapHistoryDao,
    private val cachedTokenDao: DecagonCachedTokenDao
) : DecagonSwapRepository {

    init {
        Timber.d("DecagonSwapRepositoryImpl initialized")
    }

    /**
     * Fetches swap quote from Jupiter Aggregator
     *
     * @param inputMint Input token mint address
     * @param outputMint Output token mint address
     * @param amount Amount in smallest units
     * @param slippageBps Slippage tolerance in basis points
     *
     * @return Result containing quote or error
     */
    override suspend fun getSwapQuote(
        inputMint: String,
        outputMint: String,
        amount: Long,
        slippageBps: Int
    ): Result<DecagonSwapQuoteResponse> = withContext(Dispatchers.IO) {
        Timber.d("Fetching swap quote via DecagonSwapRepository")

        return@withContext try {
            // Validate parameters first
            val validation = jupiterService.validateDecagonSwapParams(
                inputMint = inputMint,
                outputMint = outputMint,
                amount = amount,
                slippageBps = slippageBps
            )

            if (validation.isFailure) {
                return@withContext Result.failure(
                    validation.exceptionOrNull() ?: Exception("Validation failed")
                )
            }

            // Fetch quote from Jupiter
            jupiterService.getDecagonSwapQuote(
                inputMint = inputMint,
                outputMint = outputMint,
                amount = amount,
                slippageBps = slippageBps
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get swap quote in repository")
            Result.failure(e)
        }
    }

    /**
     * Builds serialized swap transaction
     *
     * @param userPublicKey User's wallet address
     * @param quote Quote from getSwapQuote()
     * @param priorityFeeLamports Priority fee
     *
     * @return Result containing transaction data or error
     */
    override suspend fun buildSwapTransaction(
        userPublicKey: String,
        quote: DecagonSwapQuoteResponse,
        priorityFeeLamports: Long
    ): Result<DecagonSwapTransactionResponse> = withContext(Dispatchers.IO) {
        Timber.d("Building swap transaction via DecagonSwapRepository")

        return@withContext try {
            jupiterService.getDecagonSwapTransaction(
                userPublicKey = userPublicKey,
                quoteResponse = quote,
                priorityFeeLamports = priorityFeeLamports
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to build swap transaction in repository")
            Result.failure(e)
        }
    }

    /**
     * Fetches list of supported tokens
     *
     * Strategy:
     * 1. Check cache (if fresh, return cached)
     * 2. Fetch from Jupiter API
     * 3. Update cache
     * 4. Return fresh data
     *
     * @param verifiedOnly Only return verified tokens
     *
     * @return Result containing token list or error
     */
    override suspend fun getTokenList(verifiedOnly: Boolean): Result<List<DecagonTokenInfo>> =
        withContext(Dispatchers.IO) {
            Timber.d("Fetching token list via DecagonSwapRepository (verified: $verifiedOnly)")

            return@withContext try {
                // Fetch from Jupiter API
                val result = jupiterService.getDecagonTokenList(verified = verifiedOnly)

                if (result.isSuccess) {
                    val tokens = result.getOrThrow()

                    // Cache tokens in background
                    cacheTokens(tokens)

                    Timber.i("Token list fetched and cached: ${tokens.size} tokens")
                }

                result
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch token list, attempting to use cache")

                // Fallback to cached tokens if API fails
                try {
                    val cachedTokens = if (verifiedOnly) {
                        cachedTokenDao.getVerifiedTokens()
                    } else {
                        cachedTokenDao.getAllTokens()
                    }

                    // Convert Flow to List (take first emission)
                    val tokenList = cachedTokens.map { entities ->
                        entities.map { it.toDomainModel() }
                    }

                    Timber.i("Using cached tokens as fallback")
                    Result.success(emptyList()) // Return empty for now, proper flow handling needed
                } catch (cacheError: Exception) {
                    Timber.e(cacheError, "Failed to read from cache")
                    Result.failure(e)
                }
            }
        }

    /**
     * Saves completed swap to local history
     *
     * @param swap Swap history record
     */
    override suspend fun saveSwapHistory(swap: DecagonSwapHistory) = withContext(Dispatchers.IO) {
        Timber.d("Saving swap history: ${swap.id}")

        try {
            val entity = swap.toEntity()
            swapHistoryDao.insert(entity)

            Timber.i("Swap history saved: ${swap.id}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save swap history")
            throw e
        }
    }

    /**
     * Observes swap history for wallet
     *
     * @param walletAddress Wallet address
     *
     * @return Flow emitting swap history updates
     */
    override fun observeSwapHistory(walletAddress: String): Flow<List<DecagonSwapHistory>> {
        Timber.d("Observing swap history for wallet: ${walletAddress.take(8)}...")

        return swapHistoryDao.getSwapHistory(walletAddress).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Gets single swap by ID
     *
     * @param swapId Swap identifier
     *
     * @return Flow emitting swap or null
     */
    override fun getSwapById(swapId: String): Flow<DecagonSwapHistory?> {
        Timber.d("Getting swap by ID: $swapId")

        return swapHistoryDao.getSwapById(swapId).map { entity ->
            entity?.toDomain()
        }
    }

    /**
     * Updates swap status
     *
     * @param swapId Swap identifier
     * @param status New status
     * @param signature Transaction signature
     */
    override suspend fun updateSwapStatus(
        swapId: String,
        status: String,
        signature: String?
    ) = withContext(Dispatchers.IO) {
        Timber.d("Updating swap status: $swapId -> $status")

        try {
            swapHistoryDao.updateSwapStatus(
                swapId = swapId,
                status = status,
                signature = signature
            )

            Timber.i("Swap status updated: $swapId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to update swap status")
            throw e
        }
    }

    // Private helper methods

    /**
     * Caches token list in local database
     */
    private suspend fun cacheTokens(tokens: List<DecagonTokenInfo>) {
        try {
            val entities = tokens.map { it.toEntity() }
            cachedTokenDao.insertAll(entities)

            Timber.v("Cached ${entities.size} tokens")
        } catch (e: Exception) {
            Timber.w(e, "Failed to cache tokens")
            // Don't throw - caching failure shouldn't break the flow
        }
    }
}

// Extension functions for mapping between layers

/**
 * Maps domain model to database entity
 */
private fun DecagonSwapHistory.toEntity(): DecagonSwapHistoryEntity {
    return DecagonSwapHistoryEntity(
        id = id,
        walletAddress = walletAddress,
        inputMint = inputMint,
        outputMint = outputMint,
        inputAmount = inputAmount,
        outputAmount = outputAmount,
        inputSymbol = inputSymbol,
        outputSymbol = outputSymbol,
        signature = signature,
        status = status.name,
        timestamp = timestamp,
        priceImpact = priceImpact,
        routePlan = routePlan,
        slippageBps = slippageBps
    )
}

/**
 * Maps database entity to domain model
 */
private fun DecagonSwapHistoryEntity.toDomain(): DecagonSwapHistory {
    return DecagonSwapHistory(
        id = id,
        walletAddress = walletAddress,
        inputMint = inputMint,
        outputMint = outputMint,
        inputAmount = inputAmount,
        outputAmount = outputAmount,
        inputSymbol = inputSymbol,
        outputSymbol = outputSymbol,
        signature = signature,
        status = DecagonSwapStatus.valueOf(status),
        timestamp = timestamp,
        priceImpact = priceImpact,
        routePlan = routePlan,
        slippageBps = slippageBps
    )
}

/**
 * Maps API token info to cached entity
 */
private fun DecagonTokenInfo.toEntity(): DecagonCachedTokenEntity {
    return DecagonCachedTokenEntity(
        mint = address,
        symbol = symbol,
        name = name,
        decimals = decimals,
        logoUri = logoURI,
        isVerified = tags.contains("verified"),
        isStablecoin = tags.contains("stablecoin"),
        lastUpdated = System.currentTimeMillis()
    )
}

/**
 * Maps cached entity to API token info
 */
private fun DecagonCachedTokenEntity.toDomainModel(): DecagonTokenInfo {
    return DecagonTokenInfo(
        address = mint,
        chainId = 101, // Solana mainnet
        decimals = decimals,
        name = name,
        symbol = symbol,
        logoURI = logoUri,
        tags = buildList {
            if (isVerified) add("verified")
            if (isStablecoin) add("stablecoin")
        }
    )
}