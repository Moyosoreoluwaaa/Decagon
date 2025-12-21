package com.octane.wallet.domain.models

data class Contact(
    val id: String,
    val name: String,
    val address: String,
    val chainId: String,
    val createdAt: Long,
    val lastUsed: Long
)
