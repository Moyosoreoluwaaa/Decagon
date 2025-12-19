package com.koin.data.portfolio

import com.koin.domain.portfolio.PortfolioBalance
import com.koin.domain.portfolio.PortfolioHolding
import com.koin.domain.portfolio.PortfolioTransaction

fun PortfolioBalanceEntity.toDomain(): PortfolioBalance = PortfolioBalance(
    balance = balance,
    lastUpdated = lastUpdated
)

fun PortfolioHoldingEntity.toDomain(): PortfolioHolding = PortfolioHolding(
    coinId = coinId,
    coinName = coinName,
    coinSymbol = coinSymbol,
    coinImageUrl = coinImageUrl,
    quantity = quantity,
    averagePurchasePrice = averagePurchasePrice,
    totalTransactionFees = totalTransactionFees,
    currentPrice = currentPrice,
)

fun PortfolioTransactionEntity.toDomain(): PortfolioTransaction = PortfolioTransaction(
    id = id,
    coinId = coinId,
    type = type,
    quantity = quantity,
    pricePerCoin = pricePerCoin,
    transactionFee = transactionFee,
    timestamp = timestamp
)