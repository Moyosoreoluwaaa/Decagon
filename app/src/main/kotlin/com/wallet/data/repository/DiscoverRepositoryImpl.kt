package com.wallet.data.repository

import com.octane.wallet.core.network.NetworkMonitor
import com.wallet.data.local.database.dao.DiscoverDao
import com.octane.wallet.data.mappers.toDomainDApps
import com.octane.wallet.data.mappers.toEntities
import com.octane.wallet.data.remote.api.DeFiLlamaApi
import com.octane.wallet.data.remote.api.DiscoverApi
import com.octane.wallet.data.remote.api.DriftApi
import com.octane.wallet.domain.models.DApp
import com.octane.wallet.domain.models.DAppCategory
import com.octane.wallet.domain.models.Perp
import com.octane.wallet.domain.models.Token
import com.wallet.domain.repository.DiscoverRepository
import com.wallet.core.util.LoadingState
import com.wallet.data.mappers.toDomainPerps
import com.wallet.data.mappers.toDomainTokens
import com.wallet.data.mappers.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes

/**
 * ✅ OPTIMIZED: Offline-first repository with performance improvements.
 *
 * Key Changes:
 * 1. Removed TokenLogoResolver (slow API calls)
 * 2. Perp logos now instant via PerpLogoProvider
 * 3. Token/DApp logos come directly from API responses
 * 4. Limit to 10 items per category on Discover screen
 */
