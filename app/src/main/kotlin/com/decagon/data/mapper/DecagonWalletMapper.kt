package com.decagon.data.mapper

import com.decagon.data.local.entity.DecagonWalletEntity
import com.decagon.domain.model.ChainWallet
import com.decagon.domain.model.DecagonWallet
import kotlinx.serialization.json.Json

// DecagonWalletMapper.kt - UPDATE:

fun DecagonWallet.toEntity(
    encryptedSeed: ByteArray,
    encryptedMnemonic: ByteArray
): DecagonWalletEntity {
    return DecagonWalletEntity(
        id = id,
        name = name,
        encryptedSeed = encryptedSeed,
        encryptedMnemonic = encryptedMnemonic,
        publicKey = publicKey,
        address = address,
        accountIndex = accountIndex,
        createdAt = createdAt,
        isActive = isActive,
        chains = Json.encodeToString(chains), // ✅ Serialize to JSON
        activeChainId = activeChainId
    )
}

fun DecagonWalletEntity.toDomain(): DecagonWallet {
    return DecagonWallet(
        id = id,
        name = name,
        publicKey = publicKey,
        address = address,
        accountIndex = accountIndex,
        createdAt = createdAt,
        isActive = isActive,
        chains = Json.decodeFromString(chains), // ✅ Deserialize from JSON
        activeChainId = activeChainId
    )
}


object DecagonWalletMapper {

    fun DecagonWallet.toEntity(
        encryptedSeed: ByteArray,
        encryptedMnemonic: ByteArray
    ): DecagonWalletEntity {
        return DecagonWalletEntity(
            id = id,
            name = name,
            encryptedSeed = encryptedSeed,
            encryptedMnemonic = encryptedMnemonic,
            publicKey = publicKey,
            address = address,
            accountIndex = accountIndex,
            createdAt = createdAt,
            isActive = isActive,
            chains = Json.encodeToString(chains),
            activeChainId = activeChainId
        )
    }

    fun DecagonWalletEntity.toDomain(): DecagonWallet {
        return DecagonWallet(
            id = id,
            name = name,
            publicKey = publicKey,
            address = address,
            accountIndex = accountIndex,
            createdAt = createdAt,
            isActive = isActive,
            chains = try {
                Json.decodeFromString<List<ChainWallet>>(chains)
            } catch (e: Exception) {
                emptyList() // Migration fallback
            },
            activeChainId = activeChainId
        )
    }
}