package com.octane.wallet.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create discover_tokens table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS discover_tokens (
                id TEXT PRIMARY KEY NOT NULL,
                symbol TEXT NOT NULL,
                name TEXT NOT NULL,
                logoUrl TEXT,
                currentPrice REAL NOT NULL,
                priceChange24h REAL NOT NULL,
                marketCap REAL NOT NULL,
                volume24h REAL NOT NULL,
                rank INTEGER NOT NULL,
                isVerified INTEGER NOT NULL,
                tags TEXT NOT NULL,
                mintAddress TEXT,
                lastUpdated INTEGER NOT NULL
            )
        """.trimIndent())
        
        // Create discover_perps table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS discover_perps (
                id TEXT PRIMARY KEY NOT NULL,
                symbol TEXT NOT NULL,
                name TEXT NOT NULL,
                logoUrl TEXT,
                indexPrice REAL NOT NULL,
                markPrice REAL NOT NULL,
                fundingRate REAL NOT NULL,
                nextFundingTime INTEGER NOT NULL,
                openInterest REAL NOT NULL,
                volume24h REAL NOT NULL,
                priceChange24h REAL NOT NULL,
                leverage TEXT NOT NULL,
                exchange TEXT NOT NULL,
                lastUpdated INTEGER NOT NULL
            )
        """.trimIndent())
        
        // Create discover_dapps table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS discover_dapps (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                logoUrl TEXT,
                category TEXT NOT NULL,
                url TEXT NOT NULL,
                tvl REAL,
                volume24h REAL,
                users24h INTEGER,
                isVerified INTEGER NOT NULL,
                chains TEXT NOT NULL,
                rating REAL NOT NULL,
                tags TEXT NOT NULL,
                lastUpdated INTEGER NOT NULL
            )
        """.trimIndent())
    }
}