package com.octane.wallet.domain.di

import com.octane.wallet.domain.usecases.asset.ObservePortfolioUseCase
import com.octane.wallet.domain.usecases.asset.RefreshAssetsUseCase
import com.octane.wallet.domain.usecases.asset.ToggleAssetVisibilityUseCase
import com.octane.wallet.domain.usecases.discover.ObserveDAppsByCategoryUseCase
import com.octane.wallet.domain.usecases.discover.ObserveDAppsUseCase
import com.octane.wallet.domain.usecases.discover.ObservePerpsUseCase
import com.octane.wallet.domain.usecases.discover.ObserveTokensUseCase
import com.octane.wallet.domain.usecases.discover.ObserveTrendingTokensUseCase
import com.octane.wallet.domain.usecases.discover.RefreshDAppsUseCase
import com.octane.wallet.domain.usecases.discover.RefreshPerpsUseCase
import com.octane.wallet.domain.usecases.discover.RefreshTokensUseCase
import com.octane.wallet.domain.usecases.discover.SearchDAppsUseCase
import com.octane.wallet.domain.usecases.discover.SearchPerpsUseCase
import com.octane.wallet.domain.usecases.discover.SearchTokensUseCase
import com.octane.wallet.domain.usecases.network.ObserveNetworkStatusUseCase
import com.octane.wallet.domain.usecases.network.ObserveRpcStatusUseCase
import com.octane.wallet.domain.usecases.network.SwitchRpcEndpointUseCase
import com.octane.wallet.domain.usecases.preference.ObserveCurrencyPreferenceUseCase
import com.octane.wallet.domain.usecases.preference.TogglePrivacyModeUseCase
import com.octane.wallet.domain.usecases.preference.UpdateCurrencyPreferenceUseCase
import com.wallet.domain.usecases.security.AuthenticateWithBiometricsUseCase
import com.octane.wallet.domain.usecases.security.CheckBiometricAvailabilityUseCase
import com.octane.wallet.domain.usecases.security.ObserveApprovalsUseCase
import com.octane.wallet.domain.usecases.security.RevokeApprovalUseCase
import com.octane.wallet.domain.usecases.security.ValidateSolanaAddressUseCase
import com.octane.wallet.domain.usecases.staking.ClaimRewardsUseCase
import com.octane.wallet.domain.usecases.staking.ObserveStakingPositionsUseCase
import com.octane.wallet.domain.usecases.staking.StakeTokensUseCase
import com.octane.wallet.domain.usecases.staking.UnstakeTokensUseCase
import com.octane.wallet.domain.usecases.transaction.EstimateTransactionFeeUseCase
import com.wallet.domain.usecases.transaction.MonitorPendingTransactionsUseCase
import com.wallet.domain.usecases.transaction.ObserveTransactionHistoryUseCase
import com.octane.wallet.domain.usecases.transaction.SendSolUseCase
import com.octane.wallet.domain.usecases.transaction.SwapTokensUseCase
import com.wallet.domain.usecases.wallet.CreateWalletUseCase
import com.wallet.domain.usecases.wallet.DeleteWalletUseCase
import com.octane.wallet.domain.usecases.wallet.ExportSeedPhraseUseCase
import com.wallet.domain.usecases.wallet.ImportWalletUseCase
import com.octane.wallet.domain.usecases.wallet.ObserveActiveWalletUseCase
import com.wallet.domain.usecases.wallet.ObserveWalletsUseCase
import com.wallet.domain.usecases.wallet.SetActiveWalletUseCase
import com.octane.wallet.domain.usecases.wallet.SwitchActiveWalletUseCase
import com.octane.wallet.domain.usecases.wallet.UpdateWalletMetadataUseCase
import com.wallet.domain.usecases.wallet.UpdateWalletUseCase
import com.octane.wallet.domain.usecases.wallet.ValidateSeedPhraseUseCase
import org.koin.dsl.module

val domainModule = module {

    // Wallet Use Cases
    factory { CreateWalletUseCase(get(), get(), get()) }
    factory { ImportWalletUseCase(get(), get(), get()) }
    factory { SetActiveWalletUseCase(get()) }
    factory { SwitchActiveWalletUseCase(get(), get()) }
    factory { ObserveWalletsUseCase(get()) }
    factory { ObserveActiveWalletUseCase(get()) }
    factory { DeleteWalletUseCase(get(), get()) }
    factory { ClaimRewardsUseCase(get(), get()) }
    factory { UpdateWalletMetadataUseCase(get()) }
    factory { ValidateSeedPhraseUseCase() }
    factory { ExportSeedPhraseUseCase(get(), get(), get()) }

    // Asset Use Cases
    factory { ObservePortfolioUseCase(get(), get()) }
    factory { RefreshAssetsUseCase(get(), get(), get()) }
    factory { ToggleAssetVisibilityUseCase(get()) }


    // Transaction Use Cases
    factory { SendSolUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { EstimateTransactionFeeUseCase(get(), get()) }
    factory { ObserveTransactionHistoryUseCase(get(), get()) }
    factory { MonitorPendingTransactionsUseCase(get(), get()) }
    factory { UpdateWalletUseCase(get()) }
    factory { ValidateSolanaAddressUseCase() }
    factory { SwapTokensUseCase(get(), get(), get(), get(), get()) }

    // PRICE USE CASES
    factory { ObserveRpcStatusUseCase(get()) }

    // Stacking Use Case
    factory { StakeTokensUseCase(get(), get(), get()) }
    factory { UnstakeTokensUseCase(get(), get()) }
    factory { ObserveStakingPositionsUseCase(get(), get()) }

    // Send Use Case
    factory { EstimateTransactionFeeUseCase(get(),get()) }


    // Security Use Cases
    factory { CheckBiometricAvailabilityUseCase(get()) }
    factory { AuthenticateWithBiometricsUseCase(get()) }

    // Network Use Cases
    factory { ObserveNetworkStatusUseCase(get()) }
    factory { SwitchRpcEndpointUseCase(get()) }

    // Preferences Use Cases
    factory { ObserveCurrencyPreferenceUseCase(get()) }
    factory { UpdateCurrencyPreferenceUseCase(get()) }
    factory { TogglePrivacyModeUseCase(get()) }

    // Approval use case
    factory { RevokeApprovalUseCase(get(), get()) }
    factory { ObserveApprovalsUseCase(get(), get()) }

    // Discover Use Cases - Tokens
    factory { ObserveTokensUseCase(get()) }
    factory { ObserveTrendingTokensUseCase(get()) }
    factory { SearchTokensUseCase(get()) }
    factory { RefreshTokensUseCase(get()) }

// Discover Use Cases - Perps
    factory { ObservePerpsUseCase(get()) }
    factory { SearchPerpsUseCase(get()) }
    factory { RefreshPerpsUseCase(get()) }

// Discover Use Cases - dApps
    factory { ObserveDAppsUseCase(get()) }
    factory { ObserveDAppsByCategoryUseCase(get()) }
    factory { SearchDAppsUseCase(get()) }
    factory { RefreshDAppsUseCase(get()) }
}