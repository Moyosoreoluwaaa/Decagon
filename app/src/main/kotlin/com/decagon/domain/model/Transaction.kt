package com.decagon.domain.model

enum class TransactionType {
    SEND,
    RECEIVE,
    SWAP,
    STAKE,
    UNSTAKE,
    CLAIM_REWARDS,
    APPROVE,
    REVOKE,
    NFT_MINT,
    NFT_TRANSFER,
    SINGLE,
    MULTIPLE,
    MESSAGE
}

data class Transaction(
    val id: String,
    val walletId: String,
    val chainId: String,
    val txHash: String,
    val type: TransactionType,
    val status: TransactionStatus,
    val fromAddress: String,
    val toAddress: String?,
    val amount: String,
    val tokenSymbol: String,
    val tokenMint: String?,
    val fee: String,
    val feePriority: String?,
    val blockNumber: Long?,
    val confirmationCount: Int,
    val errorMessage: String?,
    val memo: String?,
    val timestamp: Long,
    val simulated: Boolean = false,
    val simulationSuccess: Boolean? = null
)

