package com.decagon.data.mapper

import com.decagon.data.local.entity.DecagonWalletEntity
import com.decagon.domain.model.DecagonWallet

object DecagonWalletMapper {

    fun DecagonWalletEntity.toDomain(): DecagonWallet {
        return DecagonWallet(
            id = id,
            name = name,
            publicKey = publicKey,
            address = address,
            accountIndex = accountIndex,
            balance = 0.0,
            createdAt = createdAt,
            isActive = isActive
        )
    }

    fun DecagonWallet.toEntity(
        encryptedSeed: ByteArray,
        encryptedMnemonic: ByteArray // âœ… NEW parameter
    ): DecagonWalletEntity {
        return DecagonWalletEntity(
            id = id,
            name = name,
            encryptedSeed = encryptedSeed,
            encryptedMnemonic = encryptedMnemonic, // âœ… Store encrypted mnemonic
            publicKey = publicKey,
            address = address,
            accountIndex = accountIndex,
            createdAt = createdAt,
            isActive = isActive
        )
    }

    fun List<DecagonWalletEntity>.toDomain(): List<DecagonWallet> {
        return map { it.toDomain() }
    }
}