package com.decagon.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.decagon.ui.screen.onboarding.DecagonOnboardingScreen
import com.decagon.ui.screen.wallet.DecagonWalletScreen

@Composable
fun DecagonNavGraph() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "onboarding"
    ) {
        composable("onboarding") {
            DecagonOnboardingScreen(
                onWalletCreated = {
                    navController.navigate("wallet") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        
        composable("wallet") {
            DecagonWalletScreen()
        }
    }
}