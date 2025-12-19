package com.koin.domain.portfolio

import com.koin.domain.model.Coin
import kotlinx.coroutines.flow.Flow

class GetPortfolioUseCase (
    private val repository: PortfolioRepository
) {
    operator fun invoke(): Flow<List<PortfolioHolding>> = repository.getHoldings()
}


class BuyCoinUseCase (
    private val repository: PortfolioRepository
) {
    suspend operator fun invoke(
        coin: Coin,
        amount: Double
    ): Unit = repository.buyCoin(coin, amount)
}


class SellCoinUseCase (
    private val repository: PortfolioRepository
) {
    suspend operator fun invoke(
        coinId: String,
        quantity: Double,
        pricePerCoin: Double
    ): Unit = repository.sellCoin(coinId, quantity, pricePerCoin)
}


class RefreshPortfolioUseCase (
    private val repository: PortfolioRepository
) {
    suspend operator fun invoke() = repository.refreshPortfolio()
}


class GetTransactionHistoryUseCase (
    private val repository: PortfolioRepository
) {
    suspend operator fun invoke(): List<Transaction> = repository.getTransactionHistory()
}


class GetBalanceUseCase (
    private val repository: PortfolioRepository
) {
    operator fun invoke(): Flow<PortfolioBalance?> = repository.getBalance()
}


class ResetPortfolioUseCase (
    private val repository: PortfolioRepository
) {
    suspend operator fun invoke() = repository.resetPortfolio()
}
