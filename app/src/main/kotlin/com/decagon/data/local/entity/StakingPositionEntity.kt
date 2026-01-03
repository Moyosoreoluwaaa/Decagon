package com.decagon.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "staking_positions",
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
        Index(value = ["validator_address"])
    ]
)
data class StakingPositionEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "wallet_id")
    val walletId: String,

    @ColumnInfo(name = "chain_id")
    val chainId: String,

    @ColumnInfo(name = "validator_address")
    val validatorAddress: String,

    @ColumnInfo(name = "validator_name")
    val validatorName: String?,

    @ColumnInfo(name = "amount_staked")
    val amountStaked: String,

    @ColumnInfo(name = "rewards_earned")
    val rewardsEarned: String,

    @ColumnInfo(name = "apy")
    val apy: Double,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean,

    @ColumnInfo(name = "staked_at")
    val stakedAt: Long,

    @ColumnInfo(name = "unstaked_at")
    val unstakedAt: Long?
)