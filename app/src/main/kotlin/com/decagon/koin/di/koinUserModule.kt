package com.decagon.koin.di

import com.koin.data.user.LocalUserRepositoryImpl
import com.koin.domain.user.UserRepository
import com.koin.ui.auth.AuthViewModel
import com.koin.ui.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val koinUserModule = module {
    single<UserRepository> {
        LocalUserRepositoryImpl(get())
    }

    viewModel {
        ProfileViewModel(get(), get(), get())
    }
    viewModel {
        AuthViewModel(get(), get())
    }
}