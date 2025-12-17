package com.octane.wallet.data.mappers

import com.octane.wallet.data.local.database.entities.WalletEntity
import com.octane.wallet.domain.models.Wallet

/**
 * Entity → Domain
 */
fun WalletEntity.toDomain(): Wallet = Wallet(
    id = id,
    name = name,
    publicKey = publicKey,
    iconEmoji = iconEmoji,
    colorHex = colorHex,
    chainId = chainId,
    isActive = isActive,
    isHardwareWallet = isHardwareWallet,
    hardwareDeviceName = hardwareDeviceName,
    createdAt = createdAt,
    lastUpdated = lastUpdated
)


/**
 * Domain → Entity
 */
fun Wallet.toEntity(): WalletEntity = WalletEntity(
    id = id,
    name = name,
    publicKey = publicKey,
    iconEmoji = iconEmoji,
    colorHex = colorHex,
    chainId = chainId,
    isActive = isActive,
    isHardwareWallet = isHardwareWallet,
    hardwareDeviceName = hardwareDeviceName,
    createdAt = createdAt,
    lastUpdated = lastUpdated
)
