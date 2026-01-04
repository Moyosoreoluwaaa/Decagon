package com.decagon.data.mapper

import com.decagon.data.local.entity.DecagonWalletEntity
import com.decagon.domain.model.ChainWallet
import com.decagon.domain.model.DecagonWallet
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

fun DecagonWallet.toEntity(
    encryptedSeed: ByteArray,
    encryptedMnemonic: ByteArray?
): DecagonWalletEntity {
    return DecagonWalletEntity(
        id = id,
        name = name,
        encryptedSeed = encryptedSeed,
        encryptedMnemonic = encryptedMnemonic!!,
        publicKey = publicKey,
        address = address,
        accountIndex = accountIndex,
        createdAt = createdAt,
        isActive = isActive,
        chains = Json.encodeToString(serializer<List<ChainWallet>>(), chains),
        activeChainId = activeChainId,
        cachedBalance = balance,  // ✅ Map current balance to cache
        lastBalanceFetch = System.currentTimeMillis(),
        balanceStale = false
    )
}

fun DecagonWalletEntity.toDomain(): DecagonWallet {
    val chainList = try {
        Json.decodeFromString<List<ChainWallet>>(chains)
    } catch (e: Exception) {
        emptyList()
    }

    return DecagonWallet(
        id = id,
        name = name,
        publicKey = publicKey,
        address = address,
        accountIndex = accountIndex,
        createdAt = createdAt,
        isActive = isActive,
        chains = chainList,
        activeChainId = activeChainId,
        balance = cachedBalance  // ✅ Use cached balance
    )
}