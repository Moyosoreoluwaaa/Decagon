package com.koin.domain.coin

class RefreshCoinsUseCase (
    private val repository: CoinRepository
) {
    suspend operator fun invoke() = repository.refreshCoins()
} 