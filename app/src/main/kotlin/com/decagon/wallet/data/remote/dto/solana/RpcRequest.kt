package com.octane.wallet.data.remote.dto.solana

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Generic Solana RPC request wrapper.
 */
@Serializable
data class RpcRequest<T>(
    val jsonrpc: String = "2.0",
    val id: Int = 1,
    val method: String,
    val params: T
)

/**
 * Generic RPC response wrapper.
 */
@Serializable
data class RpcResponse<T>(
    val jsonrpc: String,
    val id: Int,
    val result: T? = null,
    val error: RpcError? = null
)

@Serializable
data class RpcError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null
)

// Balance
@Serializable
data class BalanceResult(
    val value: Long
)

// Token Accounts
@Serializable
data class TokenAccountsParams(
    val owner: String,
    val options: TokenAccountsOptions
)

@Serializable
data class TokenAccountsOptions(
    val encoding: String = "jsonParsed",
    val programId: String = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
)

@Serializable
data class TokenAccountsResult(
    val value: List<TokenAccountInfo>
)

@Serializable
data class TokenAccountInfo(
    val pubkey: String,
    val account: TokenAccountData
)

@Serializable
data class TokenAccountData(
    val data: TokenAccountParsed,
    val executable: Boolean,
    val lamports: Long,
    val owner: String,
    val rentEpoch: Long
)

@Serializable
data class TokenAccountParsed(
    val parsed: TokenAccountInfoParsed,
    val program: String,
    val space: Long
)

@Serializable
data class TokenAccountInfoParsed(
    val info: TokenInfo,
    val type: String
)

@Serializable
data class TokenInfo(
    val mint: String,
    val owner: String,
    val tokenAmount: TokenAmount,
    val state: String? = null,
    val isNative: Boolean = false
)

@Serializable
data class TokenAmount(
    val amount: String,
    val decimals: Int,
    val uiAmount: Double?,
    val uiAmountString: String
)

// Transaction
@Serializable
data class TransactionResult(
    val slot: Long,
    val transaction: TransactionData,
    val meta: TransactionMeta?
)

@Serializable
data class TransactionData(
    val signatures: List<String>,
    val message: MessageData
)

@Serializable
data class MessageData(
    val accountKeys: List<String>,
    val instructions: List<JsonElement>
)

@Serializable
data class TransactionMeta(
    val err: JsonElement?,
    val fee: Long,
    val preBalances: List<Long>,
    val postBalances: List<Long>,
    val logMessages: List<String>?
)

// Simulation
@Serializable
data class SimulateParams(
    val transaction: String,
    val options: SimulateOptions
)

@Serializable
data class SimulateOptions(
    val encoding: String = "base64",
    val commitment: String = "confirmed"
)

@Serializable
data class SimulationResult(
    val err: JsonElement?,
    val logs: List<String>?,
    val accounts: List<JsonElement>?
)

// Blockhash
@Serializable
data class BlockhashResult(
    val blockhash: String,
    val lastValidBlockHeight: Long
)

// Signature Status
@Serializable
data class SignatureStatusesResult(
    val value: List<SignatureStatus?>
)

@Serializable
data class SignatureStatus(
    val slot: Long,
    val confirmations: Int?,
    val err: JsonElement?,
    val confirmationStatus: String?
)

// Signatures
@Serializable
data class SignaturesParams(
    val address: String,
    val options: SignaturesOptions
)

@Serializable
data class SignaturesOptions(
    val limit: Int = 50,
    val before: String? = null,
    val until: String? = null
)

@Serializable
data class SignatureInfo(
    val signature: String,
    val slot: Long,
    val err: JsonElement?,
    val memo: String?,
    val blockTime: Long?
)