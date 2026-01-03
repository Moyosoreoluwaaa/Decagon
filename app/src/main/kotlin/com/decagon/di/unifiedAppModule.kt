package com.decagon.di

import org.koin.dsl.module

val unifiedAppModule = module {
    includes(
        coreModule,
        dataModule,
        domainModule,
        viewModelModule
    )
}