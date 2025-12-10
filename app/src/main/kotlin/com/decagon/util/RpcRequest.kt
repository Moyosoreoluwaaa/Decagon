package com.decagon.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.add

// 1. Define the RpcRequest data class (must be @Serializable for Ktor)
@Serializable
data class RpcRequest(
    val jsonrpc: String = "2.0",
    val id: String,
    val method: String,
    // Using JsonElement is flexible enough to hold null, JsonArray, or JsonObject for params
    val params: kotlinx.serialization.json.JsonElement? = null
)

// 2. Define the missing helper function
/**
 * Helper function to construct a JSON-RPC 2.0 request object.
 * This function manually converts the list of mixed parameters into a serializable JsonArray.
 */
fun buildJsonRpcRequest(method: String, params: List<Any>? = null): RpcRequest {
    // 3. The existing calls (getTransactionStatus, getTransaction) pass a List<Any> that we need to convert to a JsonArray
    val jsonParams: kotlinx.serialization.json.JsonElement? = if (params.isNullOrEmpty()) {
        null
    } else {
        kotlinx.serialization.json.buildJsonArray {
            params.forEach { arg ->
                when (arg) {
                    is String -> add(arg)
                    is Int -> add(arg)
                    is Long -> add(arg)
                    is Boolean -> add(arg)
                    // Handles the case where an argument is an entire JsonObject (like the config objects)
                    is kotlinx.serialization.json.JsonObject -> add(arg)
                    // Handles the case where an argument is a List of strings (like listOf(signature))
                    is List<*> -> {
                        add(kotlinx.serialization.json.buildJsonArray {
                            arg.forEach { inner ->
                                inner?.let { add(it.toString()) }
                            }
                        })
                    }
                    else -> throw IllegalArgumentException("Unsupported RPC parameter type: ${arg::class.simpleName}")
                }
            }
        }
    }

    return RpcRequest(
        id = java.util.UUID.randomUUID().toString(), // Using UUID like in your other functions
        method = method,
        params = jsonParams
    )
}