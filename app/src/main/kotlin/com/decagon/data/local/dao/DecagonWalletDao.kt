package com.decagon.data.local.dao

import androidx.room.*
import com.decagon.data.local.entity.DecagonWalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DecagonWalletDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(wallet: DecagonWalletEntity)

    @Update
    suspend fun update(wallet: DecagonWalletEntity)

    @Delete
    suspend fun delete(wallet: DecagonWalletEntity)

    @Query("SELECT * FROM decagon_wallets WHERE id = :id")
    fun getById(id: String): Flow<DecagonWalletEntity?>

    @Query("SELECT * FROM decagon_wallets ORDER BY createdAt DESC")
    fun getAll(): Flow<List<DecagonWalletEntity>>

    @Query("SELECT * FROM decagon_wallets WHERE isActive = 1 LIMIT 1")
    fun getActive(): Flow<DecagonWalletEntity?>

    @Transaction
    suspend fun setActive(walletId: String) {
        deactivateAll()
        activateWallet(walletId)
    }

    @Query("UPDATE decagon_wallets SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE decagon_wallets SET isActive = 1 WHERE id = :walletId")
    suspend fun activateWallet(walletId: String)

    @Query("UPDATE decagon_wallets SET activeChainId = :chainId WHERE id = :walletId")
    suspend fun updateActiveChain(walletId: String, chainId: String)

    // ✅ NEW: Update cached balance
    @Query("""
        UPDATE decagon_wallets 
        SET cachedBalance = :balance, 
            lastBalanceFetch = :timestamp,
            balanceStale = 0
        WHERE id = :walletId
    """)
    suspend fun updateBalance(walletId: String, balance: Double, timestamp: Long)

    // ✅ NEW: Mark balance as stale
    @Query("UPDATE decagon_wallets SET balanceStale = 1 WHERE id = :walletId")
    suspend fun markBalanceStale(walletId: String)

    @Query("SELECT COUNT(*) FROM decagon_wallets")
    fun getCount(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM decagon_wallets WHERE id = :id)")
    fun exists(id: String): Flow<Boolean>
}