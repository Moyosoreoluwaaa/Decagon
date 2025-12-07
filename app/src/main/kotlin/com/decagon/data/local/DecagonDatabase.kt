package com.decagon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.decagon.data.local.dao.DecagonWalletDao
import com.decagon.data.local.entity.DecagonWalletEntity

/**
 * Decagon Room database.
 * 
 * Version 1: Wallet storage only
 * Future versions will add: Tokens, NFTs, Transactions, Contacts
 */
@Database(
    entities = [
        DecagonWalletEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DecagonTypeConverters::class)
abstract class DecagonDatabase : RoomDatabase() {
    
    abstract fun walletDao(): DecagonWalletDao
    
    companion object {
        const val DATABASE_NAME = "decagon_database"
    }
}