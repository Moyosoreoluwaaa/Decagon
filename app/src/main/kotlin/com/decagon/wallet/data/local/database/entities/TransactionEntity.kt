package com.octane.wallet.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.octane.wallet.data.mappers.EntityStatus
import com.octane.wallet.data.mappers.EntityType

/**
 * Room entity for transaction history.
 * Supports v0.2 basic history, v0.8 advanced debugging, v1.0 swaps, v1.1 staking.
 */
@Entity(
    tableName = "transactions",
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
        Index(value = ["chain_id"]),
        Index(value = ["tx_hash"], unique = true),
        Index(value = ["status"]),
        Index(value = ["timestamp"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "wallet_id")
    val walletId: String,

    @ColumnInfo(name = "chain_id")
    val chainId: String = "solana",

    @ColumnInfo(name = "tx_hash")
    val txHash: String,

    @ColumnInfo(name = "type")
    val type: EntityType,

    @ColumnInfo(name = "status")
    val status: EntityStatus,

    @ColumnInfo(name = "from_address")
    val fromAddress: String,

    @ColumnInfo(name = "to_address")
    val toAddress: String?,

    @ColumnInfo(name = "amount")
    val amount: String, // Stored as string to avoid precision loss

    @ColumnInfo(name = "token_symbol")
    val tokenSymbol: String,

    @ColumnInfo(name = "token_mint")
    val tokenMint: String?,

    @ColumnInfo(name = "fee")
    val fee: String,

    @ColumnInfo(name = "fee_priority")
    val feePriority: String? = null, // v0.5: Fast/Normal/Low

    @ColumnInfo(name = "block_number")
    val blockNumber: Long? = null,

    @ColumnInfo(name = "confirmation_count")
    val confirmationCount: Int = 0,

    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,

    @ColumnInfo(name = "memo")
    val memo: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "simulated")
    val simulated: Boolean = false, // v1.6: simulation result

    @ColumnInfo(name = "simulation_success")
    val simulationSuccess: Boolean? = null
)
