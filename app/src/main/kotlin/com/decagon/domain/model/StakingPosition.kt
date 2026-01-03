package com.decagon.domain.model

data class StakingPosition(
    val id: String,
    val walletId: String,
    val chainId: String,
    val validatorAddress: String,
    val validatorName: String,
    val amountStaked: String,
    val rewardsEarned: String,
    val apy: Double,
    val isActive: Boolean,
    val stakedAt: Long,
    val unstakedAt: Long?
)