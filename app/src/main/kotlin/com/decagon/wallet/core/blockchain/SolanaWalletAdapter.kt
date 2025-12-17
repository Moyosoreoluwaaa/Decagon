package com.octane.wallet.core.blockchain

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.octane.wallet.core.security.KeystoreManager
import com.octane.wallet.domain.models.TransactionType
import com.octane.wallet.domain.models.Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Solana Wallet Adapter for WebView DApps.
 * Implements Solana Wallet Standard for seamless integration.
 *
 * Supported Methods:
 * - connect() - Request wallet connection
 * - disconnect() - Disconnect wallet
 * - signTransaction() - Sign single transaction
 * - signAllTransactions() - Sign multiple transactions
 * - signMessage() - Sign arbitrary message
 */
class SolanaWalletAdapter(
    private val webView: WebView,
    private val keystoreManager: KeystoreManager,
    private val solanaKeyGenerator: SolanaKeyGenerator,
    private val scope: CoroutineScope,
    private val onConnectionRequest: (String) -> Unit, // Callback to show connect dialog
    private val onTransactionRequest: (TransactionRequest) -> Unit
) {

    private var connectedWallet: Wallet? = null
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Inject wallet provider into WebView.
     * Makes window.solana available to DApps.
     */
    fun injectProvider(wallet: Wallet?) {
        connectedWallet = wallet

        val providerScript = buildProviderScript(wallet)

        webView.evaluateJavascript(providerScript) { result ->
            println("Solana provider injected: $result")
        }
    }

    private fun buildProviderScript(wallet: Wallet?): String {
        val publicKey = wallet?.publicKey ?: "null"
        val isConnected = wallet != null

        return """
            (function() {
                window.solana = {
                    isPhantom: true,
                    isOctane: true,
                    publicKey: ${if (wallet != null) "{ toBase58: () => '$publicKey' }" else "null"},
                    isConnected: $isConnected,
                    
                    // Connect to wallet
                    connect: async function(options = {}) {
                        return new Promise((resolve, reject) => {
                            if (this.isConnected) {
                                resolve({ publicKey: this.publicKey });
                                return;
                            }
                            
                            // Request connection via native interface
                            window.OctaneWallet.requestConnection(
                                JSON.stringify({ onlyIfTrusted: options.onlyIfTrusted || false })
                            );
                            
                            // Listen for connection response
                            window.addEventListener('walletConnected', (event) => {
                                this.publicKey = { toBase58: () => event.detail.publicKey };
                                this.isConnected = true;
                                resolve({ publicKey: this.publicKey });
                            }, { once: true });
                            
                            window.addEventListener('walletConnectionRejected', () => {
                                reject(new Error('User rejected connection'));
                            }, { once: true });
                        });
                    },
                    
                    // Disconnect wallet
                    disconnect: async function() {
                        this.publicKey = null;
                        this.isConnected = false;
                        window.OctaneWallet.disconnect();
                    },
                    
                    // Sign transaction
                    signTransaction: async function(transaction) {
                        return new Promise((resolve, reject) => {
                            if (!this.isConnected) {
                                reject(new Error('Wallet not connected'));
                                return;
                            }
                            
                            const txBase64 = btoa(String.fromCharCode(...transaction.serialize()));
                            const requestId = Date.now().toString();
                            
                            window.OctaneWallet.signTransaction(requestId, txBase64);
                            
                            window.addEventListener('transactionSigned_' + requestId, (event) => {
                                const signedTxBytes = Uint8Array.from(atob(event.detail.signedTransaction), c => c.charCodeAt(0));
                                resolve(signedTxBytes);
                            }, { once: true });
                            
                            window.addEventListener('transactionRejected_' + requestId, () => {
                                reject(new Error('User rejected transaction'));
                            }, { once: true });
                        });
                    },
                    
                    // Sign all transactions
                    signAllTransactions: async function(transactions) {
                        const signed = [];
                        for (const tx of transactions) {
                            signed.push(await this.signTransaction(tx));
                        }
                        return signed;
                    },
                    
                    // Sign message
                    signMessage: async function(message) {
                        return new Promise((resolve, reject) => {
                            if (!this.isConnected) {
                                reject(new Error('Wallet not connected'));
                                return;
                            }
                            
                            const messageBase64 = btoa(String.fromCharCode(...message));
                            const requestId = Date.now().toString();
                            
                            window.OctaneWallet.signMessage(requestId, messageBase64);
                            
                            window.addEventListener('messageSigned_' + requestId, (event) => {
                                const signature = Uint8Array.from(atob(event.detail.signature), c => c.charCodeAt(0));
                                resolve({ signature, publicKey: this.publicKey });
                            }, { once: true });
                            
                            window.addEventListener('messageRejectionRejected_' + requestId, () => {
                                reject(new Error('User rejected signing'));
                            }, { once: true });
                        });
                    }
                };
                
                // Emit ready event
                window.dispatchEvent(new Event('solana#initialized'));
            })();
        """.trimIndent()
    }

    /**
     * JavaScript interface for native callbacks.
     */
    inner class WalletInterface {

        @JavascriptInterface
        fun requestConnection(optionsJson: String) {
            scope.launch(Dispatchers.Main) {
                val options = json.decodeFromString<ConnectionOptions>(optionsJson)

                if (options.onlyIfTrusted && connectedWallet == null) {
                    rejectConnection("User interaction required")
                } else {
                    onConnectionRequest(options.toString())
                }
            }
        }

        @JavascriptInterface
        fun disconnect() {
            scope.launch(Dispatchers.Main) {
                connectedWallet = null
                injectProvider(null)
            }
        }

        @JavascriptInterface
        fun signTransaction(requestId: String, txBase64: String) {
            scope.launch(Dispatchers.Main) {
                val request = TransactionRequest(
                    id = requestId,
                    transactionBase64 = txBase64,
                    type = TransactionType.SINGLE
                )
                onTransactionRequest(request)
            }
        }

        @JavascriptInterface
        fun signMessage(requestId: String, messageBase64: String) {
            scope.launch(Dispatchers.Main) {
                val request = TransactionRequest(
                    id = requestId,
                    transactionBase64 = messageBase64,
                    type = TransactionType.MESSAGE
                )
                onTransactionRequest(request)
            }
        }
    }

    /**
     * Approve connection and inject wallet.
     */
    fun approveConnection(wallet: Wallet) {
        connectedWallet = wallet
        injectProvider(wallet)

        webView.evaluateJavascript("""
            window.dispatchEvent(new CustomEvent('walletConnected', {
                detail: { publicKey: '${wallet.publicKey}' }
            }));
        """.trimIndent(), null)
    }

    /**
     * Reject connection request.
     */
    fun rejectConnection(reason: String = "User rejected") {
        webView.evaluateJavascript("""
            window.dispatchEvent(new CustomEvent('walletConnectionRejected', {
                detail: { reason: '$reason' }
            }));
        """.trimIndent(), null)
    }

    /**
     * Sign and return transaction to DApp.
     */
    fun approveTransaction(requestId: String, signedTxBase64: String) {
        webView.evaluateJavascript("""
            window.dispatchEvent(new CustomEvent('transactionSigned_$requestId', {
                detail: { signedTransaction: '$signedTxBase64' }
            }));
        """.trimIndent(), null)
    }

    /**
     * Reject transaction request.
     */
    fun rejectTransaction(requestId: String) {
        webView.evaluateJavascript("""
            window.dispatchEvent(new CustomEvent('transactionRejected_$requestId'));
        """.trimIndent(), null)
    }
}

// Data models
@Serializable
data class ConnectionOptions(
    val onlyIfTrusted: Boolean = false
)

data class TransactionRequest(
    val id: String,
    val transactionBase64: String,
    val type: TransactionType
)
//
//enum class TransactionType {
//    SINGLE,
//    MULTIPLE,
//    MESSAGE
//}