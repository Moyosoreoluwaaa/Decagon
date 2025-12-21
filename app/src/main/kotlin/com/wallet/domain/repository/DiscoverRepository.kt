package com.wallet.domain.repository

import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.DApp
import com.octane.wallet.domain.models.DAppCategory
import com.octane.wallet.domain.models.Perp
import com.octane.wallet.domain.models.Token
import kotlinx.coroutines.flow.Flow


/**
 * Repository interface for Discover screen data.
 * Abstracts data sources (API + Database) from domain layer.
 */
interface DiscoverRepository {

    // ==================== TOKENS ====================

    /**
     * Observe all tokens (reactive).
     * Emits cached data immediately, refreshes in background.
     */
    fun observeTokens(): Flow<LoadingState<List<Token>>>

    /**
     * Observe trending tokens (top 20).
     */
    fun observeTrendingTokens(): Flow<LoadingState<List<Token>>>

    /**
     * Search tokens by query.
     */
    fun searchTokens(query: String): Flow<LoadingState<List<Token>>>

    /**
     * Refresh tokens from API.
     * Updates database, Flow emits automatically.
     */
    suspend fun refreshTokens(): LoadingState<Unit>

    // ==================== PERPS ====================

    /**
     * Observe all perps (reactive).
     */
    fun observePerps(): Flow<LoadingState<List<Perp>>>

    /**
     * Search perps by query.
     */
    fun searchPerps(query: String): Flow<LoadingState<List<Perp>>>

    /**
     * Refresh perps from API.
     */
    suspend fun refreshPerps(): LoadingState<Unit>

    // ==================== DAPPS ====================

    /**
     * Observe all dApps (reactive).
     */
    fun observeDApps(): Flow<LoadingState<List<DApp>>>

    /**
     * Observe dApps by category.
     */
    fun observeDAppsByCategory(category: DAppCategory): Flow<LoadingState<List<DApp>>>

    /**
     * Search dApps by query.
     */
    fun searchDApps(query: String): Flow<LoadingState<List<DApp>>>

    /**
     * Refresh dApps from API.
     */
    suspend fun refreshDApps(): LoadingState<Unit>
}