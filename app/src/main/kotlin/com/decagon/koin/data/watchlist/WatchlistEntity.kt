package com.koin.data.watchlist

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.koin.data.user.UserEntity

@Entity(
    tableName = "watchlist",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["coinId"]),
        Index(value = ["userId", "coinId"], unique = true)
    ]
)
data class WatchlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val coinId: String,
    val coinName: String,
    val coinSymbol: String,
    val coinImageUrl: String,
    val addedAt: Long = System.currentTimeMillis()
)
