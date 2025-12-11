package com.decagon.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "onramp_transactions")
data class OnRampTransactionEntity(
    @PrimaryKey
    val id: String,
    val walletId: String,
    val walletAddress: String,
    val chainId: String, // Which chain: solana, ethereum, polygon
    val fiatAmount: Double,
    val fiatCurrency: String, // NGN, USD, EUR
    val cryptoAmount: Double?, // Expected amount (null until confirmed)
    val cryptoAsset: String, // SOL, ETH, MATIC
    val provider: String, // ramp, onramper, yellow_card
    val providerTxId: String?, // Provider's transaction ID
    val status: String, // INITIATED, PENDING, COMPLETED, FAILED, CANCELLED
    val createdAt: Long,
    val completedAt: Long? = null,
    val signature: String? = null, // Blockchain tx signature when funds arrive
    val errorMessage: String? = null
)
