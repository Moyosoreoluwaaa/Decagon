plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ktorfit)
    id("kotlin-parcelize")
}

android {
    namespace = "com.decagon"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.decagon"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlin.get()
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {

    // Web-View Core (CRITICAL for browser app)
    implementation(libs.androidx.webkit)

    // Core Android & Desugaring
    coreLibraryDesugaring(libs.android.desugarJdkLibs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose (UI)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.wear.compose.navigation)
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.compose.animation)
    implementation(libs.navigation.compose) // Keeping this for simple screen navigation
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.accompanist.permissions)

    // Room (Simplified, just for local data caching/history)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging) // Useful for Paging large history/bookmarks
    ksp(libs.androidx.room.compiler)

    // Dependency Injection
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.compose)

    // Networking, Data Scraping, Downloads
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.okhttp) // Use OkHttp for the engine
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Ktorfit (API client generation)
    implementation(libs.ktorfit.lib)
    implementation(libs.ktor.client.logging)
    ksp(libs.ktorfit.ksp)

    // Solana (0.2)
    implementation(libs.sol4k)

    // Specific Utilities for browser tasks
    implementation(libs.network.okhttp) // Direct OkHttp usage
    implementation(libs.parsing.jsoup) // HTML parsing/scraping
    implementation(libs.download.manager.fetch2) // Download manager
    implementation(libs.download.manager.fetch2okhttp)
    implementation(libs.video.extraction.junkfood.library) // Video extraction (like for embedded media)
    implementation(libs.video.extraction.junkfood.ffmpeg)

    // Utilities & Logging
    implementation(libs.napier)
    implementation(libs.timber)
    debugImplementation(libs.timber)

    // Image Loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.kamel.image)

    // Immutable Collections
    implementation(libs.kotlinx.collections.immutable)

    // AndroidX Additions
    implementation(libs.androidx.paging.compose) // For showing history/bookmarks in lists
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.work.runtime.ktx) // For background tasks like pre-fetching

    // Security & Compliance
    implementation(libs.scottyab.rootbeer.lib)  // Root/jailbreak detection
    implementation(libs.androidx.security.crypto) // Encrypted storage
    implementation(libs.kotlin.bip39)
    implementation(libs.bitcoinj.core)
    implementation(libs.eddsa)
    implementation(libs.androidx.biometric)

    // Retrofit (Added as requested)
    implementation(libs.squareup.retrofit.core)
    implementation(libs.squareup.retrofit.converter.gson)

    // QR Code Scanner (Added as requested)
    implementation(libs.zxing.android.embedded)

    // Preferences Storage
    implementation(libs.androidx.datastore.preferences)

    // Testing (Simplified)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.benchmark.macro) // For performance testing

    // Testing (Advanced)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
//    debugImplementation(libs.leakcanary.android)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.ui.tooling.preview)

}