package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.wallet.presentation.navigation.AppRoute
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.Dimensions

/**
 * Type-safe bottom navigation bar.
 * Automatically highlights correct tab based on current route.
 */
@Composable
fun BottomNavBar(
    navController: NavController,
    onBackToHome: (() -> Unit)? = null, // ✅ Optional callback for non-standard back
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(AppColors.Background)
            .drawBehind {
                // Metallic top border
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color(0xFF666666), Color.Black)
                    ),
                    topLeft = Offset.Zero,
                    size = size.copy(height = 1.dp.toPx())
                )
            }
            .padding(horizontal = Dimensions.Spacing.large),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home Tab
        NavIcon(
            label = "Home",
            icon = Icons.Rounded.Home,
            isSelected = currentDestination?.hierarchy?.any {
                it.hasRoute(AppRoute.Home::class)
            } == true,
            onClick = {
                // ✅ Special handling for WalletScreen back navigation
                if (onBackToHome != null && currentDestination?.hasRoute(AppRoute.Home::class) == true) {
                    onBackToHome()
                } else {
                    navController.navigate(AppRoute.Home) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )

        // Swap Tab
        NavIcon(
            label = "Swap",
            icon = Icons.Rounded.SwapHoriz,
            isSelected = currentDestination?.hierarchy?.any {
                it.hasRoute(AppRoute.Swap::class)
            } == true,
            onClick = {
                navController.navigate(AppRoute.Swap) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        // Search Tab
        NavIcon(
            label = "Search",
            icon = Icons.Rounded.Search,
            isSelected = currentDestination?.hierarchy?.any {
                it.hasRoute(AppRoute.Search::class)
            } == true,
            onClick = {
                navController.navigate(AppRoute.Search) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}

/**
 * Individual navigation icon button.
 */
@Composable
private fun NavIcon(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = Dimensions.Spacing.small)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)

        ){
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) AppColors.Primary else AppColors.IconSecondary,
                    modifier = Modifier.size(Dimensions.IconSize.large)
                )
            }
        }

    }
}