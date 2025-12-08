package com.decagon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.decagon.data.local.dao.DecagonWalletDao
import com.decagon.data.local.dao.PendingTxDao
import com.decagon.data.local.dao.TransactionDao
import com.decagon.data.local.entity.DecagonWalletEntity
import com.decagon.data.local.entity.PendingTxEntity
import com.decagon.data.local.entity.TransactionEntity

@Database(
    entities = [
        DecagonWalletEntity::class,
        PendingTxEntity::class,
        TransactionEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(DecagonTypeConverters::class)
abstract class DecagonDatabase : RoomDatabase() {

    abstract fun walletDao(): DecagonWalletDao
    abstract fun pendingTxDao(): PendingTxDao
    abstract fun transactionDao(): TransactionDao

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
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS pending_transactions (
                        id TEXT PRIMARY KEY NOT NULL,
                        fromWalletId TEXT NOT NULL,
                        toAddress TEXT NOT NULL,
                        amount REAL NOT NULL,
                        lamports INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        retryCount INTEGER NOT NULL DEFAULT 0
                    )
                """)

                db.execSQL("""
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
                """)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // âœ… Add encryptedMnemonic column
                // NOTE: Existing wallets will have NULL mnemonic (cannot recover)
                // New wallets created after this migration will store mnemonic
                db.execSQL("""
                    ALTER TABLE decagon_wallets 
                    ADD COLUMN encryptedMnemonic BLOB NOT NULL DEFAULT ''
                """)
            }
        }
    }
}