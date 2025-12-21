package com.octane.wallet.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionOptions(
    val onlyIfTrusted: Boolean = false
)

data class TransactionRequest(
    val id: String,
    val transactionBase64: String,
    val type: TransactionType
)
