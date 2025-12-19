package com.koin.data.coin

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.koin.data.notification.NotificationDao
import com.koin.data.portfolio.PortfolioBalanceEntity
import com.koin.data.portfolio.PortfolioDao
import com.koin.data.portfolio.PortfolioHoldingEntity
import com.koin.data.portfolio.PortfolioTransactionEntity
import com.koin.data.user.UserDao
import com.koin.data.user.UserEntity
import com.koin.data.watchlist.WatchlistDao
import com.koin.data.watchlist.WatchlistEntity
import com.koin.domain.notification.Notification

@Database(
    entities = [
        CoinEntity::class, 
        CoinChartEntity::class,
        PortfolioHoldingEntity::class,
        PortfolioTransactionEntity::class,
        PortfolioBalanceEntity::class,
        UserEntity::class,
        WatchlistEntity::class,
        Notification::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CoinDatabase : RoomDatabase() {
    abstract fun coinDao(): CoinDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun userDao(): UserDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun notificationDao(): NotificationDao


    companion object {
        const val DATABASE_NAME ="coin_database"

        @Volatile
        private var INSTANCE: CoinDatabase? = null

        fun getDatabase(context: Context): CoinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CoinDatabase::class.java,
                    "coin_database"
                )
//                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }
//
//        val MIGRATION_2_3 = object : Migration(2, 3) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                // Create a new table with the composite primary key
//                database.execSQL("""
//                    CREATE TABLE `coin_chart_new` (
//                        `coinId` TEXT NOT NULL,
//                        `timeRange` TEXT NOT NULL,
//                        `timestamp` INTEGER NOT NULL,
//                        `priceDataJson` TEXT NOT NULL,
//                        PRIMARY KEY(`coinId`, `timeRange`)
//                    )
//                """)
//                // Copy the data from the old table to the new table
//                // This handles potential duplicates by taking the most recent entry for each coin/timerange pair
//                database.execSQL("""
//                    INSERT INTO `coin_chart_new` (coinId, timeRange, timestamp, priceDataJson)
//                    SELECT coinId, timeRange, timestamp, priceDataJson FROM `coin_chart`
//                    GROUP BY coinId, timeRange
//                    HAVING timestamp = MAX(timestamp)
//                """)
//                // Remove the old table
//                database.execSQL("DROP TABLE `coin_chart`")
//                // Rename the new table to the original table name
//                database.execSQL("ALTER TABLE `coin_chart_new` RENAME TO `coin_chart`")
//            }
//        }
//
//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("""
//                    CREATE TABLE IF NOT EXISTS `coin_chart` (
//                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//                        `coinId` TEXT NOT NULL,
//                        `timeRange` TEXT NOT NULL,
//                        `timestamp` INTEGER NOT NULL,
//                        `priceDataJson` TEXT NOT NULL
//                    )
//                """)
//                database.execSQL("CREATE INDEX IF NOT EXISTS `index_coin_chart_coinId_timeRange` ON `coin_chart` (`coinId`, `timeRange`)")
//            }
//        }
//
//        val MIGRATION_3_4 = object : Migration(3, 4) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                // Create portfolio holdings table
//                database.execSQL("""
//                    CREATE TABLE IF NOT EXISTS `portfolio_holdings` (
//                        `coinId` TEXT NOT NULL,
//                        `coinName` TEXT NOT NULL,
//                        `coinSymbol` TEXT NOT NULL,
//                        `coinImageUrl` TEXT NOT NULL,
//                        `quantity` REAL NOT NULL,
//                        `averagePurchasePrice` REAL NOT NULL,
//                        `totalTransactionFees` REAL NOT NULL,
//                        `lastUpdated` INTEGER NOT NULL,
//                        PRIMARY KEY(`coinId`)
//                    )
//                """)
//
//                // Create portfolio transactions table
//                database.execSQL("""
//                    CREATE TABLE IF NOT EXISTS `portfolio_transactions` (
//                        `id` TEXT NOT NULL,
//                        `coinId` TEXT NOT NULL,
//                        `type` TEXT NOT NULL,
//                        `quantity` REAL NOT NULL,
//                        `pricePerCoin` REAL NOT NULL,
//                        `transactionFee` REAL NOT NULL,
//                        `timestamp` INTEGER NOT NULL,
//                        PRIMARY KEY(`id`)
//                    )
//                """)
//
//                // Create portfolio balance table
//                database.execSQL("""
//                    CREATE TABLE IF NOT EXISTS `portfolio_balance` (
//                        `id` INTEGER NOT NULL,
//                        `balance` REAL NOT NULL,
//                        `lastUpdated` INTEGER NOT NULL,
//                        PRIMARY KEY(`id`)
//                    )
//                """)
//
//                // Initialize balance with $10,000
//                database.execSQL("""
//                    INSERT INTO `portfolio_balance` (id, balance, lastUpdated)
//                    VALUES (1, 10000.0, ${System.currentTimeMillis()})
//                """)
//            }
//        }
//
//        val MIGRATION_4_5 = object : Migration(4, 5) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("""
//                    CREATE TABLE IF NOT EXISTS `price_alerts` (
//                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//                        `coinId` TEXT NOT NULL,
//                        `targetPrice` REAL NOT NULL,
//                        `alertType` TEXT NOT NULL,
//                        `isActive` INTEGER NOT NULL,
//                        `isTriggered` INTEGER NOT NULL,
//                        `timestamp` INTEGER NOT NULL
//                    )
//                """)
//            }
//        }
    }
}

class Converters {
    @TypeConverter
    fun fromStringList(value: List<Double>?): String? {
        return value?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<Double>? {
        return value?.let {
            Gson().fromJson(it, object : TypeToken<List<Double>>() {}.type)
        }
    }

    // For chart data: List<Pair<Long, Double>>
    @TypeConverter
    fun fromChartList(value: List<Pair<Long, Double>>?): String? {
        return value?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toChartList(value: String?): List<Pair<Long, Double>>? {
        return value?.let {
            Gson().fromJson(it, object : TypeToken<List<Pair<Long, Double>>>() {}.type)
        }
    }
}