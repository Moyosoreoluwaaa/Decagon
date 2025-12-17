package com.octane.wallet.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.octane.wallet.data.local.datastore.DAppPreferencesStore
import com.octane.wallet.data.local.datastore.DAppPreferencesStoreImpl
import com.octane.wallet.data.local.datastore.UserPreferencesStore
import com.octane.wallet.data.local.datastore.UserPreferencesStoreImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * DataStore extension for Context
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "octane_preferences"
)

/**
 * ✅ Preferences Module: Fixed Context injection
 */
val preferencesModule = module {

    // Provide DataStore instance
    single<DataStore<Preferences>> {
        androidContext().dataStore
    }

    // Provide UserPreferencesStore implementation
    single<UserPreferencesStore> {
        UserPreferencesStoreImpl(context = androidContext()) // ✅ Use androidContext()
    }

    single<DAppPreferencesStore> {
        DAppPreferencesStoreImpl(context = androidContext())
    }
}