package com.decagon.data.local.entity

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Decagon Swap History Entity
 * 
 * Room database entity for persisting swap history locally.
 * This allows users to view past swaps even when offline.
 * 
 * Table: decagon_swap_history
 */
@Entity(
    tableName = "decagon_swap_history",
    indices = [
        Index(value = ["walletAddress"]),
        Index(value = ["timestamp"]),
        Index(value = ["status"])
    ]
)
data class DecagonSwapHistoryEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "walletAddress")
    val walletAddress: String,
    
    @ColumnInfo(name = "inputMint")
    val inputMint: String,
    
    @ColumnInfo(name = "outputMint")
    val outputMint: String,
    
    @ColumnInfo(name = "inputAmount")
    val inputAmount: Double,
    
    @ColumnInfo(name = "outputAmount")
    val outputAmount: Double,
    
    @ColumnInfo(name = "inputSymbol")
    val inputSymbol: String,
    
    @ColumnInfo(name = "outputSymbol")
    val outputSymbol: String,
    
    @ColumnInfo(name = "signature")
    val signature: String? = null,
    
    @ColumnInfo(name = "status")
    val status: String, // PENDING, CONFIRMED, FAILED
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "priceImpact")
    val priceImpact: Double,
    
    @ColumnInfo(name = "routePlan")
    val routePlan: String? = null,
    
    @ColumnInfo(name = "slippageBps")
    val slippageBps: Int
)

/**
 * Decagon Swap History DAO
 * 
 * Data Access Object for swap history operations.
 * All queries return Flow for reactive UI updates.
 */
@Dao
interface DecagonSwapHistoryDao {
    
    /**
     * Inserts new swap record
     * 
     * @param swap Swap history entity
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(swap: DecagonSwapHistoryEntity)
    
    /**
     * Gets swap history for wallet, ordered by timestamp (newest first)
     * 
     * @param walletAddress Wallet address
     * @return Flow emitting list of swaps
     */
    @Query("""
        SELECT * FROM decagon_swap_history 
        WHERE walletAddress = :walletAddress 
        ORDER BY timestamp DESC
    """)
    fun getSwapHistory(walletAddress: String): Flow<List<DecagonSwapHistoryEntity>>
    
    /**
     * Gets single swap by ID
     * 
     * @param swapId Swap identifier
     * @return Flow emitting swap or null
     */
    @Query("SELECT * FROM decagon_swap_history WHERE id = :swapId")
    fun getSwapById(swapId: String): Flow<DecagonSwapHistoryEntity?>
    
    /**
     * Gets recent swaps (last 50) for wallet
     * 
     * @param walletAddress Wallet address
     * @return Flow emitting recent swaps
     */
    @Query("""
        SELECT * FROM decagon_swap_history 
        WHERE walletAddress = :walletAddress 
        ORDER BY timestamp DESC 
        LIMIT 50
    """)
    fun getRecentSwaps(walletAddress: String): Flow<List<DecagonSwapHistoryEntity>>
    
    /**
     * Gets pending swaps (not yet confirmed)
     * 
     * @param walletAddress Wallet address
     * @return Flow emitting pending swaps
     */
    @Query("""
        SELECT * FROM decagon_swap_history 
        WHERE walletAddress = :walletAddress 
        AND status = 'PENDING' 
        ORDER BY timestamp DESC
    """)
    fun getPendingSwaps(walletAddress: String): Flow<List<DecagonSwapHistoryEntity>>
    
    /**
     * Updates swap status
     * 
     * @param swapId Swap identifier
     * @param status New status
     * @param signature Transaction signature (if confirmed)
     */
    @Query("""
        UPDATE decagon_swap_history 
        SET status = :status, signature = :signature 
        WHERE id = :swapId
    """)
    suspend fun updateSwapStatus(
        swapId: String,
        status: String,
        signature: String? = null
    )
    
    /**
     * Deletes swap record
     * 
     * @param swap Swap to delete
     */
    @Delete
    suspend fun delete(swap: DecagonSwapHistoryEntity)
    
