package com.decagon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.decagon.data.local.dao.DecagonWalletDao
import com.decagon.data.local.dao.OnRampDao
import com.decagon.data.local.dao.PendingTxDao
import com.decagon.data.local.dao.SwapHistoryDao
import com.decagon.data.local.dao.TokenCacheDao
import com.decagon.data.local.dao.TransactionDao
import com.decagon.data.local.entity.DecagonWalletEntity
import com.decagon.data.local.entity.OnRampTransactionEntity
import com.decagon.data.local.entity.PendingTxEntity
import com.decagon.data.local.entity.SwapHistoryEntity
import com.decagon.data.local.entity.TokenCacheEntity
import com.decagon.data.local.entity.TransactionEntity

@Database(
    entities = [
        DecagonWalletEntity::class,
        PendingTxEntity::class,
        TransactionEntity::class,
        OnRampTransactionEntity::class,
        SwapHistoryEntity::class,      // ✅ NEW
        TokenCacheEntity::class         // ✅ NEW
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
    abstract fun swapHistoryDao(): SwapHistoryDao      // ✅ NEW
    abstract fun tokenCacheDao(): TokenCacheDao        // ✅ NEW

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

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create swap_history table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS swap_history (
                        id TEXT PRIMARY KEY NOT NULL,
                        walletId TEXT NOT NULL,
                        inputMint TEXT NOT NULL,
                        outputMint TEXT NOT NULL,
                        inputAmount REAL NOT NULL,
                        outputAmount REAL NOT NULL,
                        inputSymbol TEXT NOT NULL,
                        outputSymbol TEXT NOT NULL,
                        signature TEXT,
                        status TEXT NOT NULL,
                        slippageBps INTEGER NOT NULL,
                        priceImpactPct REAL NOT NULL,
                        feeBps INTEGER NOT NULL,
                        priorityFee INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        errorMessage TEXT
                    )
                    """
                )

                // Create token_cache table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS token_cache (
                        address TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        symbol TEXT NOT NULL,
                        decimals INTEGER NOT NULL,
                        logoURI TEXT,
                        tags TEXT NOT NULL,
                        dailyVolume REAL,
                        hasFreezableAuthority INTEGER NOT NULL,
                        hasMintableAuthority INTEGER NOT NULL,
                        coingeckoId TEXT,
                        cachedAt INTEGER NOT NULL
                    )
                    """
                )

                // Create indices for performance
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_swap_history_walletId ON swap_history(walletId)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_swap_history_timestamp ON swap_history(timestamp DESC)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_token_cache_symbol ON token_cache(symbol)"
                )
            }
        }
    }
}