package com.decagon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.decagon.data.local.dao.DecagonWalletDao
import com.decagon.data.local.dao.OnRampDao
import com.decagon.data.local.dao.PendingTxDao
import com.decagon.data.local.dao.TransactionDao
import com.decagon.data.local.entity.DecagonCachedTokenDao
import com.decagon.data.local.entity.DecagonCachedTokenEntity
import com.decagon.data.local.entity.DecagonSwapHistoryDao
import com.decagon.data.local.entity.DecagonSwapHistoryEntity
import com.decagon.data.local.entity.DecagonWalletEntity
import com.decagon.data.local.entity.OnRampTransactionEntity
import com.decagon.data.local.entity.PendingTxEntity
import com.decagon.data.local.entity.TransactionEntity

@Database(
    entities = [
        DecagonWalletEntity::class,
        PendingTxEntity::class,
        TransactionEntity::class,
        OnRampTransactionEntity::class, // ✅ ADD
        DecagonSwapHistoryEntity::class,  // ← NEW
        DecagonCachedTokenEntity::class   // ← NEW
    ],
    version = 8,
    exportSchema = true
)
@TypeConverters(DecagonTypeConverters::class)
abstract class DecagonDatabase : RoomDatabase() {

    abstract fun walletDao(): DecagonWalletDao
    abstract fun pendingTxDao(): PendingTxDao
    abstract fun transactionDao(): TransactionDao
    abstract fun onRampDao(): OnRampDao
    // NEW: Swap feature DAOs
    abstract fun swapHistoryDao(): DecagonSwapHistoryDao
    abstract fun cachedTokenDao(): DecagonCachedTokenDao


    companion object {
        const val DATABASE_NAME = "decagon_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE decagon_wallets ADD COLUMN address TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE decagon_wallets SET address = publicKey")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS pending_transactions (
                        id TEXT PRIMARY KEY NOT NULL,
                        fromWalletId TEXT NOT NULL,
                        toAddress TEXT NOT NULL,
                        amount REAL NOT NULL,
                        lamports INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        retryCount INTEGER NOT NULL DEFAULT 0
                    )
                """
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS transactions (
                        id TEXT PRIMARY KEY NOT NULL,
                        fromAddress TEXT NOT NULL,
                        toAddress TEXT NOT NULL,
                        amount REAL NOT NULL,
                        lamports INTEGER NOT NULL,
                        signature TEXT,
                        status TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        fee INTEGER NOT NULL
                    )
                """
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // âœ… Add encryptedMnemonic column
                // NOTE: Existing wallets will have NULL mnemonic (cannot recover)
                // New wallets created after this migration will store mnemonic
                db.execSQL(
                    """
                    ALTER TABLE decagon_wallets 
                    ADD COLUMN encryptedMnemonic BLOB NOT NULL DEFAULT ''
                """
                )
            }


        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE decagon_wallets 
                    ADD COLUMN chains TEXT NOT NULL DEFAULT '[]'
                """
                )

                db.execSQL(
                    """
                    ALTER TABLE decagon_wallets 
                    ADD COLUMN activeChainId TEXT NOT NULL DEFAULT 'solana'
                """
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN priorityFee INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS onramp_transactions (
                        id TEXT PRIMARY KEY NOT NULL,
                        walletId TEXT NOT NULL,
                        walletAddress TEXT NOT NULL,
                        chainId TEXT NOT NULL,
                        fiatAmount REAL NOT NULL,
                        fiatCurrency TEXT NOT NULL,
                        cryptoAmount REAL,
                        cryptoAsset TEXT NOT NULL,
                        provider TEXT NOT NULL,
                        providerTxId TEXT,
                        status TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        completedAt INTEGER,
                        signature TEXT,
                        errorMessage TEXT
                    )
                """
                )
            }
        }
        /**
         * Migration 7 → 8: Add swap feature tables
         *
         * Changes:
         * - Creates decagon_swap_history table
         * - Creates decagon_cached_tokens table
         * - Adds indices for performance
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                timber.log.Timber.i("Running migration 7 → 8: Adding swap feature tables")

                // Create swap history table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS decagon_swap_history (
                        id TEXT PRIMARY KEY NOT NULL,
                        walletAddress TEXT NOT NULL,
                        inputMint TEXT NOT NULL,
                        outputMint TEXT NOT NULL,
                        inputAmount REAL NOT NULL,
                        outputAmount REAL NOT NULL,
                        inputSymbol TEXT NOT NULL,
                        outputSymbol TEXT NOT NULL,
                        signature TEXT,
                        status TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        priceImpact REAL NOT NULL,
                        routePlan TEXT,
                        slippageBps INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create indices for swap history
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_decagon_swap_history_walletAddress 
                    ON decagon_swap_history(walletAddress)
                """.trimIndent())

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_decagon_swap_history_timestamp 
                    ON decagon_swap_history(timestamp)
                """.trimIndent())

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_decagon_swap_history_status 
                    ON decagon_swap_history(status)
                """.trimIndent())

                // Create cached tokens table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS decagon_cached_tokens (
                        mint TEXT PRIMARY KEY NOT NULL,
                        symbol TEXT NOT NULL,
                        name TEXT NOT NULL,
                        decimals INTEGER NOT NULL,
                        logoUri TEXT,
                        isVerified INTEGER NOT NULL DEFAULT 0,
                        isStablecoin INTEGER NOT NULL DEFAULT 0,
                        lastUpdated INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create index for token search
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_decagon_cached_tokens_symbol 
                    ON decagon_cached_tokens(symbol)
                """.trimIndent())

                timber.log.Timber.i("Migration 7 → 8 completed successfully")
            }
        }
    }
}