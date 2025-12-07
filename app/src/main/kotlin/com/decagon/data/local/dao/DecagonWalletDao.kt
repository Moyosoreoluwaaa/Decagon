package com.decagon.data.local.dao

import androidx.room.*
import com.decagon.data.local.entity.DecagonWalletEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for wallet CRUD operations.
 * 
 * All queries return Flow for reactive UI updates.
 */
@Dao
interface DecagonWalletDao {
    
    /**
     * Inserts a new wallet.
     * 
     * @param wallet Wallet entity
     * @throws SQLiteConstraintException if wallet with same ID exists
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(wallet: DecagonWalletEntity)
    
    /**
     * Updates existing wallet.
     * 
     * @param wallet Wallet entity with updated fields
     */
    @Update
    suspend fun update(wallet: DecagonWalletEntity)
    
    /**
     * Deletes a wallet.
     * 
     * WARNING: This makes wallet unrecoverable without mnemonic backup.
     * 
     * @param wallet Wallet to delete
     */
    @Delete
    suspend fun delete(wallet: DecagonWalletEntity)
    
    /**
     * Gets wallet by ID.
     * 
     * @param id Wallet identifier
     * @return Flow emitting wallet or null
     */
    @Query("SELECT * FROM decagon_wallets WHERE id = :id")
    fun getById(id: String): Flow<DecagonWalletEntity?>
    
    /**
     * Gets all wallets ordered by creation date.
     * 
     * @return Flow emitting list of wallets
     */
    @Query("SELECT * FROM decagon_wallets ORDER BY createdAt DESC")
    fun getAll(): Flow<List<DecagonWalletEntity>>
    
    /**
     * Gets active wallet.
     * 
     * @return Flow emitting active wallet or null
     */
    @Query("SELECT * FROM decagon_wallets WHERE isActive = 1 LIMIT 1")
    fun getActive(): Flow<DecagonWalletEntity?>
    
    /**
     * Sets a wallet as active and deactivates all others.
     * 
     * @param walletId ID of wallet to activate
     */
    @Transaction
    suspend fun setActive(walletId: String) {
        deactivateAll()
        activateWallet(walletId)
    }
    
    @Query("UPDATE decagon_wallets SET isActive = 0")
    suspend fun deactivateAll()
    
    @Query("UPDATE decagon_wallets SET isActive = 1 WHERE id = :walletId")
    suspend fun activateWallet(walletId: String)
    
    /**
     * Gets wallet count.
     * 
     * @return Flow emitting total wallet count
     */
    @Query("SELECT COUNT(*) FROM decagon_wallets")
    fun getCount(): Flow<Int>
    
    /**
     * Checks if wallet with ID exists.
     * 
     * @param id Wallet identifier
     * @return Flow emitting true if exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM decagon_wallets WHERE id = :id)")
    fun exists(id: String): Flow<Boolean>
}