package com.decagon.data.local

import androidx.room.TypeConverter

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
}