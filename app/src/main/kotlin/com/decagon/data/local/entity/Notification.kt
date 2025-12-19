package com.decagon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: NotificationType, // TRANSACTION, BALANCE_CHANGE, SYSTEM
    val relatedId: String? = null // txHash or walletId
)

enum class NotificationType {
    TRANSACTION_CONFIRMED,
    TRANSACTION_FAILED,
    BALANCE_INCREASED,
    BALANCE_DECREASED,
    NEW_AIRDROP,
    SYSTEM_UPDATE
}