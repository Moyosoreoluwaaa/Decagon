package com.decagon.di

import com.decagon.data.repository.OnRampRepositoryImpl
import com.decagon.domain.repository.OnRampRepository
import com.decagon.ui.screen.onramp.DecagonOnRampViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val decagonOnRampModule = module {

    // Repository
    single<OnRampRepository> {
        OnRampRepositoryImpl(
            onRampDao = get()
        )
    }

    // ViewModel
    viewModel {
        DecagonOnRampViewModel(
            onRampRepository = get(),
            rpcClient = get()
        )
    }
}