package com.octane.wallet.data.mappers

import com.octane.wallet.data.local.database.entities.StakingPositionEntity
import com.octane.wallet.domain.models.StakingPosition


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