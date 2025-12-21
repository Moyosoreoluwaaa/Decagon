package com.octane.wallet.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.octane.wallet.data.local.database.converters.OctaneTypeConverters
import com.octane.wallet.data.local.database.dao.ApprovalDao
import com.octane.wallet.data.local.database.dao.AssetDao
import com.octane.wallet.data.local.database.dao.ContactDao
import com.wallet.data.local.database.dao.DiscoverDao
import com.octane.wallet.data.local.database.dao.StakingDao
import com.octane.wallet.data.local.database.dao.TransactionDao
import com.octane.wallet.data.local.database.dao.WalletDao
import com.wallet.data.local.database.entities.ApprovalEntity
import com.wallet.data.local.database.entities.AssetEntity
import com.octane.wallet.data.local.database.entities.ContactEntity
import com.octane.wallet.data.local.database.entities.DAppEntity
import com.octane.wallet.data.local.database.entities.PerpEntity
import com.octane.wallet.data.local.database.entities.StakingPositionEntity
import com.wallet.data.local.database.entities.TokenEntity
import com.octane.wallet.data.local.database.entities.TransactionEntity
import com.octane.wallet.data.local.database.entities.WalletEntity

/**
 * Main Room database for Octane Wallet.
 * Handles all local storage for wallets, transactions, assets, and settings.
 * 
 * Version history:
 * - v1: Initial schema (v0.1-v0.9 features)
 * - v2: Add staking positions (v1.1)
 * - v3: Add multi-chain support (v2.0)
 */
@Database(
    entities = [
        WalletEntity::class,
        TransactionEntity::class,
        AssetEntity::class,
        ContactEntity::class,
        ApprovalEntity::class,
        StakingPositionEntity::class,
        // ✅ ADD THESE:
        TokenEntity::class,
        PerpEntity::class,
        DAppEntity::class
    ],
    version = 2, // ✅ INCREMENT
    exportSchema = true
)
@TypeConverters(OctaneTypeConverters::class)
abstract class OctaneDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun assetDao(): AssetDao
    abstract fun contactDao(): ContactDao
    abstract fun approvalDao(): ApprovalDao
    abstract fun stakingDao(): StakingDao
    abstract fun discoverDao(): DiscoverDao // ✅ ADD THIS

    companion object {
        const val DATABASE_NAME = "octane_database"
    }
}