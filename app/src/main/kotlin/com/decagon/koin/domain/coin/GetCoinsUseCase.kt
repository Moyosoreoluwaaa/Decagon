package com.koin.domain.coin

import com.koin.domain.model.Coin
import kotlinx.coroutines.flow.Flow

class GetCoinsUseCase (
    private val repository: CoinRepository
) {
    operator fun invoke(): Flow<Result<List<Coin>>> = repository.getAllCoins()
} 