package com.decagon.data.mapper

import com.decagon.data.local.entity.DecagonWalletEntity
import com.decagon.data.mapper.DecagonWalletMapper.toDomain
import com.decagon.domain.model.DecagonWallet

/**
 * Maps between data and domain layers.
 */
object DecagonWalletMapper {
    
    fun DecagonWalletEntity.toDomain(): DecagonWallet {
        return DecagonWallet(
            id = id,
            name = name,
            publicKey = publicKey,
            address = publicKey, // Solana public key IS the address
            accountIndex = accountIndex,
            balance = 0.0, // Mocked in 0.1, real RPC call in 0.2
            createdAt = createdAt,
            isActive = isActive
        )
    }

    fun DecagonWallet.toEntity(encryptedSeed: ByteArray): DecagonWalletEntity {
        return DecagonWalletEntity(
            id = id,
            name = name,
            encryptedSeed = encryptedSeed,
            publicKey = publicKey,
            accountIndex = accountIndex,
            createdAt = createdAt,
            isActive = isActive
        )
    }
    
    fun List<DecagonWalletEntity>.toDomain(): List<DecagonWallet> {
        return map { it.toDomain() }
    }
}