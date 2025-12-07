package com.decagon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.decagon.data.local.dao.DecagonWalletDao
import com.decagon.data.local.entity.DecagonWalletEntity

/**
 * Decagon Room database.
 * 
 * Version 1: Wallet storage only
 * Future versions will add: Tokens, NFTs, Transactions, Contacts
 */
@Database(
    entities = [DecagonWalletEntity::class],
    version = 2, // ✅ Bump version
    exportSchema = true
)
@TypeConverters(DecagonTypeConverters::class)
abstract class DecagonDatabase : RoomDatabase() {

    abstract fun walletDao(): DecagonWalletDao

    companion object {
        const val DATABASE_NAME = "decagon_database"

        // ✅ Add migration
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add address column, default to publicKey for existing rows
                db.execSQL(
                    "ALTER TABLE decagon_wallets ADD COLUMN address TEXT NOT NULL DEFAULT ''"
                )
                // Update existing rows: set address = publicKey
                db.execSQL(
                    "UPDATE decagon_wallets SET address = publicKey"
                )
            }
        }
    }
}