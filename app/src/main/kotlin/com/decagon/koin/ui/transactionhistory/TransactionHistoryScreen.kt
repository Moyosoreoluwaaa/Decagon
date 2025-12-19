package com.koin.ui.transactionhistory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.koin.util.NetworkMonitor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.koin.navigation.Screen
import com.koin.ui.coinlist.DataEmptyState
import com.koin.ui.coinlist.ErrorEmptyState
import com.koin.ui.coinlist.LoadingState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    navController: NavController,
    viewModel: TransactionHistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val networkMonitor = remember { NetworkMonitor(context) }
    val isNetworkAvailable by networkMonitor.isNetworkAvailable.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Transaction History") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.error != null && uiState.transactions.isEmpty() -> {
                    ErrorEmptyState(
                        error = uiState.error!!,
                        isNetworkAvailable = isNetworkAvailable,
                        onRetry = { /* Implement retry logic if needed */ }
                    )
                }
                uiState.transactions.isEmpty() -> {
                    DataEmptyState(
                        isNetworkAvailable = isNetworkAvailable,
                        onRetry = { /* Implement retry logic if needed */ }
                    )
                }
                else -> {
                    LazyColumn {
                        items(uiState.transactions) { transaction ->
                            TransactionCard(transaction = transaction) { transactionId ->
                                navController.navigate(Screen.TransactionDetail.createRoute(transactionId))
                            }
                        }
                    }
                }
            }
        }
    }
}