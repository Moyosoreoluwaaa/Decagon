package com.octane.wallet.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.octane.wallet.data.local.database.entities.DAppEntity
import com.octane.wallet.data.local.database.entities.PerpEntity
import com.octane.wallet.data.local.database.entities.TokenEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Discover screen data.
 * Provides reactive queries for offline-first UX.
 */
@Dao
interface DiscoverDao {

    // ==================== TOKENS ====================

    /**
     * Observe all tokens (reactive).
     * UI subscribes to this for real-time updates.
     */
    @Query("SELECT * FROM discover_tokens ORDER BY rank ASC")
    fun observeTokens(): Flow<List<TokenEntity>>

    /**
     * Observe trending tokens (top 20 by rank).
     */
    @Query("SELECT * FROM discover_tokens ORDER BY rank ASC LIMIT 20")
    fun observeTrendingTokens(): Flow<List<TokenEntity>>

    /**
     * Search tokens by name or symbol.
     */
    @Query("""
        SELECT * FROM discover_tokens 
        WHERE name LIKE '%' || :query || '%' 
        OR symbol LIKE '%' || :query || '%'
        ORDER BY rank ASC
    """)
    fun searchTokens(query: String): Flow<List<TokenEntity>>

    /**
     * Insert or update tokens.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokens(tokens: List<TokenEntity>)

    /**
     * Get staleness timestamp for tokens.
     */
    @Query("SELECT MAX(lastUpdated) FROM discover_tokens")
    suspend fun getTokensLastUpdateTime(): Long?

    // ==================== PERPS ====================

    /**
     * Observe all perps (reactive).
     */
    @Query("SELECT * FROM discover_perps ORDER BY volume24h DESC")
    fun observePerps(): Flow<List<PerpEntity>>

    /**
     * Search perps by symbol.
     */
    @Query("""
        SELECT * FROM discover_perps 
        WHERE symbol LIKE '%' || :query || '%' 
        OR name LIKE '%' || :query || '%'
        ORDER BY volume24h DESC
    """)
    fun searchPerps(query: String): Flow<List<PerpEntity>>

    /**
     * Insert or update perps.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerps(perps: List<PerpEntity>)

    /**
     * Get staleness timestamp for perps.
     */
    @Query("SELECT MAX(lastUpdated) FROM discover_perps")
    suspend fun getPerpsLastUpdateTime(): Long?

    // ==================== DAPPS ====================

    /**
     * Observe all dApps (reactive).
     */
    @Query("SELECT * FROM discover_dapps ORDER BY tvl DESC")
    fun observeDApps(): Flow<List<DAppEntity>>

    /**
     * Observe dApps by category.
     */
    @Query("SELECT * FROM discover_dapps WHERE category = :category ORDER BY tvl DESC")
    fun observeDAppsByCategory(category: String): Flow<List<DAppEntity>>

    /**
     * Search dApps by name.
     */
    @Query("""
        SELECT * FROM discover_dapps 
        WHERE name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY tvl DESC
    """)
    fun searchDApps(query: String): Flow<List<DAppEntity>>

    /**
     * Insert or update dApps.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDApps(dapps: List<DAppEntity>)

    /**
     * Get staleness timestamp for dApps.
     */
    @Query("SELECT MAX(lastUpdated) FROM discover_dapps")
    suspend fun getDAppsLastUpdateTime(): Long?

    // ==================== UTILITY ====================

    /**
     * Clear all discover data (for testing or reset).
     */
    @Query("DELETE FROM discover_tokens")
    suspend fun clearTokens()

    @Query("DELETE FROM discover_perps")
    suspend fun clearPerps()

    @Query("DELETE FROM discover_dapps")
    suspend fun clearDApps()
}