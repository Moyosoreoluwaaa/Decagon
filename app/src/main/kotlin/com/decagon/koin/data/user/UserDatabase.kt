package com.koin.data.user

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.koin.data.watchlist.WatchlistDao
import com.koin.data.watchlist.WatchlistEntity

@Database(
    entities = [UserEntity::class, WatchlistEntity::class],
    version = 1,
    exportSchema = false
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        @Volatile private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            UserDatabase::class.java,
            DATABASE_NAME
        ).build()

        const val DATABASE_NAME = "user_database"
    }
}
