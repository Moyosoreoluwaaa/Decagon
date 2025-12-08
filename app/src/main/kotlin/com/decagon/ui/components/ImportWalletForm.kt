package com.decagon.ui.components

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decagon.ui.screen.onboarding.DecagonOnboardingViewModel
import com.decagon.ui.screen.onboarding.ValidationState
import timber.log.Timber // <-- Added Timber import

@Composable
fun ImportWalletForm(
    onImport: (name: String, mnemonic: String) -> Unit,
    onBack: () -> Unit,
    viewModel: DecagonOnboardingViewModel
) {
    var walletName by remember {
        Timber.d("ImportWalletForm: Initializing walletName state")
        mutableStateOf("")
    }
    var mnemonicInput by remember {
        Timber.d("ImportWalletForm: Initializing mnemonicInput state")
        mutableStateOf(TextFieldValue(""))
    }
    var validationState by remember {
        Timber.d("ImportWalletForm: Initializing validationState to None")
        mutableStateOf<ValidationState>(ValidationState.None)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Import Wallet",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

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
            onValueChange = {
                walletName = it
                Timber.d("Wallet Name changed to: $it") // <-- Log wallet name change
            },
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
                Timber.d("Mnemonic Input changed. Resetting validationState to None.") // <-- Log mnemonic change
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

        when (val state = validationState) {
            is ValidationState.Valid -> {
                Timber.i("Mnemonic validation is Valid. Showing success UI.") // <-- Log Valid state
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
                Timber.w("Mnemonic validation is Invalid. Showing error UI: ${state.message}") // <-- Log Invalid state
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
                        state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            ValidationState.None -> {
                Timber.d("Validation state is None.") // <-- Log None state
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                val phrase = mnemonicInput.text.trim().lowercase()
                Timber.i("Validate Phrase button clicked. Calling validateMnemonic.") // <-- Log validation attempt
                validationState = viewModel.validateMnemonic(phrase)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = mnemonicInput.text.isNotBlank()
        ) {
            Text("Validate Phrase")
        }

        Spacer(modifier = Modifier.height(8.dp))

        val isImportEnabled = validationState is ValidationState.Valid
        Button(
            onClick = {
                val finalName = walletName.ifBlank { "Imported Wallet" }
                val finalMnemonic = mnemonicInput.text.trim().lowercase()
                Timber.i("Import Wallet button clicked. Name: $finalName. Calling onImport.") // <-- Log import attempt
                onImport(
                    finalName,
                    finalMnemonic
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isImportEnabled
        ) {
            Text("Import Wallet")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBack) {
            Timber.i("Back button clicked. Calling onBack.") // <-- Log back action
            Text("Back")
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
        is ValidationState.Valid -> MaterialTheme.colorScheme.primary.also { Timber.v("MnemonicInputBox border color: Primary (Valid)") }
        is ValidationState.Invalid -> MaterialTheme.colorScheme.error.also { Timber.v("MnemonicInputBox border color: Error (Invalid)") }
        ValidationState.None -> MaterialTheme.colorScheme.outline.also { Timber.v("MnemonicInputBox border color: Outline (None)") }
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