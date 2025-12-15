package com.decagon.ui.screen.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.chains.ChainType
import com.decagon.core.network.NetworkManager
import com.decagon.core.network.RpcClientFactory
import com.decagon.core.util.DecagonLoadingState
import com.decagon.data.remote.CoinPriceService
import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class DecagonWalletViewModel(
    private val repository: DecagonWalletRepository,
    private val rpcFactory: RpcClientFactory,  // ← CHANGED
    private val networkManager: NetworkManager,  // ← NEW
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
                    repository.getWalletById(wallet.id).filterNotNull(),
                    networkManager.currentNetwork  // ← TRIGGERS RE-FETCH ON NETWORK CHANGE
                ) { _, currency, updatedWallet, currentNetwork ->
                    Timber.d("Updating: currency=$currency, activeChain=${updatedWallet.activeChainId}, network=$currentNetwork")

                    try {
                        val activeChain = updatedWallet.activeChain
                            ?: throw IllegalStateException("No active chain")

                        // Create network-aware RPC client
                        val rpcClient = rpcFactory.createSolanaClient(activeChain.chainId)

                        val balanceResult = when (activeChain.chainType) {
                            ChainType.Solana -> rpcClient.getBalance(activeChain.address)
                            else -> Result.success(0L)
                        }

                        val balance = balanceResult.getOrNull()?.let {
                            it / 1_000_000_000.0
                        } ?: 0.0

                        val coinId = when (activeChain.chainType) {
                            ChainType.Solana -> CoinPriceService.COIN_ID_SOLANA
                            ChainType.Ethereum -> CoinPriceService.COIN_ID_ETHEREUM
                            ChainType.Polygon -> CoinPriceService.COIN_ID_POLYGON
                        }

                        val priceResult = priceService.getPrices(listOf(coinId), currency)
                        val price = priceResult.getOrNull()?.get(coinId) ?: 0.0
                        _fiatPrice.value = price

                        Timber.i("Balance: $balance ${activeChain.chainType.name} on $currentNetwork, Price: $price $currency")

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