package com.decagon.ui.screen.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.network.NetworkManager
import com.decagon.core.network.RpcClientFactory
import com.decagon.core.util.LoadingState
import com.decagon.data.local.dao.TokenBalanceDao
import com.decagon.data.mapper.toDomain
import com.decagon.data.remote.api.CoinPriceService
import com.decagon.data.repository.TokenReceiveManager
import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.model.TokenBalance
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class DecagonWalletViewModel(
    private val repository: DecagonWalletRepository,
    private val rpcFactory: RpcClientFactory,
    private val networkManager: NetworkManager,
    private val priceService: CoinPriceService,
    private val tokenBalanceDao: TokenBalanceDao,
    private val tokenReceiveManager: TokenReceiveManager,
) : ViewModel() {

    private val _selectedCurrency = MutableStateFlow("usd")
    val selectedCurrency = _selectedCurrency.asStateFlow()

    private val _fiatPrice = MutableStateFlow(0.0)
    val fiatPrice = _fiatPrice.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow("1D")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    private val _chartData = MutableStateFlow<LoadingState<List<Double>>>(LoadingState.Idle)
    val chartData: StateFlow<LoadingState<List<Double>>> = _chartData.asStateFlow()

    // ✅ INSTANT CACHED WALLET (No more LoadingState wrapper)
    val walletState: StateFlow<DecagonWallet?> =
        repository.getActiveWalletCached()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    val allWallets: StateFlow<List<DecagonWallet>> =
        repository.getAllWallets()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // ✅ NEW: Token balances (reactive from Room)
    val tokenBalances: StateFlow<List<TokenBalance>> = walletState
        .filterNotNull()
        .flatMapLatest { wallet ->
            tokenBalanceDao.getByWallet(wallet.address)
                .map { entities -> entities.map { it.toDomain() } }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ✅ NEW: Refresh indicator
    private val _isRefreshingBalances = MutableStateFlow(false)
    val isRefreshingBalances = _isRefreshingBalances.asStateFlow()

    init {
        Timber.d("DecagonWalletViewModel initialized.")
        // ✅ Background refresh on init
        viewModelScope.launch {
            walletState.filterNotNull().first().let { wallet ->
                repository.refreshBalance(wallet.id)
                fetchFiatPrice(wallet)
            }
        }
    }

    // ✅ NEW: Manual refresh
    fun refreshTokenBalances() {
        val wallet = walletState.value ?: return

        viewModelScope.launch {
            _isRefreshingBalances.value = true

            tokenReceiveManager.discoverNewTokens(wallet.address)
                .onSuccess { balances ->
                    Timber.i("✅ Token balances refreshed: ${balances.size} tokens")
                }
                .onFailure { error ->
                    Timber.e(error, "❌ Failed to refresh token balances")
                }

            delay(500) // Show indicator briefly
            _isRefreshingBalances.value = false
        }
    }

    private suspend fun fetchFiatPrice(wallet: DecagonWallet) {
        val coinId = wallet.activeChain?.chainType?.symbol?.lowercase() ?: "solana"
        val priceResult = priceService.getPrices(listOf(coinId), _selectedCurrency.value)
        _fiatPrice.value = priceResult.getOrNull()?.get(coinId) ?: 0.0
    }

    fun refresh() {
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                walletState.value?.let { wallet ->
                    repository.refreshBalance(wallet.id)
                    fetchFiatPrice(wallet)
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun switchWallet(walletId: String) {
        Timber.i("Switching to wallet: $walletId")
        viewModelScope.launch {
            repository.setActiveWallet(walletId)
            repository.refreshBalance(walletId)
        }
    }

    fun onTimeframeSelected(timeframe: String) {
        if (_selectedTimeframe.value == timeframe) return
        _selectedTimeframe.value = timeframe
        fetchChartData(timeframe)
    }

    private fun fetchChartData(timeframe: String) {
        viewModelScope.launch {
            _chartData.value = LoadingState.Loading
            try {
                delay(1000)
                val mockData = List(50) { 1000.0 + (Math.random() * 100 - 50) }
                _chartData.value = LoadingState.Success(mockData)
            } catch (e: Exception) {
                _chartData.value = LoadingState.Error(e, "Failed to load chart")
            }
        }
    }
}