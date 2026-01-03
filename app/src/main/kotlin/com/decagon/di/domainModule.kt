package com.decagon.di

import com.decagon.domain.provider.OnRampProviderFactory
import com.decagon.domain.usecase.DecagonCreateWalletUseCase
import com.decagon.domain.usecase.DecagonImportWalletUseCase
import com.decagon.domain.usecase.DecagonSendTokenUseCase
import com.decagon.domain.usecase.ExecuteSwapUseCase
import com.decagon.domain.usecase.GetSwapHistoryUseCase
import com.decagon.domain.usecase.GetSwapQuoteUseCase
import com.decagon.domain.usecase.GetTokenBalancesUseCase
import com.decagon.domain.usecase.ObserveAllPerpsUseCase
import com.decagon.domain.usecase.ValidateTokenSecurityUseCase
import com.decagon.domain.usecase.asset.ObservePortfolioUseCase
import com.decagon.domain.usecase.asset.RefreshAssetsUseCase
import com.decagon.domain.usecase.asset.ToggleAssetVisibilityUseCase
import com.decagon.domain.usecase.discover.ObserveAllTokensUseCase
import com.decagon.domain.usecase.discover.ObserveDAppsByCategoryUseCase
import com.decagon.domain.usecase.discover.ObserveDAppsUseCase
import com.decagon.domain.usecase.discover.ObservePerpsUseCase
import com.decagon.domain.usecase.discover.ObserveTokensUseCase
import com.decagon.domain.usecase.discover.ObserveTrendingTokensUseCase
import com.decagon.domain.usecase.discover.RefreshDAppsUseCase
import com.decagon.domain.usecase.discover.RefreshPerpsUseCase
import com.decagon.domain.usecase.discover.RefreshTokensUseCase
import com.decagon.domain.usecase.discover.SearchDAppsUseCase
import com.decagon.domain.usecase.discover.SearchPerpsUseCase
import com.decagon.domain.usecase.discover.SearchTokensUseCase
import com.decagon.domain.usecase.network.ObserveNetworkStatusUseCase
import com.decagon.domain.usecase.security.AuthenticateWithBiometricsUseCase
import com.decagon.domain.usecase.security.CheckBiometricAvailabilityUseCase
import com.decagon.domain.usecase.security.ObserveApprovalsUseCase
import com.decagon.domain.usecase.security.RevokeApprovalUseCase
import com.decagon.domain.usecase.security.ValidateSolanaAddressUseCase
import com.decagon.domain.usecase.staking.ClaimRewardsUseCase
import com.decagon.domain.usecase.staking.StakeTokensUseCase
import com.decagon.domain.usecase.staking.UnstakeTokensUseCase
import com.decagon.domain.usecase.wallet.ObserveActiveWalletUseCase
import com.decagon.domain.usecase.wallet.ObserveWalletsUseCase
import com.decagon.domain.usecase.wallet.SetActiveWalletUseCase
import com.decagon.domain.usecase.wallet.SwitchActiveWalletUseCase
import org.koin.dsl.module

val domainModule = module {
    // Wallet & Onboarding
    factory { DecagonCreateWalletUseCase(get(), get()) }
    factory { DecagonImportWalletUseCase(get(), get()) }
    factory { ObserveActiveWalletUseCase(get()) }
    factory { ObserveWalletsUseCase(get()) }
    factory { SetActiveWalletUseCase(get()) }
    factory { SwitchActiveWalletUseCase(get(), get()) }

    // Assets & Transactions
    factory { ObservePortfolioUseCase(get(), get()) }
    factory { RefreshAssetsUseCase(get(), get(), get()) }
    factory { ToggleAssetVisibilityUseCase(get()) }
    factory { DecagonSendTokenUseCase(get(), get(), get(), get(), get()) }
    factory { ValidateSolanaAddressUseCase() }

    // Swap Use Cases
    factory { GetSwapQuoteUseCase(get()) }
    factory { ExecuteSwapUseCase(get(), get(), get(), get(), get()) }
    factory { GetTokenBalancesUseCase(get()) }
    factory { ValidateTokenSecurityUseCase(get()) }
    factory { GetSwapHistoryUseCase(get()) }

    // Discover Use Cases
    factory { ObserveTokensUseCase(get()) }
    factory { ObserveTrendingTokensUseCase(get()) }
    factory { RefreshTokensUseCase(get()) }
    factory { ObserveAllTokensUseCase(get()) }
    factory { ObservePerpsUseCase(get()) }
    factory { ObserveAllPerpsUseCase(get()) }
    factory { RefreshPerpsUseCase(get()) }
    factory { SearchPerpsUseCase(get()) }
    factory { SearchTokensUseCase(get()) }
    factory { ObserveDAppsUseCase(get()) }
    factory { ObserveDAppsByCategoryUseCase(get()) }
    factory { RefreshDAppsUseCase(get()) }
    factory { SearchDAppsUseCase(get()) }


    // Staking, Security & Network
    factory { StakeTokensUseCase(get(), get(), get()) }
    factory { UnstakeTokensUseCase(get(), get()) }
    factory { ClaimRewardsUseCase(get(), get()) }
    factory { AuthenticateWithBiometricsUseCase(get()) }
    factory { CheckBiometricAvailabilityUseCase(get()) }
    factory { ObserveApprovalsUseCase(get(), get()) }
    factory { RevokeApprovalUseCase(get(), get()) }
    factory { ObserveNetworkStatusUseCase(get()) }

    // Factories
    single { OnRampProviderFactory() }
}