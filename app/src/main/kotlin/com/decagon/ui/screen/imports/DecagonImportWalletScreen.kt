package com.decagon.ui.screen.imports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.decagon.core.util.DecagonLoadingState
import com.decagon.ui.screen.onboarding.DecagonOnboardingViewModel
import com.decagon.ui.screen.onboarding.ValidationState
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonImportWalletScreen(
    onBackClick: () -> Unit,
    onWalletImported: () -> Unit,
    viewModel: DecagonOnboardingViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    
    DisposableEffect(activity) {
        Timber.d("ImportWalletScreen: Setting activity")
        activity?.let { viewModel.setActivity(it) }
        onDispose {
            viewModel.setActivity(null)
            Timber.d("ImportWalletScreen: Cleared activity")
        }
    }

    val importState by viewModel.importState.collectAsState()
    var walletName by remember { mutableStateOf("") }
    var mnemonicInput by remember { mutableStateOf(TextFieldValue("")) }
    var validationState by remember { mutableStateOf<ValidationState>(ValidationState.None) }

    Timber.d("ImportWalletScreen state: ${importState.javaClass.simpleName}")

    LaunchedEffect(importState) {
        if (importState is DecagonLoadingState.Success) {
            Timber.i("ImportWalletScreen: Success, navigating away")
            onWalletImported()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Wallet") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    "Enter your 12 or 24-word recovery phrase to restore your wallet.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = walletName,
                onValueChange = { walletName = it },
                label = { Text("Wallet Name") },
                placeholder = { Text("Imported Wallet") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Recovery Phrase",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            MnemonicInputBox(
                value = mnemonicInput,
                onValueChange = {
                    mnemonicInput = it
                    validationState = ValidationState.None
                },
                validationState = validationState,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            val wordCount = mnemonicInput.text.trim()
                .split("\\s+".toRegex())
                .filter { it.isNotEmpty() }
                .size

            Text(
                "$wordCount words",
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    wordCount == 12 || wordCount == 24 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.align(Alignment.Start)
            )

            when (validationState) {
                is ValidationState.Valid -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Valid recovery phrase",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                is ValidationState.Invalid -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            (validationState as ValidationState.Invalid).message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                ValidationState.None -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = {
                    Timber.d("ImportWalletScreen: Validate clicked")
                    val phrase = mnemonicInput.text.trim().lowercase()
                    validationState = viewModel.validateMnemonic(phrase)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = mnemonicInput.text.isNotBlank()
            ) {
                Text("Validate Phrase")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    Timber.i("ImportWalletScreen: Import clicked")
                    activity?.window?.decorView?.post {
                        Timber.i("ImportWalletScreen: Calling viewModel.importWallet")
                        viewModel.importWallet(
                            walletName.ifBlank { "Imported Wallet" },
                            mnemonicInput.text.trim().lowercase()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = validationState is ValidationState.Valid &&
                         importState !is DecagonLoadingState.Loading
            ) {
                if (importState is DecagonLoadingState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (importState is DecagonLoadingState.Loading) 
                    "Importing..." else "Import Wallet")
            }

            if (importState is DecagonLoadingState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        (importState as DecagonLoadingState.Error).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun MnemonicInputBox(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    validationState: ValidationState,
    modifier: Modifier = Modifier
) {
    val borderColor = when (validationState) {
        is ValidationState.Valid -> MaterialTheme.colorScheme.primary
        is ValidationState.Invalid -> MaterialTheme.colorScheme.error
        ValidationState.None -> MaterialTheme.colorScheme.outline
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .height(150.dp)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(12.dp)
    )
}