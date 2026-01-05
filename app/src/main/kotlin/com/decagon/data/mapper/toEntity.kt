package com.decagon.data.mapper

import com.decagon.data.local.entity.TokenBalanceEntity
import com.decagon.data.remote.dto.TokenHoldingDto
import com.decagon.domain.model.TokenBalance
import java.lang.reflect.Modifier.isNative


/**
 * Mapper: TokenBalance ↔ TokenBalanceEntity
 */

/**
 * Domain → Entity
 */
fun TokenBalance.toEntity(walletAddress: String): TokenBalanceEntity {
    return TokenBalanceEntity(
        id = TokenBalanceEntity.generateId(walletAddress, mint),
        walletAddress = walletAddress,
        mint = mint,
        amount = amount,
        decimals = decimals,
        uiAmount = uiAmount,
        symbol = symbol,
        name = name,
        logoUrl = logoUrl,
        isNative = isNative,
        tokenAccount = tokenAccount,
        valueUsd = valueUsd,
        change24h = change24h,
        lastUpdated = System.currentTimeMillis()
    )
}

/**
 * Entity → Domain
 */
fun TokenBalanceEntity.toDomain(): TokenBalance {
    return TokenBalance(
        mint = mint,
        amount = amount,
        decimals = decimals,
        uiAmount = uiAmount,
        tokenAccount = tokenAccount ?: mint,
        isNative = isNative,
        symbol = symbol,
        name = name,
        tokenInfo = null,
        logoUrl = logoUrl,
        valueUsd = valueUsd,
        change24h = change24h
    )
}


/**
 * DTO → Domain
 */
fun TokenHoldingDto.toDomain(): TokenBalance {
    return TokenBalance(
        mint = mint,
        amount = amount,
        decimals = decimals,
        uiAmount = uiAmount,
        tokenAccount = tokenAccount ?: mint,
        isNative = isNative,
        symbol = symbol ?: "???",
        name = name ?: "Unknown Token",
        tokenInfo = null, // Will be enriched later
        logoUrl = logoUrl,
        valueUsd = valueUsd ?: 0.0,
        change24h = change24h
    )
}

// List extensions
fun List<TokenHoldingDto>.toDomain(): List<TokenBalance> = map { it.toDomain() }
fun List<TokenBalanceEntity>.toDomainBalances(): List<TokenBalance> = map { it.toDomain() }
fun List<TokenBalance>.toEntities(walletAddress: String): List<TokenBalanceEntity> {
    return map { it.toEntity(walletAddress) }
}