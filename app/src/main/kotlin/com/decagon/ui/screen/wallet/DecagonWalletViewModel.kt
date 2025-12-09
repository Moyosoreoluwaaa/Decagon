package com.decagon.ui.screen.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.util.DecagonLoadingState
import com.decagon.data.remote.CoinPriceService
import com.decagon.data.remote.SolanaRpcClient
import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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

    val walletState: StateFlow<DecagonLoadingState<DecagonWallet>> =
        repository.getActiveWallet()
            .filterNotNull()
            .flatMapLatest { wallet ->
                combine(
                    flow { emit(wallet) },
                    _selectedCurrency
                ) { w, currency ->
                    Timber.d("Updating wallet data for currency: $currency")

                    try {
                        // Fetch balance
                        val balanceResult = rpcClient.getBalance(w.address)
                        val balanceSol = balanceResult.getOrNull()?.let { it / 1_000_000_000.0 } ?: 0.0

                        // Fetch price
                        val priceResult = priceService.getPrices(
                            listOf(CoinPriceService.COIN_ID_SOLANA),
                            currency
                        )
                        val price = priceResult.getOrNull()?.get(CoinPriceService.COIN_ID_SOLANA) ?: 0.0
                        _fiatPrice.value = price

                        Timber.i("Wallet balance: $balanceSol SOL, Price: $price $currency")

                        DecagonLoadingState.Success(w.copy(balance = balanceSol))
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