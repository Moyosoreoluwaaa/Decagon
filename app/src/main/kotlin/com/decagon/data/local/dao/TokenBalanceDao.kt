package com.decagon.data.local.dao

import androidx.room.*
import com.decagon.data.local.entity.TokenBalanceEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for token balance caching.
 *
 * Queries:
 * - Get all balances for a wallet (sorted by value)
 * - Get specific token balance
 * - Update balances after swaps
 * - Clear stale cache
 */
@Dao
interface TokenBalanceDao {

    /**
     * Get all token balances for a wallet.
     * Sorted by USD value (highest first).
     *
     * @param address Wallet address
     * @return Flow emitting balance list
     */
    @Query("""
        SELECT * FROM token_balances 
        WHERE walletAddress = :address 
        ORDER BY valueUsd DESC, uiAmount DESC
    """)
    fun getByWallet(address: String): Flow<List<TokenBalanceEntity>>

    /**
     * Get specific token balance.
     *
     * @param address Wallet address
     * @param mint Token mint address
     * @return Flow emitting balance or null
     */
    @Query("""
        SELECT * FROM token_balances 
        WHERE walletAddress = :address AND mint = :mint
    """)
    fun getBalance(address: String, mint: String): Flow<TokenBalanceEntity?>

    /**
     * Insert or replace balances.
     *
     * @param balances List of balances to cache
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(balances: List<TokenBalanceEntity>)

    /**
     * Insert or replace single balance.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(balance: TokenBalanceEntity)

    /**
     * Delete all balances for a wallet.
     *
     * @param address Wallet address
     */
    @Query("DELETE FROM token_balances WHERE walletAddress = :address")
    suspend fun deleteByWallet(address: String)

    /**
     * Delete stale balances (older than threshold).
     *
     * @param timestamp Threshold timestamp (ms)
     */
    @Query("DELETE FROM token_balances WHERE lastUpdated < :timestamp")
    suspend fun deleteStale(timestamp: Long)

    /**
     * Get count of cached balances.
     *
     * @param address Wallet address
     * @return Flow emitting count
     */
    @Query("SELECT COUNT(*) FROM token_balances WHERE walletAddress = :address")
    fun getCount(address: String): Flow<Int>

    /**
     * Check if balance exists.
     *
     * @param address Wallet address
     * @param mint Token mint
     * @return Flow emitting true if exists
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM token_balances 
            WHERE walletAddress = :address AND mint = :mint
        )
    """)
    fun exists(address: String, mint: String): Flow<Boolean>
}