package com.decagon.ui.screen.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.chains.ChainType
import com.decagon.core.util.DecagonLoadingState
import com.decagon.data.remote.CoinPriceService
import com.decagon.data.remote.SolanaRpcClient
import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class DecagonWalletViewModel(
    private val repository: DecagonWalletRepository,
    private val rpcClient: SolanaRpcClient,
    private val priceService: CoinPriceService
) : ViewModel() {

    init {
        Timber.d("DecagonWalletViewModel initialized.")
    }

    private val _selectedCurrency = MutableStateFlow("usd")
    val selectedCurrency = _selectedCurrency.asStateFlow()

    private val _fiatPrice = MutableStateFlow(0.0)
    val fiatPrice = _fiatPrice.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val walletState: StateFlow<DecagonLoadingState<DecagonWallet>> =
        repository.getActiveWallet()
            .filterNotNull()
            .flatMapLatest { wallet ->
                combine(
                    flow { emit(wallet) },
                    _selectedCurrency,
                    // ✅ Re-emit when wallet updates (e.g., active chain changes)
                    repository.getWalletById(wallet.id).filterNotNull()
                ) { _, currency, updatedWallet ->
                    Timber.d("Updating wallet data for currency: $currency, activeChain: ${updatedWallet.activeChainId}")

                    try {
                        // Get active chain for balance fetch
                        val activeChain = updatedWallet.activeChain
                            ?: throw IllegalStateException("No active chain")

                        // Fetch balance for active chain
                        val balanceResult = when (activeChain.chainType) {
                            ChainType.Solana -> rpcClient.getBalance(activeChain.address)
                            else -> Result.success(0L) // Other chains not implemented yet
                        }

                        val balance = balanceResult.getOrNull()?.let {
                            it / 1_000_000_000.0 // Convert lamports to SOL
                        } ?: 0.0

                        // Fetch price for active chain
                        val coinId = when (activeChain.chainType) {
                            ChainType.Solana -> CoinPriceService.COIN_ID_SOLANA
                            ChainType.Ethereum -> CoinPriceService.COIN_ID_ETHEREUM
                            ChainType.Polygon -> CoinPriceService.COIN_ID_POLYGON
                        }

                        val priceResult = priceService.getPrices(listOf(coinId), currency)
                        val price = priceResult.getOrNull()?.get(coinId) ?: 0.0
                        _fiatPrice.value = price

                        Timber.i("Wallet balance: $balance ${activeChain.chainType.name}, Price: $price $currency")

                        // Update active chain's balance
                        val updatedChains = updatedWallet.chains.map { chain ->
                            if (chain.chainId == updatedWallet.activeChainId) {
                                chain.copy(balance = balance)
                            } else {
                                chain
                            }
                        }

                        DecagonLoadingState.Success(
                            updatedWallet.copy(
                                balance = balance,
                                chains = updatedChains
                            )
                        )
                    } catch (error: Throwable) {
                        Timber.e(error, "Failed to fetch wallet data")
                        DecagonLoadingState.Error(error, error.message ?: "Failed to load wallet")
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DecagonLoadingState.Loading
            )

    val allWallets: StateFlow<List<DecagonWallet>> =
        repository.getAllWallets()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun switchWallet(walletId: String) {
        Timber.i("Switching to wallet: $walletId")
        viewModelScope.launch {
            repository.setActiveWallet(walletId)
        }
    }

    fun setCurrency(currency: String) {
        Timber.i("Changing currency to: $currency")
        _selectedCurrency.value = currency
    }
}