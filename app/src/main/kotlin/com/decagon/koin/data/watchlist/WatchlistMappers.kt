package com.koin.data.watchlist

import com.koin.domain.watchlist.WatchlistItem

fun WatchlistEntity.toDomain(): WatchlistItem = WatchlistItem(
    id = id,
    userId = userId,
    coinId = coinId,
    coinName = coinName,
    coinSymbol = coinSymbol,
    coinImageUrl = coinImageUrl,
    addedAt = addedAt
)

fun WatchlistItem.toEntity(): WatchlistEntity = WatchlistEntity(
    id = id,
    userId = userId,
    coinId = coinId,
    coinName = coinName,
    coinSymbol = coinSymbol,
    coinImageUrl = coinImageUrl,
    addedAt = addedAt
)
