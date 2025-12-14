package com.decagon.data.mapper

import com.decagon.data.local.entity.SwapHistoryEntity
import com.decagon.data.local.entity.TokenCacheEntity
import com.decagon.data.remote.dto.*
import com.decagon.domain.model.*

// ==================== DTO → DOMAIN ====================

fun JupiterOrderResponse.toDomain(
    securityWarnings: Map<String, List<SecurityWarning>> = emptyMap()
): SwapOrder {
    return SwapOrder(
        inputMint = inputMint,
        outputMint = outputMint,
        inAmount = inAmount,
        outAmount = outAmount,
        slippageBps = slippageBps,
        priceImpactPct = priceImpactPct.toDoubleOrNull() ?: 0.0,
        routePlan = routePlan.map { it.toDomain() },
        feeBps = feeBps,
        transaction = transaction,
        requestId = requestId,
        securityWarnings = securityWarnings
    )
}

fun RoutePlanDto.toDomain(): RoutePlan {
    return RoutePlan(
        ammLabel = swapInfo.label,
        inputMint = swapInfo.inputMint,
        outputMint = swapInfo.outputMint,
        inAmount = swapInfo.inAmount,
        outAmount = swapInfo.outAmount,
        feeAmount = swapInfo.feeAmount,
        percent = percent
    )
}

fun TokenInfoDto.toDomain(): TokenInfo {
    return TokenInfo(
        address = address,
        name = name,
        symbol = symbol,
        decimals = decimals,
        logoURI = logoURI,
        tags = tags,
        isVerified = tags.contains("verified"),
        isStrict = tags.contains("strict"),
        dailyVolume = dailyVolume,
        hasFreezableAuthority = freezeAuthority != null,
        hasMintableAuthority = mintAuthority != null,
        coingeckoId = extensions?.coingeckoId
    )
}

fun TokenHoldingDto.toDomain(tokenInfo: TokenInfo? = null): TokenBalance {
    return TokenBalance(
        mint = mint,
        amount = amount,
        decimals = decimals,
        uiAmount = uiAmount,
        tokenAccount = tokenAccount,
        isNative = isNative,
        tokenInfo = tokenInfo
    )
}

fun TokenWarningDto.toDomain(): SecurityWarning {
    return SecurityWarning(
        type = WarningType.fromString(type),
        message = message,
        severity = WarningSeverity.fromString(severity)
    )
}

// ==================== DOMAIN → ENTITY ====================

fun SwapHistory.toEntity(): SwapHistoryEntity {
    return SwapHistoryEntity(
        id = id,
        walletId = walletId,
        inputMint = inputMint,
        outputMint = outputMint,
        inputAmount = inputAmount,
        outputAmount = outputAmount,
        inputSymbol = inputSymbol,
        outputSymbol = outputSymbol,
        signature = signature,
        status = status.name,
        slippageBps = slippageBps,
        priceImpactPct = priceImpactPct,
        feeBps = feeBps,
        priorityFee = priorityFee,
        timestamp = timestamp,
        errorMessage = errorMessage
    )
}

fun TokenInfo.toEntity(): TokenCacheEntity {
    return TokenCacheEntity(
        address = address,
        name = name,
        symbol = symbol,
        decimals = decimals,
        logoURI = logoURI,
        tags = tags.joinToString(","),
        dailyVolume = dailyVolume,
        hasFreezableAuthority = hasFreezableAuthority,
        hasMintableAuthority = hasMintableAuthority,
        coingeckoId = coingeckoId,
        cachedAt = System.currentTimeMillis()
    )
}

// ==================== ENTITY → DOMAIN ====================

fun SwapHistoryEntity.toDomain(): SwapHistory {
    return SwapHistory(
        id = id,
        walletId = walletId,
        inputMint = inputMint,
        outputMint = outputMint,
        inputAmount = inputAmount,
        outputAmount = outputAmount,
        inputSymbol = inputSymbol,
        outputSymbol = outputSymbol,
        signature = signature,
        status = SwapStatus.valueOf(status),
        slippageBps = slippageBps,
        priceImpactPct = priceImpactPct,
        feeBps = feeBps,
        priorityFee = priorityFee,
        timestamp = timestamp,
        errorMessage = errorMessage
    )
}

fun TokenCacheEntity.toDomain(): TokenInfo {
    return TokenInfo(
        address = address,
        name = name,
        symbol = symbol,
        decimals = decimals,
        logoURI = logoURI,
        tags = tags.split(",").filter { it.isNotBlank() },
        isVerified = tags.contains("verified"),
        isStrict = tags.contains("strict"),
        dailyVolume = dailyVolume,
        hasFreezableAuthority = hasFreezableAuthority,
        hasMintableAuthority = hasMintableAuthority,
        coingeckoId = coingeckoId
    )
}