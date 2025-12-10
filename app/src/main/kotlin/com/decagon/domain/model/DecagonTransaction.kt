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
    val fee: Long = 5000, // Base fee
    val priorityFee: Long = 0 // ✅ NEW: Priority fee in microlamports
) {
    val truncatedSignature: String?
        get() = signature?.let { "${it.take(4)}...${it.takeLast(4)}" }

    val totalFee: Long get() = fee + (priorityFee / 1_000_000) // Convert microlamports to lamports

    val totalFeeSol: Double get() = totalFee / 1_000_000_000.0
}
enum class TransactionStatus {
    PENDING,
    CONFIRMED,
    FAILED
}