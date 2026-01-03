package com.decagon.data.mapper

import com.decagon.data.local.entity.ApprovalEntity
import com.decagon.domain.model.Approval


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