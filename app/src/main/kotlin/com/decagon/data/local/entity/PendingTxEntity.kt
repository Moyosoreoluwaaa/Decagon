package com.decagon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "pending_transactions")
data class PendingTxEntity(
    @PrimaryKey
    val id: String,
    val fromWalletId: String,
    val toAddress: String,
    val amount: Double,
    val lamports: Long,
    val createdAt: Long,
    val retryCount: Int = 0
)
