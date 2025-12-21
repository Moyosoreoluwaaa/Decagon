// app/core/data/local/database/entities/ContactEntity.kt

package com.octane.wallet.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for saved contacts (v0.6).
 */
@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["address"], unique = true),
        Index(value = ["name"])
    ]
)
data class ContactEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "address")
    val address: String,
    
    @ColumnInfo(name = "chain_id")
    val chainId: String = "solana",
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "last_used")
    val lastUsed: Long
)
