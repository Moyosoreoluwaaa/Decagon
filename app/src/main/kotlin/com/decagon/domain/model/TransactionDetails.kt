package com.decagon.domain.model

/**
* Data class for transaction details.
*/
data class TransactionDetails(
    val signature: String,
    val slot: Long,
    val blockTime: Long,
    val fee: Long,
    val status: String // "confirmed" or "failed"
)