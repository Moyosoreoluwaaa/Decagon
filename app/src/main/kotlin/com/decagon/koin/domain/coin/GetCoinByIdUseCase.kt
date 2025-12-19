package com.koin.domain.coin

import com.koin.domain.model.Coin
import kotlinx.coroutines.flow.Flow

class GetCoinByIdUseCase (
    private val repository: CoinRepository
) {
    operator fun invoke(coinId: String): Flow<Result<Coin?>> = repository.getCoinById(coinId)
} 