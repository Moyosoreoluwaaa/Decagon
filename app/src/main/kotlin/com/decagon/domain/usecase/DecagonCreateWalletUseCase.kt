package com.decagon.domain.usecase

import androidx.fragment.app.FragmentActivity
import com.decagon.core.crypto.DecagonMnemonic
import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.repository.DecagonWalletRepository
import timber.log.Timber // Import added

/**
 * Creates a new wallet with mnemonic generation.
 */
class DecagonCreateWalletUseCase(
    private val repository: DecagonWalletRepository,
    private val mnemonic: DecagonMnemonic
) {
    init {
        Timber.d("DecagonCreateWalletUseCase initialized.") // Log added
    }

    /**
     * Creates wallet with auto-generated mnemonic.
     * * @param name Wallet name
     * @return Result containing wallet and mnemonic phrase
     */
    suspend operator fun invoke(
        name: String,
        activity: FragmentActivity
    ): Result<WalletCreationResult> {
        Timber.d("Executing create wallet use case for name: $name")
        return try {
            Timber.v("Generating 12-word mnemonic phrase.")
            val phrase = mnemonic.generate12WordPhrase()
            Timber.d("Mnemonic generated successfully.")

            Timber.v("Calling repository to create wallet and secure seed.")
            val wallet = repository.createWallet(name, phrase, 0, activity).getOrThrow()

            Timber.i("Wallet created and seed secured: ${wallet.id}")
            Result.success(WalletCreationResult(wallet, phrase))
        } catch (e: Exception) {
            Timber.e(e, "Failed to create wallet in use case.")
            Result.failure(e)
        }
    }

    data class WalletCreationResult(
        val wallet: DecagonWallet,
        val mnemonic: String
    )
}