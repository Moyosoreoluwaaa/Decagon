package com.decagon.data.mapper

import com.decagon.data.local.entity.TokenBalanceEntity
import com.decagon.domain.model.TokenBalance
import java.lang.reflect.Modifier.isNative

/**
 * Mapper: TokenBalance ↔ TokenBalanceEntity
 */

// ==================== DOMAIN → ENTITY ====================

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
        logoUrl = tokenInfo?.logoURI ?: logoUrl,
        isNative = isNative,
        tokenAccount = tokenAccount,
        valueUsd = valueUsd,
        change24h = change24h,
        lastUpdated = System.currentTimeMillis()
    )
}

fun List<TokenBalance>.toEntities(walletAddress: String): List<TokenBalanceEntity> {
    return map { it.toEntity(walletAddress) }
}

// ==================== ENTITY → DOMAIN ====================

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
        tokenInfo = null, // Will be enriched from TokenInfo cache
        logoUrl = logoUrl,
        valueUsd = valueUsd,
        change24h = change24h
    )
}

fun List<TokenBalanceEntity>.toDomainBalances(): List<TokenBalance> {
    return map { it.toDomain() }
}