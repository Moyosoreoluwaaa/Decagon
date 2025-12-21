package com.wallet.core.testing//package com.octane.core.testing
//
//import com.octane.core.monitoring.AnalyticsLogger
//import com.octane.core.monitoring.FakeAnalyticsLogger
//import com.octane.core.network.FakeNetworkMonitor
//import com.octane.core.network.NetworkMonitor
//import org.koin.core.qualifier.named
//import org.koin.dsl.module
//
///**
// * Test module for Core layer.
// * Provides fakes and test dispatchers.
// */
//val testCoreModule = module {
//
//    // Test Dispatchers
//    val testDispatcher: TestDispatcher = StandardTestDispatcher()
//    single(named("IO")) { testDispatcher }
//    single(named("Main")) { testDispatcher }
//    single(named("Default")) { testDispatcher }
//
//    // Fakes
//    single<NetworkMonitor> { FakeNetworkMonitor() }
//    single<AnalyticsLogger> { FakeAnalyticsLogger() }
//
//    // Real implementations for testing
//    // (Add as needed)
//}