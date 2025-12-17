package com.octane.wallet.core.blockchain

/**
 * Simulates transactions before signing.
 * Integrates with Helius Transaction Simulation API.
 */
interface TransactionSimulator {
    /**
     * Simulate a transaction to preview its effects.
     * @param transaction Serialized transaction bytes
     * @return Simulation result with account changes
     */
    suspend fun simulate(
        transaction: ByteArray
    ): SimulationResult
}

/**
 * Transaction simulation result.
 */
sealed class SimulationResult {
    /**
     * Simulation succeeded - transaction would succeed on-chain.
     */
    data class Success(
        val accounts: List<AccountChange>,
        val logs: List<String>,
        val computeUnitsConsumed: Int
    ) : SimulationResult()

    /**
     * Simulation failed - transaction would fail on-chain.
     */
    data class Error(
        val message: String,
        val logs: List<String>,
        val errorCode: Int? = null
    ) : SimulationResult()

    /**
     * Simulation shows suspicious activity.
     */
    data class Warning(
        val risk: RiskLevel,
        val reason: String,
        val accounts: List<AccountChange>
    ) : SimulationResult()
}

/**
 * Transaction risk level from simulation.
 */
enum class RiskLevel {
    SAFE,           // Normal transaction
    SUSPICIOUS,     // Unusual but not necessarily dangerous
    DANGEROUS,      // High likelihood of scam
    MALICIOUS       // Known malicious contract/address
}

/**
 * Account balance change from simulation.
 */
data class AccountChange(
    val address: String,
    val before: Long,       // Balance before (lamports)
    val after: Long,        // Balance after (lamports)
    val delta: Long,        // Change in balance
    val tokenMint: String?  // Token mint if SPL token
) {
    val isDecrease: Boolean get() = delta < 0
    val isIncrease: Boolean get() = delta > 0
}

/**
 * Stub implementation - integrate Helius API later.
 */
class TransactionSimulatorImpl : TransactionSimulator {
    override suspend fun simulate(
        transaction: ByteArray
    ): SimulationResult {
        // TODO: Integrate Helius API
        // POST https://api.helius.xyz/v0/transactions/parse

        // For now, return success with empty changes
        return SimulationResult.Success(
            accounts = emptyList(),
            logs = listOf("Transaction simulation not yet implemented"),
            computeUnitsConsumed = 200_000
        )
    }
}