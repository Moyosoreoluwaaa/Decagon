package com.decagon.data.mapper

import com.decagon.data.local.entity.StakingPositionEntity
import com.decagon.domain.model.StakingPosition


fun StakingPositionEntity.toDomain(): StakingPosition = StakingPosition(
    id = id,
    walletId = walletId,
    chainId = chainId,
    validatorAddress = validatorAddress,
    validatorName = validatorName.toString(),
    amountStaked = amountStaked,
    rewardsEarned = rewardsEarned,
    apy = apy,
    isActive = isActive,
    stakedAt = stakedAt,
    unstakedAt = unstakedAt
)

fun StakingPosition.toEntity(): StakingPositionEntity = StakingPositionEntity(
    id = id,
    walletId = walletId,
    chainId = chainId,
    validatorAddress = validatorAddress,
    validatorName = validatorName,
    amountStaked = amountStaked,
    rewardsEarned = rewardsEarned,
    apy = apy,
    isActive = isActive,
    stakedAt = stakedAt,
    unstakedAt = unstakedAt
)