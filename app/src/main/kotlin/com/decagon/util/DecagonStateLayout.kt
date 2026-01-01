package com.decagon.util

import androidx.compose.runtime.Composable
import com.decagon.core.util.DecagonLoadingState

@Composable
fun <T> DecagonStateLayout(
    state: DecagonLoadingState<T>,
    onRetry: () -> Unit,
    idleContent: @Composable () -> Unit = {},
    loadingMessage: String = "Processing...",
    successContent: @Composable (T) -> Unit
) {
    when (state) {
        is DecagonLoadingState.Idle -> idleContent()
        is DecagonLoadingState.Loading -> LoadingView(loadingMessage)
        is DecagonLoadingState.Error -> ErrorView(state.message, onRetry)
        is DecagonLoadingState.Success -> successContent(state.data)
    }
}