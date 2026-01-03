package com.decagon.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.decagon.data.local.dao.ApprovalDao
import com.decagon.data.local.dao.AssetDao
import com.decagon.data.local.dao.ContactDao
import com.decagon.data.local.dao.DecagonWalletDao
import com.decagon.data.local.dao.DiscoverDao
import com.decagon.data.local.dao.OnRampDao
import com.decagon.data.local.dao.PendingTxDao
import com.decagon.data.local.dao.StakingDao
import com.decagon.data.local.dao.SwapHistoryDao
import com.decagon.data.local.dao.TokenCacheDao
import com.decagon.data.local.dao.TransactionDao
import com.decagon.data.local.entity.ApprovalEntity
import com.decagon.data.local.entity.AssetEntity
import com.decagon.data.local.entity.ContactEntity
import com.decagon.data.local.entity.DAppEntity
import com.decagon.data.local.entity.DecagonWalletEntity
import com.decagon.data.local.entity.OnRampTransactionEntity
import com.decagon.data.local.entity.PendingTxEntity
import com.decagon.data.local.entity.PerpEntity
import com.decagon.data.local.entity.StakingPositionEntity
import com.decagon.data.local.entity.SwapHistoryEntity
import com.decagon.data.local.entity.TokenCacheEntity
import com.decagon.data.local.entity.TokenEntity
import com.decagon.data.local.entity.TransactionEntity

@Database(
    entities = [
        DecagonWalletEntity::class,
        PendingTxEntity::class,
        TransactionEntity::class,
        OnRampTransactionEntity::class,
        SwapHistoryEntity::class,
        TokenCacheEntity::class,
        AssetEntity::class,
        ContactEntity::class,
        ApprovalEntity::class,
        StakingPositionEntity::class,
        TokenEntity::class,
        PerpEntity::class,
        DAppEntity::class,
    ],
    version = 9,
    exportSchema = true
)
@TypeConverters(DecagonTypeConverters::class)
abstract class DecagonDatabase : RoomDatabase() {

    abstract fun walletDao(): DecagonWalletDao
    abstract fun pendingTxDao(): PendingTxDao
    abstract fun transactionDao(): TransactionDao
    abstract fun onRampDao(): OnRampDao
    abstract fun swapHistoryDao(): SwapHistoryDao
    abstract fun tokenCacheDao(): TokenCacheDao
    abstract fun assetDao(): AssetDao
    abstract fun contactDao(): ContactDao
    abstract fun approvalDao(): ApprovalDao
    abstract fun stakingDao(): StakingDao
    abstract fun discoverDao(): DiscoverDao // ✅ ADD THIS

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

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Assets Table
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS assets (
                id TEXT PRIMARY KEY NOT NULL,
                wallet_id TEXT NOT NULL,
                chain_id TEXT NOT NULL DEFAULT 'solana',
                symbol TEXT NOT NULL,
                name TEXT NOT NULL,
                mint_address TEXT,
                balance TEXT NOT NULL,
                decimals INTEGER NOT NULL,
                price_usd REAL,
                value_usd REAL,
                price_change_24h REAL,
                icon_url TEXT,
                is_native INTEGER NOT NULL DEFAULT 0,
                is_hidden INTEGER NOT NULL DEFAULT 0,
                cost_basis_usd REAL,
                last_updated INTEGER NOT NULL,
                FOREIGN KEY(wallet_id) REFERENCES decagon_wallets(id) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """)

                // 2. Approvals Table
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS approvals (
                id TEXT PRIMARY KEY NOT NULL,
                wallet_id TEXT NOT NULL,
                chain_id TEXT NOT NULL,
                token_mint TEXT NOT NULL,
                token_symbol TEXT NOT NULL,
                spender_address TEXT NOT NULL,
                spender_name TEXT,
                allowance TEXT NOT NULL,
                is_revoked INTEGER NOT NULL,
                approved_at INTEGER NOT NULL,
                revoked_at INTEGER,
                FOREIGN KEY(wallet_id) REFERENCES decagon_wallets(id) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """)

                // 3. Staking Positions Table
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS staking_positions (
                id TEXT PRIMARY KEY NOT NULL,
                wallet_id TEXT NOT NULL,
                chain_id TEXT NOT NULL,
                validator_address TEXT NOT NULL,
                validator_name TEXT,
                amount_staked TEXT NOT NULL,
                rewards_earned TEXT NOT NULL,
                apy REAL NOT NULL,
                is_active INTEGER NOT NULL,
                staked_at INTEGER NOT NULL,
                unstaked_at INTEGER,
                FOREIGN KEY(wallet_id) REFERENCES decagon_wallets(id) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """)

                // 4. Contacts Table
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS contacts (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                address TEXT NOT NULL,
                memo TEXT,
                added_at INTEGER NOT NULL
            )
        """)

                // 5. Discover Tables: Tokens
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS tokens (
                id TEXT PRIMARY KEY NOT NULL,
                symbol TEXT NOT NULL,
                name TEXT NOT NULL,
                address TEXT NOT NULL,
                logo_url TEXT,
                price REAL NOT NULL,
                change_24h REAL NOT NULL,
                is_trending INTEGER NOT NULL
            )
        """)

                // 6. Discover Tables: Perps
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS perps (
                id TEXT PRIMARY KEY NOT NULL,
                symbol TEXT NOT NULL,
                name TEXT NOT NULL,
                base_asset TEXT NOT NULL,
                quote_asset TEXT NOT NULL,
                price REAL NOT NULL,
                change_24h REAL NOT NULL,
                volume_24h REAL NOT NULL,
                open_interest REAL NOT NULL
            )
        """)

                // 7. Discover Tables: DApps
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS dapps (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                url TEXT NOT NULL,
                icon_url TEXT NOT NULL,
                category TEXT NOT NULL
            )
        """)

                // Indices for performance
                db.execSQL("CREATE INDEX IF NOT EXISTS index_assets_wallet_id ON assets(wallet_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_approvals_wallet_id ON approvals(wallet_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_staking_positions_wallet_id ON staking_positions(wallet_id)")
            }
        }
    }
}