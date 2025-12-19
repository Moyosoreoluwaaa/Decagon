package com.decagon.koin.di

import com.koin.data.coin.CoinRepositoryImpl
import com.koin.domain.coin.CoinRepository
import org.koin.dsl.module

val coinModule = module {
    single<CoinRepository> {
        CoinRepositoryImpl(get(), get(), get())
    }
}