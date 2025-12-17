package com.octane.wallet.core.security

/**
 * Detects potentially malicious transactions before signing.
 * Ready for Blowfish API integration.
 */
interface MaliciousSignatureDetector {
    suspend fun analyzeTransaction(
        transaction: ByteArray
    ): TransactionRiskAssessment
}

data class TransactionRiskAssessment(
    val riskLevel: RiskLevel,
    val warnings: List<String>,
    val details: RiskDetails
)

enum class RiskLevel {
    SAFE,           // Normal transaction
    SUSPICIOUS,     // Unusual but not necessarily dangerous
    DANGEROUS,      // High likelihood of scam
    MALICIOUS       // Known malicious contract/address
}

data class RiskDetails(
    val isKnownScam: Boolean,
    val unusualPermissions: List<String>,
    val suspiciousRecipients: List<String>,
    val estimatedLoss: Double? = null
)

// Stub implementation (integrate Blowfish API later)
class MaliciousSignatureDetectorImpl : MaliciousSignatureDetector {
    override suspend fun analyzeTransaction(
        transaction: ByteArray
    ): TransactionRiskAssessment {
        // TODO: Integrate Blowfish API
        // For now, return safe assessment
        return TransactionRiskAssessment(
            riskLevel = RiskLevel.SAFE,
            warnings = emptyList(),
            details = RiskDetails(
                isKnownScam = false,
                unusualPermissions = emptyList(),
                suspiciousRecipients = emptyList()
            )
        )
    }
}