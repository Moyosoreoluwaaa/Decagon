package com.decagon.domain.model

data class Approval(
    val id: String,
    val walletId: String,
    val chainId: String,
    val tokenMint: String,
    val tokenSymbol: String,
    val spenderAddress: String,
    val spenderName: String?,
    val allowance: String,
    val isRevoked: Boolean,
    val approvedAt: Long,
    val revokedAt: Long?
)