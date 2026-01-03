package com.decagon.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for asset balances.
 * Supports v0.3 USDC, v0.9 portfolio filtering, v1.2 P&L tracking.
 */
@Entity(
    tableName = "assets",
    foreignKeys = [
        ForeignKey(
            entity = DecagonWalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["wallet_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["wallet_id"]),
        Index(value = ["chain_id"]),
        Index(value = ["symbol"]),
        Index(value = ["is_hidden"])
    ]
)
data class AssetEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "wallet_id")
    val walletId: String,
    
    @ColumnInfo(name = "chain_id")
    val chainId: String = "solana",
    
    @ColumnInfo(name = "symbol")
    val symbol: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "mint_address")
    val mintAddress: String?,
    
    @ColumnInfo(name = "balance")
    val balance: String, // Stored as string to avoid precision loss
    
    @ColumnInfo(name = "decimals")
    val decimals: Int,
    
    @ColumnInfo(name = "price_usd")
    val priceUsd: Double? = null,
    
    @ColumnInfo(name = "value_usd")
    val valueUsd: Double? = null,
    
    @ColumnInfo(name = "price_change_24h")
    val priceChange24h: Double? = null,
    
    @ColumnInfo(name = "icon_url")
    val iconUrl: String? = null,
    
    @ColumnInfo(name = "is_native")
    val isNative: Boolean = false,
    
    @ColumnInfo(name = "is_hidden")
    val isHidden: Boolean = false, // v0.9: hide zero balance
    
    @ColumnInfo(name = "cost_basis_usd")
    val costBasisUsd: Double? = null, // v1.2: P&L tracking
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long
)