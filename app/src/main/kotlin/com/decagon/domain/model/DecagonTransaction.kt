package com.decagon.domain.model

data class DecagonTransaction(
    val id: String,
    val from: String,
    val to: String,
    val amount: Double,
    val lamports: Long,
    val signature: String? = null,
    val status: TransactionStatus,
    val timestamp: Long,
    val fee: Long = 5000 // Default Solana fee in lamports
) {
    val truncatedSignature: String?
        get() = signature?.let { "${it.take(4)}...${it.takeLast(4)}" }
}

enum class TransactionStatus {
    PENDING,
    CONFIRMED,
    FAILED
}