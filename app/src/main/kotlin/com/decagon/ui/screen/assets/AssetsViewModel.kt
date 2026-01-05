package com.decagon.ui.screen.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.data.local.dao.TokenBalanceDao
import com.decagon.data.mapper.toDomain
import com.decagon.data.repository.TokenReceiveManager
import com.decagon.domain.model.TokenBalance
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class AssetsViewModel(
    private val walletRepository: DecagonWalletRepository,
    private val tokenBalanceDao: TokenBalanceDao,
    private val tokenReceiveManager: TokenReceiveManager
) : ViewModel() {
    
    val activeWallet = walletRepository.getActiveWalletCached()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    val tokenBalances: StateFlow<List<TokenBalance>> = activeWallet
        .filterNotNull()
        .flatMapLatest { wallet ->
            tokenBalanceDao.getByWallet(wallet.address)
                .map { entities -> entities.map { it.toDomain() } }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val totalPortfolioValue: StateFlow<Double> = tokenBalances
        .map { balances -> balances.sumOf { it.valueUsd } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    
    val portfolioChange24h: StateFlow<Double> = tokenBalances
        .map { balances ->
            val totalCurrent = balances.sumOf { it.valueUsd }
            val totalPrevious = balances.sumOf { balance ->
                val change = balance.change24h ?: 0.0
                balance.valueUsd / (1 + change / 100)
            }
            
            if (totalPrevious > 0) {
                ((totalCurrent - totalPrevious) / totalPrevious) * 100
            } else 0.0
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    
    init {
        refresh()
    }
    
    fun refresh() {
        val wallet = activeWallet.value ?: return
        
        viewModelScope.launch {
            _isRefreshing.value = true
            
            tokenReceiveManager.discoverNewTokens(wallet.address)
                .onSuccess {
                    Timber.i("✅ Assets refreshed")
                }
                .onFailure {
                    Timber.e(it, "❌ Asset refresh failed")
                }
            
            delay(500)
            _isRefreshing.value = false
        }
    }
}