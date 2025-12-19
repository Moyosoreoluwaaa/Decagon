package com.decagon.koin.di

import androidx.room.Room
import com.koin.app.notification.NotificationService
import com.koin.data.coin.CoinDatabase
import com.koin.data.coin.CoinGeckoApiService
import com.koin.data.coin.NetworkUtil
import com.koin.data.notification.NotificationRepository
import com.koin.data.portfolio.PortfolioInitializer
import com.koin.data.session.SessionManager
import com.koin.ui.notification.NotificationViewModel
import com.koin.ui.session.SessionViewModel
import com.koin.util.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
// You will need to add these imports and their corresponding Gradle dependencies
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val koinCoreModule = module {

    // Dispatchers (Keep these)
    single(named("IO")) { Dispatchers.IO }
    single(named("Main")) { Dispatchers.Main }
    single(named("Default")) { Dispatchers.Default }

    // **********************************************
    // NEW: NETWORK CONFIGURATION FIXES
    // 1. Define OkHttpClient
    single {
        OkHttpClient.Builder()
            // Add a timeout for better performance/reliability
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            // You would add logging or auth interceptors here
            // .addInterceptor(get<LoggingInterceptor>())
            .build()
    }

    // 2. Define Retrofit (now injects OkHttpClient using get())
    single {
        Retrofit.Builder()
            // IMPORTANT: Replace this with your actual base URL
            .baseUrl("https://api.coingecko.com/api/v3/")
            .client(get()) // Inject the OkHttpClient defined above
            .addConverterFactory(GsonConverterFactory.create()) // Assuming you use Gson
            .build()
    }

    // 3. Define CoinGeckoApiService (now successfully gets the Retrofit instance)
    single<CoinGeckoApiService> {
        get<Retrofit>().create(CoinGeckoApiService::class.java)
    }
    // **********************************************

    single { NetworkUtil(get()) }
    single { PortfolioInitializer(get()) }
    single { NotificationRepository(get()) }
    single {
        NotificationService(get())
    }

    single {
        SessionManager(get())
    }

    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            CoinDatabase::class.java,
            CoinDatabase.DATABASE_NAME
        )
            .build()
    }

    viewModel {
        SessionViewModel(get())
    }
 viewModel {
     NotificationViewModel(get())
    }


    single { get<CoinDatabase>().coinDao() }
    single { get<CoinDatabase>().portfolioDao() }
    single { get<CoinDatabase>().userDao() }
    single { get<CoinDatabase>().watchlistDao() }
    single { get<CoinDatabase>().notificationDao() }
    single { NetworkMonitor(get()) }
}