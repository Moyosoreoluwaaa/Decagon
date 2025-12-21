package com.octane.wallet.data.mappers

import com.octane.wallet.domain.models.TransactionStatus
import com.octane.wallet.domain.models.TransactionType

fun EntityType.toDomain(): TransactionType = when (this) {
    EntityType.SEND -> TransactionType.SEND
    EntityType.RECEIVE -> TransactionType.RECEIVE
    EntityType.SWAP -> TransactionType.SWAP
    EntityType.STAKE -> TransactionType.STAKE
    EntityType.UNSTAKE -> TransactionType.UNSTAKE
    EntityType.CLAIM_REWARDS -> TransactionType.CLAIM_REWARDS
    EntityType.APPROVE -> TransactionType.APPROVE
    EntityType.REVOKE -> TransactionType.REVOKE
}

fun TransactionType.toEntity(): EntityType = when (this) {
    TransactionType.SEND -> EntityType.SEND
    TransactionType.RECEIVE -> EntityType.RECEIVE
    TransactionType.SWAP -> EntityType.SWAP
    TransactionType.STAKE -> EntityType.STAKE
    TransactionType.UNSTAKE -> EntityType.UNSTAKE
    TransactionType.CLAIM_REWARDS -> EntityType.CLAIM_REWARDS
    TransactionType.APPROVE -> EntityType.APPROVE
    TransactionType.REVOKE -> EntityType.REVOKE
    TransactionType.NFT_MINT -> TODO()
    TransactionType.NFT_TRANSFER -> TODO()
    TransactionType.SINGLE -> TODO()
    TransactionType.MULTIPLE -> TODO()
    TransactionType.MESSAGE -> TODO()
}

fun EntityStatus.toDomain(): TransactionStatus = when (this) {
    EntityStatus.PENDING -> TransactionStatus.PENDING
    EntityStatus.CONFIRMED -> TransactionStatus.CONFIRMED
    EntityStatus.FAILED -> TransactionStatus.FAILED
}

fun TransactionStatus.toEntity(): EntityStatus = when (this) {
    TransactionStatus.PENDING -> EntityStatus.PENDING
    TransactionStatus.CONFIRMED -> EntityStatus.CONFIRMED
    TransactionStatus.FAILED -> EntityStatus.FAILED
}

/**
 * Database entity representation for TransactionType.
 * Must mirror TransactionType values exactly for Mappers to work.
 */
enum class EntityType {
    SEND,
    RECEIVE,
    SWAP,
    STAKE,
    UNSTAKE,
    CLAIM_REWARDS,
    APPROVE,
    REVOKE
}

/**
 * Database entity representation for TransactionStatus.
 * Must mirror TransactionStatus values exactly for Mappers to work.
 */
enum class EntityStatus {
    PENDING,
    CONFIRMED,
    FAILED
}