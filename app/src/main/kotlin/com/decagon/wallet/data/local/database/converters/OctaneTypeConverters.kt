// app/core/data/local/database/converters/OctaneTypeConverters.kt

package com.octane.wallet.data.local.database.converters

import androidx.room.TypeConverter
import com.octane.wallet.domain.models.TransactionStatus
import com.octane.wallet.domain.models.TransactionType

/**
 * Type converters for Room to handle enums.
 */
class OctaneTypeConverters {
    
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }
    
    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
    
    @TypeConverter
    fun fromTransactionStatus(value: TransactionStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toTransactionStatus(value: String): TransactionStatus {
        return TransactionStatus.valueOf(value)
    }
}