    /**
     * Deletes all swap history for wallet
     * 
     * WARNING: Irreversible operation
     * 
     * @param walletAddress Wallet address
     */
    @Query("DELETE FROM decagon_swap_history WHERE walletAddress = :walletAddress")
    suspend fun deleteAllSwapsForWallet(walletAddress: String)
    
    /**
     * Gets total swap count for wallet
     * 
     * @param walletAddress Wallet address
     * @return Flow emitting swap count
     */
    @Query("SELECT COUNT(*) FROM decagon_swap_history WHERE walletAddress = :walletAddress")
    fun getSwapCount(walletAddress: String): Flow<Int>
    
    /**
     * Gets swaps by status
     * 
     * @param walletAddress Wallet address
     * @param status Status filter
     * @return Flow emitting filtered swaps
     */
    @Query("""
        SELECT * FROM decagon_swap_history 
        WHERE walletAddress = :walletAddress 
        AND status = :status 
        ORDER BY timestamp DESC
    """)
    fun getSwapsByStatus(
        walletAddress: String,
        status: String
    ): Flow<List<DecagonSwapHistoryEntity>>
}

/**
 * Decagon Cached Token Entity
 * 
 * Caches token metadata locally to reduce API calls.
 * 
 * Table: decagon_cached_tokens
 */
@Entity(
    tableName = "decagon_cached_tokens",
    indices = [Index(value = ["symbol"])]
)
data class DecagonCachedTokenEntity(
    @PrimaryKey
    val mint: String,
    
    @ColumnInfo(name = "symbol")
    val symbol: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "decimals")
    val decimals: Int,
    
    @ColumnInfo(name = "logoUri")
    val logoUri: String? = null,
    
    @ColumnInfo(name = "isVerified")
    val isVerified: Boolean = false,
    
    @ColumnInfo(name = "isStablecoin")
    val isStablecoin: Boolean = false,
    
    @ColumnInfo(name = "lastUpdated")
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Decagon Cached Token DAO
 */
@Dao
interface DecagonCachedTokenDao {
    
    /**
     * Inserts or updates token metadata
     * 
     * @param token Token entity
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(token: DecagonCachedTokenEntity)
    
    /**
     * Inserts multiple tokens
     * 
     * @param tokens List of token entities
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tokens: List<DecagonCachedTokenEntity>)
    
    /**
     * Gets all cached tokens
     * 
     * @return Flow emitting token list
     */
    @Query("SELECT * FROM decagon_cached_tokens ORDER BY symbol ASC")
    fun getAllTokens(): Flow<List<DecagonCachedTokenEntity>>
    
    /**
     * Gets verified tokens only
     * 
     * @return Flow emitting verified tokens
     */
    @Query("SELECT * FROM decagon_cached_tokens WHERE isVerified = 1 ORDER BY symbol ASC")
    fun getVerifiedTokens(): Flow<List<DecagonCachedTokenEntity>>
    
    /**
     * Gets token by mint address
     * 
     * @param mint Token mint address
     * @return Flow emitting token or null
     */
    @Query("SELECT * FROM decagon_cached_tokens WHERE mint = :mint")
    fun getTokenByMint(mint: String): Flow<DecagonCachedTokenEntity?>
    
    /**
     * Searches tokens by symbol or name
     * 
     * @param query Search query
     * @return Flow emitting matching tokens
     */
    @Query("""
        SELECT * FROM decagon_cached_tokens 
        WHERE symbol LIKE '%' || :query || '%' 
        OR name LIKE '%' || :query || '%' 
        ORDER BY symbol ASC
    """)
    fun searchTokens(query: String): Flow<List<DecagonCachedTokenEntity>>
    
    /**
     * Deletes all cached tokens
     * 
     * Used when refreshing token list
     */
    @Query("DELETE FROM decagon_cached_tokens")
    suspend fun deleteAll()
    
    /**
     * Deletes tokens older than specified timestamp
     * 
     * @param timestamp Cutoff timestamp
     */
    @Query("DELETE FROM decagon_cached_tokens WHERE lastUpdated < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}