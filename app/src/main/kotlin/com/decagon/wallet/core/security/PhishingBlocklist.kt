package com.octane.wallet.core.security

/**
 * Maintains blocklist of known phishing sites and malicious dApps.
 */
class PhishingBlocklist {

    private val blockedDomains = mutableSetOf<String>()
    private val blockedAddresses = mutableSetOf<String>()

    /**
     * Check if URL is on phishing blocklist.
     */
    fun isPhishingSite(url: String): Boolean {
        val domain = extractDomain(url)
        return domain in blockedDomains
    }

    /**
     * Check if wallet address is flagged as malicious.
     */
    fun isMaliciousAddress(address: String): Boolean {
        return address in blockedAddresses
    }

    /**
     * Add domain to blocklist.
     */
    fun blockDomain(domain: String) {
        blockedDomains.add(domain.lowercase())
    }

    /**
     * Add address to blocklist.
     */
    fun blockAddress(address: String) {
        blockedAddresses.add(address)
    }

    /**
     * Load blocklist from remote source (e.g., GitHub, API).
     */
    suspend fun refreshBlocklist() {
        // TODO: Fetch from remote source
        // Example sources:
        // - https://github.com/MetaMask/eth-phishing-detect
        // - Solana-specific phishing lists
    }

    private fun extractDomain(url: String): String {
        return try {
            val withoutProtocol = url.substringAfter("://")
            val domain = withoutProtocol.substringBefore("/")
            domain.lowercase()
        } catch (e: Exception) {
            url.lowercase()
        }
    }
}