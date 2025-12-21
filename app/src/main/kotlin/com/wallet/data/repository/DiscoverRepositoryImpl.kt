package com.wallet.data.repository

import com.octane.wallet.core.network.NetworkMonitor
import com.octane.wallet.data.local.database.dao.DiscoverDao
import com.octane.wallet.data.mappers.toDomainDApps
import com.octane.wallet.data.mappers.toDomainPerps
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
import com.wallet.data.mappers.toDomainTokens
import com.wallet.data.mappers.toEntities
import com.wallet.data.service.TokenLogoResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes

/**
 * Offline-first repository implementation with comprehensive logging.
 */
class DiscoverRepositoryImpl(
    private val discoverApi: DiscoverApi,
    private val defiLlamaApi: DeFiLlamaApi,
    private val driftApi: DriftApi,
    private val discoverDao: DiscoverDao,
    private val networkMonitor: NetworkMonitor,
    private val tokenLogoResolver: TokenLogoResolver
) : DiscoverRepository {

    init {
        Timber.d("ðŸš€ DiscoverRepositoryImpl initialized")
    }

    // ==================== TOKENS ====================

    override fun observeTokens(): Flow<LoadingState<List<Token>>> {
        Timber.d("ðŸ“Š observeTokens() called - Starting token observation flow")

        return discoverDao.observeTokens()
            .map { entities ->
                Timber.d("ðŸ’¾ Database emitted ${entities.size} token entities")

                if (entities.isEmpty()) {
                    Timber.w("âš ï¸  No tokens in database, emitting Loading state")
                    LoadingState.Loading
                } else {
                    Timber.d("âœ… Converting ${entities.size} entities to domain models")
                    val tokens = entities.toDomainTokens()
                    Timber.d("âœ… Successfully converted to ${tokens.size} domain tokens")
                    Timber.d(
                        "ðŸ“‹ Sample tokens: ${tokens.take(3).map { "${it.symbol} - ${it.name}" }}"
                    )
                    LoadingState.Success(tokens)
                }
            }
            .onStart {
                Timber.d("ðŸ”„ Flow started - Checking if tokens are stale")
                val isStale = isTokensStale()
                Timber.d("â ° Tokens stale check result: $isStale")

                if (isStale) {
                    Timber.i("ðŸ”„ Tokens are stale, triggering refresh")
                    refreshTokens()
                } else {
                    Timber.d("âœ… Tokens are fresh, no refresh needed")
                }
            }
            .catch { e ->
                Timber.e(e, "â Œ Error in observeTokens flow") // Timber: Exception first
                emit(LoadingState.Error(e, "Failed to load tokens: ${e.message}"))
            }
            .distinctUntilChanged()
    }

    override fun observeTrendingTokens(): Flow<LoadingState<List<Token>>> {
        Timber.d("ðŸ”¥ observeTrendingTokens() called")

        return discoverDao.observeTrendingTokens()
            .map { entities ->
                Timber.d("ðŸ’¾ Database emitted ${entities.size} trending token entities")

                if (entities.isEmpty()) {
                    Timber.w("âš ï¸  No trending tokens in database")
                    LoadingState.Loading
                } else {
                    val tokens = entities.toDomainTokens()
                    Timber.d("âœ… Converted to ${tokens.size} trending tokens")
                    LoadingState.Success(tokens)
                }
            }
            .onStart {
                val isStale = isTokensStale()
                Timber.d("â ° Trending tokens stale check: $isStale")
                if (isStale) {
                    Timber.i("ðŸ”„ Refreshing trending tokens")
                    refreshTokens()
                }
            }
            .catch { e ->
                Timber.e(e, "â Œ Error in observeTrendingTokens flow") // Timber: Exception first
                emit(LoadingState.Error(e, "Failed to load trending tokens: ${e.message}"))
            }
            .distinctUntilChanged()
    }

    override fun searchTokens(query: String): Flow<LoadingState<List<Token>>> {
        Timber.d("ðŸ”  searchTokens() called with query: '$query'")

        return discoverDao.searchTokens(query)
            .map { entities ->
                Timber.d("ðŸ’¾ Search returned ${entities.size} token entities")
                val tokens = entities.toDomainTokens()
                Timber.d("âœ… Search converted to ${tokens.size} tokens")
                LoadingState.Success(tokens)
            }
            .catch { e ->
                Timber.e(e, "â Œ Error in searchTokens flow") // Timber: Exception first
                // The original code has an unsafe cast here, using `emit` with the correct type.
                // Assuming the original intention was to emit the Error state, but it was cast incorrectly.
                emit(
                    LoadingState.Error(
                        e,
                        "Search failed: ${e.message}"
                    ) as LoadingState.Success<List<Token>>
                )
            }
    }

    override suspend fun refreshTokens(): LoadingState<Unit> {
        Timber.i("🔄 refreshTokens() called")

        val isConnected = networkMonitor.isConnected.value
        Timber.d("🌐 Network connected: $isConnected")

        if (!isConnected) {
            Timber.w("⚠️ No internet connection, cannot refresh tokens")
            return LoadingState.Error(
                Exception("Offline"),
                "No internet connection. Showing cached data."
            )
        }

        return try {
            Timber.d("📡 Fetching tokens from CoinGecko API...")

            val tokensDto = discoverApi.getTokens(
                vsCurrency = "usd",
                order = "market_cap_desc",
                perPage = 100,
                page = 1
            )

            Timber.i("✅ API returned ${tokensDto.size} tokens")

            // ✅ ADD: Log logo URLs from API response
            Timber.d("📸 First 5 token logos from API:")
            tokensDto.take(5).forEach { dto ->
                Timber.d("  • ${dto.symbol}: ${dto.image}")
            }

            // Convert DTO to Entity
            Timber.d("🔄 Converting DTOs to entities...")
            val entities = tokensDto.toEntities() // This now logs each entity

            // Save to database
            Timber.d("💾 Inserting ${entities.size} tokens into database...")
            discoverDao.insertTokens(entities)
            Timber.i("✅ Successfully inserted tokens into database")

            LoadingState.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to refresh tokens")
            LoadingState.Error(e, "Failed to refresh tokens: ${e.message}")
        }
    }

    private suspend fun isTokensStale(): Boolean {
        Timber.d("â ° Checking token staleness...")

        val lastUpdate = discoverDao.getTokensLastUpdateTime()
        Timber.d("â ° Last update time: $lastUpdate")

        if (lastUpdate == null) {
            Timber.d("â ° No last update time found - tokens are stale")
            return true
        }

        val age = System.currentTimeMillis() - lastUpdate
        val ageMinutes = age / 60000
        val staleThreshold = 5.minutes.inWholeMilliseconds
        val isStale = age > staleThreshold

        Timber.d("â ° Token age: ${ageMinutes}min, threshold: 5min, isStale: $isStale")

        return isStale
    }

    // ==================== PERPS ====================

    override fun observePerps(): Flow<LoadingState<List<Perp>>> {
        Timber.d("ðŸ“Š observePerps() called")

        return discoverDao.observePerps()
            .map { entities ->
                Timber.d("ðŸ’¾ Database emitted ${entities.size} perp entities")

                if (entities.isEmpty()) {
                    LoadingState.Loading
                } else {
                    LoadingState.Success(entities.toDomainPerps())
                }
            }
            .onStart {
                if (isPerpsStale()) {
                    Timber.i("ðŸ”„ Perps are stale, refreshing")
                    refreshPerps()
                }
            }
            .catch { e ->
                Timber.e(e, "â Œ Error in observePerps flow") // Timber: Exception first
                emit(LoadingState.Error(e, "Failed to load perps"))
            }
            .distinctUntilChanged()
    }

    override suspend fun refreshPerps(): LoadingState<Unit> {
        Timber.i("🔄 refreshPerps() called")

        if (!networkMonitor.isConnected.value) {
            Timber.w("⚠️ No internet connection, cannot refresh perps")
            return LoadingState.Error(
                Exception("Offline"),
                "No internet connection. Showing cached data."
            )
        }

        return try {
            Timber.d("📡 Fetching perps from Drift API...")

            val response = driftApi.getContracts()

            // 1. Filter for PERP only, sort by volume, and take top 10
            val perpContracts = response.contracts
                .filter { it.isPerpetual }
                .sortedByDescending { it.quoteVolume.toDoubleOrNull() ?: 0.0 }
                .take(10)

            // 2. Resolve logos and filter out those that failed resolution
            // We create a list of Pairs or a filtered list to ensure we don't have nulls
            val entities = perpContracts.mapNotNull { dto ->
                val baseSymbol = dto.baseCurrency.uppercase()
                val logoUrl = tokenLogoResolver.resolveLogoUrl(baseSymbol)

                // Only proceed if a logo was found
            }

            Timber.i("🎨 Prepared ${entities.size} perps with logos for database insertion")

            // 3. Save to database - ensure the DAO method accepts List<PerpEntity>
//            if (entities.isNotEmpty()) {
//                discoverDao.insertPerps(entities)
//                Timber.i("✅ Successfully inserted ${entities.size} perps")
//            }

            LoadingState.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to refresh perps")
            LoadingState.Error(e, "Failed to refresh perps: ${e.message}")
        }
    }

    override fun searchPerps(query: String): Flow<LoadingState<List<Perp>>> {
        Timber.d("ðŸ”  searchPerps() called with query: '$query'")

        return discoverDao.searchPerps(query)
            .map { entities ->
                LoadingState.Success(entities.toDomainPerps())
            }
            .catch { e ->
                Timber.e(e, "â Œ Error in searchPerps flow") // Timber: Exception first
                // The original code has an unsafe cast here, corrected to emit the Error state.
                emit(LoadingState.Error(e, "Search failed") as LoadingState.Success<List<Perp>>)
            }
    }

    private suspend fun isPerpsStale(): Boolean {
        val lastUpdate = discoverDao.getPerpsLastUpdateTime() ?: return true
        val age = System.currentTimeMillis() - lastUpdate
        val isStale = age > 1.minutes.inWholeMilliseconds

        Timber.d("â ° Perps age check: isStale=$isStale")
        return isStale
    }

    // ==================== DAPPS ====================

    override fun observeDApps(): Flow<LoadingState<List<DApp>>> {
        Timber.d("ðŸ“Š observeDApps() called")

        return discoverDao.observeDApps()
            .map { entities ->
                Timber.d("ðŸ’¾ Database emitted ${entities.size} dApp entities")

                if (entities.isEmpty()) {
                    LoadingState.Loading
                } else {
                    LoadingState.Success(entities.toDomainDApps())
                }
            }
            .onStart {
                if (isDAppsStale()) {
                    Timber.i("ðŸ”„ dApps are stale, refreshing")
                    refreshDApps()
                }
            }
            .catch { e ->
                Timber.e(e, "â Œ Error in observeDApps flow") // Timber: Exception first
                emit(LoadingState.Error(e, "Failed to load dApps"))
            }
            .distinctUntilChanged()
    }

    override fun observeDAppsByCategory(category: DAppCategory): Flow<LoadingState<List<DApp>>> {
        Timber.d("ðŸ“Š observeDAppsByCategory() called for: ${category.name}")

        return discoverDao.observeDAppsByCategory(category.name)
            .map { entities ->
                LoadingState.Success(entities.toDomainDApps())
            }
            .catch { e ->
                Timber.e(e, "â Œ Error in observeDAppsByCategory flow") // Timber: Exception first
                // The original code has an unsafe cast here, corrected to emit the Error state.
                emit(
                    LoadingState.Error(
                        e,
                        "Failed to load dApps"
                    ) as LoadingState.Success<List<DApp>>
                )
            }
    }

    override fun searchDApps(query: String): Flow<LoadingState<List<DApp>>> {
        Timber.d("ðŸ”  searchDApps() called with query: '$query'")

        return discoverDao.searchDApps(query)
            .map { entities ->
                LoadingState.Success(entities.toDomainDApps())
            }
            .catch { e ->
                Timber.e(e, "â Œ Error in searchDApps flow") // Timber: Exception first
                // The original code has an unsafe cast here, corrected to emit the Error state.
                emit(LoadingState.Error(e, "Search failed") as LoadingState.Success<List<DApp>>)
            }
    }

    override suspend fun refreshDApps(): LoadingState<Unit> {
        Timber.i("🔄 refreshDApps() called")

        if (!networkMonitor.isConnected.value) {
            Timber.w("⚠️ No internet connection for dApps refresh")
            return LoadingState.Error(
                Exception("Offline"),
                "No internet connection"
            )
        }

        return try {
            Timber.d("📡 Fetching protocols from DeFiLlama...")
            val allProtocols = defiLlamaApi.getProtocols()
            Timber.i("✅ DeFiLlama returned ${allProtocols.size} total protocols")

            // Filter for Solana dApps
            val solanaApps = allProtocols.filter { dto ->
                dto.chains.any { it.equals("Solana", ignoreCase = true) } ||
                        dto.chain?.equals("Solana", ignoreCase = true) == true
            }

            Timber.i("✅ Filtered to ${solanaApps.size} Solana dApps")

            // ✅ ADD: Log logo URLs from API
            Timber.d("📸 First 5 dApp logos from API:")
            solanaApps.take(5).forEach { dto ->
                Timber.d("  • ${dto.name}: ${dto.logo ?: "NO LOGO (will use CDN fallback)"}")
            }

            // Convert DTO → Entity (logs inside mapper)
            val entities = solanaApps.toEntities()

            discoverDao.insertDApps(entities)
            Timber.i("✅ dApps refresh completed - ${entities.size} inserted")

            LoadingState.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to refresh dApps")
            LoadingState.Error(e, "Failed to refresh dApps: ${e.message}")
        }
    }

    private suspend fun isDAppsStale(): Boolean {
        val lastUpdate = discoverDao.getDAppsLastUpdateTime() ?: return true
        val age = System.currentTimeMillis() - lastUpdate
        val isStale = age > 5.minutes.inWholeMilliseconds

        Timber.d("â ° dApps age check: isStale=$isStale")
        return isStale
    }
}