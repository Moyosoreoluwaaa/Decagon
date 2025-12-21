package com.octane.wallet.data.mappers

import com.wallet.data.local.database.entities.ApprovalEntity
import com.octane.wallet.domain.models.Approval

fun ApprovalEntity.toDomain(): Approval = Approval(
    id = id,
    walletId = walletId,
    chainId = chainId,
    tokenMint = tokenMint,
    tokenSymbol = tokenSymbol,
    spenderAddress = spenderAddress,
    spenderName = spenderName,
    allowance = allowance,
    isRevoked = isRevoked,
    approvedAt = approvedAt,
    revokedAt = revokedAt
)

fun Approval.toEntity(): ApprovalEntity = ApprovalEntity(
    id = id,
    walletId = walletId,
    chainId = chainId,
    tokenMint = tokenMint,
    tokenSymbol = tokenSymbol,
    spenderAddress = spenderAddress,
    spenderName = spenderName,
    allowance = allowance,
    isRevoked = isRevoked,
    approvedAt = approvedAt,
    revokedAt = revokedAt
)