package com.octane.wallet.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "approvals",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["wallet_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["wallet_id"]),
        Index(value = ["spender_address"])
    ]
)
data class ApprovalEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "wallet_id")
    val walletId: String,
    
    @ColumnInfo(name = "chain_id")
    val chainId: String,
    
    @ColumnInfo(name = "token_mint")
    val tokenMint: String,
    
    @ColumnInfo(name = "token_symbol")
    val tokenSymbol: String,
    
    @ColumnInfo(name = "spender_address")
    val spenderAddress: String,
    
    @ColumnInfo(name = "spender_name")
    val spenderName: String?,
    
    @ColumnInfo(name = "allowance")
    val allowance: String,
    
    @ColumnInfo(name = "is_revoked")
    val isRevoked: Boolean,
    
    @ColumnInfo(name = "approved_at")
    val approvedAt: Long,
    
    @ColumnInfo(name = "revoked_at")
    val revokedAt: Long?
)