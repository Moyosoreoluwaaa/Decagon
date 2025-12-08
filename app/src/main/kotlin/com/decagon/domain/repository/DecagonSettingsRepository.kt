package com.decagon.domain.repository

import androidx.fragment.app.FragmentActivity

/**
 * Settings repository interface.
 * Handles wallet management operations that require user authentication.
 */
interface DecagonSettingsRepository {

    /**
     * Reveals recovery phrase with biometric authentication.
     *
     * @param walletId Wallet ID
     * @param activity FragmentActivity for biometric prompt
     * @return Recovery phrase or error
     */
    suspend fun revealRecoveryPhrase(
        walletId: String,
        activity: FragmentActivity
    ): Result<String>

    /**
     * Reveals private key with biometric authentication.
     *
     * @param walletId Wallet ID
     * @param activity FragmentActivity for biometric prompt
     * @return Private key hex or error
     */
    suspend fun revealPrivateKey(
        walletId: String,
        activity: FragmentActivity
    ): Result<String>

    /**
     * Updates wallet name.
     *
     * @param walletId Wallet ID
     * @param newName New wallet name
     */
    suspend fun updateWalletName(
        walletId: String,
        newName: String
    ): Result<Unit>

    /**
     * Removes wallet with biometric confirmation.
     *
     * @param walletId Wallet ID
     * @param activity FragmentActivity for biometric prompt
     */
    suspend fun removeWallet(
        walletId: String,
        activity: FragmentActivity
    ): Result<Unit>
}