package com.decagon.domain.usecase.security

// Validate Solana Address
class ValidateSolanaAddressUseCase {
    operator fun invoke(address: String): Boolean {
        // Solana addresses are base58-encoded, 32-44 characters
        if (address.length !in 32..44) return false

        // Check for valid base58 characters
        val base58Regex = "^[1-9A-HJ-NP-Za-km-z]+$".toRegex()
        if (!address.matches(base58Regex)) return false

        // TODO: Add SNS domain resolution (@alice.sol)
        if (address.endsWith(".sol")) {
            // Resolve SNS domain to public key
            return true // For now
        }

        return true
    }
}