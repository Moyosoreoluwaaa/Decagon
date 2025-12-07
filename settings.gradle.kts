pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // JitPack - Required for GitHub-hosted libraries
        maven {
            url = uri("https://jitpack.io")
        }

        // Solana Maven Repository - Required for Solana Mobile Wallet Adapter
        maven {
            url = uri("https://maven.pkg.github.com/solana-mobile/mobile-wallet-adapter")
        }

        // Maven Central as explicit fallback
        maven {
            url = uri("https://repo1.maven.org/maven2/")
            isAllowInsecureProtocol = false
        }
    }
}

rootProject.name = "Decagon"
include(":app")