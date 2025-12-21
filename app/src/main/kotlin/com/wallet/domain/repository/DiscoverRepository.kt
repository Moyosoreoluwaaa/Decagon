package com.wallet.domain.repository

import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.DApp
import com.octane.wallet.domain.models.DAppCategory
import com.octane.wallet.domain.models.Perp
import com.octane.wallet.domain.models.Token
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Discover screen data.
 *
 * ✅ UPDATED: Added unlimited data methods for "View All" screens.
 *
 * Method Naming Convention:
 * - observeTrendingTokens() → Limited to 10 for Discover screen
 * - observeAllTokens() → Unlimited for "View All" screen
 */
interface DiscoverRepository {

    // ==================== TOKENS (LIMITED - FOR DISCOVER SCREEN) ====================

    /**
     * Observe tokens (limited to top 10).
     * ✅ Used by: Discover screen tabs
     */
    fun observeTokens(): Flow<LoadingState<List<Token>>>

    /**
     * Observe trending tokens (limited to top 10).
     * ✅ Used by: Discover screen "Tokens" tab
     */
    fun observeTrendingTokens(): Flow<LoadingState<List<Token>>>

    /**
     * Search tokens (limited to top 10 results).
     * ✅ Used by: Discover screen search
     */
    fun searchTokens(query: String): Flow<LoadingState<List<Token>>>

    // ==================== TOKENS (UNLIMITED - FOR VIEW ALL SCREEN) ====================

    /**
     * ✅ NEW: Observe ALL tokens (no limit).
     * Used by: AllTokensScreen
     */
    fun observeAllTokens(): Flow<LoadingState<List<Token>>>

    /**
     * ✅ NEW: Search ALL tokens (no limit).
     * Used by: AllTokensScreen search
     */
    fun searchAllTokens(query: String): Flow<LoadingState<List<Token>>>

    /**
     * Refresh tokens from API.
     * Updates database, Flow emits automatically.
     */
    suspend fun refreshTokens(): LoadingState<Unit>

    // ==================== PERPS (LIMITED - FOR DISCOVER SCREEN) ====================

    /**
     * Observe perps (limited to top 10).
     * ✅ Used by: Discover screen "Perps" tab
     */
    fun observePerps(): Flow<LoadingState<List<Perp>>>

    /**
     * Search perps (limited to top 10 results).
     * ✅ Used by: Discover screen search
     */
    fun searchPerps(query: String): Flow<LoadingState<List<Perp>>>

    // ==================== PERPS (UNLIMITED - FOR VIEW ALL SCREEN) ====================

    /**
     * ✅ NEW: Observe ALL perps (no limit).
     * Used by: AllPerpsScreen
     */
    fun observeAllPerps(): Flow<LoadingState<List<Perp>>>

    /**
     * ✅ NEW: Search ALL perps (no limit).
     * Used by: AllPerpsScreen search
     */
    fun searchAllPerps(query: String): Flow<LoadingState<List<Perp>>>

    /**
     * Refresh perps from API.
     */
    suspend fun refreshPerps(): LoadingState<Unit>

    // ==================== DAPPS (LIMITED - FOR DISCOVER SCREEN) ====================

    /**
     * Observe dApps (limited to top 10).
     * ✅ Used by: Discover screen "Lists" tab
     */
    fun observeDApps(): Flow<LoadingState<List<DApp>>>

    /**
     * Observe dApps by category (limited to top 10).
     */
    fun observeDAppsByCategory(category: DAppCategory): Flow<LoadingState<List<DApp>>>

    /**
     * Search dApps (limited to top 10 results).
     * ✅ Used by: Discover screen search
     */
    fun searchDApps(query: String): Flow<LoadingState<List<DApp>>>

    // ==================== DAPPS (UNLIMITED - FOR VIEW ALL SCREEN) ====================

    /**
     * ✅ NEW: Observe ALL dApps (no limit).
     * Used by: AllDAppsScreen
     */
    fun observeAllDApps(): Flow<LoadingState<List<DApp>>>

    /**
     * ✅ NEW: Search ALL dApps (no limit).
     * Used by: AllDAppsScreen search
     */
    fun searchAllDApps(query: String): Flow<LoadingState<List<DApp>>>

    /**
     * Refresh dApps from API.
     */
    suspend fun refreshDApps(): LoadingState<Unit>
}