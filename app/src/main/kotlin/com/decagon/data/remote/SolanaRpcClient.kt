package com.decagon.data.remote

import com.decagon.domain.model.TransactionDetails
import com.decagon.util.buildJsonRpcRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import timber.log.Timber
import java.util.UUID


class SolanaRpcClient(
    private val httpClient: HttpClient,
    private val rpcUrl: String = "https://api.devnet.solana.com"
) {

    init {
        Timber.d("SolanaRpcClient initialized with URL: $rpcUrl")
    }

    suspend fun getBalance(address: String): Result<Long> {
        Timber.d("Fetching balance for address: ${address.take(4)}...${address.takeLast(4)}")
        return try {
            val response = httpClient.post(rpcUrl) {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("jsonrpc", "2.0")
                    put("id", UUID.randomUUID().toString())
                    put("method", "getBalance")
                    put("params", buildJsonArray {
                        add(address)
                    })
                })
            }

            val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val result = json["result"]?.jsonObject
            val lamports = result?.get("value")?.jsonPrimitive?.long ?: 0L

            Timber.i("Balance fetched: $lamports lamports")
            Result.success(lamports)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch balance")
            Result.failure(e)
        }
    }

    /**
     * Fetches the latest valid blockhash from the network.
     * This is required for transaction construction.
     */
    suspend fun getLatestBlockhash(): Result<String> {
        Timber.d("Fetching latest blockhash")
        return try {
            val response = httpClient.post(rpcUrl) {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("jsonrpc", "2.0")
                    put("id", UUID.randomUUID().toString())
                    put("method", "getLatestBlockhash")
                })
            }

            val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject

            // Navigate the JSON path: result.value.blockhash
            val blockhash = json["result"]
                ?.jsonObject
                ?.get("value")
                ?.jsonObject
                ?.get("blockhash")
                ?.jsonPrimitive
                ?.content

            if (blockhash.isNullOrBlank()) {
                throw IllegalStateException("Failed to parse blockhash from RPC response.")
            }

            Timber.i("Latest blockhash fetched: ${blockhash.take(8)}...")
            Result.success(blockhash)

        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch latest blockhash")
            Result.failure(e)
        }
    }

    suspend fun sendTransaction(serializedTx: ByteArray): Result<String> {
        Timber.d("Sending transaction")
        return try {
            val base64Tx = android.util.Base64.encodeToString(
                serializedTx,
                android.util.Base64.NO_WRAP
            )

            val requestBody = buildJsonObject {
                put("jsonrpc", "2.0")
                put("id", UUID.randomUUID().toString())
                put("method", "sendTransaction")
                put("params", buildJsonArray {
                    add(base64Tx)
                    // ✅ ADD THIS ENCODING CONFIG
                    add(buildJsonObject {
                        put("encoding", "base64")
                        put("skipPreflight", false)
                        put("preflightCommitment", "confirmed")
                    })
                })
            }

            Timber.d("Request body: $requestBody")

            val response = httpClient.post(rpcUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val rawResponse = response.bodyAsText()
            Timber.d("RPC Response: $rawResponse")

            val json = Json.parseToJsonElement(rawResponse).jsonObject

            val error = json["error"]?.jsonObject
            if (error != null) {
                val errorMsg = error["message"]?.jsonPrimitive?.content ?: "Unknown RPC error"
                Timber.e("RPC Error: $errorMsg")
                throw IllegalStateException("RPC Error: $errorMsg")
            }

            val signature = json["result"]?.jsonPrimitive?.content
                ?: throw IllegalStateException("No signature in response")

            Timber.i("Transaction signature: ${signature.take(8)}...")
            Result.success(signature)

        } catch (e: Exception) {
            Timber.e(e, "Transaction sending failed")
            Result.failure(e)
        }
    }

    suspend fun simulateTransaction(serializedTx: ByteArray): Result<SimulationResult> {
        Timber.d("Simulating transaction")
        return try {
            val base64Tx = android.util.Base64.encodeToString(
                serializedTx,
                android.util.Base64.NO_WRAP
            )

            val response = httpClient.post(rpcUrl) {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("jsonrpc", "2.0")
                    put("id", UUID.randomUUID().toString())
                    put("method", "simulateTransaction")
                    put("params", buildJsonArray {
                        add(base64Tx)
                        buildJsonObject {
                            put("encoding", "base64") // ✅ Add this
                            put("commitment", "confirmed")
                        }
                    })
                })
            }

            val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
//            val result = json["result"]?.jsonObject?.get("value")?.jsonObject
//            val error = result?.get("err")

//            val simulationResult = SimulationResult(
//                willSucceed = error == null || error is JsonNull,
//                errorMessage = error?.toString()
//            )
//
//            Timber.i("Simulation result: ${if (simulationResult.willSucceed) "SUCCESS" else "FAIL"}")
//            Result.success(simulationResult)

            val result = json["result"]?.jsonObject
            val value = result?.get("value")?.jsonObject

            val err = value?.get("err")
            val willSucceed = err == null || err is JsonNull

            // ✅ Extract fee from simulation
            val fee = value?.get("unitsConsumed")?.jsonPrimitive?.longOrNull

            return Result.success(
                SimulationResult(
                    willSucceed = willSucceed,
                    errorMessage = if (!willSucceed) err.toString() else null,
                    fee = fee
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Simulation failed")
            Result.failure(e)
        }
    }

    /**
     * Gets transaction status from blockchain.
     * This checks if the transaction has been processed/confirmed.
     *
     * @param signature Transaction signature
     * @return Status: "pending", "processed", "confirmed", "finalized", or "failed"
     */
    suspend fun getTransactionStatus(signature: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = buildJsonRpcRequest(
                    method = "getSignatureStatuses",
                    params = listOf(
                        listOf(signature),
                        buildJsonObject {
                            put("searchTransactionHistory", true)
                        }
                    )
                )

                val response = httpClient.post(rpcUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                if (!response.status.isSuccess()) {
                    return@withContext Result.failure(
                        IOException("RPC error: ${response.status}")
                    )
                }

                val body = response.bodyAsText()
                val json = Json.parseToJsonElement(body).jsonObject

                // Check for RPC error
                json["error"]?.let { error ->
                    val errorMsg = error.jsonObject["message"]?.jsonPrimitive?.content
                        ?: "Unknown RPC error"
                    return@withContext Result.failure(IOException(errorMsg))
                }

                // Parse result
                val result = json["result"]?.jsonObject
                    ?: return@withContext Result.failure(
                        IOException("Missing result in response")
                    )

                val statusArray = result["value"]?.jsonArray
                    ?: return@withContext Result.failure(
                        IOException("Missing value array")
                    )

                // Get first status (we only queried one signature)
                val statusObj = statusArray.firstOrNull()?.jsonObject

                if (statusObj == null) {
                    // Transaction not found yet (still pending)
                    return@withContext Result.success("pending")
                }

                // Check if transaction failed
                val err = statusObj["err"]
                if (err != null && err !is JsonNull) {
                    Timber.w("Transaction failed with error: $err")
                    return@withContext Result.success("failed")
                }

                // Get confirmation status
                val confirmationStatus = statusObj["confirmationStatus"]
                    ?.jsonPrimitive?.content ?: "pending"

                Timber.d("Transaction $signature status: $confirmationStatus")
                Result.success(confirmationStatus)

            } catch (e: Exception) {
                Timber.e(e, "Failed to get transaction status")
                Result.failure(e)
            }
        }
    }

    /**
     * Gets transaction details from blockchain.
     * Use this to verify a transaction exists and get full details.
     *
     * @param signature Transaction signature
     * @return Transaction details or null if not found
     */
    suspend fun getTransaction(signature: String): Result<TransactionDetails?> {
        return withContext(Dispatchers.IO) {
            try {
                val request = buildJsonRpcRequest(
                    method = "getTransaction",
                    params = listOf(
                        signature,
                        buildJsonObject {
                            put("encoding", "json")
                            put("maxSupportedTransactionVersion", 0)
                        }
                    )
                )

                val response = httpClient.post(rpcUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                if (!response.status.isSuccess()) {
                    return@withContext Result.failure(
                        IOException("RPC error: ${response.status}")
                    )
                }

                val body = response.bodyAsText()
                val json = Json.parseToJsonElement(body).jsonObject

                // Check for RPC error
                json["error"]?.let { error ->
                    val errorMsg = error.jsonObject["message"]?.jsonPrimitive?.content
                        ?: "Unknown RPC error"
                    return@withContext Result.failure(IOException(errorMsg))
                }

                // Parse result
                val result = json["result"]

                if (result is JsonNull || result == null) {
                    // Transaction not found
                    return@withContext Result.success(null)
                }

                val resultObj = result.jsonObject

                // Extract slot (block number)
                val slot = resultObj["slot"]?.jsonPrimitive?.long ?: 0L

                // Extract block time (Unix timestamp)
                val blockTime = resultObj["blockTime"]?.jsonPrimitive?.long ?: 0L

                // Extract meta (includes fee and status)
                val meta = resultObj["meta"]?.jsonObject
                val fee = meta?.get("fee")?.jsonPrimitive?.long ?: 0L
                val err = meta?.get("err")
                val status = if (err == null || err is JsonNull) "confirmed" else "failed"

                val details = TransactionDetails(
                    signature = signature,
                    slot = slot,
                    blockTime = blockTime,
                    fee = fee,
                    status = status
                )

                Timber.d("Transaction details: $details")
                Result.success(details)

            } catch (e: Exception) {
                Timber.e(e, "Failed to get transaction details")
                Result.failure(e)
            }
        }
    }

    suspend fun getSignaturesForAddress(
        address: String,
        limit: Int = 50
    ): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = buildJsonRpcRequest(
                    method = "getSignaturesForAddress",
                    params = listOf(
                        address,
                        buildJsonObject {
                            put("limit", limit)
                        }
                    )
                )

                val response = httpClient.post(rpcUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                val body = response.bodyAsText()
                val json = Json.parseToJsonElement(body).jsonObject

                json["error"]?.let { error ->
                    val errorMsg = error.jsonObject["message"]?.jsonPrimitive?.content
                        ?: "Unknown RPC error"
                    return@withContext Result.failure(IOException(errorMsg))
                }

                val result = json["result"]?.jsonArray
                    ?: return@withContext Result.failure(IOException("Missing result"))

                val signatures = result.mapNotNull { elem ->
                    elem.jsonObject["signature"]?.jsonPrimitive?.content
                }

                Result.success(signatures)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Confirms a transaction by polling its status.
     * Waits until the transaction reaches the desired commitment level.
     *
     * @param signature Transaction signature
     * @param commitment Desired commitment: "processed", "confirmed", "finalized"
     * @param timeoutSeconds Maximum time to wait (default: 30 seconds)
     * @return true if confirmed, false if timeout
     */
    suspend fun confirmTransaction(
        signature: String,
        commitment: String = "confirmed",
        timeoutSeconds: Int = 30
    ): Result<Boolean> {
        return withContext(Dispatchers.Default) {
            try {
                val startTime = System.currentTimeMillis()
                val timeoutMillis = timeoutSeconds * 1000L

                while (System.currentTimeMillis() - startTime < timeoutMillis) {
                    val statusResult = getTransactionStatus(signature)

                    if (statusResult.isFailure) {
                        Timber.w("Error checking status: ${statusResult.exceptionOrNull()}")
                        kotlinx.coroutines.delay(1000)
                        continue
                    }

                    val status = statusResult.getOrNull()

                    when {
                        status == "failed" -> {
                            return@withContext Result.success(false)
                        }
                        status == "finalized" -> {
                            return@withContext Result.success(true)
                        }
                        status == "confirmed" && commitment != "finalized" -> {
                            return@withContext Result.success(true)
                        }
                        status == "processed" && commitment == "processed" -> {
                            return@withContext Result.success(true)
                        }
                    }

                    kotlinx.coroutines.delay(1000)
                }

                // Timeout
                Timber.w("Transaction confirmation timeout after $timeoutSeconds seconds")
                Result.success(false)

            } catch (e: Exception) {
                Timber.e(e, "Error confirming transaction")
                Result.failure(e)
            }
        }
    }

}



data class SimulationResult(
    val willSucceed: Boolean,
    val errorMessage: String? = null,
    val fee: Long? = null // ✅ NEW: Actual fee from simulation
)