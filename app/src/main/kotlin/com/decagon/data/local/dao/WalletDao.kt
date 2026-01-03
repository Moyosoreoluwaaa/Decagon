package com.decagon.data.local.dao

import androidx.room.*
import com.decagon.data.local.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for wallet operations.
 * All queries return Flow for reactive updates (observability pattern).
 */
@Dao
interface WalletDao {
    
    /**
     * Observe all wallets for v1.4 multi-account switcher.
     */
    @Query("SELECT * FROM wallets ORDER BY is_active DESC, created_at DESC")
    fun observeAllWallets(): Flow<List<WalletEntity>>
    
    /**
     * Observe active wallet (v0.1-v1.3).
     */
    @Query("SELECT * FROM wallets WHERE is_active = 1 LIMIT 1")
    fun observeActiveWallet(): Flow<WalletEntity?>
    
    /**
     * Get wallet by ID (for immediate access, no observation).
     */
    @Query("SELECT * FROM wallets WHERE id = :walletId")
    suspend fun getWalletById(walletId: String): WalletEntity?
    
    /**
     * Get wallet by public key (for validation).
     */
    @Query("SELECT * FROM wallets WHERE public_key = :publicKey")
    suspend fun getWalletByPublicKey(publicKey: String): WalletEntity?
    
    /**
     * Insert or update wallet (v0.1 setup, v0.7 naming).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)
    
    /**
     * Update wallet fields (v0.7 icon/color, v1.5 hardware status).
     */
    @Update
    suspend fun updateWallet(wallet: WalletEntity)
    
    /**
     * Switch active wallet (v1.4 multi-account).
     */
    @Transaction
    suspend fun setActiveWallet(walletId: String) {
        // Deactivate all wallets
        deactivateAllWallets()
        // Activate selected wallet
        activateWallet(walletId)
    }
    
    @Query("UPDATE wallets SET is_active = 0")
    suspend fun deactivateAllWallets()
    
    @Query("UPDATE wallets SET is_active = 1 WHERE id = :walletId")
    suspend fun activateWallet(walletId: String)
    
    /**
     * Delete wallet (v1.4 account deletion).
     */
    @Query("DELETE FROM wallets WHERE id = :walletId")
    suspend fun deleteWallet(walletId: String)
    
    /**
     * Count total wallets (for UI empty state).
     */
    @Query("SELECT COUNT(*) FROM wallets")
    fun observeWalletCount(): Flow<Int>
}