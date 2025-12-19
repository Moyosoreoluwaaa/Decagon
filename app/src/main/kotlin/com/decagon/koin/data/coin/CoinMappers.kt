package com.koin.data.coin

import com.koin.domain.model.Coin

fun CoinEntity.toCoin(): Coin {
    return Coin(
        id = this.id,
        name = this.name,
        symbol = this.symbol,
        imageUrl = this.imageUrl,
        currentPrice = this.currentPrice,
        marketCap = this.marketCap,
        marketCapRank = this.marketCapRank,
        priceChange24h = this.priceChange24h,
        priceChangePercentage24h = this.priceChangePercentage24h,
        priceChangePercentage1h = this.priceChangePercentage1h,
        priceChangePercentage7d = this.priceChangePercentage7d,
        priceChangePercentage30d = this.priceChangePercentage30d,
        sparklineData = this.sparklineData,
        high24h = this.high24h,
        low24h = this.low24h,
        totalVolume = this.totalVolume,
        circulatingSupply = this.circulatingSupply,
        totalSupply = this.totalSupply,
        maxSupply = this.maxSupply,
        ath = this.ath,
        athDate = this.athDate,
        atl = this.atl,
        atlDate = this.atlDate
    )
}