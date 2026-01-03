package com.decagon.ui.screen.dapp

import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.decagon.ui.components.WalletSwitcherSheet
import com.decagon.ui.theme.AppColors
import com.decagon.ui.viewmodels.DAppBrowserViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DAppBrowserScreen(
    initialUrl: String,
    initialTitle: String,
    viewModel: DAppBrowserViewModel = koinViewModel(),
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeWallet by viewModel.activeWallet.collectAsState()

    var currentUrl by remember { mutableStateOf(initialUrl) }
    var wallet by remember { mutableStateOf(activeWallet) }
    var currentTitle by remember { mutableStateOf(initialTitle) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableIntStateOf(0) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    // Auto-connect wallet if previously connected
    LaunchedEffect(currentUrl, activeWallet) {
        if (activeWallet != null) {
            val connected = viewModel.getConnectedWallet(currentUrl)
            if (connected != null && connected.walletId == activeWallet?.id) {
                // Inject wallet connection script
                webView?.evaluateJavascript(
                    """
                    window.solana = {
                        publicKey: "${activeWallet?.publicKey}",
                        isPhantom: true,
                        isConnected: true
                    };
                    """.trimIndent(),
                    null
                )
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(currentTitle, maxLines = 1) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Rounded.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        // Wallet Connection Indicator
                        if (uiState.isWalletConnected && activeWallet != null) {
                            IconButton(onClick = { viewModel.showWalletSwitcher() }) {
                                Badge {
                                    Icon(Icons.Rounded.AccountBalanceWallet, "Wallet")
                                }
                            }
                        } else {
                            IconButton(onClick = { viewModel.connectWallet(currentUrl, wallet!!) }) {
                                Icon(Icons.Rounded.AccountBalanceWallet, "Connect Wallet")
                            }
                        }

                        // Navigation
                        IconButton(onClick = { webView?.goBack() }, enabled = canGoBack) {
                            Icon(Icons.Rounded.ArrowBack, "Back")
                        }
                        IconButton(onClick = { webView?.goForward() }, enabled = canGoForward) {
                            Icon(Icons.Rounded.ArrowForward, "Forward")
                        }

                        // Home Button
                        IconButton(onClick = { webView?.loadUrl(initialUrl) }) {
                            Icon(Icons.Rounded.Home, "Home")
                        }

                        // Refresh
                        IconButton(onClick = { webView?.reload() }) {
                            Icon(Icons.Rounded.Refresh, "Refresh")
                        }

                        // Options Menu
                        IconButton(onClick = { viewModel.showOptionsMenu() }) {
                            Icon(Icons.Rounded.MoreVert, "Options")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppColors.Surface
                    )
                )

                // Progress Bar
                if (isLoading && loadingProgress < 100) {
                    LinearProgressIndicator(
                        progress = { loadingProgress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.Success,
                    )
                }
            }
        }
    ) { padding ->
        AndroidView(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(
                            view: WebView?,
                            url: String?,
                            favicon: Bitmap?
                        ) {
                            isLoading = true
                            loadingProgress = 0
                            url?.let { currentUrl = it }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            loadingProgress = 100
                            view?.title?.let {
                                currentTitle = it
                                url?.let { u -> viewModel.addToHistory(u, it) }
                            }
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            loadingProgress = newProgress
                        }
                    }

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                    }

                    loadUrl(initialUrl)
                    webView = this
                }
            },
            update = { view ->
                canGoBack = view.canGoBack()
                canGoForward = view.canGoForward()
            }
        )
    }

    // Wallet Switcher Sheet
    if (uiState.showWalletSwitcher) {
        WalletSwitcherSheet(
            onDismiss = { viewModel.hideWalletSwitcher() },
            onWalletSelected = { wallet ->
                // FIX: Call the wrapper function that handles both setActiveWallet and connectWallet
//                viewModel.onWalletSelectedForSwitch(wallet, currentUrl)
                viewModel.hideWalletSwitcher()
            }
        )
    }

    // Options Menu
    if (uiState.showOptionsMenu) {
        DAppOptionsSheet(
            currentUrl = currentUrl,
            onDismiss = { viewModel.hideOptionsMenu() },
            onBookmark = { viewModel.toggleBookmark(currentUrl, currentTitle) },
            onDisconnectWallet = { viewModel.disconnectWallet(currentUrl) },
            onOpenInBrowser = { /* Open in external browser */ }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DAppOptionsSheet(
    currentUrl: String,
    onDismiss: () -> Unit,
    onBookmark: () -> Unit,
    onDisconnectWallet: () -> Unit,
    onOpenInBrowser: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            ListItem(
                headlineContent = { Text("Bookmark") },
                leadingContent = { Icon(Icons.Rounded.Bookmark, null) },
                modifier = Modifier.clickable { onBookmark(); onDismiss() }
            )
            ListItem(
                headlineContent = { Text("Disconnect Wallet") },
                leadingContent = { Icon(Icons.Rounded.LinkOff, null) },
                modifier = Modifier.clickable { onDisconnectWallet(); onDismiss() }
            )
            ListItem(
                headlineContent = { Text("Open in Browser") },
                leadingContent = { Icon(Icons.Rounded.OpenInBrowser, null) },
                modifier = Modifier.clickable { onOpenInBrowser(); onDismiss() }
            )
        }
    }
}