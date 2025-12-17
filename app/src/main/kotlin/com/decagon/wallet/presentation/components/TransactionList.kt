package com.octane.wallet.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.octane.wallet.domain.models.Transaction
import com.octane.wallet.domain.models.TransactionStatus
import com.octane.wallet.domain.models.TransactionType
import com.octane.wallet.presentation.theme.Dimensions

@Composable
internal fun TransactionList(
    transactions: List<Transaction>,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    onTransactionClick: (Transaction) -> Unit,
    formatTransactionType: (TransactionType) -> String,
    getTransactionIcon: (TransactionType) -> String,
    getStatusColor: (TransactionStatus) -> Color
) {
    LazyColumn(
        state = scrollState,
        contentPadding = PaddingValues(Dimensions.Padding.standard),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
    ) {
        items(transactions) { transaction ->
            TransactionRow(
                transaction = transaction,
                onClick = { onTransactionClick(transaction) },
                formatType = formatTransactionType,
                getIcon = getTransactionIcon,
                getStatusColor = getStatusColor
            )
        }
    }
}
