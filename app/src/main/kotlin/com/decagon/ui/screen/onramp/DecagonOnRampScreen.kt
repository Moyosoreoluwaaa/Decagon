// ============================================================================
// FILE: ui/screen/onramp/DecagonOnRampScreen.kt
// COMPLETE: Multi-provider with manual selection support
// ============================================================================

package com.decagon.ui.screen.onramp

import android.graphics.Bitmap
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.decagon.domain.model.DecagonWallet
import com.decagon.ui.components.DecagonProviderSelectionSheet
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonOnRampScreen(
    wallet: DecagonWallet,
    onBackClick: () -> Unit,
    onTransactionComplete: (String) -> Unit,
    viewModel: DecagonOnRampViewModel = koinViewModel()
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    val onRampState by viewModel.onRampState.collectAsState()
    val showProviderSelection by viewModel.showProviderSelection.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()

    val activeChain = wallet.activeChain
    val cryptoAsset = when (activeChain?.chainType?.id) {
        "solana" -> "SOLANA_SOL"
        "ethereum" -> "ETH"
        "polygon" -> "MATIC_POLYGON"
        else -> "SOLANA_SOL"
    }

    Timber.d("🎯 DecagonOnRampScreen initialized")
    Timber.d("📍 Wallet Address: ${wallet.address}")
    Timber.d("📍 Crypto Asset: $cryptoAsset")

    // Start on-ramp flow (shows provider selection or auto-selects)
    LaunchedEffect(Unit) {
        Timber.d("🔄 Starting on-ramp flow...")
        viewModel.startOnRampFlow(
            wallet = wallet,
            cryptoAsset = cryptoAsset
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (onRampState) {
                            is OnRampState.Ready -> {
                                "Add Funds via ${(onRampState as OnRampState.Ready).provider}"
                            }
                            is OnRampState.SelectingProvider -> "Select Payment Provider"
                            else -> "Add Funds"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        Timber.d("📙 Back button clicked")
                        when (onRampState) {
                            is OnRampState.SelectingProvider -> {
                                viewModel.cancelProviderSelection()
                            }
                            else -> onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = onRampState) {
                is OnRampState.Idle -> {
                    // Initial state
                }

                is OnRampState.SelectingProvider -> {
                    // Provider selection handled by bottom sheet
                }

                OnRampState.Loading -> {
                    LoadingView()
                }

                is OnRampState.Ready -> {
                    WebViewContent(
                        widgetUrl = state.widgetUrl,
                        providerName = state.provider,
                        onWebViewCreated = { webView = it },
                        onPageFinished = {
                            Timber.d("✅ Widget loaded, starting balance monitoring")
                            viewModel.startMonitoring(wallet.address)
                        }
                    )
                }

                is OnRampState.Completed -> {
                    CompletedView(
                        amount = state.amount,
                        onDismiss = {
                            onTransactionComplete("Received ${state.amount} SOL")
                            onBackClick()
                        }
                    )
                }

                is OnRampState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = {
                            viewModel.startOnRampFlow(wallet, cryptoAsset)
                        },
                        onBack = onBackClick,
                        onChangeProvider = {
                            viewModel.startOnRampFlow(wallet, cryptoAsset)
                        }
                    )
                }
            }
        }
    }

    // Provider selection bottom sheet
    if (showProviderSelection) {
        val availableProviders = remember { viewModel.getAvailableProviders() }

        DecagonProviderSelectionSheet(
            wallet = wallet,
            availableProviders = availableProviders,
            currentProvider = selectedProvider,
            onProviderSelected = { provider ->
                viewModel.onProviderSelected(provider)
            },
            onDismiss = {
                viewModel.cancelProviderSelection()
                onBackClick()
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            Timber.d("🧹 Cleaning up WebView...")
            webView?.apply {
                stopLoading()
                clearHistory()
                removeAllViews()
                destroy()
            }
            viewModel.stopMonitoring()
        }
    }
}

// ============================================================================
// COMPOSABLE COMPONENTS
// ============================================================================

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Initializing payment provider...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WebViewContent(
    widgetUrl: String,
    providerName: String,
    onWebViewCreated: (WebView) -> Unit,
    onPageFinished: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }

    AndroidView(
        factory = { ctx ->
            Timber.d("🏗️ Creating WebView for $providerName...")
            WebView(ctx).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )

                setupWebViewSettings()

                webViewClient = OnRampWebViewClient(
                    onPageStarted = {
                        Timber.d("🔄 Page started: $it")
                        isLoading = true
                    },
                    onPageFinished = { url ->
                        Timber.d("✅ Page finished: $url")
                        isLoading = false
                        onPageFinished()
                    },
                    onError = { error ->
                        Timber.e("❌ WebView error: $error")
                        isLoading = false
                    }
                )

                webChromeClient = OnRampWebChromeClient(
                    onConsoleMessage = { message, _, _ ->
                        when {
                            message.contains("error", ignoreCase = true) -> {
                                Timber.e("🔴 [JS Error] $message")
                            }
                            message.contains("warning", ignoreCase = true) -> {
                                Timber.w("🟡 [JS Warning] $message")
                            }
                            else -> {
                                Timber.d("🔵 [JS Log] $message")
                            }
                        }
                    },
                    onProgressChanged = { progress ->
                        if (progress % 20 == 0) {
                            Timber.d("⏳ Progress: $progress%")
                        }
                    }
                )

                Timber.d("🌐 Loading widget URL: $widgetUrl")
                loadUrl(widgetUrl)

                onWebViewCreated(this)
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Loading $providerName widget...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompletedView(
    amount: Double,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "✅ Purchase Complete!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Received %.4f SOL".format(amount),
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Funds have been added to your wallet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onChangeProvider: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌ Unable to Initialize",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Retry")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onChangeProvider,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Try Different Provider")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go Back")
        }
    }
}

// ============================================================================
// WEBVIEW CONFIGURATION
// ============================================================================

private fun WebView.setupWebViewSettings() {
    Timber.d("⚙️ Configuring WebView settings...")

    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        allowFileAccess = true
        allowContentAccess = true
        loadWithOverviewMode = true
        useWideViewPort = true
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false
        setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)

        Timber.d("✅ WebView configured")
    }

    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
}

// ============================================================================
// WEBVIEW CLIENT
// ============================================================================

private class OnRampWebViewClient(
    private val onPageStarted: (String) -> Unit,
    private val onPageFinished: (String) -> Unit,
    private val onError: (String) -> Unit
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        url?.let { onPageStarted(it) }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        url?.let { onPageFinished(it) }
    }

    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        Timber.e("❌ WebView error: $description (Code: $errorCode)")
        onError("Failed to load: $description")
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString() ?: return false
        Timber.d("🔗 Navigation: $url")

        return when {
            url.contains("ramp.network") ||
                    url.contains("moonpay.com") ||
                    url.contains("transak.com") ||
                    url.contains("onramper.com") ||
                    url.startsWith("http://") ||
                    url.startsWith("https://") -> {
                Timber.d("✅ Loading in WebView: $url")
                false
            }
            else -> {
                Timber.w("⚠️ Blocked external URL: $url")
                true
            }
        }
    }
}

// ============================================================================
// WEBCHROME CLIENT
// ============================================================================

private class OnRampWebChromeClient(
    private val onConsoleMessage: (String, Int, String) -> Unit,
    private val onProgressChanged: (Int) -> Unit
) : WebChromeClient() {

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        consoleMessage?.let {
            onConsoleMessage(it.message(), it.lineNumber(), it.sourceId())
        }
        return true
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressChanged(newProgress)
    }
}