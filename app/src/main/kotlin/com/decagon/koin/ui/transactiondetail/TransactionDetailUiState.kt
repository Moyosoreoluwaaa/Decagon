package com.koin.ui.transactiondetail

import com.koin.domain.model.Transaction

data class TransactionDetailUiState(
    val transaction: Transaction? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)