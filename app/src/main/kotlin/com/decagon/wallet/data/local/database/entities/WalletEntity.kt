package com.octane.wallet.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for wallet storage.
 * Supports v0.7 wallet naming/iconography and v1.4 multi-account management.
 */
@Entity(
    tableName = "wallets",
    indices = [
        Index(value = ["public_key"], unique = true),
        Index(value = ["is_active"])
    ]
)
data class WalletEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "public_key")
    val publicKey: String,
    
    @ColumnInfo(name = "icon_emoji")
    val iconEmoji: String? = null,
    
    @ColumnInfo(name = "color_hex")
    val colorHex: String? = null,
    
    @ColumnInfo(name = "chain_id")
    val chainId: String = "solana", // v2.0: multi-chain support
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = false,
    
    @ColumnInfo(name = "is_hardware_wallet")
    val isHardwareWallet: Boolean = false, // v1.5
    
    @ColumnInfo(name = "hardware_device_name")
    val hardwareDeviceName: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long
)