class DiscoverRepositoryImpl(
    private val discoverApi: DiscoverApi,
    private val defiLlamaApi: DeFiLlamaApi,
    private val driftApi: DriftApi,
    private val discoverDao: DiscoverDao,
    private val networkMonitor: NetworkMonitor
) : DiscoverRepository {

    // ==================== TOKENS ====================

    /**
     * ✅ OPTIMIZED: Observe tokens with limit for Discover screen.
     */
    override fun observeTokens(): Flow<LoadingState<List<Token>>> {
        return discoverDao.observeTokens()
            .map { entities ->
                if (entities.isEmpty()) {
                    LoadingState.Loading
                } else {
                    // ✅ Limit to top 10 for Discover screen performance
                    val tokens = entities.take(10).toDomainTokens()
                    LoadingState.Success(tokens)
                }
            }
            .onStart {
                val count = discoverDao.getTokensCount()
                if (count == 0 || isTokensStale()) {
                    Timber.i("🔄 Tokens refresh needed (count=$count)")
                    refreshTokens()
                } else {
                    Timber.d("✅ Using cached tokens (count=$count)")
                }
            }
            .catch { e ->
                Timber.e(e, "❌ Error in observeTokens")
                emit(LoadingState.Error(e, "Failed to load tokens"))
            }
            .distinctUntilChanged()
    }

    override fun observeTrendingTokens(): Flow<LoadingState<List<Token>>> {
        return discoverDao.observeTrendingTokens()
            .map { entities ->
                if (entities.isEmpty()) LoadingState.Loading
                else {
                    // ✅ Limit to top 10
                    val tokens = entities.take(10).toDomainTokens()
                    LoadingState.Success(tokens)
                }
            }
            .onStart {
                val count = discoverDao.getTokensCount()
                if (count == 0 || isTokensStale()) {
                    refreshTokens()
                }
            }
            .catch { e ->
                Timber.e(e, "❌ Error in observeTrendingTokens")
                emit(LoadingState.Error(e, "Failed to load trending tokens"))
            }
            .distinctUntilChanged()
    }

    override fun searchTokens(query: String): Flow<LoadingState<List<Token>>> {
        return discoverDao.searchTokens(query)
            .map { entities ->
                // ✅ Search results also limited to 10 for performance
                LoadingState.Success(entities.take(10).toDomainTokens()) as LoadingState<List<Token>>
            }
            .catch { e ->
                Timber.e(e, "❌ Error in searchTokens")
                emit(LoadingState.Error(e, "Search failed"))
            }
    }


    /**
     * ✅ NEW: Observe ALL tokens (no 10-item limit).
     * Used by AllTokensScreen.
     */
    override fun observeAllTokens(): Flow<LoadingState<List<Token>>> {
        return discoverDao.observeTokens()
            .map { entities ->
                if (entities.isEmpty()) {
                    LoadingState.Loading
                } else {
                    // ✅ NO .take(10) - return all tokens
                    val tokens = entities.toDomainTokens()
                    LoadingState.Success(tokens)
                }
            }
            .onStart {
                val count = discoverDao.getTokensCount()
                if (count == 0 || isTokensStale()) {
                    Timber.i("🔄 Tokens refresh needed (count=$count)")
                    refreshTokens()
                }
            }
            .catch { e ->
                Timber.e(e, "❌ Error in observeAllTokens")
                emit(LoadingState.Error(e, "Failed to load tokens"))
            }
            .distinctUntilChanged()
    }

    /**
     * ✅ NEW: Search ALL tokens (no limit).
     */
    override fun searchAllTokens(query: String): Flow<LoadingState<List<Token>>> {
        return discoverDao.searchTokens(query)
            .map { entities ->
                // ✅ NO .take(10) - return all search results
                LoadingState.Success(entities.toDomainTokens()) as LoadingState<List<Token>>
            }
            .catch { e ->
                Timber.e(e, "❌ Error in searchAllTokens")
                emit(LoadingState.Error(e, "Search failed"))
            }
    }


    override suspend fun refreshTokens(): LoadingState<Unit> = coroutineScope {
        if (!networkMonitor.isConnected.value) {
            Timber.w("⚠️ Offline - skipping token refresh")
            return@coroutineScope LoadingState.Error(
                Exception("Offline"),
                "No internet connection"
            )
        }

        try {
            Timber.d("📡 Fetching tokens from CoinGecko...")

            // ✅ Parallel fetch
            val fetchJob = async(Dispatchers.IO) {
                discoverApi.getTokens(
                    vsCurrency = "usd",
                    order = "market_cap_desc",
                    perPage = 100,
                    page = 1
                )
            }

            val tokensDto = fetchJob.await()
            Timber.i("✅ API returned ${tokensDto.size} tokens")

            // ✅ CoinGecko provides logos in `image` field - no resolver needed
            val entities = tokensDto.map { dto ->
                dto.toEntity() // Already has logo from API
            }

            discoverDao.insertTokens(entities)
            Timber.i("✅ Inserted ${entities.size} tokens")

            LoadingState.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to refresh tokens")
            LoadingState.Error(e, "Failed to refresh tokens")
        }
    }

    private suspend fun isTokensStale(): Boolean {
        val lastUpdate = discoverDao.getTokensLastUpdateTime() ?: return true
        val age = System.currentTimeMillis() - lastUpdate
        val isStale = age > 5.minutes.inWholeMilliseconds
        Timber.d("⏰ Token age check: isStale=$isStale")
        return isStale
    }

    // ==================== PERPS ====================

    /**
     * ✅ OPTIMIZED: Observe perps with limit for Discover screen.
     */
    override fun observePerps(): Flow<LoadingState<List<Perp>>> {
        return discoverDao.observePerps()
            .map { entities ->
                if (entities.isEmpty()) LoadingState.Loading
                else {
                    // ✅ Limit to top 10
                    val perps = entities.take(10).toDomainPerps()
                    LoadingState.Success(perps)
                }
            }
            .onStart {
                val count = discoverDao.getPerpsCount()
                if (count == 0 || isPerpsStale()) {
                    refreshPerps()
                }
            }
            .catch { e ->
                Timber.e(e, "❌ Error in observePerps")
                emit(LoadingState.Error(e, "Failed to load perps"))
            }
            .distinctUntilChanged()
    }

    override suspend fun refreshPerps(): LoadingState<Unit> = coroutineScope {
        if (!networkMonitor.isConnected.value) {
            return@coroutineScope LoadingState.Error(
                Exception("Offline"),
                "No internet connection"
            )
        }

        try {
            Timber.d("📡 Fetching perps from Drift...")
            val response = driftApi.getContracts()

            val perpContracts = response.contracts
                .filter { it.isPerpetual }
                .sortedByDescending { it.quoteVolume.toDoubleOrNull() ?: 0.0 }
                .take(20) // Fetch 20, but only show 10 on Discover

            // ✅ NO LOGO RESOLVER - logos resolved instantly in mapper
            val entities = perpContracts.map { dto ->
                dto.toEntity() // Instant logo via PerpLogoProvider
            }

            discoverDao.insertPerps(entities)
            Timber.i("✅ Inserted ${entities.size} perps")

            LoadingState.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to refresh perps")
            LoadingState.Error(e, "Failed to refresh perps")
        }
    }

    override fun searchPerps(query: String): Flow<LoadingState<List<Perp>>> {
        return discoverDao.searchPerps(query)
            .map { entities ->
                // ✅ Search limited to 10
                LoadingState.Success(entities.take(10).toDomainPerps()) as LoadingState<List<Perp>>
            }
            .catch { e ->
                Timber.e(e, "❌ Error in searchPerps")
                emit(LoadingState.Error(e, "Search failed"))
            }
    }


// ==================== PERPS (UNLIMITED) ====================

    /**
     * ✅ NEW: Observe ALL perps (no 10-item limit).
     * Used by AllPerpsScreen.
     */
    override fun observeAllPerps(): Flow<LoadingState<List<Perp>>> {
        return discoverDao.observePerps()
            .map { entities ->
                if (entities.isEmpty()) LoadingState.Loading
                else {
                    // ✅ NO .take(10) - return all perps
                    val perps = entities.toDomainPerps()
                    LoadingState.Success(perps)
                }
            }
            .onStart {
                val count = discoverDao.getPerpsCount()
                if (count == 0 || isPerpsStale()) {
                    refreshPerps()
                }
            }
            .catch { e ->
                Timber.e(e, "❌ Error in observeAllPerps")
                emit(LoadingState.Error(e, "Failed to load perps"))
            }
            .distinctUntilChanged()
    }

    /**
     * ✅ NEW: Search ALL perps (no limit).
     */
    override fun searchAllPerps(query: String): Flow<LoadingState<List<Perp>>> {
        return discoverDao.searchPerps(query)
            .map { entities ->
                // ✅ NO .take(10) - return all search results
                LoadingState.Success(entities.toDomainPerps()) as LoadingState<List<Perp>>
            }
            .catch { e ->
                Timber.e(e, "❌ Error in searchAllPerps")
                emit(LoadingState.Error(e, "Search failed"))
            }
    }

    private suspend fun isPerpsStale(): Boolean {
        val lastUpdate = discoverDao.getPerpsLastUpdateTime() ?: return true
        return System.currentTimeMillis() - lastUpdate > 1.minutes.inWholeMilliseconds
    }

    // ==================== DAPPS ====================

    /**
     * ✅ OPTIMIZED: Observe dApps with limit for Discover screen.
     */
    override fun observeDApps(): Flow<LoadingState<List<DApp>>> {
        return discoverDao.observeDApps()
            .map { entities ->
                if (entities.isEmpty()) LoadingState.Loading
                else {
                    // ✅ Limit to top 10
                    val dapps = entities.take(10).toDomainDApps()
                    LoadingState.Success(dapps)
                }
            }
            .onStart {
                val count = discoverDao.getDAppsCount()
                if (count == 0 || isDAppsStale()) {
                    refreshDApps()
                }
            }
            .catch { e ->
                Timber.e(e, "❌ Error in observeDApps")
                emit(LoadingState.Error(e, "Failed to load dApps"))
            }
            .distinctUntilChanged()
    }

    override fun observeDAppsByCategory(category: DAppCategory): Flow<LoadingState<List<DApp>>> {
        return discoverDao.observeDAppsByCategory(category.name)
            .map { entities ->
                // ✅ Limit to 10 per category
                LoadingState.Success(entities.take(10).toDomainDApps()) as LoadingState<List<DApp>>
            }
            .catch { e ->
                Timber.e(e, "❌ Error in observeDAppsByCategory")
                emit(LoadingState.Error(e, "Failed to load dApps"))
            }
    }

    override fun searchDApps(query: String): Flow<LoadingState<List<DApp>>> {
        return discoverDao.searchDApps(query)
            .map { entities ->
                // ✅ Search limited to 10
                LoadingState.Success(entities.take(10).toDomainDApps()) as LoadingState<List<DApp>>
            }
            .catch { e ->
                Timber.e(e, "❌ Error in searchDApps")
                emit(LoadingState.Error(e, "Search failed"))
            }
    }


// ==================== DAPPS (UNLIMITED) ====================

    /**
     * ✅ NEW: Observe ALL dApps (no 10-item limit).
     * Used by AllDAppsScreen.
     */
    override fun observeAllDApps(): Flow<LoadingState<List<DApp>>> {
        return discoverDao.observeDApps()
            .map { entities ->
                if (entities.isEmpty()) LoadingState.Loading
                else {
                    // ✅ NO .take(10) - return all dApps
                    val dapps = entities.toDomainDApps()
                    LoadingState.Success(dapps)
                }
            }
            .onStart {
                val count = discoverDao.getDAppsCount()
                if (count == 0 || isDAppsStale()) {
                    refreshDApps()
                }
            }
            .catch { e ->
                Timber.e(e, "❌ Error in observeAllDApps")
                emit(LoadingState.Error(e, "Failed to load dApps"))
            }
            .distinctUntilChanged()
    }

    /**
     * ✅ NEW: Search ALL dApps (no limit).
     */
    override fun searchAllDApps(query: String): Flow<LoadingState<List<DApp>>> {
        return discoverDao.searchDApps(query)
            .map { entities ->
                // ✅ NO .take(10) - return all search results
                LoadingState.Success(entities.toDomainDApps()) as LoadingState<List<DApp>>
            }
            .catch { e ->
                Timber.e(e, "❌ Error in searchAllDApps")
                emit(LoadingState.Error(e, "Search failed"))
            }
    }

    override suspend fun refreshDApps(): LoadingState<Unit> = coroutineScope {
        if (!networkMonitor.isConnected.value) {
            return@coroutineScope LoadingState.Error(
                Exception("Offline"),
                "No internet connection"
            )
        }

        try {
            Timber.d("📡 Fetching dApps from DeFiLlama...")
            val allProtocols = defiLlamaApi.getProtocols()

            val solanaApps = allProtocols.filter { dto ->
                dto.chains.any { it.equals("Solana", ignoreCase = true) }
            }

            Timber.i("✅ Filtered to ${solanaApps.size} Solana dApps")

            // ✅ DeFiLlama provides logos - no resolver needed
            val entities = solanaApps.toEntities()
            discoverDao.insertDApps(entities)
            Timber.i("✅ Inserted ${entities.size} dApps")

            LoadingState.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to refresh dApps")
            LoadingState.Error(e, "Failed to refresh dApps")
        }
    }

    private suspend fun isDAppsStale(): Boolean {
        val lastUpdate = discoverDao.getDAppsLastUpdateTime() ?: return true
        return System.currentTimeMillis() - lastUpdate > 5.minutes.inWholeMilliseconds
    }
}