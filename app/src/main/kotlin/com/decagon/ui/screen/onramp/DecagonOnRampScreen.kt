// ============================================================================
// FILE: ui/screen/onramp/DecagonOnRampScreen.kt
// FIXED: Direct URL loading instead of HTML with external scripts
// ============================================================================

package com.decagon.ui.screen.onramp

import android.graphics.Bitmap
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.decagon.domain.model.DecagonWallet
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
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }

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

    // Initialize on-ramp transaction
    LaunchedEffect(Unit) {
        Timber.d("🔄 Initializing on-ramp transaction...")
        viewModel.initializeOnRamp(wallet, cryptoAsset)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Funds") },
                navigationIcon = {
                    IconButton(onClick = {
                        Timber.d("🔙 Back button clicked")
                        onBackClick()
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
            if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        Timber.d("🔄 Retry button clicked")
                        errorMessage = null
                        isLoading = true
                    }) {
                        Text("Retry")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onBackClick) {
                        Text("Go Back")
                    }
                }
            } else {
                AndroidView(
                    factory = { ctx ->
                        Timber.d("🏗️ Creating WebView for direct Ramp URL loading...")
                        WebView(ctx).apply {
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            setupWebViewSettings()

                            webViewClient = RampWebViewClient(
                                onPageStarted = {
                                    Timber.d("📄 Page started: $it")
                                    isLoading = true
                                },
                                onPageFinished = { url ->
                                    Timber.d("✅ Page finished: $url")
                                    isLoading = false
                                    viewModel.startMonitoring(wallet.address)
                                },
                                onError = { error ->
                                    Timber.e("❌ WebView error: $error")
                                    errorMessage = error
                                    isLoading = false
                                }
                            )

                            webChromeClient = RampWebChromeClient(
                                onConsoleMessage = { message, lineNumber, sourceId ->
                                    Timber.d("🌐 [Console] $message")
                                },
                                onProgressChanged = { progress ->
                                    if (progress % 20 == 0) {
                                        Timber.d("⏳ Progress: $progress%")
                                    }
                                }
                            )

                            // ✅ SOLUTION: Load Ramp widget directly via URL
                            val rampUrl = buildRampWidgetUrl(
                                walletAddress = wallet.address,
                                cryptoAsset = cryptoAsset,
                                isTestMode = true
                            )

                            Timber.d("🌐 Loading Ramp widget URL: $rampUrl")
                            loadUrl(rampUrl)

                            webView = this
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
                                text = "Loading Ramp Network...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
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
// WEBVIEW SETTINGS
// ============================================================================

private fun WebView.setupWebViewSettings() {
    Timber.d("⚙️ Configuring WebView settings...")

    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

        // ✅ CRITICAL: Allow mixed content
        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // ✅ CRITICAL: Allow file access for local resources
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
// BUILD RAMP WIDGET URL (Direct Loading)
// ============================================================================

/**
 * Builds direct Ramp widget URL with all parameters
 * This bypasses the need for external JavaScript loading
 */
private fun buildRampWidgetUrl(
    walletAddress: String,
    cryptoAsset: String,
    isTestMode: Boolean
): String {
    val baseUrl = if (isTestMode) {
        "https://app.demo.rampnetwork.com"
    } else {
        "https://app.ramp.network"
    }

    // URL encode parameters
    val params = mapOf(
        "hostAppName" to "Decagon Wallet",
        "swapAsset" to cryptoAsset,
        "userAddress" to walletAddress,
        "fiatCurrency" to "NGN",
        "fiatValue" to "10000",
        "variant" to "mobile"
    )

    val queryString = params.entries.joinToString("&") { (key, value) ->
        "$key=${java.net.URLEncoder.encode(value, "UTF-8")}"
    }

    val fullUrl = "$baseUrl?$queryString"

    Timber.d("🔗 Ramp URL built:")
    Timber.d("  Base: $baseUrl")
    Timber.d("  Asset: $cryptoAsset")
    Timber.d("  Address: ${walletAddress.take(8)}...")
    Timber.d("  Test Mode: $isTestMode")
    Timber.d("  Full URL: $fullUrl")

    return fullUrl
}

// ============================================================================
// WEBVIEW CLIENT
// ============================================================================

private class RampWebViewClient(
    private val onPageStarted: (String) -> Unit,
    private val onPageFinished: (String) -> Unit,
    private val onError: (String) -> Unit
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        Timber.d("🌐 Page started: $url")
        url?.let { onPageStarted(it) }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Timber.d("🌐 Page finished: $url")
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

        // Allow all Ramp URLs
        if (url.contains("ramp.network") ||
            url.contains("cdn.ramp.network") ||
            url.startsWith("http://") ||
            url.startsWith("https://")) {
            Timber.d("✅ Loading in WebView: $url")
            return false
        }

        Timber.w("⚠️ Blocked external URL: $url")
        return true
    }
}

// ============================================================================
// WEBCHROME CLIENT
// ============================================================================

private class RampWebChromeClient(
    private val onConsoleMessage: (String, Int, String) -> Unit,
    private val onProgressChanged: (Int) -> Unit
) : WebChromeClient() {

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        consoleMessage?.let {
            val message = it.message()
            val lineNumber = it.lineNumber()
            val sourceId = it.sourceId()

            when (it.messageLevel()) {
                ConsoleMessage.MessageLevel.ERROR -> {
                    Timber.e("🔴 [JS Error] $message")
                }
                ConsoleMessage.MessageLevel.WARNING -> {
                    Timber.w("🟡 [JS Warning] $message")
                }
                else -> {
                    Timber.d("🔵 [JS Log] $message")
                }
            }

            onConsoleMessage(message, lineNumber, sourceId)
        }
        return true
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressChanged(newProgress)
    }
}