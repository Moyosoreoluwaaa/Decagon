package com.octane.wallet.core.util

/**
 * Input validation utilities for Octane Wallet.
 * Handles Solana addresses, SOL amounts, SNS domains, etc.
 */
object Validators {

    /**
     * Validate Solana wallet address (base58, 32-44 chars).
     */
    fun isValidSolanaAddress(address: String): Boolean {
        if (address.isBlank() || address.length !in 32..44) return false

        // Base58 alphabet (no 0, O, I, l)
        val base58Regex = "^[1-9A-HJ-NP-Za-km-z]+$".toRegex()
        return address.matches(base58Regex)
    }

    /**
     * Validate SOL amount.
     * @param amount Amount as string
     * @param maxDecimals Maximum decimal places (default: 9)
     * @param min Minimum value (default: 0)
     * @param max Maximum value (default: unlimited)
     */
    fun validateSOLAmount(
        amount: String,
        maxDecimals: Int = 9,
        min: Double = 0.0,
        max: Double = Double.MAX_VALUE
    ): ValidationResult {
        if (amount.isBlank()) {
            return ValidationResult.Invalid("Amount cannot be empty")
        }

        val value = amount.toDoubleOrNull()
            ?: return ValidationResult.Invalid("Invalid number format")

        if (value < min) {
            return ValidationResult.Invalid("Minimum amount is ${Formatters.formatSOL(min)}")
        }

        if (value > max) {
            return ValidationResult.Invalid("Maximum amount is ${Formatters.formatSOL(max)}")
        }

        // Check decimal places
        val parts = amount.split(".")
        if (parts.size == 2 && parts[1].length > maxDecimals) {
            return ValidationResult.Invalid("Maximum $maxDecimals decimal places allowed")
        }

        return ValidationResult.Valid
    }

    /**
     * Validate SNS domain (.sol names).
     */
    fun isValidSNSDomain(domain: String): Boolean {
        if (!domain.endsWith(".sol")) return false
        val name = domain.substringBeforeLast(".sol")
        if (name.isEmpty() || name.length < 2) return false

        // SNS names: lowercase, numbers, hyphens
        val snsRegex = "^[a-z0-9-]+$".toRegex()
        return name.matches(snsRegex)
    }

    /**
     * Validate memo (optional transaction note).
     * @param memo Memo text
     * @param maxLength Maximum length (Solana limit: ~566 bytes)
     */
    fun validateMemo(memo: String, maxLength: Int = 500): ValidationResult {
        if (memo.isEmpty()) return ValidationResult.Valid

        if (memo.length > maxLength) {
            return ValidationResult.Invalid("Memo too long (max $maxLength chars)")
        }

        return ValidationResult.Valid
    }

    /**
     * Validate custom RPC endpoint URL.
     */
    fun isValidRpcUrl(url: String): Boolean {
        if (url.isBlank()) return false

        val urlRegex = "^(https?|wss?)://[^\\s/$.?#].[^\\s]*$".toRegex(RegexOption.IGNORE_CASE)
        return url.matches(urlRegex)
    }

    /**
     * Validate slippage percentage (for swaps).
     * @param slippage Slippage as percentage (e.g., 1.0 = 1%)
     */
    fun validateSlippage(slippage: Double): ValidationResult {
        return when {
            slippage < 0 -> ValidationResult.Invalid("Slippage cannot be negative")
            slippage > 50 -> ValidationResult.Invalid("Slippage too high (max 50%)")
            else -> ValidationResult.Valid
        }
    }
}

/**
 * Validation result sealed class.
 */
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val message: String) : ValidationResult

    val isValid: Boolean get() = this is Valid
    val errorMessage: String? get() = (this as? Invalid)?.message
}