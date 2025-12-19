package com.koin.ui.auth

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(viewModel: AuthViewModel, onRegistered: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    if (state.isRegistered) {
        onRegistered()
    }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val emailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    var bio by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register")
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email") },
                singleLine = true,
                isError = email.isNotBlank() && !emailValid,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                placeholder = { Text("Bio (optional)") },
                singleLine = false,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
        Spacer(Modifier.height(16.dp))
        Button(
                enabled = name.isNotBlank() && emailValid,
                onClick = { viewModel.onEvent(AuthUiEvent.Register(name, email, bio)) }
            ) {
            Text("Register")
        }

        state.error?.let { Snackbar { Text(it) } }
    }
}
