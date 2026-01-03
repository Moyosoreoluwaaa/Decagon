package com.decagon.data.local.database

import androidx.room.TypeConverter
import com.decagon.domain.model.ChainWallet
import kotlinx.serialization.json.Json

/**
 * Room type converters for custom types.
 */
class DecagonTypeConverters {
    
    @TypeConverter
    fun fromByteArray(value: ByteArray): String {
        return value.joinToString(",") { it.toString() }
    }
    
    @TypeConverter
    fun toByteArray(value: String): ByteArray {
        return value.split(",").map { it.toByte() }.toByteArray()
    }

    @TypeConverter
    fun chainWalletsToJson(chains: List<ChainWallet>): String {
        return Json.encodeToString(chains)
    }

    @TypeConverter
    fun jsonToChainWallets(json: String): List<ChainWallet> {
        return Json.decodeFromString(json)
    }
}