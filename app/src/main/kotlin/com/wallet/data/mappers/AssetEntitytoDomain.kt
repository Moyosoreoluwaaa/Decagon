package com.octane.wallet.data.mappers

import com.wallet.data.local.database.entities.AssetEntity
import com.octane.wallet.domain.models.Asset

fun AssetEntity.toDomain(): Asset = Asset(
    id = id,
    walletId = walletId,
    chainId = chainId,
    symbol = symbol,
    name = name,
    mintAddress = mintAddress,
    balance = balance,
    decimals = decimals,
    priceUsd = priceUsd,
    valueUsd = valueUsd,
    priceChange24h = priceChange24h,
    iconUrl = iconUrl,
    isNative = isNative,
    isHidden = isHidden,
    costBasisUsd = costBasisUsd,
    lastUpdated = lastUpdated
)

fun Asset.toEntity(): AssetEntity = AssetEntity(
    id = id,
    walletId = walletId,
    chainId = chainId,
    symbol = symbol,
    name = name,
    mintAddress = mintAddress,
    balance = balance,
    decimals = decimals,
    priceUsd = priceUsd,
    valueUsd = valueUsd,
    priceChange24h = priceChange24h,
    iconUrl = iconUrl,
    isNative = isNative,
    isHidden = isHidden,
    costBasisUsd = costBasisUsd,
    lastUpdated = lastUpdated
)
