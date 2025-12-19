package com.decagon.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.decagon.ui.navigation.DecagonRoute

@Composable
fun DecagonBottomNavBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem(
            route = DecagonRoute.Portfolio,
            icon = Icons.Default.AccountBalanceWallet,
            label = "Portfolio"
        ),
        BottomNavItem(
            route = DecagonRoute.Swap,
            icon = Icons.Default.SwapHoriz,
            label = "Swap"
        ),
        BottomNavItem(
            route = DecagonRoute.Activity,
            icon = Icons.Default.History,
            label = "Activity"
        )
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            val routeName = item.route::class.simpleName ?: ""
            val selected = currentDestination?.hierarchy?.any { 
                it.route?.contains(routeName) == true 
            } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            // Pop up to Portfolio (the start of bottom nav)
                            popUpTo(DecagonRoute.Portfolio) {
                                saveState = true
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(item.icon, contentDescription = item.label)
                },
                label = { Text(item.label) }
            )
        }
    }
}

private data class BottomNavItem(
    val route: DecagonRoute,
    val icon: ImageVector,
    val label: String
)