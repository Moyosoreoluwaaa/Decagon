package com.decagon.data.local.dao

import androidx.room.*
import com.decagon.data.local.entity.SwapHistoryEntity
import com.decagon.data.local.entity.TokenCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SwapHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(swap: SwapHistoryEntity)

    @Query("SELECT * FROM swap_history WHERE walletId = :walletId ORDER BY timestamp DESC")
    fun getByWallet(walletId: String): Flow<List<SwapHistoryEntity>>

    @Query("SELECT * FROM swap_history WHERE id = :swapId")
    fun getById(swapId: String): Flow<SwapHistoryEntity?>

    @Query("SELECT * FROM swap_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<SwapHistoryEntity>>

    @Query("UPDATE swap_history SET status = :status, signature = :signature WHERE id = :swapId")
    suspend fun updateStatus(swapId: String, status: String, signature: String?)

    @Query("UPDATE swap_history SET status = :status, errorMessage = :error WHERE id = :swapId")
    suspend fun updateStatusWithError(swapId: String, status: String, error: String)

    @Query("DELETE FROM swap_history WHERE walletId = :walletId")
    suspend fun deleteByWallet(walletId: String)
}

@Dao
interface TokenCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(token: TokenCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tokens: List<TokenCacheEntity>)

    @Query("SELECT * FROM token_cache WHERE address = :address")
    suspend fun getByAddress(address: String): TokenCacheEntity?

    @Query("SELECT * FROM token_cache WHERE address IN (:addresses)")
    suspend fun getByAddresses(addresses: List<String>): List<TokenCacheEntity>

    @Query("SELECT * FROM token_cache WHERE symbol LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<TokenCacheEntity>

    @Query("DELETE FROM token_cache WHERE cachedAt < :expireTime")
    suspend fun deleteExpired(expireTime: Long)

    @Query("DELETE FROM token_cache")
    suspend fun clearAll()
}