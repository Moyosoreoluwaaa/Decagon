package com.koin.data.portfolio

import com.koin.domain.portfolio.PortfolioRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class PortfolioInitializer (
    private val portfolioRepository: PortfolioRepository
) {
    fun initialize() {
        runBlocking {
            val balance = portfolioRepository.getBalance().first()
            if (balance == null) {
                portfolioRepository.insertInitialBalance(10000.0) // Initial balance of 10,000
            }
        }
    }
}