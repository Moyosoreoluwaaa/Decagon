package com.decagon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val fromAddress: String,
    val toAddress: String,
    val amount: Double,
    val lamports: Long,
    val signature: String?,
    val status: String, // PENDING/CONFIRMED/FAILED
    val timestamp: Long,
    val fee: Long,
    val priorityFee: Long = 0 // ✅ NEW: Add to DB schema
)