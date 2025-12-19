package com.koin.domain.portfolio

import java.text.NumberFormat
import java.util.Locale

data class Portfolio(
    val balance: Double,
    val holdings: List<PortfolioHolding>,
    val transactions: List<Transaction>
) {
    // Core portfolio calculations
    val totalValue: Double
        get() = holdings.sumOf { it.currentValue }
    
    val totalInvested: Double
        get() = holdings.sumOf { it.totalCostBasis }
    
    val unrealizedPnL: Double
        get() = totalValue - totalInvested
    
    val unrealizedPnLPercentage: Double
        get() = if (totalInvested > 0) (unrealizedPnL / totalInvested) * 100 else 0.0
    
    val totalPortfolioValue: Double
        get() = balance + totalValue
    
    val portfolioPerformancePercentage: Double
        get() = if (INITIAL_BALANCE > 0) ((totalPortfolioValue - INITIAL_BALANCE) / INITIAL_BALANCE) * 100 else 0.0
    
    // Formatted properties
    val formattedBalance: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(balance)
    
    val formattedTotalValue: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(totalValue)
    
    val formattedTotalPortfolioValue: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(totalPortfolioValue)
    
    val formattedUnrealizedPnL: String
        get() = "${if (unrealizedPnL >= 0) "+" else ""}${NumberFormat.getCurrencyInstance(Locale.US).format(unrealizedPnL)}"
    
    val formattedUnrealizedPnLPercentage: String
        get() = "${if (unrealizedPnLPercentage >= 0) "+" else ""}${String.format(Locale.US, "%.2f", unrealizedPnLPercentage)}%"
    
    val formattedPortfolioPerformancePercentage: String
        get() = "${if (portfolioPerformancePercentage >= 0) "+" else ""}${String.format(Locale.US, "%.2f", portfolioPerformancePercentage)}%"
    
    val isPositivePerformance: Boolean
        get() = portfolioPerformancePercentage >= 0
    
    val isPositiveUnrealizedPnL: Boolean
        get() = unrealizedPnL >= 0
    
    // Portfolio allocation calculations
    fun getHoldingAllocation(coinId: String): Double {
        val holding = holdings.find { it.coinId == coinId }
        return if (holding != null && totalValue > 0) {
            (holding.currentValue / totalValue) * 100
        } else 0.0
    }
    
    fun getFormattedHoldingAllocation(coinId: String): String {
        return "${String.format(Locale.US, "%.1f", getHoldingAllocation(coinId))}%"
    }
    
    companion object {
        const val INITIAL_BALANCE = 10000.0
        
        fun empty(): Portfolio {
            return Portfolio(
                balance = INITIAL_BALANCE,
                holdings = emptyList(),
                transactions = emptyList()
            )
        }
    }
}

data class PortfolioHolding(
    val coinId: String,
    val coinName: String,
    val coinSymbol: String,
    val coinImageUrl: String,
    val quantity: Double,
    val averagePurchasePrice: Double,
    val currentPrice: Double,
    val totalTransactionFees: Double = 0.0
) {
    val currentValue: Double
        get() = quantity * currentPrice
    
    val totalCostBasis: Double
        get() = (quantity * averagePurchasePrice) + totalTransactionFees
    
    val unrealizedPnL: Double
        get() = currentValue - totalCostBasis
    
    val unrealizedPnLPercentage: Double
        get() = if (totalCostBasis > 0) (unrealizedPnL / totalCostBasis) * 100 else 0.0
    
    // Formatted properties
    val formattedQuantity: String
        get() = String.format(Locale.US, "%.6f", quantity)
    
    val formattedCurrentValue: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(currentValue)
    
    val formattedAveragePurchasePrice: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(averagePurchasePrice)
    
    val formattedCurrentPrice: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(currentPrice)
    
    val formattedUnrealizedPnL: String
        get() = "${if (unrealizedPnL >= 0) "+" else ""}${NumberFormat.getCurrencyInstance(Locale.US).format(unrealizedPnL)}"
    
    val formattedUnrealizedPnLPercentage: String
        get() = "${if (unrealizedPnLPercentage >= 0) "+" else ""}${String.format(Locale.US, "%.2f", unrealizedPnLPercentage)}%"
    
    val formattedTotalCostBasis: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(totalCostBasis)
    
    val isPositiveUnrealizedPnL: Boolean
        get() = unrealizedPnL >= 0
}

data class Transaction(
    val id: String,
    val coinId: String,
    val type: TransactionType,
    val quantity: Double,
    val pricePerCoin: Double,
    val transactionFee: Double,
    val timestamp: Long
) {
    val totalAmount: Double
        get() = (quantity * pricePerCoin) + transactionFee
    
    val formattedTotalAmount: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(totalAmount)
    
    val formattedPricePerCoin: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(pricePerCoin)
    
    val formattedQuantity: String
        get() = String.format(Locale.US, "%.6f", quantity)
    
    val formattedTransactionFee: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(transactionFee)
}

enum class TransactionType {
    BUY,
    SELL
}
