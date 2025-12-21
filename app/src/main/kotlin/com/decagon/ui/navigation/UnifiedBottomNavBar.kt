package com.decagon.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun UnifiedBottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = currentRoute?.contains(item.route::class.simpleName ?: "") == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

private enum class BottomNavItem(
    val route: UnifiedRoute,
    val icon: ImageVector,
    val label: String
) {
    WALLET(UnifiedRoute.Wallet, Icons.Rounded.AccountBalanceWallet, "Wallet"),
    SWAP(UnifiedRoute.Swap, Icons.Rounded.SwapHoriz, "Swap"),
    DISCOVER(UnifiedRoute.Discover, Icons.Rounded.Explore, "Discover"),
    SETTINGS(UnifiedRoute.Settings, Icons.Rounded.Settings, "Settings")  // ← CHANGED
}